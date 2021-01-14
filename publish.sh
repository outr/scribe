#!/usr/bin/env bash

set -e

sbt +clean
sbt test
sbt +coreJS/publishSigned +coreJVM/publishSigned +fileModuleJVM/publishSigned +jsonJS/publishSigned +jsonJVM/publishSigned +slf4j/publishSigned +slf4j18/publishSigned +migration/publishSigned +config/publishSigned +slack/publishSigned +logstash/publishSigned coreNative/publishSigned fileModuleNative/publishSigned
sbt sonatypeRelease