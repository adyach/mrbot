package main

import (
	"log"
	"net/http"
	"io/ioutil"
	"io"
	"encoding/json"
	"github.com/gocql/gocql"
	"time"
	"os"
)

type WeatherStatusRequest struct {
		DeviceId string
		Humidity float32
		Temperature float32
		HeatIndex float32
		Vcc int32
}

type WeatherStatusResponse struct {
		Timestamp string
		DeviceId string
		Humidity float32
		Temperature float32
		HeatIndex float32
}

var cluster *gocql.ClusterConfig
var session *gocql.Session

func main() {
	cassandraHost, ok := os.LookupEnv("CASSANDRA_HOST")
	if !ok {
		cassandraHost = "127.0.0.1"
	}
	log.Println("cassandraHost: ", cassandraHost)
	cluster = gocql.NewCluster(cassandraHost)
	cluster.Keyspace = "temp_service"
	cluster.Consistency = gocql.Quorum
	session, _ = cluster.CreateSession()
	defer session.Close()

	http.HandleFunc("/home/weather/status", func(w http.ResponseWriter, r *http.Request) {
		if r.Method == "POST" {
			post(w, r)
		} else if r.Method == "GET" {
			get(w)
		}
	})
	log.Fatal(http.ListenAndServe(":8081", nil))
}

func post(w http.ResponseWriter, r *http.Request) {
	body, err := ioutil.ReadAll(io.LimitReader(r.Body, 1048576))
	if err != nil {
		log.Println(err)
		w.WriteHeader(http.StatusInternalServerError)
		return
	}
	weatherStatus := WeatherStatusRequest{}
	json.Unmarshal(body, &weatherStatus)
	log.Println("Post: ", weatherStatus)

	if err := session.Query(`INSERT INTO weather (timestamp, device_id, temperature, humidity, heat_index, vcc) VALUES (?, ?, ?, ?, ?, ?)`,
	time.Now().UnixNano(),
	weatherStatus.DeviceId,
	weatherStatus.Humidity,
	weatherStatus.Temperature,
	weatherStatus.HeatIndex,
	weatherStatus.Vcc).Exec(); err != nil {
		log.Println("Error while inserting weather: ", err)
		w.WriteHeader(http.StatusInternalServerError)
		return
	}
	w.WriteHeader(http.StatusNoContent)
}

func get(w http.ResponseWriter) {
	var timestamp int64
	var device_id string
	var humidity float32
	var temperature float32
	var heat_index float32

	if err := session.Query(`SELECT timestamp, device_id, temperature, humidity, heat_index FROM weather LIMIT 1`).Consistency(gocql.One).Scan(
		&timestamp, &device_id, &temperature, &humidity, &heat_index); err != nil {
		log.Println("Error while quering weather: ", err)
		w.WriteHeader(http.StatusInternalServerError)
		return
	}
	weatherStatus := WeatherStatusResponse{Timestamp: time.Unix(0, timestamp).String(), DeviceId: device_id, Temperature: temperature, Humidity: humidity, HeatIndex: heat_index}
	jsonStr, _ := json.Marshal(weatherStatus)
	log.Println("Get: ", weatherStatus)

	w.Header().Set("Content-Type", "application/json; charset=UTF-8")
	w.WriteHeader(http.StatusOK)
	w.Write(jsonStr)
}
