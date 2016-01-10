package org.marissa.lib.model;

/**
 * Created by ed on 10/01/16.
 */
public class ChannelEvent<T> {

    public enum EventType {
        XMPP,
        CONTROL
    }

    private EventType eventType;
    private T payload;

    public ChannelEvent(EventType eventType, T payload) {
        this.eventType = eventType;
        this.payload = payload;
    }

    public EventType getEventType() {
        return eventType;
    }

    public T getPayload() {
        return payload;
    }
}
