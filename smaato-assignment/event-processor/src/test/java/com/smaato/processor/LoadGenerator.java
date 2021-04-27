package com.smaato.processor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;

public class LoadGenerator {

    private static void requestBuilder(String host, int port, int noOfTimes) {
        final RestTemplate restTemplate = new RestTemplate();

        for (long i = 0; i < noOfTimes; i++) {
            final StringBuilder sb = new StringBuilder();
            sb.append("http://").append(host).append(':').append(port).append("/inboundevent/msg?id=")
                    .append(i).append("&query=http://")
                    .append(host).append(':').append(port).append("/query/receive");
            final String uri = sb.toString();
            ResponseEntity<HttpStatus> result = null;
            try {
                result = restTemplate.getForEntity(new URI(uri), HttpStatus.class);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            System.out.println("Received: " + result.toString());
        }
    }

    public static void main(String[] args) throws URISyntaxException {

        //"http://localhost:19000/inboundevent/msg?id=1&query=http://localhost:19000/query/receive";


        final String host = "localhost";

        final int port0 = 19000;
        new Thread(() -> requestBuilder(host, port0, 5)).start();

        final int port = 20000;
        new Thread(() -> requestBuilder(host, port, 5)).start();
    }
}
