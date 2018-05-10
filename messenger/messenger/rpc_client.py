import logging
import os
import threading
import uuid

import pika

import protobuf.door_sensor_data_pb2
import protobuf.door_sensor_data_request_pb2
import protobuf.temp_sensor_data_pb2

_LOG = logging.getLogger('rpc.client')
RABBITMQ_EXCHANGE = 'doors.events'
RABBITMQ_QUEUE = 'doors.state.changed'


class DoorSensorListener(object):
    def __init__(self, callback):
        self.callback = callback
        self.connection = get_rabbit_connection()
        self.channel = self.connection.channel()
        self.channel.exchange_declare(exchange=RABBITMQ_EXCHANGE, exchange_type='fanout')
        self.channel.queue_bind(exchange=RABBITMQ_EXCHANGE, queue=RABBITMQ_QUEUE)
        self.channel.basic_consume(self._on_door_sensor_listener,
                                   queue=RABBITMQ_QUEUE,
                                   no_ack=True)

    def run(self):
        thread = threading.Thread(target=self.channel.start_consuming, args=())
        thread.daemon = True
        thread.start()

    def _on_door_sensor_listener(self, ch, method, properties, body):
        try:
            door_sensor_data = protobuf.door_sensor_data_pb2.DoorSensorData()
            door_sensor_data.ParseFromString(body)
            self.callback(door_sensor_data)
        except Exception as e:
            _LOG.error('Failed to send message: %s', e)


class RpcClient(object):
    def __init__(self):
        self.connection = get_rabbit_connection()
        self.channel = self.connection.channel()
        result = self.channel.queue_declare(exclusive=True)
        self.callback_queue = result.method.queue
        self.channel.basic_consume(self.on_response, no_ack=True,
                                   queue=self.callback_queue)
        self.response = None
        self.corr_id = None

    def on_response(self, ch, method, props, body):
        if self.corr_id == props.correlation_id:
            self.response = body

    def _call(self, exchange: str, routing_key: str, body: bytes):
        self.response = None
        self.corr_id = str(uuid.uuid4())
        self.channel.basic_publish(exchange=exchange,
                                   routing_key=routing_key,
                                   properties=pika.BasicProperties(
                                       reply_to=self.callback_queue,
                                       correlation_id=self.corr_id,
                                   ),
                                   body=body)
        while self.response is None:
            self.connection.process_data_events()


class DoorRpcClient(RpcClient):
    def get_state(self, device_id: str) -> protobuf.door_sensor_data_pb2.DoorSensorData:
        req = protobuf.door_sensor_data_request_pb2.DoorSensorDataRequest(device_id)
        self._call('doors.rpc', 'rpc.doors.state', req.SerializeToString())
        door_sensor_data = protobuf.door_sensor_data_pb2.DoorSensorData()
        return door_sensor_data.ParseFromString(self.response)


class TempRpcClient(RpcClient):
    def get_state(self, temp: str) -> protobuf.temp_sensor_data_pb2.TemperatureSensorData:
        self._call('temps.rpc', 'rpc.temp.state', temp)
        temp_sensor_data = protobuf.temp_sensor_data_pb2.TemperatureSensorData()
        return temp_sensor_data.ParseFromString(self.response)


def get_rabbit_connection():
    credentials = pika.PlainCredentials(os.getenv('RABBITMQ_USERNAME'), os.getenv('RABBITMQ_PASSWORD'))
    params = pika.ConnectionParameters(host=os.getenv('RABBITMQ_HOST'), credentials=credentials)
    return pika.BlockingConnection(params)
