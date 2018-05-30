#!/bin/bash
set -eux -o pipefail

export VETAFI_CLIENT_ID="$(biscuit get --filename=conf/biscuit/secrets.yaml local::id-me-client-id)"
export VETAFI_CLIENT_SECRET="$(biscuit get --filename=conf/biscuit/secrets.yaml local::id-me-client-secret)"

cd ui

npm run-script build-prod

cd ..

sbt run -Dconfig.resource=application.local.conf -Dlogger.resource=logback.xml
