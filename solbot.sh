#!/bin/bash

java -cp ~/solbot/solbot.jar com.sol.bot.SolBot ~/solbot/solbot.token /tmp/sol_stdin 1>~/solbot/solbot.log 2>~/solbot/solbot.err &
