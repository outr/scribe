#!/usr/bin/env bash

set -e

sbt +clean +compile
sbt +test