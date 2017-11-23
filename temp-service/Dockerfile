FROM golang:1.9

WORKDIR /go/src/temp-service
COPY ./src/temp-service .

RUN go-wrapper download
RUN go-wrapper install

CMD ["go-wrapper", "run"]