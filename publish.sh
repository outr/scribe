#!/usr/bin/env bash

set -e

sbt +clean
sbt +compile +slack/compile +logstash/compile
sbt +test +slack/test +logstash/test
sbt docs/mdoc
sbt +publishSigned +slack/publishSigned +logstash/publishSigned
sbt sonatypeBundleRelease