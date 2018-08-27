#!/usr/bin/env bash

set -e

sbt +clean +compile
sbt +testsJS/test +testsJVM/test +slack/test +slf4j/test +slf4j18/test +logstash/test