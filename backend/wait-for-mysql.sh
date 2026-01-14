#!/bin/bash

# Wait for MySQL to be fully ready before continuing
# This script is used by services that depend on MySQL

MYSQL_HOST=${1:-mysql}
MYSQL_PORT=${2:-3306}
MYSQL_USER=${3:-restaurant}
MYSQL_PASSWORD=${4:-}

echo "⏳ Waiting for MySQL at $MYSQL_HOST:$MYSQL_PORT..."

# Max retries: 90 seconds with 1 second interval
max_retries=90
retry_count=0

while [ $retry_count -lt $max_retries ]; do
    if mysqladmin ping -h "$MYSQL_HOST" -P "$MYSQL_PORT" -u "$MYSQL_USER" -p"$MYSQL_PASSWORD" --silent 2>/dev/null; then
        echo "✅ MySQL is ready!"
        exit 0
    fi
    
    retry_count=$((retry_count + 1))
    echo "⏳ MySQL not ready yet... ($retry_count/$max_retries)"
    sleep 1
done

echo "❌ MySQL failed to start after ${max_retries} seconds"
exit 1
