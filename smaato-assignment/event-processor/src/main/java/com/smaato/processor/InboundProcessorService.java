package com.smaato.processor;

import com.smaato.client.DistCacheClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.net.URI;

@Service
public final class InboundProcessorService implements InboundProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(InboundProcessor.class);

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private TimerTaskAPI timerTaskAPI;

    @PostConstruct
    public void init() {
        LOG.info("InboundProcessorService initialized");
    }

    @PreDestroy
    public void dest() {}

    @Override
    public void process(long id, String query) {
        long eventCount = timerTaskAPI.deduplication(id);
        if (query != null) {
            try {
                URI uri = new URI(query);
                HttpEntity<Long> request = new HttpEntity<>(eventCount, new HttpHeaders());
                ResponseEntity<HttpStatus> result = restTemplate.postForEntity(uri, request, HttpStatus.class);
                if(result.getStatusCodeValue() != HttpStatus.OK.value()) {
                    LOG.warn("query not processed: status code: {}", result.getStatusCodeValue());
                }
            } catch (Throwable thr) {
                LOG.error("Error in Procession query: {}", thr.toString()); //not printing full stack trace
            }
        }
    }
}
