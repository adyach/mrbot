#!/bin/bash

export REPO=adyach/mrbot-messenger
export TAG=0.2
export PATH_CONFIG=./messenger

docker login
docker build -f $PATH_CONFIG/Dockerfile -t $REPO:$TAG $PATH_CONFIG
docker tag $REPO:$TAG $REPO:$TAG
docker push $REPO
