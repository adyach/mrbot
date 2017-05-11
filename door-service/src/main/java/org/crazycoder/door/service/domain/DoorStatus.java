package org.crazycoder.door.service.domain;

import org.springframework.data.annotation.Id;

public class DoorStatus {

    public enum Status {
        OPENED, CLOSED
    }
    @Id
    private String id;
    private final long timestamp;
    private final String deviceId;
    private final Status status;

    public DoorStatus(String deviceId, Status status) {
        this.timestamp = System.currentTimeMillis();
        this.deviceId = deviceId;
        this.status = status;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public Status getStatus() {
        return status;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DoorStatus{");
        sb.append("id='").append(id).append('\'');
        sb.append(", timestamp=").append(timestamp);
        sb.append(", deviceId='").append(deviceId).append('\'');
        sb.append(", status=").append(status);
        sb.append('}');
        return sb.toString();
    }
}
