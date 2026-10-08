package ru.withoutyo.mixin;

import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/**
 * Mixin-плагин, который отключает JEI Mixin'ы при отсутствии JEI на classpath.
 * Используется в withoutyo.jei.mixins.json и обеспечивает безопасную загрузку
 * мода без JEI — никаких ошибок classloading.
 *
 * <p>Использует только {@link Class#forName(String, boolean, ClassLoader)}, без
 * рантайм-проверок загрузчика или платформенных API, чтобы работать одинаково
 * на Fabric, Forge, NeoForge.
 */
public class OptionalJeiMixinPlugin implements IMixinConfigPlugin {

    private static final String JEI_MARKER = "mezz.jei.core.search.suffixtree.GeneralizedSuffixTree";

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        try {
            Class.forName(JEI_MARKER, false, OptionalJeiMixinPlugin.class.getClassLoader());
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    @Override
    public void onLoad(String mixinPackage) {}

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, org.objectweb.asm.tree.ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

    @Override
    public void postApply(String targetClassName, org.objectweb.asm.tree.ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
}