#!/bin/bash

export REPO=adyach/mrbot-temp-service
export TAG=0.4
export PATH_CONFIG=./temp-service

docker login
docker build -f $PATH_CONFIG/Dockerfile -t $REPO:$TAG $PATH_CONFIG
docker tag $REPO:$TAG $REPO:$TAG
docker push $REPO
