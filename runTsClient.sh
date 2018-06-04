#!/usr/bin/env bash
./tsCompile.sh > /dev/null;
node clients/typescript-node-client/target/tsOutput/main.js
