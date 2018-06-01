#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

rm -f $DIR/src/app/models.js $DIR/src/app/models.d.ts

./node_modules/protobufjs/bin/pbjs -t static-module -w commonjs -o $DIR/src/app/models.js $DIR/../protos/*
./node_modules/protobufjs/bin/pbts -o $DIR/src/app/models.d.ts $DIR/src/app/models.js
