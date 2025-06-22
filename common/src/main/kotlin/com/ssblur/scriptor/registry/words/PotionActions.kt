package com.ssblur.scriptor.registry.words

import com.ssblur.scriptor.api.word.Word
import com.ssblur.scriptor.effect.ScriptorEffects
import com.ssblur.scriptor.registry.words.WordRegistry.register
import com.ssblur.scriptor.word.action.potions.*
import net.minecraft.world.effect.MobEffects

@Suppress("unused")
object PotionActions {
  val POISON_POTION = register(
    "poison",
    PotionAction(
      MobEffects.POISON,
      60.0,
      1.0 / 3.0,
      Word.Cost(8.0, Word.COSTTYPE.ADDITIVE),
      registryKey="poison",
    )
  )
  val SLOW_POTION = register(
    "slow",
    PotionAction(
      MobEffects.MOVEMENT_SLOWDOWN,
      80.0,
      1.0 / 3.0,
      Word.Cost(6.0, Word.COSTTYPE.ADDITIVE),
      registryKey="slow",
    )
  )
  val REGENERATION_POTION = register(
    "regeneration",
    PotionAction(
      MobEffects.REGENERATION,
      30.0,
      1.0 / 3.0,
      Word.Cost(8.0, Word.COSTTYPE.ADDITIVE),
      registryKey="regeneration",
    )
  )
  val WITHER_POTION = register("wither", WitherAction("wither"))
  val SATURATION_POTION = register(
    "saturation",
    PotionAction(
      MobEffects.SATURATION,
      4.0,
      1.0 / 3.0,
      Word.Cost(10.0, Word.COSTTYPE.ADDITIVE),
      registryKey="saturation",
    )
  )
  val SPEED_POTION = register(
    "speed",
    PotionAction(
      MobEffects.MOVEMENT_SPEED,
      80.0,
      1.0 / 2.0,
      Word.Cost(6.0, Word.COSTTYPE.ADDITIVE),
      registryKey="speed",
    )
  )
  val HASTE_POTION = register(
    "haste",
    PotionAction(
      MobEffects.DIG_SPEED,
      50.0,
      1.0 / 3.0,
      Word.Cost(8.0, Word.COSTTYPE.ADDITIVE),
      registryKey="haste",
    )
  )
  val STRENGTH_POTION = register(
    "strength",
    StrengthAction("strength")
  )
  val JUMP_BOOST_POTION = register(
    "jump_boost",
    PotionAction(
      MobEffects.JUMP,
      60.0,
      1.0 / 3.0,
      Word.Cost(9.0, Word.COSTTYPE.ADDITIVE),
      registryKey="jump_boost",
    )
  )
  val RESISTANCE_POTION = register(
    "resistance",
    PotionAction(
      MobEffects.DAMAGE_RESISTANCE,
      50.0,
      1.0 / 3.0,
      Word.Cost(8.0, Word.COSTTYPE.ADDITIVE),
//      Prevent invincibility
      4.0,
      registryKey="resistance",
    )
  )
  val FIRE_RESISTANCE_POTION = register(
    "fire_resistance",
    PotionAction(
      MobEffects.FIRE_RESISTANCE,
      30.0,
      1.0 / 3.0,
      Word.Cost(10.0, Word.COSTTYPE.ADDITIVE),
      registryKey="fire_resistance",
    )
  )
  val WATER_BREATHING_POTION = register(
    "water_breathing",
    PotionAction(
      MobEffects.WATER_BREATHING,
      80.0,
      1.0 / 3.0,
      Word.Cost(6.0, Word.COSTTYPE.ADDITIVE),
      registryKey="water_breathing",
    )
  )
  val NIGHT_VISION_POTION = register(
    "night_vision",
    PotionAction(
      MobEffects.NIGHT_VISION,
      80.0,
      1.0 / 3.0,
      Word.Cost(6.0, Word.COSTTYPE.ADDITIVE),
      registryKey="night_vision",
    )
  )
  val WEAKNESS_POTION = register(
    "weakness",
    PotionAction(
      MobEffects.WEAKNESS,
      50.0,
      1.0 / 3.0,
      Word.Cost(6.0, Word.COSTTYPE.ADDITIVE),
      registryKey="weakness",
    )
  )
  val HERO_POTION = register(
    "hero",
    PotionAction(
      MobEffects.HERO_OF_THE_VILLAGE,
      120.0,
      1.0 / 6.0,
      Word.Cost(100.0, Word.COSTTYPE.ADDITIVE),
      registryKey="hero",
    )
  )
  val PHASING_POTION = register("phasing", PhasingAction("phasing"))
  val WILD_PHASING_POTION = register(
    "wild_phasing",
    WildPhasingAction("wild_phasing")
  )
  val FREEZING_POTION = register(
    "freeze",
    FreezeAction("freeze")
  )
  val ARCANE_POISON_POTION = register(
    "arcane_poison",
    PotionAction(
      ScriptorEffects.ARCANE_POISON.ref(),
      30.0,
      1.0/3.0,
      Word.Cost(15.0, Word.COSTTYPE.ADDITIVE),
      registryKey="arcane_poison",
    )
  )
  val LEVITATION_POTION = register(
    "levitate",
    PotionAction(
      MobEffects.LEVITATION,
      15.0,
      1.0,
      Word.Cost(15.0, Word.COSTTYPE.ADDITIVE),
      registryKey="levitate",
    )
  )
  val BLINDNESS_POTION = register(
    "blind",
    PotionAction(
      MobEffects.BLINDNESS,
      30.0,
      1.0 / 3.0,
      Word.Cost(6.0, Word.COSTTYPE.ADDITIVE),
      registryKey="blind",
    )
  )
  val ABSORPTION_POTION = register(
    "absorption",
    PotionAction(
      MobEffects.ABSORPTION,
      30.0,
      1.0 / 3.0,
      Word.Cost(8.0, Word.COSTTYPE.ADDITIVE),
      registryKey="absorption",
    )
  )
  val DOLPHINS_GRACE_POTION = register(
    "dolphins_grace",
    PotionAction(
      MobEffects.DOLPHINS_GRACE,
      80.0,
      1.0 / 2.0,
      Word.Cost(6.0, Word.COSTTYPE.ADDITIVE),
      registryKey="dolphins_grace",
    )
  )
  val SLOW_FALLING_POTION = register(
    "slow_falling",
    PotionAction(
      MobEffects.SLOW_FALLING,
      80.0,
      1.0 / 2.0,
      Word.Cost(6.0, Word.COSTTYPE.ADDITIVE),
      registryKey="slow_falling",
    )
  )
  val INVISIBILITY_POTION = register(
    "invisible",
    PotionAction(
      MobEffects.INVISIBILITY,
      15.0,
      1.0 / 3.0,
      Word.Cost(8.0, Word.COSTTYPE.ADDITIVE),
      registryKey="invisible",
    )
  )
}
