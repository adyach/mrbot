package org.crazycoder.door.service.domain;

import org.springframework.data.annotation.Id;

import java.util.Date;

public class Heartbeat {

    @Id
    private String id;
    private final Date timestamp;
    private final String deviceId;

    public Heartbeat(String deviceId) {
        this.timestamp = new Date();
        this.deviceId = deviceId;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public String getDeviceId() {
        return deviceId;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Heartbeat{");
        sb.append("id='").append(id).append('\'');
        sb.append(", timestamp=").append(timestamp);
        sb.append(", deviceId='").append(deviceId).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
