package com.a_smart_cookie.entity;

import com.a_smart_cookie.util.StringHandler;
import com.a_smart_cookie.util.translator.Translatable;
import com.a_smart_cookie.util.translator.TranslatorContext;
import com.a_smart_cookie.util.translator.strategies.LanguageTranslatorStrategies;

import java.util.Arrays;

/**
 * Language entity with ability to be translated.
 *
 */
public enum Language implements Translatable {
    UKRAINIAN("uk"),
    ENGLISH("en");

    private final String abbr;

    Language(String abbr) {
        this.abbr = abbr;
    }

    public String getAbbr() {
        return abbr;
    }

    public static Language safeFromString(String inputAbbr) {
        return Arrays.stream(Language.values())
                .filter(language -> language.abbr.equalsIgnoreCase(inputAbbr))
                .findFirst()
                .orElse(UKRAINIAN);
    }

    @Override
    public String getTranslatedValue(Language language) {
        return StringHandler.capitaliseFirstLetter(
                TranslatorContext.translateInto(LanguageTranslatorStrategies.getTranslatorByLanguage(language), this)
        );
    }
}
