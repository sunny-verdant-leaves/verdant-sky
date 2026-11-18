package com.github.verdant_leaves.verdant_sky.item;

import java.util.List;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BowlFoodItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class VerdantBowlFoodItem extends BowlFoodItem {

    private final List<EffectEntry> effects;

    public VerdantBowlFoodItem(Properties properties) {
        this(properties, List.of());
    }

    public VerdantBowlFoodItem(Properties properties, EffectEntry... effects) {
        this(properties, List.of(effects));
    }

    public VerdantBowlFoodItem(Properties properties, List<EffectEntry> effects) {
        super(properties);
        this.effects = effects;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);
        if (!level.isClientSide) {
            RandomSource random = entity.getRandom();
            for (EffectEntry entry : effects) {
                entry.tryApply(entity, random);
            }
        }
        return result;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        for (EffectEntry entry : effects) {
            MobEffect effect = BuiltInRegistries.MOB_EFFECT.get(entry.effectId());
            if (effect == null) {
                continue;
            }

            MobEffectInstance instance = entry.toInstance(effect);

            MutableComponent effectText = Component.translatable(effect.getDescriptionId());

            if (instance.getDuration() > 20) {
                effectText = Component.translatable(
                    "potion.withDuration",
                    effectText,
                    MobEffectUtil.formatDuration(instance, 1)
                );
            }

            if (entry.chance() < 1.0F) {
                effectText = effectText.append(" [").append(String.valueOf((int) (entry.chance() * 100))).append("%]");
            }

            tooltip.add(effectText.withStyle(effect.getCategory().getTooltipFormatting()));
        }
    }

    /** 食物效果，一个效果为一个实例 */
    public static final class EffectEntry {

        private final ResourceLocation effectId;
        private final int duration;
        private final int amplifier;
        private final float chance;
        private final boolean ambient;
        private final boolean visible;

        public EffectEntry(ResourceLocation effectId, int duration, int amplifier, float chance, boolean ambient, boolean visible) {
            this.effectId = effectId;
            this.duration = duration;
            this.amplifier = amplifier;
            this.chance = chance;
            this.ambient = ambient;
            this.visible = visible;
        }

        public EffectEntry(ResourceLocation effectId, int duration, int amplifier) {
            this(effectId, duration, amplifier, 1.0F, false, true);
        }

        // ---------- 静态工厂 ----------

        public static EffectEntry of(ResourceLocation effectId, int duration, int amplifier) {
            return new EffectEntry(effectId, duration, amplifier);
        }

        public static EffectEntry of(MobEffect effect, int duration, int amplifier) {
            return new EffectEntry(BuiltInRegistries.MOB_EFFECT.getKey(effect), duration, amplifier);
        }

        public static EffectEntry ofChance(ResourceLocation effectId, int duration, int amplifier, float chance) {
            return new EffectEntry(effectId, duration, amplifier, chance, false, true);
        }

        // ---------- 访问器 ----------

        public ResourceLocation effectId() { return effectId; }
        public int duration()          { return duration; }
        public int amplifier()         { return amplifier; }
        public float chance()          { return chance; }
        public boolean ambient()       { return ambient; }
        public boolean visible()       { return visible; }

        // ---------- 行为 ----------

        public MobEffectInstance toInstance(MobEffect effect) {
            return new MobEffectInstance(effect, duration, amplifier, ambient, visible);
        }

        public void tryApply(LivingEntity entity, RandomSource random) {
            if (chance < 1.0F && random.nextFloat() >= chance) {
                return;
            }
            MobEffect effect = BuiltInRegistries.MOB_EFFECT.get(effectId);
            if (effect == null) {
                return;
            }
            entity.addEffect(toInstance(effect));
        }
    }
}