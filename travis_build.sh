#!/bin/bash
set -ev

if [ -z "${TRAVIS_TAG}" ]; then
  ./gradlew check
fi
