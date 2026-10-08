package ru.withoutyo;

import java.util.concurrent.atomic.AtomicBoolean;
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
    private static final AtomicBoolean FIRST_CALL_LOGGED = new AtomicBoolean(false);

    private RussianTextNormalizer() {}

    /**
     * @param s исходная строка (может быть null)
     * @return нормализованная строка; null если на входе null; тот же экземпляр,
     *         если в строке не было «ё»/«Ё».
     */
    public static String normalize(String s) {
        if (s == null) return null;
        if (FIRST_CALL_LOGGED.compareAndSet(false, true)) {
            LOGGER.info("[withoutyo] RussianTextNormalizer.normalize invoked — Mixin is active (first arg: \"{}\")", s);
        }
        // Ранний выход: если ё/Ё нет в строке, возвращаем тот же объект как есть.
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