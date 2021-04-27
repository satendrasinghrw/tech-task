package com.smaato.client;

import java.io.IOException;

public interface DistCacheClient {
    void start() throws Throwable;
    void stop();
    boolean deduplication(long queryId, int appId) throws InterruptedException, IOException;
}
