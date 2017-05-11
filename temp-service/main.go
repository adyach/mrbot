package main

import (
	"log"
	"net/http"
	"io/ioutil"
	"io"
)

func main() {
	var weather []byte
	http.HandleFunc("/home/weather/status", func(w http.ResponseWriter, r *http.Request) {
		if r.Method == "POST" {
			log.Println(r.Body)
			w.WriteHeader(http.StatusNoContent)
			body, err := ioutil.ReadAll(io.LimitReader(r.Body, 1048576))
			if err != nil {
				panic(err)
			}
			weather = body
		} else if r.Method == "GET" {
			log.Println("Current weather: ", weather)
			w.Header().Set("Content-Type", "application/json; charset=UTF-8")
			w.WriteHeader(http.StatusOK)
			w.Write(weather)
		}
	})
	log.Fatal(http.ListenAndServe(":8081", nil))
}
