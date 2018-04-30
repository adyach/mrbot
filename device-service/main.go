package main

import (
    "log"
    "net"

    "golang.org/x/net/context"
    "google.golang.org/grpc"
    pb "github.com/adyach/mrbot/device-service/proto"
    "google.golang.org/grpc/reflection"
    "time"
)

const (
    port    = ":50051"
    network = "tcp"
    address = "localhost:50051"
)

type server struct{}

func (s *server) AddDevice(ctx context.Context, in *pb.Device) (*pb.Result, error) {
    return &pb.Result{0, "ok"}, nil
}

func (s *server) RemoveDevice(ctx context.Context, in *pb.Device) (*pb.Result, error) {
    return &pb.Result{0, "ok"}, nil
}

func (s *server) GetDevice(ctx context.Context, in *pb.DeviceId) (*pb.GetDeviceResp, error) {
    return &pb.GetDeviceResp{}, nil
}

func main() {

    go func() {
        for {
            send()
            time.Sleep(time.Second * 3)
        }
    }()

    lis, err := net.Listen(network, port)
    if err != nil {
        log.Fatalf("failed to listen: %v", err)
    }
    s := grpc.NewServer()
    pb.RegisterDeviceServiceServer(s, &server{})

    reflection.Register(s)
    if err := s.Serve(lis); err != nil {
        log.Fatalf("failed to serve: %v", err)
    }
}

func send() {
    conn, err := grpc.Dial(address, grpc.WithInsecure())
    if err != nil {
        log.Fatalf("did not connect: %v", err)
    }
    defer conn.Close()
    c := pb.NewDeviceServiceClient(conn)

    ctx, cancel := context.WithTimeout(context.Background(), time.Second)
    defer cancel()
    r, err := c.AddDevice(ctx, &pb.Device{})
    if err != nil {
        log.Fatalf("could not greet: %v", err)
    }
    log.Printf("Resp: %s %s", r.ErrorCode, r.Message)
}
