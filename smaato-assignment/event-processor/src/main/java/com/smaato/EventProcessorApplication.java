package com.smaato;

import com.smaato.client.CacheClient;
import com.smaato.client.DistCacheClient;
import com.smaato.processor.TimerTaskAPI;
import com.smaato.processor.TimerTaskProcessor;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@EnableKafka
@SpringBootApplication(
		scanBasePackages = {"com.smaato"},
		scanBasePackageClasses = {org.springframework.kafka.core.KafkaTemplate.class}
)
public class EventProcessorApplication {

	@Value("${dist.cache.host}") private String host;

	@Value("${dist.cache.port}") private int port;

	@Value(value = "${kafka.bootstrapAddress}") private String bootstrapAddress;

	@PostConstruct public void init() {}

	public static void main(String[] args) {
		SpringApplication.run(EventProcessorApplication.class, args);
	}

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

	@Bean
	public DistCacheClient distCacheClient() throws IOException {
		return new CacheClient(host, port);
	}

	@Bean
	public TimerTaskAPI timerTaskAPI() {
		return new TimerTaskProcessor();
	}

	@Bean()
	public ProducerFactory<String, Long> producerFactory() {
		Map<String, Object> props = new HashMap<>();
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, Long.class);
		return new DefaultKafkaProducerFactory<>(props);
	}

	@Bean
	public KafkaTemplate<String, Long> kafkaTemplate() {
		KafkaTemplate<String, Long> template;
		try {
			template = new KafkaTemplate<>(producerFactory());
		} catch (Throwable thr){
			template = null;
			thr.printStackTrace();
		}
		return template;
	}
}
