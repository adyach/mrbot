package org.crazycoder.door.service.view;

import java.util.Date;

public class DoorStatusView {

    private Date timestamp;
    private String deviceId;
    private String state;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DoorStatusView{");
        sb.append("timestamp=").append(timestamp);
        sb.append(", deviceId='").append(deviceId).append('\'');
        sb.append(", state='").append(state).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
