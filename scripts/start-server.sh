#!/bin/bash

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_ROOT="$SCRIPT_DIR/.."

JAR_FILE="$PROJECT_ROOT/target/telemedicineApp-1.0-SNAPSHOT.jar"
LOG_FILE="$PROJECT_ROOT/server.log"
PID_FILE="$PROJECT_ROOT/server.pid"

SPRING_PROFILE="local"

USERNAME="${ADMIN_USERNAME}"
PASSWORD="${ADMIN_PASSWORD}"
if [[ -z "$USERNAME" || -z "$PASSWORD" ]]; then
    echo "Error: ADMIN_USERNAME and ADMIN_PASSWORD environment variables are not set."
    exit 1
fi

read -p "Username: " input_user
read -s -p "Password: " input_pass
echo

if [[ "$input_user" != "$USERNAME" || "$input_pass" != "$PASSWORD" ]]; then
    echo "Authentication failed. Exiting."
    exit 1
fi

echo "Authentication successful."

if [ -f "$PID_FILE" ]; then
    PID=$(cat "$PID_FILE")
    if ps -p "$PID" > /dev/null 2>&1; then
        echo "Server already running. PID: $PID"
        exit 1
    else
        echo "Stale PID file found. Removing..."
        rm "$PID_FILE"
    fi
fi

if [ ! -f "$JAR_FILE" ]; then
    echo "Error: JAR file not found at $JAR_FILE"
    exit 1
fi

echo "Starting Telemedicine Server..."
nohup java -jar "$JAR_FILE" --spring.profiles.active="$SPRING_PROFILE" > "$LOG_FILE" 2>&1 &

echo $! > "$PID_FILE"

echo "Server started. PID: $(cat "$PID_FILE")"
echo "Logs are being written to $LOG_FILE"