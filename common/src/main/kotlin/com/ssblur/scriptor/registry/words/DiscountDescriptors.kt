package com.ssblur.scriptor.registry.words

import com.ssblur.scriptor.registry.words.WordRegistry.register
import com.ssblur.scriptor.word.descriptor.discount.*

@Suppress("unused")
object DiscountDescriptors {
  val BLOOD_COST = register(
    "blood_cost",
    BloodCostDescriptor(
    "blood_cost")
  )
  val EXPERIENCE_COST = register(
    "experience_cost",
    ExperienceCostDescriptor(
    "experience_cost")
  )
  val CHEAP = register("cheap", CheapDescriptor("cheap"))
  val HEALTHY = register(
    "healthy",
    HealthyDescriptor("healthy")
  )
  val POISONED = register(
    "poisoned",
    PoisonDescriptor(
    "poisoned")
  )
  val WEAKENED = register(
    "weakened",
    WeakDescriptor(
    "weakened")
  )
  val ON_FIRE = register(
    "on_fire",
    OnFireDescriptor(
    "on_fire")
  )
  val CRITICAL = register(
    "critical",
    CriticalDescriptor(
    "critical")
  )
  val NIGHT = register(
    "night",
    NightDiscountDescriptor(
    "night")
  )
  val RAIN = register(
    "rain",
    RainDiscountDescriptor(
    "rain")
  )
  val CLEAR_SKIES = register(
    "clear_skies",
    ClearDiscountDescriptor(
    "clear_skies")
  )
}
