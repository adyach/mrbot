import json
import os
import requests
from flask import Flask, request

app = Flask(__name__)


@app.route('/', methods=['GET'])
def verify():
    if request.args.get("hub.mode") == "subscribe" and request.args.get("hub.challenge"):
        if not request.args.get("hub.verify_token") == os.environ["VERIFY_TOKEN"]:
            return "Verification token mismatch", 403
        return request.args["hub.challenge"], 200
    return "ok", 200


@app.route('/', methods=['POST'])
def webhook():
    data = request.get_json()
    print(data)

    if data["object"] == "page":
        for entry in data["entry"]:
            for messaging_event in entry["messaging"]:
                if messaging_event.get("message"):
                    sender_id = messaging_event["sender"]["id"]
                    recipient_id = messaging_event["recipient"]["id"]
                    if 'text' in messaging_event["message"]:
                        message_text = messaging_event["message"]["text"]
                        if 'front door status' in message_text:
                            response = requests.get("http://door-service:8080/home/security/door/statuses?limit=10")
                            send_message(sender_id, response.text)
                        else:
                            send_message(sender_id, "clarify")
    return "ok", 200


def send_message(recipient_id, message_text):
    response = requests.post("https://graph.facebook.com/v2.6/me/messages",
                             params={"access_token": os.environ["PAGE_ACCESS_TOKEN"]},
                             headers={"Content-Type": "application/json"},
                             data=json.dumps({
                                 "recipient": {"id": recipient_id},
                                 "message": {"text": message_text}
                             }))
    if response.status_code != 200:
        print(response.status_code)
        print(response.text)


if __name__ == '__main__':
    app.run()
