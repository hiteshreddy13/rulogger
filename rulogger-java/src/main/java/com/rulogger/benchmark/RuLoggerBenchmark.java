package com.rulogger.benchmark;

import com.rulogger.RuLogger;
import org.openjdk.jmh.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
@Threads(4)
public class RuLoggerBenchmark {

    private static final Logger slf4jLogger = LoggerFactory.getLogger(RuLoggerBenchmark.class);

    private final String logTemplate = "User {} logged in from IP {} with session id {}";
    private final String user = "alice_wonderland";
    private final String ip = "192.168.1.100";
    private final int sessionId = 987654321;

    @Setup(Level.Trial)
    public void setup() {
        RuLogger.init("rulogger-benchmark.log");
    }

    @TearDown(Level.Trial)
    public void tearDown() {
        RuLogger.close();
    }

    @Benchmark
    public void testSlf4jParameterized() {
        slf4jLogger.info(logTemplate, user, ip, sessionId);
    }

    @Benchmark
    public void testRuLoggerParameterizedZeroAlloc() {
        RuLogger.info(logTemplate, user, ip, sessionId);
    }
}
