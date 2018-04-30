package service

import (
    "encoding/json"
    "io"
    "io/ioutil"
    "log"
    "net/http"
    "time"

    "github.com/adyach/mrbot/temp-service/repository"
    "github.com/gocql/gocql"
)

type WeatherStatusRequest struct {
    DeviceId    string
    Humidity    float32
    Temperature float32
    HeatIndex   float32
    Vcc         int32
}

type WeatherStatusResponse struct {
    Timestamp   string
    DeviceId    string
    Humidity    float32
    Temperature float32
    HeatIndex   float32
}

func SaveWeatherStatus(w http.ResponseWriter, r *http.Request) {
    body, err := ioutil.ReadAll(io.LimitReader(r.Body, 1048576))
    if err != nil {
        log.Println(err)
        w.WriteHeader(http.StatusInternalServerError)
        return
    }
    weatherStatus := WeatherStatusRequest{}
    json.Unmarshal(body, &weatherStatus)
    log.Println("SaveWeatherStatus: ", weatherStatus)

    if err := repository.Session().Query(`INSERT INTO weather (timestamp, device_id, temperature, humidity, heat_index, vcc) VALUES (?, ?, ?, ?, ?, ?)`,
        time.Now().UnixNano(),
        weatherStatus.DeviceId,
        weatherStatus.Temperature,
        weatherStatus.Humidity,
        weatherStatus.HeatIndex,
        weatherStatus.Vcc).Exec();
            err != nil {
        log.Println("Error while inserting weather: ", err)
        w.WriteHeader(http.StatusInternalServerError)
        return
    }
    w.WriteHeader(http.StatusNoContent)
}

func GetLatestWeatherStatus(w http.ResponseWriter) {
    var timestamp int64
    var device_id string
    var humidity float32
    var temperature float32
    var heat_index float32

    if err := repository.Session().Query(`SELECT timestamp, device_id, temperature, humidity, heat_index FROM weather LIMIT 1`).Consistency(gocql.One).Scan(
        &timestamp, &device_id, &temperature, &humidity, &heat_index);
            err != nil {
        log.Println("Error while quering weather: ", err)
        w.WriteHeader(http.StatusInternalServerError)
        return
    }
    weatherStatus := WeatherStatusResponse{Timestamp: time.Unix(0, timestamp).String(), DeviceId: device_id, Temperature: temperature, Humidity: humidity, HeatIndex: heat_index}
    jsonStr, _ := json.Marshal(weatherStatus)
    log.Println("GetLatestWeatherStatus: ", weatherStatus)

    w.Header().Set("Content-Type", "application/json; charset=UTF-8")
    w.WriteHeader(http.StatusOK)
    w.Write(jsonStr)
}
