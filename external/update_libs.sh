#!/bin/bash

curl --url 'https://raw.githubusercontent.com/AmanoTeam/UnalixLibs/master/lib/android-arm/libunalix.so' \
    --url 'https://raw.githubusercontent.com/AmanoTeam/UnalixLibs/master/lib/android-arm64/libunalix.so' \
    --url 'https://raw.githubusercontent.com/AmanoTeam/UnalixLibs/master/lib/android-x86-64/libunalix.so' \
    --url 'https://raw.githubusercontent.com/AmanoTeam/UnalixLibs/master/lib/android-x86/libunalix.so' \
    --output './app/src/main/jniLibs/armeabi-v7a/libunalix.so' \
    --output './app/src/main/jniLibs/arm64-v8a/libunalix.so' \
    --output './app/src/main/jniLibs/x86_64/libunalix.so' \
    --output './app/src/main/jniLibs/x86/libunalix.so' \
    --silent \
    --ipv4 \
    --connect-timeout '15' \
    --insecure \
    --no-sessionid \
    --no-keepalive
