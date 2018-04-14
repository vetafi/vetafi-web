#!/bin/bash
set -eux -o pipefail

sbt run -Dconfig.resource=application.dev.conf -Dlogger.resource=logback.xml
