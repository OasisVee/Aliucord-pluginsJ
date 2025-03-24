/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package io.github.juby210.acplugins.pronoundb;

import com.aliucord.Http;
import com.aliucord.Main;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public final class Store {
    public static Map<Long, String> cache = new HashMap<>();

    private static final Type resType = TypeToken.getParameterized(Map.class, Long.class, String.class).getType();
    private static final List<Long> buffer = Collections.synchronizedList(new ArrayList<>());
    private static Thread timerThread = null;
    private static final Object lock = new Object();
    
    public static void fetchPronouns(Long id) {
        synchronized (lock) {
            if (!buffer.contains(id)) {
                buffer.add(id);
            }
            
            if (timerThread == null || !timerThread.isAlive()) {
                timerThread = new Thread(Store::runThread);
                timerThread.start();
            }
        }
        
        try {
            // Wait for a short time to give the thread a chance to complete
            Thread.sleep(100);
        } catch (InterruptedException ignored) {}
    }

    private static void runThread() {
        try {
            // Wait a bit to collect multiple requests
            Thread.sleep(50);
            
            Long[] bufferCopy;
            synchronized (buffer) {
                if (buffer.isEmpty()) return;
                
                bufferCopy = buffer.toArray(new Long[0]);
                buffer.clear();
            }
            
            Main.logger.debug("PronounDB: Fetching pronouns for " + bufferCopy.length + " users");
            
            try {
                String endpoint = Constants.Endpoints.LOOKUP(bufferCopy);
                Map<Long, String> res = Http.simpleJsonGet(endpoint, resType);
                
                if (res != null) {
                    cache.putAll(res);
                } else {
                    Main.logger.error("PronounDB: API response was null");
                }
                
                // Mark users with no pronouns as "unspecified"
                for (Long id : bufferCopy) {
                    if (!cache.containsKey(id)) {
                        cache.put(id, "unspecified");
                    }
                }
            } catch (Throwable e) {
                Main.logger.error("PronounDB: Error fetching pronouns", e);
                
                // Mark all as unspecified on error
                for (Long id : bufferCopy) {
                    if (!cache.containsKey(id)) {
                        cache.put(id, "unspecified");
                    }
                }
            }
        } catch (Throwable e) {
            Main.logger.error("PronounDB: Thread error", e);
        }
    }
                }
