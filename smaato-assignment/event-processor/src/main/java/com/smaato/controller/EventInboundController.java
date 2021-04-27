package com.smaato.controller;

import com.smaato.processor.InboundProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;

@RestController
@RequestMapping("/inboundevent")
public class EventInboundController {
    private static final Logger LOG = LoggerFactory.getLogger(EventInboundController.class);

    @Autowired
    private InboundProcessor processor;

    @PostConstruct
    public void init() {
        LOG.info("EventInboundController initialized {}", processor);
    }

    @GetMapping("/msg")
    public void inboundEvent(@RequestParam(name = "id") long id, @RequestParam(required = false) String query) {
        processor.process(id, query);
    }
}
