package com.ssblur.scriptor.word.action.potions

import com.ssblur.scriptor.api.word.Descriptor
import com.ssblur.scriptor.effect.ScriptorEffects.FREEZE
import com.ssblur.scriptor.helpers.targetable.Targetable
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.LiquidBlock
import net.minecraft.world.level.material.Fluids

class FreezeAction: PotionAction(FREEZE.ref(), 30.0, 1.0 / 3.0, Cost(4.0, COSTTYPE.ADDITIVE)) {
  override fun applyToPosition(
    caster: Targetable?,
    targetable: Targetable?,
    descriptors: Array<Descriptor>?,
    strength: Double,
    duration: Double
  ) {

    val level = targetable!!.level
//    Prioritise freezing liquids
    val pos = targetable.targetBlockPos

    val state = targetable.level.getBlockState(pos)
    val block = state.block
    if (block.defaultDestroyTime() < 0) return

    if(block is LiquidBlock) {
      when (state.fluidState.getType()) {
        Fluids.WATER -> level.setBlockAndUpdate(pos, Blocks.ICE.defaultBlockState())
        Fluids.FLOWING_WATER -> level.setBlockAndUpdate(pos, Blocks.SNOW.defaultBlockState())
        Fluids.LAVA -> level.setBlockAndUpdate(pos, Blocks.OBSIDIAN.defaultBlockState())
        Fluids.FLOWING_LAVA -> level.setBlockAndUpdate(pos, Blocks.COBBLESTONE.defaultBlockState())
      }
      return
    }

    if (!level.getBlockState(pos).canBeReplaced()) return

    val blockEntity = level.getBlockEntity(pos)
    level.setBlock(
      pos,
      Blocks.POWDER_SNOW.defaultBlockState(),
      2
    )
    if (blockEntity != null) {
      level.removeBlockEntity(pos)
      level.setBlockEntity(blockEntity)
    }

    level.playSound(
      null,
      pos,
      SoundEvents.BUCKET_EMPTY_POWDER_SNOW,
      SoundSource.BLOCKS,
      1.0f,
      level.getRandom().nextFloat() * 0.4f + 0.8f
    )
  }
}
