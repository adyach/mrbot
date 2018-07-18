import datetime
import logging

import rpc_client
import db
import protobuf
from messenger_client import Messenger

_LOG = logging.getLogger('user.subscriptions')
_SERVICE_FRONT_DOOR = 'front_door'


class UserSubscription(object):
    def __init__(self, users_db: db.UsersDb, messenger: Messenger):
        self.users_db = users_db
        self.messenger = messenger
        self.doorSensorListener = None

    def subscribe_user(self, user_id, service):
        _LOG.info('%s wants subscribe for %s', user_id, service)
        services = self.users_db.get_user_services_by_id(user_id)
        if service in services:
            _LOG.debug('%s already subscribed to %s'.format(user_id, service))
            return False

        services.append(service)
        doc_id = self.users_db.set_user_services(user_id, services)
        if not doc_id:
            _LOG.error('failed to subscribe %s for %s'.format(user_id, service))
            return False

        return True

    def unsubscribe_user(self, user_id, service):
        _LOG.info('%s wants unsubscribe from %s', user_id, service)
        services = self.users_db.get_user_services_by_id(user_id)
        if service not in services:
            _LOG.debug('%s already unsubscribed from %s'.format(user_id, service))
            return False

        services.remove(service)
        doc_id = self.users_db.set_user_services(user_id, services)
        if not doc_id:
            _LOG.error('failed to unsubscribe %s for %s'.format(user_id, service))
            return False

        return True

    def _send_message_to_service_users(self, service, message):
        users = self.users_db.get_users_by_service(service)
        if not users:
            _LOG.warning('no users found for service: %s', service)
            return

        for u in users:
            self.messenger.send_message_to_user(u, message)

    def run_door_service_listener(self):
        def door_listener_callback(data: protobuf.door_sensor_data_pb2.DoorSensorData):
            date = datetime.datetime.fromtimestamp(data.timestamp / 1000.0).strftime('%Y-%m-%d %H:%M:%S')
            self._send_message_to_service_users(_SERVICE_FRONT_DOOR, 'Door touched at {}'.format(date))
            return

        if not self.doorSensorListener:
            self.doorSensorListener = rpc_client.DoorSensorListener(callback=door_listener_callback)
            self.doorSensorListener.run()
