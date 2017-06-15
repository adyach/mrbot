CREATE KEYSPACE temp_service WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 };
CREATE TABLE temp_service.weather (
  timestamp bigint,
  device_id text,
  temperature float,
  humidity float,
  heat_index float,
  PRIMARY KEY(device_id, timestamp)
) WITH CLUSTERING ORDER BY (timestamp DESC);
