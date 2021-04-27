package com.smaato.processor;

import com.smaato.client.DistCacheClient;
import com.smaato.common.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public final class TimerTaskProcessor implements TimerTaskAPI {
    private static final Logger LOG = LoggerFactory.getLogger(TimerTaskProcessor.class);

    private final AtomicLong eventCount = new AtomicLong(0L);
    private final Set<Long> queryIdSet = ConcurrentHashMap.newKeySet();
    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    @Autowired
    private DistCacheClient distCacheClient;

    @Value("${kafka.producer.topic.name}")
    private String topicName;

    /**
        this port will be used as appId as unique and help in
        managing distributed cache in case of multiple instance of this
        service launched -  as it required each time new port for server
    */
    @Value("${server.port}")
    private int appId;

    @Value("${kafka.enable:false}")
    private boolean kafkaEnable;

    @Autowired
    private KafkaTemplate<String, Long> kafkaTemplate;

    @PostConstruct
    public void init() {
        LOG.info("TimerTaskProcessor initialized: {} : {} : appId: {} topicName: {} kafkaEnable: {}",
                distCacheClient, kafkaTemplate, appId, topicName, kafkaEnable);
        try {
            distCacheClient.start();
        } catch (Throwable thr) {
            LOG.error("Since Cache service is not good to stopping app: {}", thr);
            Runtime.getRuntime().halt(-1);
        }
        executor.scheduleAtFixedRate(() -> {
            try {
                distCacheClient.deduplication(Event.CLEAN_CACHE, appId);

                if (kafkaEnable) {
                    kafkaTemplate.send(topicName, eventCount.get());
                } else {
                    LOG.info("{} number of unique ids received in a minute", eventCount.get());
                }
                eventCount.set(0L);
                queryIdSet.clear();
            } catch (InterruptedException | IOException e) {
                LOG.error("Error in distributed cache during clean: {}", e);
            }
        },1L, 1L, TimeUnit.MINUTES);
    }

    @PreDestroy
    public void destroy() {
        distCacheClient.stop();
        executor.shutdown();
        kafkaTemplate.destroy();
    }

    @Override
    public long deduplication(long id) {
        try {
            //First, check&update deduplication: in local and then distributed cache
            if (!queryIdSet.contains(id)) {
                queryIdSet.add(id);
                if (!distCacheClient.deduplication(id, appId)) {
                    return eventCount.incrementAndGet();
                }
            }
        } catch (InterruptedException | IOException e) {
            LOG.error("Error in distributed cache: {}", e);
        }
        return eventCount.get();
    }
}
