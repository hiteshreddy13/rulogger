package com.rulogger;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("Testing RuLogger...");
        RuLogger.init("app.log");

        RuLogger.info("This is an info message.");
        RuLogger.warn("This is a warning message.");
        RuLogger.error("This is an error message.");

        // Give it a tiny bit of time to ensure async flush completes
        // Or better yet, just close which flushes
        RuLogger.close();

        // Small sleep to let the background thread finish before JVM exits
        Thread.sleep(100);
        System.out.println("Done.");
    }
}
