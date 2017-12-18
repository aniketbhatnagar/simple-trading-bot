# Trading Bot

This repo contains an implementation of a trading bot that can use multiple strategies for trades.

To prove the framework, it contains a contrarian trading strategy which works by counting the M consecutive upticks and N consecutive downticks.

## Features

- Event sourcing architecture to enable scaling and fault tolerance.
- State handling of strategies to support bot restarts/recovery from crashes.

## Writing a strategy
Implement the TradingStrategy interface and publish trade messages as part of decision.

Please make sure that TradingStrategy implementation does not have any internal state and any state is depends on is returned as part of decision.

## Starting the application
Execute the provided buildAndRun.sh. Alternatively, here are the manual steps:
- Build the app using maven - mvn clean package jfx:jar
- Run the app - java -jar target/jfx/app/challenge-1.0-SNAPSHOT-jfx.jar
