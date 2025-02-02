package com.ssblur.scriptor.block

import com.ssblur.unfocused.extension.BlockExtension.renderType
import com.ssblur.unfocused.extension.BlockPropertiesExtension.isEphemeral
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.renderer.RenderType
import net.minecraft.world.item.DyeColor
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.SoundType
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument

class MagicBlock(val color: DyeColor): Block(
  Properties.of().instrument(NoteBlockInstrument.HAT)
    .strength(0.3f)
    .sound(SoundType.GLASS)
    .noOcclusion()
    .mapColor(color)
    .isEphemeral()
) {
  init {
    try {
      clientInit()
    } catch (_: NoSuchMethodError) {
    }
  }

  @Environment(EnvType.CLIENT)
  fun clientInit() {
    this.renderType(RenderType.translucent())
  }
}
