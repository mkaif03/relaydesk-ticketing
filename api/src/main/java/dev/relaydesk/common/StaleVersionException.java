package dev.relaydesk.common;

public class StaleVersionException extends RuntimeException {
    
    private final long currentVersion;

    public StaleVersionException(String message, long currentVersion) {
        super(message);
        this.currentVersion = currentVersion;
    }

    public long getCurrentVersion() {
        return currentVersion;
    }
}
