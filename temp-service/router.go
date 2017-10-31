package main

import (
	"net/http"
)

func RouteWeatherStatus(w http.ResponseWriter, r *http.Request) {
	if r.Method == "POST" {
		SaveWeatherStatus(w, r)
	} else if r.Method == "GET" {
		GetLatestWeatherStatus(w)
	}
}
