package org.crazycoder.door.service;

import com.google.protobuf.InvalidProtocolBufferException;
import org.crazycoder.door.service.domain.DoorStatus;
import org.crazycoder.door.service.domain.Heartbeat;
import org.crazycoder.door.service.exception.DoorStatusException;
import org.crazycoder.door.service.protobuf.DoorSensorData;
import org.crazycoder.door.service.protobuf.DoorSensorDataRequest;
import org.crazycoder.door.service.protobuf.DoorSensorDataResponse;
import org.crazycoder.door.service.protobuf.Error;
import org.crazycoder.door.service.protobuf.ErrorCode;
import org.crazycoder.door.service.protobuf.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
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
    @Value("${doors.state.changed}:doors.state.changed")
    private String doorServiceQueue;
    @Value("${doors.events}:doors.state.changed")
    private String doorsStatusExchange;

    @RabbitListener(queues = "rpc.doors.state")
    public DoorSensorDataResponse processDoorStatusRequest(byte[] data) {
        LOG.info(" [x] Received request for " + new String(data));
        Error.Builder errorBuilder = Error.newBuilder();
        DoorSensorData.Builder doorSensorDataBuilder = DoorSensorData.newBuilder();
        try {
            DoorSensorDataRequest req = DoorSensorDataRequest.parseFrom(data);
            Optional<DoorStatus> doorStatusOpt = doorStatusRepository.findByDeviceIdOrderByTimestampDesc(req
                    .getDeviceId())
                    .stream()
                    .findFirst();

            if (doorStatusOpt.isPresent()) {
                DoorStatus doorStatus = doorStatusOpt.get();
                errorBuilder.setErrorCode(ErrorCode.OK);
                doorSensorDataBuilder.setDeviceId(doorStatus.getDeviceId())
                        .setStatusValue(doorStatus.getStatus().getValue());
            } else {
                errorBuilder.setErrorCode(ErrorCode.INTERNAL_SERIVCE_ERROR);
            }
        } catch (InvalidProtocolBufferException e) {
            LOG.error(e.getMessage(), e);
            errorBuilder.setErrorCode(ErrorCode.INTERNAL_SERIVCE_ERROR);
        }

        return DoorSensorDataResponse.newBuilder()
                .setError(errorBuilder.build())
                .setDoorSensorData(doorSensorDataBuilder.build())
                .build();
    }

    public void saveDoorStatus(DoorStatus doorStatus) {
        LOG.info("Door status {}", doorStatus);
        DoorStatus result = doorStatusRepository.save(doorStatus);
        if (result == null) {
            throw new DoorStatusException("failed to save door status to database");
        } else {
            DoorSensorData doorSensorData = DoorSensorData.newBuilder()
                    .setDeviceId(doorStatus.getDeviceId())
                    .setTimestamp(doorStatus.getTimestamp())
                    .setStatus(Status.forNumber(doorStatus.getStatus().getValue()))
                    .build();
            rabbitTemplate.convertAndSend(doorsStatusExchange, doorServiceQueue, doorSensorData.toByteArray());
        }
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
