package com.rulogger;

import java.nio.ByteBuffer;

public class RuLogger {

    static {
        System.loadLibrary("rulogger_rs");
    }

    public static native void init(String filePath);
    public static native void close();
    public static native void info(String message);
    public static native void warn(String message);
    public static native void error(String message);

    // Zero-copy logging using DirectByteBuffer
    public static native void logDirect(ByteBuffer buffer, int length);
}
