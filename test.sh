#!/usr/bin/env bash

set -e

sbt +clean +compile +slack/compile +logstash/compile
sbt +test +slack/test +logstash/test