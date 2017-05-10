package org.crazycoder.door.service.view;

import java.util.Date;

public class HeartbeatView {

    private Date timestamp;
    private String deviceId;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("HeartbeatView{");
        sb.append("timestamp=").append(timestamp);
        sb.append(", deviceId='").append(deviceId).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
