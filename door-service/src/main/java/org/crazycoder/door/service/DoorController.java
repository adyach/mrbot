package org.crazycoder.door.service;

import org.crazycoder.door.service.domain.DoorStatus;
import org.crazycoder.door.service.domain.Heartbeat;
import org.crazycoder.door.service.exception.DoorStatusException;
import org.crazycoder.door.service.view.DoorStatusView;
import org.crazycoder.door.service.view.HeartbeatView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class DoorController {

    private static final Logger LOG = LoggerFactory.getLogger(DoorController.class);

    @Autowired
    private DoorService doorService;

    @PostMapping("/home/security/door/status")
    public ResponseEntity status(@RequestBody DoorStatusView doorStatusView) {
        doorService.saveDoorStatus(new DoorStatus(doorStatusView.getDeviceId(), DoorStatus.Status.valueOf(doorStatusView.getState())));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/home/security/door/heartbeat")
    public ResponseEntity heartbeat(@RequestBody HeartbeatView heartbeatView) {
        doorService.saveHeartbeat(new Heartbeat(heartbeatView.getDeviceId()));
        return ResponseEntity.ok().build();
    }

    @GetMapping("/home/security/door/heartbeat/{device_id}")
    public ResponseEntity<List<HeartbeatView>> getHeartbeat(
            @PathVariable("device_id") String deviceId,
            @RequestParam(value = "limit", required = false, defaultValue = "5") int limit) {
        return ResponseEntity.ok(doorService.getHeartbeat(deviceId, limit).stream()
                .map(this::createHeartbeatView).collect(Collectors.toList()));
    }

    @GetMapping("/home/security/door/heartbeats")
    public ResponseEntity<List<HeartbeatView>> getHeartbeats(@RequestParam(value = "limit", required = false, defaultValue = "5") int limit) {
        return ResponseEntity.ok(doorService.getHeartbeats(limit).stream()
                .map(this::createHeartbeatView).collect(Collectors.toList()));
    }

    private HeartbeatView createHeartbeatView(Heartbeat heartbeat) {
        HeartbeatView heartbeatView = new HeartbeatView();
        heartbeatView.setDeviceId(heartbeat.getDeviceId());
        heartbeatView.setTimestamp(heartbeat.getTimestamp());
        return heartbeatView;
    }

    @GetMapping("/home/security/door/status/{device_id}")
    public ResponseEntity<List<DoorStatusView>> getDoorStatus(
            @PathVariable("device_id") String deviceId,
            @RequestParam(value = "limit", required = false, defaultValue = "5") int limit) {
        return ResponseEntity.ok(doorService.getDoorStatus(deviceId, limit).stream()
                .map(this::createDoorStatus).collect(Collectors.toList()));
    }

    @GetMapping("/home/security/door/statuses")
    public ResponseEntity<List<DoorStatusView>> getDoorStatuses(@RequestParam(value = "limit", required = false, defaultValue = "5") int limit) {
        return ResponseEntity.ok(doorService.getDoorStatuses(limit).stream()
                .map(this::createDoorStatus).collect(Collectors.toList()));
    }

    private DoorStatusView createDoorStatus(DoorStatus doorStatus) {
        DoorStatusView doorStatusView = new DoorStatusView();
        doorStatusView.setDeviceId(doorStatus.getDeviceId());
        doorStatusView.setState(doorStatus.getStatus().toString().toLowerCase());
        doorStatusView.setTime(Instant.ofEpochMilli(doorStatus.getTimestamp()).atZone(ZoneId.systemDefault()).toLocalDateTime());
        return doorStatusView;
    }

    @ExceptionHandler(DoorStatusException.class)
    private ResponseEntity doorStatusException(DoorStatusException dse) {
        LOG.error(dse.getMessage(), dse);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(dse.getMessage());
    }

}
