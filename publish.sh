#!/usr/bin/env bash

sbt +clean +testsJS/test +testsJVM/test +slack/test +slf4j/test +macrosJS/publishSigned +macrosJVM/publishSigned +coreJS/publishSigned +coreJVM/publishSigned +extrasJS/publishSigned +extrasJVM/publishSigned +slf4j/publishSigned +slack/publishSigned ++2.11.12 macrosNative/publishSigned coreNative/publishSigned extrasNative/publishSigned sonatypeRelease