#!/usr/bin/env bash
protoc --proto_path=../door-service/src/main/java/proto/ --python_out=./protobuf ../door-service/src/main/java/proto/door-sensor-data.proto
