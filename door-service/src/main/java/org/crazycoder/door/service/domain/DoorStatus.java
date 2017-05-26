package org.crazycoder.door.service.domain;

import org.springframework.data.annotation.Id;

public class DoorStatus {

    private final long timestamp;
    private final String deviceId;
    private final Status status;
    @Id
    private String id;

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

    public enum Status {
        OPENED(1), CLOSED(2);

        private final int value;

        Status(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }
}
