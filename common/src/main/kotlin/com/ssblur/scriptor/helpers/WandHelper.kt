package com.ssblur.scriptor.helpers

import com.ssblur.scriptor.ScriptorDamage.overload
import com.ssblur.scriptor.ScriptorDamage.overload_no_flinch
import com.ssblur.scriptor.advancement.ScriptorAdvancements
import com.ssblur.scriptor.api.word.Word
import com.ssblur.scriptor.config.ScriptorConfig
import com.ssblur.scriptor.data.saved_data.DictionarySavedData.Companion.computeIfAbsent
import com.ssblur.scriptor.data.saved_data.LastCastSpellSavedData
import com.ssblur.scriptor.effect.EmpoweredStatusEffect
import com.ssblur.scriptor.helpers.targetable.WandTargetable
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack

object WandHelper {
  var WANDS: List<Item> = ArrayList()

  fun getPlayerLastSpell(player: Player): String? {
    val lastCastSpellData = LastCastSpellSavedData.computeIfAbsent(player)
    if ((lastCastSpellData != null) && (lastCastSpellData.getSpell() != null)) {
      return lastCastSpellData.getSpell()!!
    }
    return null
  }

  fun isInventoryCaster(
    player: Player,
    level: ServerLevel
  ): Boolean {
    val text = getPlayerLastSpell(player) ?: return false
    val spell = computeIfAbsent(level).parse(text)
    return (spell != null && (spell.subject.canBeCastOnInventory()))
  }

  fun castFromItem(
    itemStack: ItemStack,
    player: Player,
    maxCost: Int? = null,
    costMultiplier: Float? = null,
    permittedActions: List<Word>? = null,
    cooldownFunc: (Player, Int) -> Unit = ::addCooldown
  ): Boolean {
    val adjustedMaxCost = maxCost ?: ScriptorConfig.TOME_MAX_COST()
    val adjustedCostMultiplier = (costMultiplier ?: ScriptorConfig.TOME_COOLDOWN_MULTIPLIER()).toDouble() / 100.0
    val level = player.level()
    val text = getPlayerLastSpell(player)
    if (text == null || level !is ServerLevel) return false

    val spell = computeIfAbsent(level).parse(text)
    if (spell != null) {
      spell.deduplicatedDescriptorsForSubjects()


      val spellIsCompatible = ((permittedActions == null) || (spell.containedActions().all{it in permittedActions}))

      level.playSound(
        null,
        player.blockPosition(),
        SoundEvents.EVOKER_CAST_SPELL,
        SoundSource.PLAYERS,
        0.4f,
        level.getRandom().nextFloat() * 1.2f + 0.6f
      )
      if ((!spellIsCompatible) || ((spell.cost() > adjustedMaxCost) && (adjustedMaxCost != -1))) {
        player.sendSystemMessage(Component.translatable("extra.scriptor.wand_fizzle"))
        ScriptorAdvancements.FIZZLE.get().trigger(player as ServerPlayer)
        if (!player.isCreative()) cooldownFunc(
          player,
          Math.round(350.0 * adjustedCostMultiplier).toInt()
        )
        return true
      }

      var costScale = 1.0
      for (instance in player.activeEffects)
        if (instance.effect.value() is EmpoweredStatusEffect)
          for (i in 0..instance.amplifier) costScale *= (instance.effect.value() as EmpoweredStatusEffect).scale.toDouble()
      var adjustedCost =
        costScale * spell.cost() * adjustedCostMultiplier

      if (adjustedCost > ScriptorConfig.VOCAL_DAMAGE_THRESHOLD())
        player.hurt(overload_no_flinch(player)!!, (adjustedCost.toFloat() - ScriptorConfig.VOCAL_DAMAGE_THRESHOLD() * 0.75f) / 200f)
      if (player.health > 0)
        spell.cast(WandTargetable(itemStack, player, player.inventory.selected).withTargetItem(false))
      if (!player.isCreative) {
        player.cooldowns.addCooldown(itemStack.item, Math.round(adjustedCost * 3.5).toInt())
        cooldownFunc(player, Math.round(adjustedCost * 3.5).toInt())
        return true
      }
      return false
    }
    return true
  }

  fun addCooldown(player: Player, time: Int) {
    for (wand in WANDS) player.cooldowns.addCooldown(wand, time)
  }
}
