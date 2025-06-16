package com.ssblur.scriptor.effect

import net.minecraft.world.effect.MobEffect
import net.minecraft.world.effect.MobEffectCategory
import net.minecraft.world.entity.LivingEntity

open class VoodooStatusEffect: MobEffect {

    constructor(): super(MobEffectCategory.BENEFICIAL, 8954814)
    constructor(mobEffectCategory: MobEffectCategory, i: Int): super(mobEffectCategory, i)
}