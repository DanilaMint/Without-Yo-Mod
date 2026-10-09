package ru.withoutyo.mixin.vanilla;

import net.minecraft.client.search.SuffixArray;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import ru.withoutyo.RussianTextNormalizer;

/**
 * Нормализация русского текста «ё»→«е» в vanilla-индексе Minecraft (Fabric/Yarn).
 *
 * <p>Mixin перехватывает оба направления в низкоуровневом поисковом классе
 * {@link SuffixArray}, через который проходят ВСЕ {@code SearchTree}-варианты
 * (creative, recipe-book и др.):
 * <ul>
 *   <li>{@code add(T, String)} — аргумент-индекс (нормализуем имя предмета/тултипа
 *       перед записью в суффикс-массив),</li>
 *   <li>{@code findAll(String)} — аргумент-запрос (нормализуем пользовательский
 *       ввод перед поиском).</li>
 * </ul>
 *
 * <p>Это дополняет {@link VanillaSearchMixin}, который нормализует запрос
 * раньше — в {@code EditBox.getValue()} внутри
 * {@code CreativeModeInventoryScreen.refreshSearchResults}. Двойная
 * нормализация идемпотентна: после применения строки без ё ещё раз
 * нормализовывать бессмысленно, но и безвредно.
 *
 * <p>Yarn (Fabric) сигнатура: класс {@code net.minecraft.client.search.SuffixArray};
 * метод поиска называется {@code findAll}, в отличие от Mojang mappings
 * (см. {@code neoforge/.../SuffixArrayMixin.java}, где метод {@code search}).
 */
@Mixin(value = SuffixArray.class, remap = false)
public abstract class SuffixArrayMixin<T> {

    @ModifyVariable(method = "add(Ljava/lang/Object;Ljava/lang/String;)V",
                   at = @At("HEAD"),
                   argsOnly = true,
                   index = 1,
                   remap = false)
    private String withoutyoNormalizeIndexedString(String text) {
        return RussianTextNormalizer.normalize(text, "vanilla-suffix-add");
    }

    @ModifyVariable(method = "findAll(Ljava/lang/String;)Ljava/util/List;",
                   at = @At("HEAD"),
                   index = 1,
                   remap = false)
    private String withoutyoNormalizeQuery(String query) {
        return RussianTextNormalizer.normalize(query, "vanilla-suffix-search");
    }
}
