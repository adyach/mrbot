package main

import (
	"log"
	"net/http"
	"temp-service/repository"
)

func main() {
	repository.Repository()
	defer repository.Close()

	http.HandleFunc("/home/weather/status", func(w http.ResponseWriter, r *http.Request) {
		RouteWeatherStatus(w, r)
	})
	http.HandleFunc("/home/weather/average", func(w http.ResponseWriter, r *http.Request) {
		RouteWeatherAverage(w, r)
	})
	log.Fatal(http.ListenAndServe(":8081", nil))
}
