package com.exadel.awsiot.messaging;

public class ShadowDeviceData {
    protected State state = new State();
    protected Object metadata = new Object();

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public Object getMetadata() {
        return metadata;
    }

    public void setMetadata(Object metadata) {
        this.metadata = metadata;
    }
}
