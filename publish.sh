#!/usr/bin/env bash

set -e

sbt +clean
sbt test
sbt +coreJS/publishSigned +coreJVM/publishSigned +coreNative/publishSigned +fileModuleJVM/publishSigned +fileModuleNative/publishSigned +jsonJS/publishSigned +jsonJVM/publishSigned +slf4j/publishSigned +slf4j2/publishSigned +migration/publishSigned +config/publishSigned +slack/publishSigned +logstash/publishSigned
sbt sonatypeRelease