package com.diabetes.lbridge;

@FunctionalInterface
public interface ThrowingRunnable {
    void run() throws Throwable;
}
