package com.smaato.processor;

import java.net.URISyntaxException;

public interface InboundProcessor {
    void process(long id, String query);
}
