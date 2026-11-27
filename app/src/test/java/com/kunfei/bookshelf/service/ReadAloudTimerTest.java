package com.kunfei.bookshelf.service;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ReadAloudTimerTest {
    @Test
    public void restartingAfterExpiryViaMediaButtonRestoresConfiguredDuration() {
        ReadAloudTimer timer = new ReadAloudTimer(360);

        assertEquals(ReadAloudTimer.Update.ACTIVE, timer.addMinutes(10));
        for (int minute = 0; minute < 9; minute++) {
            assertEquals(ReadAloudTimer.Update.ACTIVE, timer.addMinutes(-1));
        }
        assertEquals(ReadAloudTimer.Update.EXPIRED, timer.addMinutes(-1));
        assertEquals(0, timer.getRemainingMinutes());
        assertEquals(ReadAloudTimer.Update.INACTIVE, timer.addMinutes(-1));

        assertTrue(timer.startSession());

        assertEquals(10, timer.getRemainingMinutes());
    }

    @Test
    public void resumingBeforeExpiryKeepsCurrentRemainingDuration() {
        ReadAloudTimer timer = new ReadAloudTimer(360);

        timer.addMinutes(10);
        timer.addMinutes(-1);
        assertTrue(timer.startSession());

        assertEquals(9, timer.getRemainingMinutes());
    }

    @Test
    public void timerReportsEnabledOnlyWhileCountingDown() {
        ReadAloudTimer timer = new ReadAloudTimer(360);
        assertFalse(timer.isEnabled());

        timer.addMinutes(10);
        assertTrue(timer.isEnabled());

        for (int minute = 0; minute < 10; minute++) {
            timer.addMinutes(-1);
        }
        assertFalse(timer.isEnabled());
    }
}
