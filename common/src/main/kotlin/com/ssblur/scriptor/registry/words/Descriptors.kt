package com.ssblur.scriptor.registry.words

import com.ssblur.scriptor.entity.SUMMON_BEHAVIOURS
import com.ssblur.scriptor.entity.SUMMON_PROPERTIES
import com.ssblur.scriptor.registry.words.WordRegistry.register
import com.ssblur.scriptor.word.descriptor.SpeedDurationDescriptor
import com.ssblur.scriptor.word.descriptor.duration.PermanentDurationDescriptor
import com.ssblur.scriptor.word.descriptor.duration.SimpleDurationDescriptor
import com.ssblur.scriptor.word.descriptor.summon.SummonBehaviourDescriptor
import com.ssblur.scriptor.word.descriptor.summon.SummonPropertyDescriptor
import com.ssblur.scriptor.word.descriptor.target.ChainDescriptor
import com.ssblur.scriptor.word.descriptor.target.CircleDescriptor
import com.ssblur.scriptor.word.descriptor.target.CollideWithWaterDescriptor
import com.ssblur.scriptor.word.descriptor.target.NetherDescriptor
import com.ssblur.scriptor.word.descriptor.target.SquareDescriptor

@Suppress("unused")
object Descriptors {
  val LONG = register(
    "long",
    SimpleDurationDescriptor(3, 7.0)
  )
  val LONGER = register(
    "longer",
    SimpleDurationDescriptor(6, 17.0)
  )
  val VERY_LONG = register(
    "very_long",
    SimpleDurationDescriptor(65, 120.0)
  )
  val STACKING_LONG =
    register(
      "stacking_long", SimpleDurationDescriptor(
        6,
        7.0
      ).allowDuplication()
    )
  val PERMANENT = register(
    "permanent",
    PermanentDurationDescriptor()
  )
  val SLOW = register(
    "slow",
    SpeedDurationDescriptor(2, 2.0, 0.75).allowDuplication()
  )
  val FAST = register(
    "fast",
    SpeedDurationDescriptor(2, -4.0, 1.25).allowDuplication()
  )
  val CHAIN = register("chain", ChainDescriptor())
  val SQUARE = register("square", SquareDescriptor())
  val CIRCLE = register("circle", CircleDescriptor())

  val NETHER = register("nether", NetherDescriptor())
  val COLLIDE_WITH_WATER = register("collide_with_water", CollideWithWaterDescriptor)
//  Summon Behaviours

  val SUMMON_BEHAVIOUR_SENTRY = register(
    "summon_behaviour_sentry",
    SummonBehaviourDescriptor(0, SUMMON_BEHAVIOURS.SENTRY)
  )
  val SUMMON_BEHAVIOUR_FOLLOWER = register(
    "summon_behaviour_follower",
    SummonBehaviourDescriptor(0, SUMMON_BEHAVIOURS.FOLLOWER)
  )
  val SUMMON_BEHAVIOUR_HUNTER = register(
    "summon_behaviour_hunter",
    SummonBehaviourDescriptor(0, SUMMON_BEHAVIOURS.HUNTER)
  )
  val SUMMON_BEHAVIOUR_PLAYER_HUNTER = register(
    "summon_behaviour_player_hunter",
    SummonBehaviourDescriptor(0, SUMMON_BEHAVIOURS.PLAYER_HUNTER)
  )
  val SUMMON_BEHAVIOUR_BERSERK = register(
    "summon_behaviour_berserk",
    SummonBehaviourDescriptor(0, SUMMON_BEHAVIOURS.BERSERK)
  )
//  Summon Properties
  val SUMMON_PROPERTY_RANGED = register(
    "summon_property_ranged",
  SummonPropertyDescriptor(4, SUMMON_PROPERTIES.RANGED)
  )
  val SUMMON_PROPERTY_INVISIBLE = register(
    "summon_property_invisible",
    SummonPropertyDescriptor(4, SUMMON_PROPERTIES.INVISIBLE)
  )
}
