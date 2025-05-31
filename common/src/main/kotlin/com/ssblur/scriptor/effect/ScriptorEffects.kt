package com.ssblur.scriptor.effect

import com.ssblur.scriptor.ScriptorMod.registerEffect
import com.ssblur.unfocused.constructors.Effect
import com.ssblur.unfocused.registry.RegistrySupplier

import net.minecraft.world.effect.MobEffect
import net.minecraft.world.effect.MobEffectCategory

@Suppress("unused")
object ScriptorEffects {
  val HOARSE: RegistrySupplier<MobEffect> = registerEffect("hoarse") { Effect(MobEffectCategory.HARMFUL, 0xcc00cc) {} }
  val MUTE: RegistrySupplier<MobEffect> = registerEffect("mute") { Effect(MobEffectCategory.HARMFUL, 0xcc00cc) {} }
  val PHASING: RegistrySupplier<MobEffect> = registerEffect("phasing") { PhasingStatusEffect() }
  val WILD_PHASING: RegistrySupplier<MobEffect> = registerEffect("wild_phasing") { WildPhasingStatusEffect() }
  val SILVER_TONGUE: RegistrySupplier<MobEffect> = registerEffect("silver_tongue") { EmpoweredStatusEffect(0.8f) }
  val FREEZE: RegistrySupplier<MobEffect> = registerEffect("freeze") { FreezingStatusEffect() }
  val ARCANE_POISON: RegistrySupplier<MobEffect> = registerEffect("arcane_poison") { ArcanePoisonStatusEffect() }
  fun register() {}
}