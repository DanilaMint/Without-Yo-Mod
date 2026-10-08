package ru.withoutyo.mixin.vanilla;

import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import ru.withoutyo.RussianTextNormalizer;

/**
 * Нормализация русского текста «ё»→«е» в vanilla creative inventory search.
 *
 * <p>Mixin перехватывает вызов {@code searchBox.getValue()} внутри
 * {@link CreativeModeInventoryScreen#refreshSearchResults()} и подменяет строку
 * нормализованной формой запроса. Эта первая половина нормализации делает так,
 * чтобы запрос «чёрная» находил «чёрная шерсть»; вторая (нормализация индекса)
 * живёт в {@code CreativeModeTabMixin}.
 *
 * <p>Используются имена, одинаковые в Yarn (Fabric) и Mojang (Forge/NeoForge)
 * маппингах: {@code CreativeModeInventoryScreen}, {@code refreshSearchResults},
 * {@code EditBox.getValue()}. Поэтому один Mixin-файл подходит для всех
 * loader-модулей через общий {@code common/}.
 */
@Mixin(value = CreativeModeInventoryScreen.class, remap = false)
public abstract class VanillaSearchMixin {

    @Redirect(
        method = "refreshSearchResults",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/components/EditBox;getValue()Ljava/lang/String;",
            remap = false
        )
    )
    private String withoutyoNormalizeQuery(EditBox box) {
        return RussianTextNormalizer.normalize(box.getValue());
    }
}