#!/bin/sh
set -o xtrace
set -e

mkdir -p bin
javac -d bin src/java/dlp/DLP.java
gcc -Wall -O3 -o bin/dlpc src/c/dlp.c -Wall -lcrypto -lpthread -std=c99
