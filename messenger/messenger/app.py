import logging
import os

from flask import Flask, request

from messenger_client import FacebookMessenger
from router import Router
from subscription import UserSubscription
from db import FacebookUsersTinyDb

logging.basicConfig(level=getattr(logging, 'INFO', None))
_LOG = logging.getLogger('mrbot.http')

app = Flask(__name__)
facebook_messenger = FacebookMessenger()
user_subscription = UserSubscription(FacebookUsersTinyDb(), facebook_messenger)


@app.route('/webhooks/facebook-messenger', methods=['GET'])
def verify():
    if request.args.get("hub.mode") == "subscribe" and request.args.get("hub.challenge"):
        if not request.args.get("hub.verify_token") == os.environ["VERIFY_TOKEN"]:
            return "Verification token mismatch", 403
        _LOG.info('Token verified')
        return request.args["hub.challenge"], 200
    return "ok", 200


@app.route('/webhooks/facebook-messenger', methods=['POST'])
def facebook_messenger():
    Router(facebook_messenger, user_subscription).route(request.get_json())
    return "ok", 200


if __name__ == '__main__':
    logging.basicConfig(level=getattr(logging, 'WARN', None))
    user_subscription.run_door_service_listener()
    app.run(port=8080)
