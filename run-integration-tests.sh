#!/bin/bash

# todo improve this so it can find sbt regardless of peoples different setups
echo 'Assembling JAR...' && sbt clean assembly || sbtx clean assembly && echo 'Running Integration Tests....' && docker-compose up --build --abort-on-container-exit --exit-code-from tester --force-recreate

if [ $? -eq 0 ] 
then
  echo ''
  echo "Integration Tests Passed!!"
else 
  echo ''
  echo "Integration Tests Failed With Exit Status Code $? :("
fi