package ru.withoutyo.mixin.jei;

import mezz.jei.core.search.suffixtree.GeneralizedSuffixTree;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import ru.withoutyo.RussianTextNormalizer;

/**
 * Нормализует русский текст «ё»→«е» в обе стороны поиска JEI:
 *  - при индексировании: аргумент {@code key} метода {@code GeneralizedSuffixTree.put};
 *  - при запросе:      аргумент {@code word} метода {@code GeneralizedSuffixTree.getSearchResults}.
 *
 * <p>Класс {@code GeneralizedSuffixTree} используется JEI для ВСЕХ режимов поиска
 * (по имени предмета, по тултипу, по мод-id), потому что ElementPrefixParser
 * всегда создаёт либо {@code LimitedStringStorage}, либо {@code GeneralizedSuffixTree}
 * (см. Core/src/main/java/mezz/jei/gui/search/ElementPrefixParser.java, ветка 1.20.4).
 *
 * <p>Mixin применяется всегда, когда JEI присутствует на classpath — без JEI
 * опциональный Mixin-плагин {@code OptionalJeiMixinPlugin} не даёт Mixin-loader'у
 * загрузить этот класс.
 */
@Mixin(GeneralizedSuffixTree.class)
public abstract class JeiSearchMixin<T> {

    @ModifyVariable(method = "put(Ljava/lang/String;Ljava/lang/Object;)V",
                   at = @At("HEAD"),
                   argsOnly = true,
                   index = 0,
                   remap = false)
    private String withoutyoNormalizeIndexKey(String key) {
        return RussianTextNormalizer.normalize(key);
    }

    @ModifyVariable(method = "getSearchResults(Ljava/lang/String;Ljava/util/function/Consumer;)V",
                   at = @At("HEAD"),
                   argsOnly = true,
                   index = 0,
                   remap = false)
    private String withoutyoNormalizeSearchWord(String word) {
        return RussianTextNormalizer.normalize(word);
    }
}