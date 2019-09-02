#!/bin/bash

case $1 in
    major|minor|patch)
        NEXT_VER="$(git semver --next-$1)"
        git tag -a "$NEXT_VER" -m "Release $NEXT_VER"
        git push origin $NEXT_VER
    ;;

    *)
        echo "Usage: ./release.sh PART, where PART is major, minor, patch"
        echo "Current: $(git semver)"
        exit 1
    ;;
esac
