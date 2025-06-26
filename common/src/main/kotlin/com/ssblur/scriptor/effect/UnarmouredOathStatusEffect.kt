package com.ssblur.scriptor.effect

import net.minecraft.world.effect.MobEffect
import net.minecraft.world.effect.MobEffectCategory
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.goal.Goal.reducedTickDelay

open class UnarmouredOathStatusEffect: MobEffect {
    constructor(): super(MobEffectCategory.NEUTRAL, 8954814)

    constructor(mobEffectCategory: MobEffectCategory, i: Int): super(mobEffectCategory, i)

    override fun applyEffectTick(entity: LivingEntity, amplifier: Int): Boolean {
        if (entity.armorCoverPercentage > 0.1) {
            entity.removeAllEffects()
            entity.addEffect(MobEffectInstance(MobEffects.BAD_OMEN, -1, entity.getRandom().nextInt(5)))
            entity.addEffect(MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, entity.getRandom().nextInt(600), 0))
            entity.addEffect(MobEffectInstance(MobEffects.BLINDNESS, entity.getRandom().nextInt(600), 0))
        }
        return true
    }

    override fun shouldApplyEffectTickThisTick(i: Int, j: Int): Boolean {
        return (i % 10 == 0)
    }
}