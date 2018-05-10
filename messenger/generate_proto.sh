#!/usr/bin/env bash
protoc --proto_path=../door-service/src/main/java/proto/ --python_out=./protobuf ../door-service/src/main/java/proto/door-sensor-data.proto
protoc --proto_path=../door-service/src/main/java/proto/ --python_out=./protobuf ../door-service/src/main/java/proto/door-sensor-data-request.proto
protoc --proto_path=../temp-service/proto/ --python_out=./protobuf ../temp-service/proto/temp-sensor-data.proto
