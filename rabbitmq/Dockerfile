FROM rabbitmq:3.6.9-management

MAINTAINER Andrey Dyachkov

COPY rabbitmq.config /etc/rabbitmq/
COPY mrbot_rabbit_config.json /etc/rabbitmq/
RUN apt-get update
RUN apt-get install -y curl 

CMD ["rabbitmq-server"]