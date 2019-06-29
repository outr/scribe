#!/usr/bin/env bash

set -e

sbt +clean +macrosJS/compile +macrosJVM/compile +coreJS/compile +coreJVM/compile +slf4j/compile +slf4j18/compile
sbt ++2.11.12 test ++2.12.8 test
sbt +macrosJS/publishSigned +macrosJVM/publishSigned +coreJS/publishSigned +coreJVM/publishSigned +slf4j/publishSigned +slf4j18/publishSigned ++2.11.12 slack/publishSigned logstash/publishSigned macrosNative/publishSigned coreNative/publishSigned ++2.12.8 slack/publishSigned logstash/publishSigned
sbt sonatypeRelease