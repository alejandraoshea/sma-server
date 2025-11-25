#!/bin/bash

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_ROOT="$SCRIPT_DIR/.."

PID_FILE="$PROJECT_ROOT/server.pid"

USERNAME="${ADMIN_USERNAME}"
PASSWORD="${ADMIN_PASSWORD}"

USERNAME="a"
PASSWORD="1"

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

if [ ! -f "$PID_FILE" ]; then
    echo "No PID file found. Is the server running?"
    exit 1
fi

PID=$(cat "$PID_FILE")

if ! ps -p "$PID" > /dev/null 2>&1; then
    echo "Stale PID file detected. No process with PID $PID is running."
    rm "$PID_FILE"
    exit 1
fi

kill "$PID" && rm "$PID_FILE"

echo "Server stopped."