
#include <ESP8266WiFi.h>
#include <ESP8266HTTPClient.h>
#include "settings.h"
#include "DHT.h"
#define DHTPIN D4
#define DHTTYPE DHT22
DHT dht(DHTPIN, DHTTYPE);

// sleep for this many seconds
const int sleepSeconds = 600;

float humidity, temperature, heatIndex;
char str_humidity[10], str_temperature[10], str_heatIndex[10];

void setup() {
  Serial.begin(9600);
 
  getDHTData();
  connectWifi();
  String deviceId = macAddress();
  post("/home/weather/status", 
  "{\"deviceId\":\""+deviceId+"\",\"humidity\":"+str_humidity+",\"temperature\":"+str_temperature+",\"heatIndex\":"+str_heatIndex+"}");
  
  // convert to microseconds
  ESP.deepSleep(sleepSeconds * 1000000);
}

void loop() {
}

void getDHTData() {
  // Connect D0 to RST to wake up
  pinMode(D0, WAKEUP_PULLUP);

  dht.begin();
  delay(2000);

  float humidity = dht.readHumidity();
  float temperature = dht.readTemperature();
  if (isnan(humidity) || isnan(temperature)) {
    Serial.println("Failed to read from DHT sensor! restarting device");
    ESP.restart();
  }
  float heatIndex = dht.computeHeatIndex(temperature, humidity, false);

  // Convert the floats to strings and round to 2 decimal places
  dtostrf(humidity, 1, 2, str_humidity);
  dtostrf(temperature, 1, 2, str_temperature);
  dtostrf(heatIndex, 1, 2, str_heatIndex);
}

void connectWifi() {
  WiFi.begin(ssid, password);
  int wifiConnectAttempts = 0;
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
    if (wifiConnectAttempts > 20) {
      Serial.print("Made 10 attemots, restarting device");
      ESP.restart();
    }
    wifiConnectAttempts++;
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
  http.setUserAgent("temp.sensor.v.0.0.1");
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
