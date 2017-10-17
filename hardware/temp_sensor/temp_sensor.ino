
#include <ESP8266WiFi.h>
#include <ESP8266HTTPClient.h>
#include "settings.h"

#include "PietteTech_DHT.h"

// system defines
#define DHTTYPE  DHT22           // Sensor type DHT11/21/22/AM2301/AM2302
#define DHTPIN   D4              // Digital pin for communications
#define DHT_PWR_PIN D8
// sleep for this many seconds
const int sleepSeconds = 600;

float humidity, temperature, dewPoint;
char str_humidity[10], str_temperature[10], str_dewPoint[10];

//declaration
void dht_wrapper(); // must be declared before the lib initialization

// Lib instantiate
PietteTech_DHT DHT(DHTPIN, DHTTYPE, dht_wrapper);

ADC_MODE(ADC_VCC);

void dht_wrapper() {
  DHT.isrCallback();
}

void setup() {
  Serial.begin(9600);

  // Connect D0 to RST to wake up
  pinMode(D0, WAKEUP_PULLUP);
  pinMode(DHT_PWR_PIN, OUTPUT);
    
  fillSensorData();
  connectWifi();
  String deviceId = macAddress();
  String vcc = readVcc();
  post("/home/weather/status", 
  "{\"deviceId\":\""+deviceId+"\",\"humidity\":"+str_humidity+",\"temperature\":"+str_temperature+",\"heatIndex\":"+str_dewPoint+",\"vcc\":"+vcc+"}");
  
  // convert to microseconds
  ESP.deepSleep(sleepSeconds * 1000000);
}

void loop() {
}

void fillSensorData() {
  digitalWrite(DHT_PWR_PIN, HIGH);
  delay(2000);

  int attempts = 0;
  while(attempts < 3) {
    Serial.println("Attempt to get DHT data");
    int acquireresult = DHT.acquireAndWait(1000);
    if (acquireresult == 0) {
      temperature = DHT.getCelsius();
      humidity = DHT.getHumidity();
      dewPoint = DHT.getDewPoint();
      break;
    } else {
      temperature = humidity = dewPoint = 0;
    }

    attempts++;
    delay(500);
  }
  DHT.reset();
  Serial.print("temperature: ");
  Serial.println(temperature);

  digitalWrite(DHT_PWR_PIN, LOW);
  delay(1000);

  // Convert the floats to strings and round to 2 decimal places
  dtostrf(humidity, 1, 2, str_humidity);
  dtostrf(temperature, 1, 2, str_temperature);
  dtostrf(dewPoint, 1, 2, str_dewPoint);
}

void connectWifi() {
  Serial.print("Wifi creds: ");
  Serial.print(ssid);
  Serial.println(password);

  WiFi.begin(ssid, password);
  int wifiConnectAttempts = 0;
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
    if (wifiConnectAttempts > 20) {
      Serial.println("Made 20 attempts, restarting device");
      Serial.print("Wifi status: ");
      Serial.println(WiFi.status());
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

String readVcc(void) {
    char str[10];
    uint32_t vcc = ESP.getVcc();
    sprintf(str, "%u", vcc);
    return String(str);
}


