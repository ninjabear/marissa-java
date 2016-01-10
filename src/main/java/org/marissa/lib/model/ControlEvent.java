package org.marissa.lib.model;

/**
 * Created by ed on 10/01/16.
 */
public class ControlEvent {

    public enum Type {
        QUIT
    }

    private Type type;
    private String additionalInfo;

    public ControlEvent(Type type, String additionalInfo) {
        this.type = type;
        this.additionalInfo = additionalInfo;
    }

    public Type getType() {
        return type;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }
}
