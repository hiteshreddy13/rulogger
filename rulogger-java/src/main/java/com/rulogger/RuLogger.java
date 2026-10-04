package com.rulogger;

import java.nio.ByteBuffer;

public class RuLogger {

    public static final int LEVEL_INFO = 1;
    public static final int LEVEL_WARN = 2;
    public static final int LEVEL_ERROR = 3;

    static {
        System.loadLibrary("rulogger_rs");
    }

    public static native void init(String filePath);
    public static native void close();

    // Original String allocations (slower)
    public static native void info(String message);
    public static native void warn(String message);
    public static native void error(String message);

    // Zero-copy logging using DirectByteBuffer
    public static native void logDirect(int level, ByteBuffer buffer, int length);

    // Advanced dynamic formatting zero-allocation loggers
    public static void info(String template, Object... args) {
        ByteBuffer buffer = ZeroCopyFormatter.format(template, args);
        logDirect(LEVEL_INFO, buffer, buffer.limit());
    }

    public static void warn(String template, Object... args) {
        ByteBuffer buffer = ZeroCopyFormatter.format(template, args);
        logDirect(LEVEL_WARN, buffer, buffer.limit());
    }

    public static void error(String template, Object... args) {
        ByteBuffer buffer = ZeroCopyFormatter.format(template, args);
        logDirect(LEVEL_ERROR, buffer, buffer.limit());
    }
}
