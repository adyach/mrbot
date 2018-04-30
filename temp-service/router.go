package main

import (
    "net/http"
    "github.com/adyach/mrbot/temp-service/service"
)

func RouteWeatherStatus(w http.ResponseWriter, r *http.Request) {
    if r.Method == "POST" {
        service.SaveWeatherStatus(w, r)
    } else if r.Method == "GET" {
        service.GetLatestWeatherStatus(w)
    }
}

func RouteWeatherAverage(w http.ResponseWriter, r *http.Request) {
    if r.Method == "GET" {
        service.GetWeatherAverage(w, r)
    }
}
