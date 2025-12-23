package com.example.hospital.test;

import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class TestLoggerExtension implements TestWatcher, BeforeTestExecutionCallback {

    private static final Logger logger = LoggerFactory.getLogger("test-logger");

    @Override
    public void testSuccessful(ExtensionContext context) {
        logger.info("TEST SUCCESS: {}", context.getDisplayName());
    }

    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        logger.error("TEST FAILED: {}", context.getDisplayName(), cause);
    }

    @Override
    public void testDisabled(ExtensionContext context, Optional<String> reason) {
        logger.warn("TEST DISABLED: {} - {}", context.getDisplayName(), reason.orElse("no reason"));
    }

    @Override
    public void testAborted(ExtensionContext context, Throwable cause) {
        logger.warn("TEST ABORTED: {}", context.getDisplayName(), cause);
    }

    @Override
    public void beforeTestExecution(ExtensionContext context) {
        logger.info("STARTING TEST: {}", context.getDisplayName());
    }
}