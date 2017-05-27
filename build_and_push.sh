#!/bin/bash

export REPO=adyach/mrbot-door-service
export TAG=0.1
export PATH_CONFIG=./door-service

docker login
docker build -f $PATH_CONFIG/Dockerfile -t $REPO:$TAG $PATH_CONFIG
docker tag $REPO:$TAG $REPO:$TAG
docker push $REPO
