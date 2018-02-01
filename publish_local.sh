#!/usr/bin/env bash

sbt clean +macrosJS/publishLocal +macrosJVM/publishLocal +coreJS/publishLocal +coreJVM/publishLocal +extrasJS/publishLocal +extrasJVM/publishLocal +slf4j/publishLocal +slack/publishLocal ++2.11.12 macrosNative/publishLocal coreNative/publishLocal extrasNative/publishLocal