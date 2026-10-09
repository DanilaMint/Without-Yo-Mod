package ru.withoutyo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Утилита для нормализации русского текста в контексте поиска.
 * Заменяет «ё» на «е» и «Ё» на «Е». Идемпотентна и null-safe.
 *
 * <p>Применяется к обеим сторонам сравнения (запросу и индексируемым строкам),
 * благодаря чему запросы вида «чёрная» и «черная» одинаково успешно находят
 * предмет с именем «Чёрная шерсть» — после нормализации обе становятся «черная».
 *
 * <p>Производительность: ранний выход если в строке нет ни «ё», ни «Ё».
 */
public final class RussianTextNormalizer {

    private static final Logger LOGGER = LoggerFactory.getLogger("withoutyo");

    private RussianTextNormalizer() {}

    /**
     * @param s исходная строка (может быть null)
     * @param source тег источника вызова (например, "vanilla-editbox", "jei-lss-get"),
     *               используется в логах диагностики Mixin-подмешивания.
     * @return нормализованная строка; null если на входе null; тот же экземпляр,
     *         если в строке не было «ё»/«Ё».
     */
    public static String normalize(String s, String source) {
        if (s == null) return null;
        String result = normalizeInternal(s);
        if (result != s) {
            LOGGER.info("[withoutyo][{}] '{}' -> '{}'", source, s, result);
        } else {
            LOGGER.info("[withoutyo][{}] '{}' (no change)", source, s);
        }
        return result;
    }

    private static String normalizeInternal(String s) {
        if (s.indexOf('ё') < 0 && s.indexOf('Ё') < 0) return s;

        StringBuilder b = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == 'ё') b.append('е');
            else if (c == 'Ё') b.append('Е');
            else b.append(c);
        }
        return b.toString();
    }
}