#!/bin/bash

# Application monitoring script
# Continuously monitors the Spring Boot + JavaFX application

APP_LOG="/tmp/app.log"
MONITOR_LOG="/tmp/monitor.log"
CHECK_INTERVAL=10

echo "=== Application Monitor Started at $(date) ===" | tee -a $MONITOR_LOG

while true; do
    # Check if the application process is running
    if ps aux | grep "com.ticari.CrmApplication" | grep -v grep > /dev/null; then
        echo "[$(date '+%Y-%m-%d %H:%M:%S')] ✓ Application is running" | tee -a $MONITOR_LOG
        
        # Check for errors in the last few lines
        ERROR_COUNT=$(tail -100 $APP_LOG | grep -i "error\|exception" | grep -v "ERROR StatusLogger" | wc -l)
        
        if [ $ERROR_COUNT -gt 0 ]; then
            echo "[$(date '+%Y-%m-%d %H:%M:%S')] ⚠ Found $ERROR_COUNT errors in logs" | tee -a $MONITOR_LOG
            tail -100 $APP_LOG | grep -i "error\|exception" | grep -v "ERROR StatusLogger" | tail -5 | tee -a $MONITOR_LOG
        fi
        
        # Show memory usage
        MEMORY=$(ps aux | grep "com.ticari.CrmApplication" | grep -v grep | awk '{print $4}')
        echo "[$(date '+%Y-%m-%d %H:%M:%S')] Memory usage: ${MEMORY}%" | tee -a $MONITOR_LOG
        
    else
        echo "[$(date '+%Y-%m-%d %H:%M:%S')] ✗ Application is NOT running" | tee -a $MONITOR_LOG
        echo "[$(date '+%Y-%m-%d %H:%M:%S')] Last 20 lines of log:" | tee -a $MONITOR_LOG
        tail -20 $APP_LOG | tee -a $MONITOR_LOG
        break
    fi
    
    sleep $CHECK_INTERVAL
done

echo "=== Monitor stopped at $(date) ===" | tee -a $MONITOR_LOG
