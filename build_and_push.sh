#!/bin/bash

export REPO=adyach/mrbot-nginx
export TAG=0.2
export PATH_CONFIG=./nginx

docker login
docker build -f $PATH_CONFIG/Dockerfile -t $REPO:$TAG $PATH_CONFIG
docker tag $REPO:$TAG $REPO:$TAG
docker push $REPO
