package com.rulogger.benchmark;

import com.rulogger.RuLogger;
import org.openjdk.jmh.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 1, time = 1)
@Measurement(iterations = 1, time = 1)
@Fork(1)
@Threads(1)
public class RuLoggerBenchmark {

    private static final Logger slf4jLogger = LoggerFactory.getLogger(RuLoggerBenchmark.class);

    private final String logMessage = "This is a standard benchmark log message with some payload data to write.";
    private ByteBuffer directBuffer;
    private int bufferLength;

    @Setup(Level.Trial)
    public void setup() {
        RuLogger.init("rulogger-benchmark.log");
        byte[] bytes = logMessage.getBytes(StandardCharsets.UTF_8);
        directBuffer = ByteBuffer.allocateDirect(bytes.length);
        directBuffer.put(bytes);
        bufferLength = bytes.length;
    }

    @TearDown(Level.Trial)
    public void tearDown() {
        RuLogger.close();
    }

    @Benchmark
    public void testSlf4jLogback() {
        slf4jLogger.info(logMessage);
    }

    @Benchmark
    public void testRuLoggerString() {
        RuLogger.info(logMessage);
    }

    @Benchmark
    public void testRuLoggerDirectBuffer() {
        directBuffer.position(0);
        RuLogger.logDirect(directBuffer, bufferLength);
    }
}
