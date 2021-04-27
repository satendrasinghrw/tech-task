package com.smaato.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/query")
public class QueryController {

    private static final Logger LOG = LoggerFactory.getLogger(EventInboundController.class);

    @PostMapping("/receive")
    public HttpStatus inboundEvent(@RequestBody long numberOfRequests) {
        LOG.info("Processed {} requests", + numberOfRequests);
        //Since Not processing any thing so just returning OK
        return HttpStatus.OK;
    }
}
