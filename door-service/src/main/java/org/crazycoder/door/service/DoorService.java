package org.crazycoder.door.service;

import org.crazycoder.door.service.domain.DoorStatus;
import org.crazycoder.door.service.domain.Heartbeat;
import org.crazycoder.door.service.protobuf.DoorSensorData;
import org.crazycoder.door.service.protobuf.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DoorService {

    private static final Logger LOG = LoggerFactory.getLogger(DoorService.class);

    @Autowired
    private HeartbeatRepository heartbeatRepository;
    @Autowired
    private DoorStatusRepository doorStatusRepository;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Value("${door.service.queue}")
    private String doorServiceQueue;
    @Value("${door.service.topic}")
    private String doorServiceTopic;

    public void saveDoorStatus(DoorStatus doorStatus) {
        LOG.info("Door status {}", doorStatus);
        doorStatusRepository.save(doorStatus);
        DoorSensorData doorSensorData = DoorSensorData.newBuilder()
                .setDeviceId(doorStatus.getDeviceId())
                .setTimestamp(doorStatus.getTimestamp())
                .setStatus(Status.valueOf(doorStatus.getStatus().name()))
                .build();
        rabbitTemplate.convertAndSend(doorServiceTopic, doorServiceQueue, doorSensorData);
    }

    public void saveHeartbeat(Heartbeat heartbeat) {
        LOG.info("Door status heartbeat {}", heartbeat);
        heartbeatRepository.save(heartbeat);
    }

    public List<Heartbeat> getHeartbeat(String deviceId, int limit) {
        return heartbeatRepository.findByDeviceIdOrderByTimestampDesc(deviceId).stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    public List<Heartbeat> getHeartbeats(int limit) {
        return heartbeatRepository.findAll().stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    public List<DoorStatus> getDoorStatus(String deviceId, int limit) {
        return doorStatusRepository.findByDeviceIdOrderByTimestampDesc(deviceId).stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    public List<DoorStatus> getDoorStatuses(int limit) {
        return doorStatusRepository.findAllByOrderByTimestampDesc().stream()
                .limit(limit)
                .collect(Collectors.toList());
    }
    
}
