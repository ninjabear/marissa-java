package org.marissa.lib;

import co.paralleluniverse.fibers.SuspendExecution;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.slf4j.LoggerFactory;

public class Router {

    private final Map<Pattern, RoutingEventListener> routingTable = new HashMap<>();
    private Pattern baseName;

    public Router(String baseName) {
        if (baseName == null) {
            throw new IllegalArgumentException("Base name can't be null");
        }

        this.baseName = Pattern.compile(baseName + "\\s+");
    }

    public void on(String pattern, RoutingEventListener routingEventListener) {
        if (routingEventListener == null) {
            throw new IllegalArgumentException("response handler can't be null");
        }

        whenContains(baseName.pattern().concat(pattern), routingEventListener);
    }

    public void whenContains(String pattern, RoutingEventListener routingEventListener) {
        if (routingEventListener == null) {
            throw new IllegalArgumentException("response handler can't be null");
        }

        routingTable.put(Pattern.compile(pattern), routingEventListener);
    }

    public void triggerHandlersForMessageText(final String sentText, final Response useResponse) {
        routingTable.keySet().stream()
            .filter(key -> key.matcher(sentText).matches())
            .forEach(key -> fireEventAsync(key, sentText, useResponse));
    }

    private void fireEventAsync(final Pattern key, final String request, Response useResponse) {
        new Thread(() -> {
            try {
                Router.this.routingTable.get(key).routingEvent(request, useResponse);
            } catch (InterruptedException | SuspendExecution e) {
                LoggerFactory.getLogger(Router.class).error("this shouldn't happen", e);
            }
        }).start();
    }

}
