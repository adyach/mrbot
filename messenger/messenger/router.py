import datetime
import logging

import client
import services

_LOG = logging.getLogger('router')


class Router(object):
    def __init__(self, messenger, subscription):
        self.messenger = messenger
        self.subscription = subscription

    def route(self, data):
        try:
            sender_id, message_text = self.messenger.parse_message(data)
            self._redirect_to_service(sender_id, message_text)
        except AttributeError as ae:
            _LOG.error(ae)

    def _redirect_to_service(self, user_id, message_text):
        if message_text == services.FRONT_DOOR:
            door = client.DoorRpcClient().get_state(services.FRONT_DOOR)
            date = datetime.datetime.fromtimestamp(door.timestamp / 1000.0).strftime('%Y-%m-%d %H:%M:%S')
            self.messenger.send_message_to_user(user_id, 'Last time door touched at {}'.format(date))
            self.subscription.subscribe_user(user_id, services.FRONT_DOOR)
            return

        if message_text == services.WEATHER:
            temp = client.TempRpcClient().get_state(services.WEATHER)
            date = datetime.datetime.fromtimestamp(temp.timestamp / 1000.0).strftime('%Y-%m-%d %H:%M:%S')
            self.messenger.send_message_to_user(user_id,
                                                'Date:{}\nTemp:{}\nHumidity:{}\nHeatIndex:{}\n'.format(date,
                                                                                                       temp.temperature,
                                                                                                       temp.humidity,
                                                                                                       temp.heatIndex))
            return

        self.messenger.send_message_to_user(user_id, "no such function")
