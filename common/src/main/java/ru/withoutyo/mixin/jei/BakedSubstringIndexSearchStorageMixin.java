package ru.withoutyo.mixin.jei;

import mezz.jei.common.search.BakedSubstringIndexSearchStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import ru.withoutyo.RussianTextNormalizer;

/**
 * Нормализует русский текст «ё»→«е» на primary search storage JEI 19.50+.
 *
 * <p>В JEI ≥19.50 основное хранилище поиска — {@link BakedSubstringIndexSearchStorage},
 * которое владеет двумя бэкендами: {@code bakedStorage} (third-party
 * {@code net.mezzdev.bakedsubstring.BakedSubstringIndex}) и
 * {@code mutableStorage} ({@code GeneralizedSuffixTreeSearchStorage}). Поиск
 * {@code getSearchResults(String word, …)} делегирует в third-party
 * {@code bakedStorage.getSearchResults(word)}, который Mixin перехватить не может.
 *
 * <p>Поэтому мы нормализуем {@code word} на входе в JEI-обёртку —
 * {@code @ModifyVariable} на параметре метода {@code getSearchResults}.
 * На стороне индексирования {@code put(String key, T)} пишет и в {@code bakedStorage},
 * и в {@code mutableStorage}; нормализация параметра {@code key} тем же
 * Mixin'ом покрывает оба бэкенда.
 *
 * <p>Mixin применяется только при наличии JEI на classpath — это обеспечивает
 * {@link ru.withoutyo.mixin.OptionalJeiMixinPlugin}, зарегистрированный в
 * {@code withoutyo.jei.mixins.json}.
 */
@Mixin(BakedSubstringIndexSearchStorage.class)
public abstract class BakedSubstringIndexSearchStorageMixin<T> {

    @ModifyVariable(method = "put(Ljava/lang/String;Ljava/lang/Object;)V",
                   at = @At("HEAD"),
                   index = 1,
                   remap = false)
    private String withoutyoNormalizeIndexKey(String key) {
        return RussianTextNormalizer.normalize(key, "jei-baked-put");
    }

    @ModifyVariable(method = "getSearchResults(Ljava/lang/String;Ljava/util/function/Consumer;)V",
                   at = @At("HEAD"),
                   index = 1,
                   remap = false)
    private String withoutyoNormalizeSearchWord(String word) {
        return RussianTextNormalizer.normalize(word, "jei-baked-get");
    }
}