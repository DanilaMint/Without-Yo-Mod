package ru.withoutyo.mixin;

import org.objectweb.asm.tree.ClassNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/**
 * Mixin-плагин, который отключает JEI Mixin'ы при отсутствии JEI на classpath.
 * Используется в withoutyo.jei.mixins.json и обеспечивает безопасную загрузку
 * мода без JEI — никаких ошибок classloading.
 *
 * <p>Использует каскадную проверку нескольких classloader'ов, чтобы корректно
 * работать в NeoForge, где каждый мод загружается в изолированном classloader'е
 * (mod'ы Fabric и Forge используют общий classloader, поэтому проверки
 * через {@code getClass().getClassLoader()} было достаточно).
 */
public class OptionalJeiMixinPlugin implements IMixinConfigPlugin {

    private static final Logger LOGGER = LoggerFactory.getLogger("withoutyo");
    private static final String JEI_MARKER = "mezz.jei.common.search.GeneralizedSuffixTreeSearchStorage";

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        ClassLoader[] candidates = new ClassLoader[] {
            OptionalJeiMixinPlugin.class.getClassLoader(),
            Thread.currentThread().getContextClassLoader(),
            ClassLoader.getSystemClassLoader()
        };
        for (ClassLoader cl : candidates) {
            if (cl == null) continue;
            try {
                Class.forName(JEI_MARKER, false, cl);
                LOGGER.info("[withoutyo][plugin] shouldApplyMixin({}) -> true via {}",
                    targetClassName, cl.getName());
                return true;
            } catch (Throwable ignored) {
                // try next classloader
            }
        }
        LOGGER.info("[withoutyo][plugin] shouldApplyMixin({}) -> false via none of {}",
            targetClassName, java.util.Arrays.toString(candidateNames(candidates)));
        return false;
    }

    private static String[] candidateNames(ClassLoader[] loaders) {
        String[] names = new String[loaders.length];
        for (int i = 0; i < loaders.length; i++) {
            names[i] = loaders[i] == null ? "null" : loaders[i].getName();
        }
        return names;
    }

    @Override
    public void onLoad(String mixinPackage) {
        LOGGER.info("[withoutyo] JEI mixin plugin loaded");
    }

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
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
}