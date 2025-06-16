package com.ssblur.scriptor.item.wands

import com.ssblur.scriptor.api.word.Word
import com.ssblur.scriptor.config.ScriptorConfig
import com.ssblur.scriptor.data.components.ScriptorDataComponents
import com.ssblur.scriptor.helpers.WandHelper
import com.ssblur.scriptor.item.books.BookOfBooks
import com.ssblur.scriptor.network.server.ScriptorNetworkC2S
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.ClickAction
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import kotlin.collections.plus

open class Wand(properties: Properties,
                val maxCost: Int = ScriptorConfig.VOCAL_MAX_COST(),
                val costMultiplier: Float? = null,
                val permittedActions: List<Word>? = null
): Item(properties) {
  init {
    WandHelper.WANDS += this
  }

  override fun use(
    level: Level,
    player: Player,
    interactionHand: InteractionHand
  ): InteractionResultHolder<ItemStack> {
    if (level.isClientSide) return InteractionResultHolder.success(player.getItemInHand(interactionHand))

    val item = player.getItemInHand(interactionHand)
    WandHelper.castFromItem(item, player, maxCost, costMultiplier, permittedActions)

    return InteractionResultHolder.fail(player.getItemInHand(interactionHand))
  }

  override fun overrideStackedOnOther(
    itemStack: ItemStack,
    slot: Slot,
    clickAction: ClickAction,
    player: Player
  ): Boolean {
    if (itemStack[ScriptorDataComponents.INVENTORY_CAST] == true && clickAction == ClickAction.SECONDARY && !slot.item.isEmpty && slot.item.item !is BookOfBooks) {

      if (player.cooldowns.isOnCooldown(this)) return true
      val level = player.level()
      if (!level.isClientSide) return true
      if (player.isCreative) return false // TODO
      else {
        ScriptorNetworkC2S.useWand(ScriptorNetworkC2S.UseBook(slot.index))
      }
      return true
    }
    return false
  }

  override fun inventoryTick(itemStack: ItemStack, level: Level, entity: Entity, i: Int, bl: Boolean) {
    if (entity is Player) {
      if(!level.isClientSide && itemStack[ScriptorDataComponents.INVENTORY_CAST] == null)
        itemStack[ScriptorDataComponents.INVENTORY_CAST] = WandHelper.isInventoryCaster(entity, level as ServerLevel)
    }
    super.inventoryTick(itemStack, level, entity, i, bl)
  }
}
