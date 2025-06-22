package com.ssblur.scriptor.effect

import net.minecraft.network.chat.Component
import net.minecraft.world.effect.MobEffect
import net.minecraft.world.effect.MobEffectCategory
import net.minecraft.world.entity.LivingEntity

open class ArcanePoisonStatusEffect: MobEffect {
    constructor(): super(MobEffectCategory.HARMFUL, 8954814)

    constructor(mobEffectCategory: MobEffectCategory, i: Int): super(mobEffectCategory, i)

    override fun applyEffectTick(entity: LivingEntity, amplifier: Int): Boolean {
        val activeEffects = entity.activeEffects
        val burningEffect = if (entity.remainingFireTicks > 0) 1 else 0
        val damage = (activeEffects.size + burningEffect) * (amplifier + 0.5)
        entity.hurt(entity.damageSources().magic(), damage.toFloat());
        entity.sendSystemMessage(Component.literal("Effects = " + (activeEffects.size + burningEffect).toString()))
        entity.sendSystemMessage(Component.literal("Damage = " + (damage).toString()))
        return true
    }

    override fun shouldApplyEffectTickThisTick(i: Int, j: Int): Boolean {
//        Tick as quickly as possible to prevent any less-damaging effects from occurring. This makes it easier to
//        balance, as this should be the only effective DOT while active
        return (i % 10 == 0)
    }
}
