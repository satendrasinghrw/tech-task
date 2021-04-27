package com.smaato.server;

public interface CacheManagerAPI {
    int checkAndUpdate(long queryId, int appId);
    void clean(long queryId, int appId);
}
