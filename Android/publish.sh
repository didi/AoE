#!/bin/bash

echo aoe publish start
echo clean project
./gradlew clean

# -------------- publish library
library=(logging common api core service)

echo library: ${library}

for name in ${library}
do
    echo run gradle task :library-${name}:publish
    ./gradlew :library-${name}:publish
done

# -------------- publish runtime
runtime=(tensorflow-lite pytorch ncnn mnn)

echo runtime: ${runtime}

for name in ${runtime}
do
    echo :runtime-${name}:publish
    ./gradlew :runtime-${name}:publish
done

# -------------- publish extensions
extensions=(pytorch)

echo extensions: ${extensions}

for name in ${extensions}
do
    echo :extensions-${name}:publish
    ./gradlew :extensions-${name}:publish
done

echo aoe published
echo done.
