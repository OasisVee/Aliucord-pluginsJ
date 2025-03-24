/*
 * Copyright (c) 2021 Juby210
 * Licensed under the Open Software License version 3.0
 */

package io.github.juby210.acplugins.pronoundb;

import com.aliucord.Http;
import com.aliucord.Main;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.*;

public final class Store {
    public static Map<Long, String> cache = new HashMap<>();

    private static final Type resType = TypeToken.getParameterized(Map.class, Long.class, JsonObject.class).getType();
    private static final List<Long> buffer = new ArrayList<>();
    private static Thread timerThread = new Thread(Store::runThread);
    
    public static void fetchPronouns(Long id) {
        var state = timerThread.getState();
        if (!timerThread.isAlive() && state != Thread.State.RUNNABLE) {
            if (state == Thread.State.TERMINATED) timerThread = new Thread(Store::runThread);
            try {
                timerThread.start();
            } catch (Throwable e) {
                Main.logger.error("Failed to start timerThread, State: " + state, e);
            }
        }
        if (!buffer.contains(id)) buffer.add(id);
        try {
            timerThread.join();
        } catch (Throwable ignored) {}
    }

    private static void runThread() {
        try {
            Thread.sleep(50);
            var bufferCopy = buffer.toArray(new Long[0]);
            buffer.clear();
            
            Map<Long, JsonObject> res = Http.simpleJsonGet(Constants.Endpoints.LOOKUP(bufferCopy), resType);
            
            // Process the new response format
            for (Map.Entry<Long, JsonObject> entry : res.entrySet()) {
                Long userId = entry.getKey();
                JsonObject userPronouns = entry.getValue();
                
                if (userPronouns != null && userPronouns.has("sets") && 
                    userPronouns.getAsJsonObject("sets").has("en")) {
                    JsonArray enPronouns = userPronouns.getAsJsonObject("sets")
                                                     .getAsJsonArray("en");
                    
                    if (enPronouns != null && enPronouns.size() >= 2) {
                        // Convert the pronoun pair to the format used in Constants.pronouns map
                        String firstPronoun = enPronouns.get(0).getAsString();
                        String secondPronoun = enPronouns.get(1).getAsString();
                        String pronounCode = getPronounCode(firstPronoun, secondPronoun);
                        cache.put(userId, pronounCode);
                    } else {
                        cache.put(userId, "unspecified");
                    }
                } else {
                    cache.put(userId, "unspecified");
                }
            }
            
            // Handle any remaining IDs that weren't in the response
            for (var id : bufferCopy) {
                if (!cache.containsKey(id)) {
                    cache.put(id, "unspecified");
                }
            }
        } catch (Throwable e) {
            Main.logger.error("PronounDB error", e);
        }
    }

    private static String getPronounCode(String first, String second) {
        // Convert from API format to our internal codes
        if (first.equals("she") && second.equals("her")) return "sh";
        if (first.equals("he") && second.equals("him")) return "hh";
        if (first.equals("they") && second.equals("them")) return "tt";
        if (first.equals("it") && second.equals("its")) return "ii";
        
        // Handle combinations
        if (first.equals("he") && second.equals("it")) return "hi";
        if (first.equals("he") && second.equals("she")) return "hs";
        if (first.equals("he") && second.equals("they")) return "ht";
        if (first.equals("it") && second.equals("him")) return "ih";
        if (first.equals("it") && second.equals("she")) return "is";
        if (first.equals("it") && second.equals("they")) return "it";
        if (first.equals("she") && second.equals("he")) return "shh";
        if (first.equals("she") && second.equals("it")) return "si";
        if (first.equals("she") && second.equals("they")) return "st";
        if (first.equals("they") && second.equals("he")) return "th";
        if (first.equals("they") && second.equals("it")) return "ti";
        if (first.equals("they") && second.equals("she")) return "ts";
        
        // Special cases
        if (first.equals("any")) return "any";
        if (first.equals("other")) return "other";
        if (first.equals("ask")) return "ask";
        if (first.equals("avoid")) return "avoid";
        
        return "unspecified";
    }
}
