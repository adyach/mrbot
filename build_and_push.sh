#!/bin/bash

export REPO=adyach/mrbot-temp-service-cassandra
export TAG=3.10
export PATH_CONFIG=./temp-service

docker login
docker build -f $PATH_CONFIG/Dockerfile.cassandra -t $REPO:$TAG $PATH_CONFIG
docker tag $REPO:$TAG $REPO:$TAG
docker push $REPO
