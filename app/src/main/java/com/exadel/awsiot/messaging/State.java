package com.exadel.awsiot.messaging;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class State {
    private Desired desired = new Desired();
    private Reported reported = new Reported();

    public Desired getDesired() {
        return desired;
    }

    public void setDesired(Desired desired) {
        this.desired = desired;
    }

    public Reported getReported() {
        return reported;
    }

    public void setReported(Reported reported) {
        this.reported = reported;
    }
}
