#!/bin/bash

if [ $# -ne 1 ]; then
    echo "Usage: bootstrap.sh SAMPLE_PROJECT_FOLDER"
    exit 1
fi

DEST=$1

if [ -a $DEST ]; then
    echo "$DEST already exists"
    exit 2
fi

mkdir -p $DEST
cd $DEST

git init
git remote add origin https://github.com/lkishalmi/gradle-gatling-plugin.git
git config core.sparseCheckout true
echo "sample-project/" >> .git/info/sparse-checkout
git pull origin master

mv sample-project/* .
rm -rf .git sample-project
