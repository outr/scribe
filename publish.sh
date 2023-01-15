#!/usr/bin/env bash

set -e

sbt +clean
sbt +compile
sbt +test
sbt docs/mdoc
sbt +publishSigned
sbt sonatypeBundleRelease