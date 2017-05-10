package org.crazycoder.door.service;

import org.crazycoder.door.service.domain.DoorStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface DoorStatusRepository extends MongoRepository<DoorStatus, String> {

    public List<DoorStatus> findByDeviceIdOrderByTimestampDesc(String deviceId);
}
