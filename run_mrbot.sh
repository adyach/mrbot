#!/bin/bash

cd door-service
./gradlew build
cd ..

docker-compose up
