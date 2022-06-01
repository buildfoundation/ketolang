#!/bin/bash
set -euo pipefail

# Run from top-level project root.
./gradlew :kotlinc:ksp-validator:assemble

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

# KSP
KSP_VERSION=1.6.21-1.0.5
if [[ ! -f ksp.zip ]]; then
  echo "Downloading KSP..."
  curl \
    --output ksp.zip \
    --location \
    --fail \
    https://github.com/google/ksp/releases/download/$KSP_VERSION/artifacts.zip
  unzip ksp.zip
fi

KSP_PLUGIN_ID=com.google.devtools.ksp.symbol-processing
KSP_PLUGIN_OPT=plugin:$KSP_PLUGIN_ID

KSP_PLUGIN_JAR=./com/google/devtools/ksp/symbol-processing-cmdline/$KSP_VERSION/symbol-processing-cmdline-$KSP_VERSION.jar
KSP_API_JAR=./com/google/devtools/ksp/symbol-processing-api/$KSP_VERSION/symbol-processing-api-$KSP_VERSION.jar
KOTLINC=./kotlinc/bin/kotlinc
KOTLINC_NATIVE=./kotlin-native-macos-aarch64-$KOTLINC_VERSION/bin/kotlinc-native

AP=../../../../kotlinc/ksp-validator/build/libs/ksp-validator.jar

rm -rf ksp-out kotlinc-out
mkdir ksp-out
mkdir kotlinc-out

set -x
echo "Validating the code..."
$KOTLINC \
        -Xplugin=$KSP_PLUGIN_JAR \
        -Xplugin=$KSP_API_JAR \
        -P $KSP_PLUGIN_OPT:apclasspath=$AP \
        -P $KSP_PLUGIN_OPT:projectBaseDir=../../. \
        -P $KSP_PLUGIN_OPT:classOutputDir=./ksp-out \
        -P $KSP_PLUGIN_OPT:javaOutputDir=./ksp-out \
        -P $KSP_PLUGIN_OPT:kotlinOutputDir=./ksp-out \
        -P $KSP_PLUGIN_OPT:resourceOutputDir=./ksp-out \
        -P $KSP_PLUGIN_OPT:kspOutputDir=./ksp-out \
        -P $KSP_PLUGIN_OPT:cachesDir=./ksp-out \
        -P $KSP_PLUGIN_OPT:incremental=false \
        -no-stdlib \
        -no-reflect \
        -no-jdk \
        ../../src/main/kotlin/com/pushtorefresh/ketolang/sample/Sample.kt

echo "Compiling for JVM..."
$KOTLINC \
        -no-reflect \
        -no-stdlib \
        -no-jdk \
        -d kotlinc-out/result.jar \
        ../../src/main/kotlin/kotlin/String.kt \
        ../../src/main/kotlin/com/pushtorefresh/ketolang/sample/Sample.kt

echo "Compiling for Native..."
$KOTLINC_NATIVE \
        -nomain \
        -produce library \
        -output kotlinc-out/result.bin \
        ../../src/main/kotlin/com/pushtorefresh/ketolang/sample/Sample.kt
