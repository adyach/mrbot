package main

import (
    "log"
    "net/http"
    "github.com/adyach/mrbot/temp-service/repository"
)

func main() {
    repository.Repository()
    defer repository.Close()

    http.HandleFunc("/home/weather/status", RouteWeatherStatus)
    http.HandleFunc("/home/weather/average", RouteWeatherAverage)
    log.Fatal(http.ListenAndServe(":8081", nil))
}
