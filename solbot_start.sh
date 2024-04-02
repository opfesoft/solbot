#!/bin/bash

solBotPid=$(pgrep -U "$LOGNAME" -f 'com.sol.bot.SolBot')

if [ "$solBotPid" != "" ]
then
  echo "SolBot already running, PID: $solBotPid"
  exit 0
fi

java -cp ~/solbot/solbot.jar com.sol.bot.SolBot ~/solbot/solbot.token /tmp/sol_stdin ~/Server.log 1>~/solbot/solbot.log 2>~/solbot/solbot.err &
