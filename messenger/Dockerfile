FROM python:3.5

MAINTAINER Andrey Dyachkov

RUN mkdir -p /usr/src/app
RUN mkdir -p /data/db
WORKDIR /usr/src/app

COPY messenger/ /usr/src/app/
COPY protobuf/ /usr/src/app/protobuf

COPY requirements.txt /usr/src/app/
COPY mrbot.ini /usr/src/app/
RUN pip3 install --no-cache-dir -r requirements.txt

CMD uwsgi --ini mrbot.ini
