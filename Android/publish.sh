#!/bin/bash

echo aoe publish start
echo clean project
./gradlew clean

repo=$1

case "$repo" in
   "github") task='publish'
   ;;
   "bintray") task='bintrayUpload'
   ;;
   "didi") task='uploadArchives'
   ;;
   *) task='build'
   ;;
esac

echo repo is ${repo}, so 'select' task: ${task}

# -------------- publish library
library=(logging common api core service)

echo library: ${library}

for name in ${library}
do
    echo run gradle task :library-${name}:${task}
    ./gradlew :library-${name}:${task}
done

# -------------- publish runtime
runtime=(tensorflow-lite pytorch ncnn mnn)

echo runtime: ${runtime}

for name in ${runtime}
do
    echo :runtime-${name}:${task}
    ./gradlew :runtime-${name}:${task}
done

## -------------- publish extensions
#extensions=(pytorch)
#
#echo extensions: ${extensions}
#
#for name in ${extensions}
#do
#    echo :extensions-${name}:${task}
#    ./gradlew :extensions-${name}:${task}
#done

echo aoe published
echo done.
