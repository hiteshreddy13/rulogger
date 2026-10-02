package com.rulogger;

public class Main {
    public static void main(String[] args) {
        System.out.println("Testing RuLogger...");
        RuLogger.info("This is an info message.");
        RuLogger.warn("This is a warning message.");
        RuLogger.error("This is an error message.");
        System.out.println("Done.");
    }
}
