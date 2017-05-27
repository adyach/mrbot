import logging
import os
from flask import Flask, request

import bus
import router

logging.basicConfig(level=getattr(logging, 'INFO', None))
_LOG = logging.getLogger('mrbot.http')

app = Flask(__name__)


@app.route('/', methods=['GET'])
def verify():
    if request.args.get("hub.mode") == "subscribe" and request.args.get("hub.challenge"):
        if not request.args.get("hub.verify_token") == os.environ["VERIFY_TOKEN"]:
            return "Verification token mismatch", 403
        _LOG.info('Tokem verifed')
        return request.args["hub.challenge"], 200
    return "ok", 200


@app.route('/', methods=['POST'])
def webhook():
    _LOG.info('Webhooked touched: %s', request)
    router.route_message(request.get_json())
    return "ok", 200


if __name__ == '__main__':
    logging.basicConfig(level=getattr(logging, 'INFO', None))
    bus.listen_on_door_state()
    app.run()
