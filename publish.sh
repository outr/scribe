#!/usr/bin/env bash

set -e

sbt +clean +compile
sbt +testsJS/test +testsJVM/test +slack/test +slf4j/test +slf4j18/test +logstash/test
sbt +macrosJS/publishSigned +macrosJVM/publishSigned +coreJS/publishSigned +coreJVM/publishSigned +extrasJS/publishSigned +extrasJVM/publishSigned +slf4j/publishSigned +slf4j18/publishSigned +slack/publishSigned +logstash/publishSigned ++2.11.12 macrosNative/publishSigned coreNative/publishSigned extrasNative/publishSigned
sbt sonatypeRelease