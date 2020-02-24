#!/usr/bin/env bash

set -e

sbt +clean
sbt test
sbt +macrosJS/publishSigned +macrosJVM/publishSigned +coreJS/publishSigned +coreJVM/publishSigned +slf4j/publishSigned +slf4j18/publishSigned +slack/publishSigned +logstash/publishSigned macrosNative/publishSigned coreNative/publishSigned
sbt sonatypeRelease