package org.crazycoder.door.service;

import org.crazycoder.door.service.domain.DoorStatus;
import org.crazycoder.door.service.domain.Heartbeat;
import org.crazycoder.door.service.view.DoorStatusView;
import org.crazycoder.door.service.view.HeartbeatView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class DoorController {

    private static final Logger LOG = LoggerFactory.getLogger(DoorController.class);

    @Autowired
    private HeartbeatRepository heartbeatRepository;
    @Autowired
    private DoorStatusRepository doorStatusRepository;

    @PostMapping("/home/security/door/status")
    public ResponseEntity status(@RequestBody DoorStatusView doorStatusView) {
        LOG.info("Door status {}", doorStatusView);
        doorStatusRepository.save(new DoorStatus(doorStatusView.getDeviceId(), DoorStatus.Status.valueOf(doorStatusView.getState())));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/home/security/door/heartbeat")
    public ResponseEntity heartbeat(@RequestBody HeartbeatView heartbeatView) {
        LOG.info("Door status heartbeat {}", heartbeatView);
        heartbeatRepository.save(new Heartbeat(heartbeatView.getDeviceId()));
        return ResponseEntity.ok().build();
    }

    @GetMapping("/home/security/door/heartbeat/{device_id}")
    public ResponseEntity<List<HeartbeatView>> getHeartbeat(
            @PathVariable("device_id") String deviceId,
            @RequestParam(value = "limit", required = false, defaultValue = "5") int limit) {
        return ResponseEntity.ok(heartbeatRepository.findByDeviceIdOrderByTimestampDesc(deviceId).stream()
                .map(this::createHeartbeatView)
                .limit(limit)
                .collect(Collectors.toList()));
    }

    @GetMapping("/home/security/door/heartbeats")
    public ResponseEntity<List<HeartbeatView>> getHeartbeats(@RequestParam(value = "limit", required = false, defaultValue = "5") final int limit) {
        return ResponseEntity.ok(heartbeatRepository.findAll().stream()
                .map(this::createHeartbeatView)
                .limit(limit)
                .collect(Collectors.toList()));
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
            @RequestParam(value = "limit", required = false, defaultValue = "5") final int limit) {
        return ResponseEntity.ok(doorStatusRepository.findByDeviceIdOrderByTimestampDesc(deviceId).stream()
                .map(this::createDoorStatus)
                .limit(limit)
                .collect(Collectors.toList()));
    }

    @GetMapping("/home/security/door/statuses")
    public ResponseEntity<List<DoorStatusView>> getDoorStatuses(@RequestParam(value = "limit", required = false, defaultValue = "5") int limit) {
        return ResponseEntity.ok(doorStatusRepository.findAllOrderByTimestampDesc().stream()
                .map(this::createDoorStatus)
                .limit(limit)
                .collect(Collectors.toList()));
    }

    private DoorStatusView createDoorStatus(DoorStatus doorStatus) {
        DoorStatusView doorStatusView = new DoorStatusView();
        doorStatusView.setDeviceId(doorStatus.getDeviceId());
        doorStatusView.setState(doorStatus.getStatus().toString().toLowerCase());
        doorStatusView.setTimestamp(doorStatus.getTimestamp());
        return doorStatusView;
    }
}
