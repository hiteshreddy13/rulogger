#!/bin/bash
set -e

echo "Building Rust library..."
cd rulogger-rs
cargo build --release
cd ..

echo "Compiling Java code..."
mkdir -p rulogger-java/out
javac -d rulogger-java/out rulogger-java/src/main/java/com/rulogger/*.java

echo "Running Java tests..."
# The .so file will be at rulogger-rs/target/release/librulogger_rs.so
# Need to set java.library.path to that directory
java -Djava.library.path=$(pwd)/rulogger-rs/target/release -cp rulogger-java/out com.rulogger.Main
