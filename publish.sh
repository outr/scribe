#!/usr/bin/env bash

set -e

sbt +clean
sbt test
sbt +coreJS/publishSigned +coreJVM/publishSigned +slf4j/publishSigned +slf4j18/publishSigned +migration/publishSigned +slack/publishSigned +logstash/publishSigned coreNative/publishSigned
sbt sonatypeRelease