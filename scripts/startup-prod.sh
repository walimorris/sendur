#!/bin/bash

cd /home/ec2-user || exit

# remove ngrok proxy once https is set up
nohup ngrok http 8082 > ngrok.log 2>&1 &

aws s3 cp s3://sendur-app-bucket/sendur-1.0-SNAPSHOT.jar .
java -jar sendur-1.0-SNAPSHOT.jar --spring.profiles.active=prod