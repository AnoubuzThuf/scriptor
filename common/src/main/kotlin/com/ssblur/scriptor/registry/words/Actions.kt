package com.ssblur.scriptor.registry.words

import com.ssblur.scriptor.item.ScriptorItems
import com.ssblur.scriptor.registry.words.WordRegistry.register
import com.ssblur.scriptor.word.action.*
import com.ssblur.scriptor.word.action.bound.BoundSwordAction
import com.ssblur.scriptor.word.action.bound.BoundToolAction
import com.ssblur.scriptor.word.action.potions.FreezeAction
import com.ssblur.scriptor.word.action.potions.LightAction
import com.ssblur.scriptor.word.action.teleport.BringAction
import com.ssblur.scriptor.word.action.teleport.GotoAction
import com.ssblur.scriptor.word.action.teleport.SwapAction
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey

@Suppress("unused")
object Actions {
  val INFLAME = register("inflame", InflameAction())
  val LIGHT = register("light", LightAction())
  val FREEZE = register("freeze", FreezeAction())
  val ARCANE_POISON = register("arcane_poison", ArcanePoisonAction())
  val HEAL = register("heal", HealAction())
  val SMITE = register("smite", SmiteAction())
  val SUMMON_VEX = register("summon_vex", SummonVexAction())
  val SUMMON_SKELETON = register("summon_skeleton", SummonSkeletonAction())
  val SUMMON_RANGED_SKELETON = register("summon_ranged_skeleton", SummonRangedSkeletonAction())
  val EXPLOSION = register("explosion", ExplosionAction())
  val GOTO = register("goto", GotoAction())
  val SWAP = register("swap", SwapAction())
  val BRING = register("bring", BringAction())
  val BREAK = register("break", BreakBlockAction())
  val DRY = register("dry", DryAction())
  val PLACE = register("place", PlaceBlockAction())
  val CONJURE_WATER = register("conjure_water", PlaceWaterAction())
  val HARM = register("harm", HarmAction())
  val COLOR = register("color", ColorAction())
  val TIME = register("time", AdvanceTimeAction())
  val CLEAR_WEATHER = register(
    "clear_weather",
    ClearWeatherAction()
  )
  val RAIN = register("rain", RainAction())

  val BOUND_SWORD = register("bound_sword", BoundSwordAction())
  val BOUND_AXE = register(
    "bound_axe",
    BoundToolAction(
      ScriptorItems.BOUND_AXE,
      listOf(TagKey.create(Registries.BLOCK, ResourceLocation.withDefaultNamespace("mineable/axe")))
    )
  )
  val BOUND_SHOVEL = register(
    "bound_shovel",
    BoundToolAction(
      ScriptorItems.BOUND_SHOVEL,
      listOf(TagKey.create(Registries.BLOCK, ResourceLocation.withDefaultNamespace("mineable/shovel")))
    )
  )
  val BOUND_PICKAXE = register(
    "bound_pickaxe",
    BoundToolAction(
      ScriptorItems.BOUND_PICKAXE,
      listOf(TagKey.create(Registries.BLOCK, ResourceLocation.withDefaultNamespace("mineable/pickaxe")))
    )
  )
}
