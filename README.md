# rulogger
A logger built in rust and used by java via jni for fast logging

### Benchmark Results Example

Running the benchmarks locally yields the following approximate operations per second (ops/s) - higher is better:

```text
Benchmark                                    Mode  Cnt        Score   Error  Units
RuLoggerBenchmark.testRuLoggerDirectBuffer  thrpt       1208109.870          ops/s
RuLoggerBenchmark.testRuLoggerString        thrpt        704661.293          ops/s
RuLoggerBenchmark.testSlf4jLogback          thrpt        168851.013          ops/s
```

* **Slf4jLogback**: Traditional Async logger in Java.
* **RuLoggerString**: JNI bridging with standard Java String copies.
* **RuLoggerDirectBuffer**: Zero-copy JNI bridging using off-heap DirectByteBuffers.
