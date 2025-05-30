package com.ssblur.scriptor.blockentity.renderers

import com.mojang.blaze3d.vertex.PoseStack
import com.ssblur.scriptor.blockentity.LightBlockEntity
import com.ssblur.scriptor.color.CustomColors.getColor
import com.ssblur.scriptor.particle.MagicParticleData
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider
import org.joml.Random

class LightBlockEntityRenderer(@Suppress("unused_parameter") context: BlockEntityRendererProvider.Context?):
  BlockEntityRenderer<LightBlockEntity> {
  override fun render(
    light: LightBlockEntity,
    tickDelta: Float,
    matrix: PoseStack,
    buffers: MultiBufferSource,
    lightLevel: Int,
    j: Int
  ) {
    if (light.level == null) return

    val c = getColor(light.getColor(), light.level!!.gameTime + tickDelta)
    val r = (c and 0xff0000) shr 16
    val g = (c and 0x00ff00) shr 8
    val b = c and 0x0000ff

    val random = Random()
    if (random.nextFloat() < 0.9f) return
    val xd = 0.25f + random.nextFloat() / 2f
    val yd = 0.25f + random.nextFloat() / 2f
    val zd = 0.25f + random.nextFloat() / 2f

    if (!Minecraft.getInstance().isPaused) light.level!!.addParticle(
      MagicParticleData.magic(r, g, b),
      (light.blockPos.x + xd).toDouble(),
      (light.blockPos.y + yd).toDouble(),
      (light.blockPos.z + zd).toDouble(),
      0.0,
      0.0,
      0.0
    )
  }
}
