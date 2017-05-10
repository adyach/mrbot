#include <ESP8266WiFi.h>
#include <ESP8266HTTPClient.h>
#include "settings.h"

// Time to sleep (in seconds):
const int sleepTimeS = 600;
const int doorPin = 4;

void setup()
{ 
  // Serial
  Serial.begin(115200);
  Serial.println("ESP8266 in normal mode");

  connectWifi();

  // read door status
  pinMode(doorPin, INPUT);
  int state = digitalRead(doorPin);
  String deviceId = macAddress();
  if (state == HIGH) {
    post("/home/security/door/status", "{\"deviceId\":\""+deviceId+"\",\"state\":\"OPENED\"}");
  } else {
  	post("/home/security/door/status", "{\"deviceId\":\""+deviceId+"\",\"state\":\"CLOSED\"}");
  }

  post("/home/security/door/heartbeat", "{\"deviceId\":\""+deviceId+"\"}");

  // Sleep
  Serial.println("ESP8266 in sleep mode");
  // ESP.deepSleep(sleepTimeS * 1000000);
  ESP.deepSleep(0);
}

void loop()
{
}

void connectWifi() {
  WiFi.begin(ssid, password);
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println("");
  Serial.println("WiFi connected");

  // Print the IP address
  Serial.println(WiFi.localIP());
}

void post(String uri, String body) {
  Serial.print("Connecting to ");
  Serial.println(server_url + uri);

  HTTPClient http;
  http.begin(server_url, server_port, uri);
  http.addHeader("Content-Type", "application/json");
  http.setUserAgent("door.sensor.v.0.0.1");
  int httpCode = http.POST(body);
  Serial.println("Response code: " + httpCode);
  http.end();
}

String macAddress(void) {
    uint8_t mac[6];
    WiFi.macAddress(mac);
    char macStr[18] = { 0 };
    sprintf(macStr, "%02X%02X%02X%02X%02X%02X", mac[0], mac[1], mac[2], mac[3], mac[4], mac[5]);
    return String(macStr);
}

