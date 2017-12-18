#!/usr/bin/env bash
mvn clean package jfx:jar
java -jar target/jfx/app/challenge-1.0-SNAPSHOT-jfx.jar

