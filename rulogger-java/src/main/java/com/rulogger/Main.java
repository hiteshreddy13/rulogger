package com.rulogger;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("Testing RuLogger with zero-allocation parameterized features...");
        RuLogger.init("app.log");

        RuLogger.info("This is an info message.");
        RuLogger.warn("This is a warning message.");
        RuLogger.error("This is an error message.");

        // Zero-copy dynamic formatting logging test
        String user = "bob_builder";
        int id = 404;
        long timestamp = System.currentTimeMillis();

        RuLogger.info("User {} failed login attempt {} at {}", user, id, timestamp);

        // Ensure async flush completes
        RuLogger.close();

        // Small sleep to let the background thread finish before JVM exits
        Thread.sleep(100);
        System.out.println("Done.");
    }
}
