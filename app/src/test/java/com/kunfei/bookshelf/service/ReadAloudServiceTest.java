package com.kunfei.bookshelf.service;

import android.speech.tts.TextToSpeech;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ReadAloudServiceTest {
    @Test
    public void acceptsAvailableLanguageStatuses() {
        assertTrue(ReadAloudService.isTtsLanguageAvailable(TextToSpeech.LANG_AVAILABLE));
        assertTrue(ReadAloudService.isTtsLanguageAvailable(TextToSpeech.LANG_COUNTRY_AVAILABLE));
        assertTrue(ReadAloudService.isTtsLanguageAvailable(TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE));
    }

    @Test
    public void rejectsMissingOrUnsupportedLanguageStatuses() {
        assertFalse(ReadAloudService.isTtsLanguageAvailable(TextToSpeech.LANG_MISSING_DATA));
        assertFalse(ReadAloudService.isTtsLanguageAvailable(TextToSpeech.LANG_NOT_SUPPORTED));
    }
}
