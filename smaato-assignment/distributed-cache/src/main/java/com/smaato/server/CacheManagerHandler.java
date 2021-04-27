package com.smaato.server;

import com.koloboke.collect.map.hash.HashIntObjMap;
import com.koloboke.collect.map.hash.HashIntObjMaps;
import com.koloboke.collect.set.hash.HashLongSet;
import com.koloboke.collect.set.hash.HashLongSets;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.smaato.common.Constant.DUPLICATE;
import static com.smaato.common.Constant.NON_DUPLICATE;

public class CacheManagerHandler implements CacheManagerAPI{

    private final Lock lock;
    private final HashIntObjMap<HashLongSet> cache;

    public CacheManagerHandler() {
        lock = new ReentrantLock();
        cache = HashIntObjMaps.newMutableMap();
    }

    //TODO will visit the lock usages later
    @Override
    public int checkAndUpdate(final long queryId, final int appId) {
        lock.lock();
        final boolean found = !cache.forEachWhile((i, set) -> !set.contains(queryId));
        lock.unlock();
        if (found) {
            return DUPLICATE;
        } else {
            if (cache.containsKey(appId)) {
                lock.lock();
                cache.get(appId).add(queryId);
                lock.unlock();
            } else {
                final HashLongSet set = HashLongSets.newMutableSet();
                lock.lock();
                cache.put(appId, set);
                set.add(queryId);
                lock.unlock();
            }
            return NON_DUPLICATE;
        }
    }

    @Override
    public void clean(long queryId, int appId) {
        lock.lock();
        final HashLongSet set = cache.get(appId);
        if(set!=null) {
            set.clear();
        }
        lock.unlock();
    }
}
