import json
import logging
import os
import api
import requests
from tinydb.storages import JSONStorage
from tinydb.middlewares import CachingMiddleware
from tinydb import TinyDB
from tinydb import Query

db = TinyDB('/data/db/users.json')

_LOG = logging.getLogger('mrbot.router')
_routes = {}
route = lambda f: _routes.setdefault(f.__name__, f)

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
    users = db.search(Query().services.any({service}))
    if users:
        for u in users:
            send_message(u['user_id'], message)
    else:
        _LOG.warn('No users found for service: %s', service)


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
            subscribe_user_to_service(sender_id, func.__name__)
            send_message(sender_id, response.text)


def subscribe_user_to_service(sender_id, service):
    users = db.search(Query()['user_id'] == sender_id)
    _LOG.info('User %s services %s and want add %s', sender_id, users, service)
    if not users:
        db.insert({'user_id': sender_id, 'services': [service]})
        return
    services = users[0]['services']
    if service not in services:
        services.append(service)
        db.update(services, where('user_id') == sender_id)
        return
