import requests


def get_front_door():
    return requests.get("http://door-service:8080/home/security/door/statuses")


def get_weather_bed_room():
    return requests.get("http://temp-service:8081/home/weather/status")
