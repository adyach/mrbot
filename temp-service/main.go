package main

import (
	"log"
	"net/http"
)

func main() {
	Service()
	defer Close()

	http.HandleFunc("/home/weather/status", func(w http.ResponseWriter, r *http.Request) {
		RouteWeatherStatus(w, r)
	})
	log.Fatal(http.ListenAndServe(":8081", nil))
}
