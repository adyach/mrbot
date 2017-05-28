import datetime
import logging
import os
import pika
import protobuf.door_sensor_data_pb2
import uwsgidecorators

import router

_LOG = logging.getLogger('mrbot.bus')
RABBITMQ_QUEUE = 'door.service.state'
RABBITMQ_EXCHANGE = 'door.service'


@uwsgidecorators.postfork
@uwsgidecorators.thread
def start_consume():
    credentials = pika.PlainCredentials(os.getenv('RABBITMQ_USERNAME'), os.getenv('RABBITMQ_PASSWORD'))
    connection = pika.BlockingConnection(
        pika.ConnectionParameters(host=os.getenv('RABBITMQ_HOST'), credentials=credentials))
    channel = connection.channel()
    channel.exchange_declare(exchange=RABBITMQ_EXCHANGE, type='topic')
    channel.queue_bind(exchange=RABBITMQ_EXCHANGE,
                       queue=RABBITMQ_QUEUE)

    _LOG.info('Waiting for messages ...')
    channel.basic_consume(callback,
                          queue=RABBITMQ_QUEUE,
                          no_ack=True)
    channel.start_consuming()


def callback(ch, method, properties, body):
    _LOG.info('Received message: %s', body)
    door_sensor_data = protobuf.door_sensor_data_pb2.DoorSensorData()
    door_sensor_data.ParseFromString(body)
    date = datetime.datetime.fromtimestamp(door_sensor_data.timestamp / 1000.0).strftime('%Y-%m-%d %H:%M:%S')
    _LOG.info(date)
    router.send_message_to_service_users(router.FRONT_DOOR, 'Door was opened at {}'.format(date))
