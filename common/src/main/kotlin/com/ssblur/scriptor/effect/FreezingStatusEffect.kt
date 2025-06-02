package com.ssblur.scriptor.effect

import net.minecraft.world.effect.MobEffect
import net.minecraft.world.effect.MobEffectCategory
import net.minecraft.world.entity.LivingEntity

open class FreezingStatusEffect: MobEffect {
    constructor(): super(MobEffectCategory.HARMFUL, 8954814)

    constructor(mobEffectCategory: MobEffectCategory, i: Int): super(mobEffectCategory, i)

    override fun applyEffectTick(entity: LivingEntity, amplifier: Int): Boolean {
        entity.setTicksFrozen(Math.min(entity.getTicksRequiredToFreeze() + 3, entity.getTicksFrozen() + 3 + amplifier * 20))
        return true
    }

    override fun shouldApplyEffectTickThisTick(i: Int, j: Int): Boolean {
        return true
    }
}