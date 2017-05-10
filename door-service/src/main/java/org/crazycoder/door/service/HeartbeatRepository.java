package org.crazycoder.door.service;

import org.crazycoder.door.service.domain.Heartbeat;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface HeartbeatRepository extends MongoRepository<Heartbeat, String> {

    public List<Heartbeat> findByDeviceIdOrderByTimestampDesc(String deviceId);
}