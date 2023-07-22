package com.github.jensco.logger;

public class UtilLogger extends Logger {
    private final org.slf4j.Logger handle;
    private boolean debug = false;

    public UtilLogger(org.slf4j.Logger logger) {
        handle = logger;
    }

    @Override
    public void info(String message) {
        handle.info(message);
    }

    @Override
    public void warn(String message) {
        handle.warn(message);
    }


    @Override
    public void error(String message) {
        handle.error(message);
    }

    @Override
    public void debug(String message) {
        if (debug) {
            handle.info(message);
        }
    }

    @Override
    public boolean isDebug() {
        return debug;
    }

    @Override
    public void setDebug(boolean debug) {
        this.debug = debug;
    }
}