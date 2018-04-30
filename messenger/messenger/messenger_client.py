import json
import logging
import os

import requests

_LOG = logging.getLogger('messenger.client')


class Messenger(object):
    def send_message_to_user(self, user_id, text):
        raise NotImplementedError()


class FacebookMessenger(Messenger):
    def parse_message(self, data):
        if data["object"] == "page":
            for entry in data["entry"]:
                for messaging_event in entry["messaging"]:
                    if messaging_event.get("message"):
                        sender_id = messaging_event["sender"]["id"]
                        if 'text' in messaging_event["message"]:
                            message_text = messaging_event["message"]["text"]
                            return sender_id, message_text

        raise AttributeError('failed to parse facebook message')

    def send_message_to_user(self, user_id, text):
        resp = requests.post("https://graph.facebook.com/v2.6/me/messages",
                             params={"access_token": os.environ["PAGE_ACCESS_TOKEN"]},
                             headers={"Content-Type": "application/json"},
                             data=json.dumps({
                                 "recipient": {"id": user_id},
                                 "message": {"text": text}
                             }))
        if resp.status_code != 200:
            _LOG.error('failed to send message to user %s: %s %s'.format(user_id, resp.status_code, resp.text))
