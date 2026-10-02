package com.rulogger;

public class RuLogger {

    static {
        System.loadLibrary("rulogger_rs");
    }

    public static native void info(String message);
    public static native void warn(String message);
    public static native void error(String message);
}
