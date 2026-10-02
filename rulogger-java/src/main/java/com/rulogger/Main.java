package com.rulogger;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("Testing RuLogger with advanced features...");
        RuLogger.init("app.log");

        RuLogger.info("This is an info message.");
        RuLogger.warn("This is a warning message.");
        RuLogger.error("This is an error message.");

        // Zero-copy logging test
        String directMessage = "This is a zero-copy direct buffer log.";
        byte[] bytes = directMessage.getBytes(StandardCharsets.UTF_8);
        ByteBuffer directBuffer = ByteBuffer.allocateDirect(bytes.length);
        directBuffer.put(bytes);

        // Ensure position is reset before passing to rust
        directBuffer.flip();

        RuLogger.logDirect(directBuffer, bytes.length);

        // Ensure async flush completes
        RuLogger.close();

        // Small sleep to let the background thread finish before JVM exits
        Thread.sleep(100);
        System.out.println("Done.");
    }
}
