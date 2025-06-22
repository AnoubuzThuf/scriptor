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
import com.ssblur.scriptor.word.action.teleport.MarkAction
import com.ssblur.scriptor.word.action.teleport.RecallAction
import com.ssblur.scriptor.word.action.teleport.SwapAction
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey

@Suppress("unused")
object Actions {
  val INFLAME = register("inflame", InflameAction("inflame"))
  val LIGHT = register("light", LightAction("light"))
  val FREEZE = register("freeze", FreezeAction("freeze"))
  val HEAL = register("heal", HealAction("heal"))
  val SMITE = register("smite", SmiteAction("smite"))
  val SUMMON_VEX = register("summon_vex", SummonVexAction("summon_vex"))
  val SUMMON_SKELETON = register("summon_skeleton", SummonSkeletonAction("summon_skeleton"))
  val VOODOO_HARM_SELF = register("voodoo_harm_self", VoodooHarmSelfAction("voodoo_harm_self"))
  val VOODOO_HARM_OTHER = register("voodoo_harm_other", VoodooHarmOtherAction("voodoo_harm_other"))
//  val SUMMON_RANGED_SKELETON = register("summon_ranged_skeleton", SummonRangedSkeletonAction())
  val EXPLOSION = register("explosion", ExplosionAction("explosion"))
  val MARK = register("mark", MarkAction("mark"))
  val RECALL = register("recall", RecallAction("recall"))
  val GOTO = register("goto", GotoAction("goto"))
  val SWAP = register("swap", SwapAction("swap"))
  val BRING = register("bring", BringAction("bring"))
  val LAUNCH = register("launch", LaunchAction("launch"))
  val BREAK = register("break", BreakBlockAction("break"))
  val DRY = register("dry", DryAction("dry"))
  val PLACE = register("place", PlaceBlockAction("place"))
  val CONJURE_WATER = register("conjure_water", PlaceWaterAction("conjure_water"))
  val HARM = register("harm", HarmAction("harm"))
  val COLOR = register("color", ColorAction("color"))
  val TIME = register("time", AdvanceTimeAction("time"))
  val CLEAR_WEATHER = register(
    "clear_weather",
    ClearWeatherAction("clear_weather")
  )
  val RAIN = register("rain", RainAction("rain"))

  val BOUND_SWORD = register("bound_sword", BoundSwordAction("bound_sword"))
  val BOUND_AXE = register(
    "bound_axe",
    BoundToolAction(
      ScriptorItems.BOUND_AXE,
      listOf(TagKey.create(Registries.BLOCK, ResourceLocation.withDefaultNamespace("mineable/axe"))),
      "bound_axe"
    )
  )
  val BOUND_SHOVEL = register(
    "bound_shovel",
    BoundToolAction(
      ScriptorItems.BOUND_SHOVEL,
      listOf(TagKey.create(Registries.BLOCK, ResourceLocation.withDefaultNamespace("mineable/shovel"))),
      "bound_shovel",
    )
  )
  val BOUND_PICKAXE = register(
    "bound_pickaxe",
    BoundToolAction(
      ScriptorItems.BOUND_PICKAXE,
      listOf(TagKey.create(Registries.BLOCK, ResourceLocation.withDefaultNamespace("mineable/pickaxe"))),
      "bound_pickaxe"
    )
  )
}
