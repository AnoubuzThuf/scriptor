package com.ssblur.scriptor.effect

import net.minecraft.world.effect.MobEffect
import net.minecraft.world.effect.MobEffectCategory
import net.minecraft.world.entity.LivingEntity

open class FreezingStatusEffect: MobEffect {
    constructor(): super(MobEffectCategory.HARMFUL, 8954814)

    constructor(mobEffectCategory: MobEffectCategory, i: Int): super(mobEffectCategory, i)

    override fun applyEffectTick(entity: LivingEntity, amplifier: Int): Boolean {
        entity.hurt(entity.damageSources().freeze(), 1.0F);
        return true
    }

    override fun shouldApplyEffectTickThisTick(i: Int, j: Int): Boolean {
        val k = 80 shr j
        return if (k > 0) (i % k == 0) else true
    }
}