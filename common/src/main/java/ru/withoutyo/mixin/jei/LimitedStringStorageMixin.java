package ru.withoutyo.mixin.jei;

import mezz.jei.common.search.LimitedStringStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import ru.withoutyo.RussianTextNormalizer;

/**
 * Defence-in-depth: нормализует «ё»→«е» в обе стороны поиска JEI на уровне
 * {@link LimitedStringStorage}.
 *
 * <p>В JEI ≥19.27 есть несколько хранилищ для поиска:
 * {@code GeneralizedSuffixTreeSearchStorage} (нормализуется в {@link JeiSearchMixin}),
 * {@code BakedSubstringIndexSearchStorage} (см. {@link BakedSubstringIndexSearchStorageMixin})
 * и {@code LimitedStringStorage}, используемое для режимов поиска с ограничением длины.
 * Чтобы их разделение или будущая миграция не сломала нашу нормализацию,
 * {@code LimitedStringStorage} тоже подменяется.
 *
 * <p>Перехватываются оба направления:
 * <ul>
 *   <li>{@code put(String, T)} — индекс-аргумент (нормализуем ключ перед
 *       записью в {@code multiMap}),</li>
 *   <li>{@code getSearchResults(String, Consumer)} — запрос-аргумент (нормализуем
 *       пользовательский ввод перед поиском).</li>
 * </ul>
 *
 * <p>Mixin грузится только когда JEI присутствует на classpath — это
 * обеспечивает {@link OptionalJeiMixinPlugin}, зарегистрированный в
 * {@code withoutyo.jei.mixins.json}.
 */
@Mixin(LimitedStringStorage.class)
public abstract class LimitedStringStorageMixin<T> {

    @ModifyVariable(method = "put(Ljava/lang/String;Ljava/lang/Object;)V",
                   at = @At("HEAD"),
                   index = 1,
                   remap = false)
    private String withoutyoNormalizeIndexKey(String key) {
        return RussianTextNormalizer.normalize(key, "jei-lss-put");
    }

    @ModifyVariable(method = "getSearchResults(Ljava/lang/String;Ljava/util/function/Consumer;)V",
                   at = @At("HEAD"),
                   index = 1,
                   remap = false)
    private String withoutyoNormalizeSearchWord(String word) {
        return RussianTextNormalizer.normalize(word, "jei-lss-get");
    }
}
