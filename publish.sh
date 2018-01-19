#!/usr/bin/env bash

sbt clean +scribeJVM/publishSigned +scribeJS/publishSigned ++2.11.12 scribeNative/publishSigned sonatypeRelease