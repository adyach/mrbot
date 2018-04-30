package repository

import (
	"log"
	"os"

	"github.com/gocql/gocql"
)

var cluster *gocql.ClusterConfig
var session *gocql.Session

func Repository() {
	cassandraHost, ok := os.LookupEnv("CASSANDRA_HOST")
	if !ok {
		cassandraHost = "127.0.0.1"
	}
	log.Println("cassandraHost: ", cassandraHost)
	cluster = gocql.NewCluster(cassandraHost)
	cluster.Keyspace = "temp_service"
	cluster.Consistency = gocql.Quorum
	sess, err := cluster.CreateSession()
	if err != nil {
		log.Println("Error while accessing Cassandra: ", err)
	} else {
		session = sess
	}
}

func Close() {
	session.Close()
}

func Session() *gocql.Session {
	return session
}
