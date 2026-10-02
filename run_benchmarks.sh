#!/bin/bash
set -e

echo "Building Rust library for release..."
cd rulogger-rs
cargo build --release
cd ..

echo "Building Java benchmarks using Maven..."
cd rulogger-java
mvn clean package -DskipTests
cd ..

echo "Running JMH Benchmarks..."
# Set java.library.path so Java can find librulogger_rs.so
java -Djava.library.path=$(pwd)/rulogger-rs/target/release -cp rulogger-java/target/rulogger-java-1.0-SNAPSHOT.jar:rulogger-java/target/benchmarks.jar org.openjdk.jmh.Main -f 1 -wi 3 -i 5 ".*RuLoggerBenchmark.*"
