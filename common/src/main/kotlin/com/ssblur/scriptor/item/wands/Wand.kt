package com.ssblur.scriptor.item.wands

import com.ssblur.scriptor.ScriptorDamage.overload
import com.ssblur.scriptor.ScriptorDamage.overload_no_flinch
import com.ssblur.scriptor.advancement.ScriptorAdvancements
import com.ssblur.scriptor.api.word.Word
import com.ssblur.scriptor.config.ScriptorConfig
import com.ssblur.scriptor.data.saved_data.DictionarySavedData.Companion.computeIfAbsent
import com.ssblur.scriptor.data.saved_data.LastCastSpellSavedData
import com.ssblur.scriptor.effect.EmpoweredStatusEffect
import com.ssblur.scriptor.effect.ScriptorEffects.HOARSE
import com.ssblur.scriptor.effect.ScriptorEffects.MUTE
import com.ssblur.scriptor.helpers.targetable.EntityTargetable
import net.minecraft.ChatFormatting
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.level.Level

open class Wand(properties: Properties,
                val maxCost: Int = ScriptorConfig.VOCAL_MAX_COST(),
                val costMultiplier: Float = 1f,
                val permittedActions: List<Word>? = null
): Item(properties) {

    override fun use(
    level: Level,
    player: Player,
    interactionHand: InteractionHand
  ): InteractionResultHolder<ItemStack> {
    if (level is ServerLevel) {
      val lastCastSpellData = LastCastSpellSavedData.computeIfAbsent(player)
      if ((lastCastSpellData == null) || (lastCastSpellData.getSpell() == null)) {
        player.sendSystemMessage(Component.literal("A wand re-casts the last spell cast through the chat."))
        return InteractionResultHolder.fail(player.getItemInHand(interactionHand))
      }

      val sentence: String = lastCastSpellData.getSpell()!!

      val spell = computeIfAbsent(level).parse(sentence)
      if (spell != null) {

        var cost = Math.round(spell.cost() * 30).toInt()
        var costScale = 1.0f
        for (instance in player.activeEffects)
          if (instance.effect.value() is EmpoweredStatusEffect)
            for (i in 0..instance.amplifier)
              costScale *= (instance.effect.value() as EmpoweredStatusEffect).scale
        cost = Math.round((cost.toFloat()) * costScale * costMultiplier)

        val spellIsCompatible = ((permittedActions == null) || (spell.containedActions().all{it in permittedActions!!}))

        if ((maxCost in 0..<cost) || !spellIsCompatible) {
          player.sendSystemMessage(Component.translatable("extra.scriptor.fizzle"))
          ScriptorAdvancements.FIZZLE.get().trigger(player as ServerPlayer)
          return InteractionResultHolder.fail(player.getItemInHand(interactionHand))
        }

        val adjustedCost = Math.round(cost * (ScriptorConfig.VOCAL_COOLDOWN_MULTIPLIER() / 100.0)).toInt()
        if (!player.isCreative) {
          if (adjustedCost > ScriptorConfig.VOCAL_HUNGER_THRESHOLD())
            player.addEffect(
              MobEffectInstance(
                MobEffects.HUNGER,
                2 * (adjustedCost - ScriptorConfig.VOCAL_HUNGER_THRESHOLD())
              )
            )
          if (adjustedCost > ScriptorConfig.VOCAL_DAMAGE_THRESHOLD())
            player.hurt(overload_no_flinch(player)!!, (adjustedCost - ScriptorConfig.VOCAL_DAMAGE_THRESHOLD() * 0.75f) / 100f)
        }
        if (player.health > 0) {
          spell.cast(EntityTargetable(player))
        }
      }
    }
    return InteractionResultHolder.success(player.getItemInHand(interactionHand))
  }
}
