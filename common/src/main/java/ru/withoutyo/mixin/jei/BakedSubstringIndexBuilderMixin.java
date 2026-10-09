package ru.withoutyo.mixin.jei;

import mezz.jei.common.search.BakedSubstringIndexBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import ru.withoutyo.RussianTextNormalizer;

/**
 * Нормализует русский текст «ё»→«е» на стадии построения индекса JEI.
 *
 * <p>JEI 19.50+ индексирует ВСЕ предметы один раз при старте через
 * {@link BakedSubstringIndexBuilder#put(String, Object)} — до того как
 * рантайм-{@code BakedSubstringIndexSearchStorage#put} когда-либо вызовется.
 * Без перехвата builder'а все ключи попадают в third-party
 * {@code BakedSubstringIndex} без нормализации, и поиск «чёрная» уже
 * не сможет найти «Чёрная шерсть», даже если рантайм-get нормализован.
 *
 * <p>Mixin применяется только при наличии JEI на classpath — это обеспечивает
 * {@link ru.withoutyo.mixin.OptionalJeiMixinPlugin}, зарегистрированный в
 * {@code withoutyo.jei.mixins.json}.
 */
@Mixin(BakedSubstringIndexBuilder.class)
public abstract class BakedSubstringIndexBuilderMixin<T> {

    @ModifyVariable(method = "put(Ljava/lang/String;Ljava/lang/Object;)V",
                   at = @At("HEAD"),
                   index = 1,
                   remap = false)
    private String withoutyoNormalizeIndexKey(String key) {
        return RussianTextNormalizer.normalize(key, "jei-builder-put");
    }
}