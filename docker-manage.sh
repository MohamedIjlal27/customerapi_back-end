#!/bin/bash

case "$1" in
  "start")
    echo "Starting MariaDB container..."
    docker-compose up -d
    echo "Waiting for MariaDB to be ready..."
    sleep 10
    echo "MariaDB is ready!"
    ;;
  "stop")
    echo "Stopping MariaDB container..."
    docker-compose down
    ;;
  "restart")
    echo "Restarting MariaDB container..."
    docker-compose down
    docker-compose up -d
    echo "Waiting for MariaDB to be ready..."
    sleep 10
    echo "MariaDB is ready!"
    ;;
  "status")
    echo "Checking MariaDB container status..."
    docker-compose ps
    ;;
  *)
    echo "Usage: $0 {start|stop|restart|status}"
    exit 1
    ;;
esac 