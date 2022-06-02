#!/bin/bash
set -euo pipefail

# Run from top-level project root.
./gradlew :kotlinc:compiler-plugin-standalone:assemble

cd samples/cli

mkdir -p build/cli-sample
cd build/cli-sample

# Kotlin compiler
KOTLINC_VERSION=1.6.21
if [[ ! -f kotlinc.zip ]]; then
  echo "Downloading kotlinc..."
  curl \
    --output kotlinc.zip \
    --location \
    --fail \
    https://github.com/JetBrains/kotlin/releases/download/v$KOTLINC_VERSION/kotlin-compiler-$KOTLINC_VERSION.zip
  unzip kotlinc.zip
fi

if [[ ! -f kotlinc-native.tar.gz ]]; then
  echo "Downloading kotlinc-native..."
  curl \
      --output kotlinc-native.tar.gz \
      --location \
      --fail \
      https://github.com/JetBrains/kotlin/releases/download/v$KOTLINC_VERSION/kotlin-native-macos-aarch64-$KOTLINC_VERSION.tar.gz
    tar -xf kotlinc-native.tar.gz
fi

KOTLINC=./kotlinc/bin/kotlinc
KOTLINC_NATIVE=./kotlin-native-macos-aarch64-$KOTLINC_VERSION/bin/kotlinc-native

KETOLANG_KOTLINC_PLUGIN_JAR=../../../../kotlinc/compiler-plugin-standalone/build/libs/compiler-plugin.jar

rm -rf kotlinc-out
mkdir kotlinc-out

set -x
echo "Compiling for JVM..."
$KOTLINC \
        -Xplugin=$KETOLANG_KOTLINC_PLUGIN_JAR \
        -no-reflect \
        -no-jdk \
        -d kotlinc-out/result.jar \
        ../../src/main/kotlin/com/pushtorefresh/ketolang/sample/Sample.kt

echo "Compiling for Native..."
$KOTLINC_NATIVE \
        -Xplugin=$KETOLANG_KOTLINC_PLUGIN_JAR \
        -nomain \
        -produce library \
        -output kotlinc-out/result.bin \
        ../../src/main/kotlin/com/pushtorefresh/ketolang/sample/Sample.kt
