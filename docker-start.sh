#!/bin/sh

set -x

# Create a new image version with latest code changes.
docker build --tag pleo-antaeus .

# Build the code.
docker run \
  --publish 7000:7000 \
  --rm \
  --interactive \
  --tty \
  --name antaeus \
  --volume $(pwd):/antaeus pleo-antaeus
  # This volume is only there so incremental builds are way faster
#  --volume pleo-antaeus-build-cache:/root/.gradle pleo-antaeus
