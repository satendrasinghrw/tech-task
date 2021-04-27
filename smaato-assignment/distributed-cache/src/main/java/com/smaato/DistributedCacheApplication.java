package com.smaato;

import com.smaato.server.CacheServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class DistributedCacheApplication {

	@Value("${dist.cache.port}")
	private int port;

	@Autowired
	private CacheServer cacheServer;

	@PostConstruct
	public void init() throws InterruptedException {
		System.out.println("\n\n PORT = "+port +", cacheServer: "+ cacheServer);
		CacheServer cs = new CacheServer();
		cacheServer.start(port);
	}

	public static void main(String[] args) throws InterruptedException {
		SpringApplication.run(DistributedCacheApplication.class, args);
	}
}
