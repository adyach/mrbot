CREATE KEYSPACE IF NOT EXISTS temp_service WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 };
CREATE TABLE IF NOT EXISTS temp_service.weather (
  timestamp bigint,
  device_id text,
  temperature float,
  humidity float,
  heat_index float,
  vcc int,
  PRIMARY KEY(device_id, timestamp)
) WITH CLUSTERING ORDER BY (timestamp DESC);
