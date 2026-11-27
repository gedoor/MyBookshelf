package com.kunfei.bookshelf.service;

final class ReadAloudTimer {
    enum Update {
        ACTIVE, EXPIRED, CANCELLED, INACTIVE
    }

    private final int maxMinutes;
    private int configuredMinutes;
    private int remainingMinutes;
    private boolean enabled;

    ReadAloudTimer(int maxMinutes) {
        this.maxMinutes = maxMinutes;
    }

    Update addMinutes(int minutes) {
        boolean wasEnabled = enabled;
        int adjustment = minutes;
        if (minutes == 10) {
            if (remainingMinutes < 30) {
                adjustment = 10;
            } else if (remainingMinutes < 120) {
                adjustment = 15;
            } else if (remainingMinutes < 180) {
                adjustment = 30;
            } else {
                adjustment = 60;
            }
        }

        remainingMinutes += adjustment;
        if (remainingMinutes > maxMinutes) {
            configuredMinutes = 0;
            remainingMinutes = 0;
            enabled = false;
            return Update.CANCELLED;
        }
        if (remainingMinutes <= 0) {
            remainingMinutes = 0;
            enabled = false;
            return wasEnabled ? Update.EXPIRED : Update.INACTIVE;
        }

        if (adjustment > 0) {
            configuredMinutes = remainingMinutes;
        }
        enabled = true;
        return Update.ACTIVE;
    }

    boolean startSession() {
        if (remainingMinutes == 0 && configuredMinutes > 0) {
            remainingMinutes = configuredMinutes;
        }
        enabled = remainingMinutes > 0;
        return enabled;
    }

    boolean isEnabled() {
        return enabled;
    }

    int getRemainingMinutes() {
        return remainingMinutes;
    }
}
