import json
import logging
import os
import api
import requests

_LOG = logging.getLogger('mrbot.router')
_routes = {}
route = lambda f: _routes.setdefault(f.__name__, f)
_users = {}

FRONT_DOOR = 'front_door'
WEATHER_BAD_ROOM = 'weather_bed_room'


def route_message(data):
    if data["object"] == "page":
        for entry in data["entry"]:
            for messaging_event in entry["messaging"]:
                if messaging_event.get("message"):
                    sender_id = messaging_event["sender"]["id"]
                    if 'text' in messaging_event["message"]:
                        _LOG.info(_routes)
                        message_text = messaging_event["message"]["text"]
                        call_func_by(sender_id, message_text)


def send_message(recipient_id, message_text):
    _LOG.info('Send message to %s', recipient_id)
    response = requests.post("https://graph.facebook.com/v2.6/me/messages",
                             params={"access_token": os.environ["PAGE_ACCESS_TOKEN"]},
                             headers={"Content-Type": "application/json"},
                             data=json.dumps({
                                 "recipient": {"id": recipient_id},
                                 "message": {"text": message_text}
                             }))
    if response.status_code != 200:
        _LOG.info(response.status_code)
        _LOG.info(response.text)


def send_message_to_service_users(service, message):
    _LOG.info('Users: %s', _users)
    for user, services in _users.items():
        if services.intersection({service}):
            send_message(user, message)


@route
def front_door():
    return api.get_front_door()


@route
def weather_bed_room():
    return api.get_weather_bed_room()


def call_func_by(sender_id, message_text):
    func = _routes.get(message_text)
    if not func:
        _LOG.info('Could not route message: %s', message_text)
        send_message(sender_id, "clarify")
        return
    response = func()
    if response:
        if response.status_code == requests.codes.ok:
            set_user_service(sender_id, func.__name__)
            send_message(sender_id, response.text)


def set_user_service(sender_id, service):
    if not _users.get(sender_id):
        _users[sender_id] = set()
    _users[sender_id].add(service)
