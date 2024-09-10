package com.myl.fastplugin.common.log.impl;


import com.myl.fastplugin.common.log.ICachedLogger;
import kotlin.Triple;
import org.gradle.api.logging.LogLevel;

import java.util.*;


public class CachedLogger extends BaseLogger implements ICachedLogger {
    protected final Map<LogLevel, Map<String, List<Triple<String, Throwable, Long>>>> logs = new LinkedHashMap<>();

    @Override
    protected synchronized void write(LogLevel level, String prefix, String msg, Throwable t) {
        logs.computeIfAbsent(level, (it) -> new HashMap<>()).computeIfAbsent(prefix, (it) -> new ArrayList<>()).add(new Triple<>(msg, t, System.currentTimeMillis()));
    }

    @Override
    public synchronized void accept(CachedLogVisitor visitor) {
        logs.forEach((logLevel, stringListMap) -> stringListMap.forEach((prefix, triples) -> {
            for (Triple<String, Throwable, Long> triple : triples) {
                visitor.visitLog(triple.getThird(), logLevel, prefix, triple.getFirst(), triple.getSecond());
            }
        }));
    }

    @Override
    public synchronized void clear() {
        logs.clear();
    }
}
