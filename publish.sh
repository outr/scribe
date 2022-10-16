#!/usr/bin/env bash

set -e

sbt +clean
sbt +compile
sbt +test
sbt +publishSigned
sbt sonatypeBundleRelease