package service

import (
	"net/http"
	"temp-service/repository"
	"github.com/gocql/gocql"
	"log"
	"time"
	"encoding/json"
	"io/ioutil"
	"io"
)

type WeatherAverageRequest struct {
	DeviceId string
	NumberOfDays int64
}

type WeatherAverageResponse struct {
	DeviceId       string
	AvgTemperature float32
	AvgHumidity    float32
	AvgHeatIndex   float32
}

func GetWeatherAverage(w http.ResponseWriter, r *http.Request) {
	body, err := ioutil.ReadAll(io.LimitReader(r.Body, 1048576))
	if err != nil {
		log.Println(err)
		w.WriteHeader(http.StatusInternalServerError)
		return
	}
	weatherAverageRequest := WeatherAverageRequest{}
	json.Unmarshal(body, &weatherAverageRequest)
	log.Println("GetWeatherAverage: ", weatherAverageRequest)

	var avgTemperature float32
	var avgHumidity float32
	var avgHeatIndex float32
	var start = time.Now().UnixNano()
	var end = start - ((1000 ^ 3) * 60 * 60 * 24 * weatherAverageRequest.NumberOfDays)

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
