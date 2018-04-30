package service

import (
    "net/http"
    "github.com/gocql/gocql"
    "log"
    "time"
    "encoding/json"
    "strconv"
    "github.com/adyach/mrbot/temp-service/repository"
)

type WeatherAverageRequest struct {
    DeviceId     string
    NumberOfDays int
}

type WeatherAverageResponse struct {
    DeviceId       string
    AvgTemperature float32
    AvgHumidity    float32
    AvgHeatIndex   float32
}

func GetWeatherAverage(w http.ResponseWriter, r *http.Request) {
    weatherAverageRequest := WeatherAverageRequest{}
    weatherAverageRequest.DeviceId = r.URL.Query().Get("device_name")
    weatherAverageRequest.NumberOfDays, _ = strconv.Atoi(r.URL.Query().Get("last_days_number"))
    log.Println("GetWeatherAverage: ", weatherAverageRequest)

    var avgTemperature float32
    var avgHumidity float32
    var avgHeatIndex float32
    var start = time.Now().UnixNano()
    var end = time.Now().AddDate(0, 0, -weatherAverageRequest.NumberOfDays).UnixNano()

    if err := repository.Session().Query(`SELECT avg(temperature), avg(humidity), avg(heat_index) from weather
												WHERE device_id = ? AND timestamp <= ? AND timestamp >= ?;`, weatherAverageRequest.DeviceId, start, end).
            Consistency(gocql.One).Scan(&avgTemperature, &avgHumidity, &avgHeatIndex);
            err != nil {
        log.Println("Error while quering weather: ", err)
        return
    }

    weatherAvg := WeatherAverageResponse{DeviceId: weatherAverageRequest.DeviceId, AvgTemperature: avgTemperature, AvgHumidity: avgHumidity, AvgHeatIndex: avgHeatIndex}
    jsonStr, _ := json.Marshal(weatherAvg)
    log.Println("GetWeatherAverage: ", weatherAvg)

    w.Header().Set("Content-Type", "application/json; charset=UTF-8")
    w.WriteHeader(http.StatusOK)
    w.Write(jsonStr)
}
