package com.ssblur.scriptor.entity.renderers

import com.ssblur.scriptor.entity.SummonedVex
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.model.geom.ModelLayers
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.MobRenderer
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer
import net.minecraft.core.BlockPos
import net.minecraft.resources.ResourceLocation


@Environment(EnvType.CLIENT)
class SummonedVexRenderer(context: EntityRendererProvider.Context) : MobRenderer<SummonedVex, SummonedVexModel?>(
  context, SummonedVexModel(
    context.bakeLayer(
      ModelLayers.VEX
    )
  ), 0.3f
) {
  init {
    this.addLayer(ItemInHandLayer<SummonedVex, SummonedVexModel?>(this, context.getItemInHandRenderer()))
  }

  override fun getBlockLightLevel(vex: SummonedVex, blockPos: BlockPos): Int {
    return 15
  }

  override fun getTextureLocation(vex: SummonedVex): ResourceLocation {
    return if (vex.isCharging()) SummonedVexRenderer.Companion.VEX_CHARGING_LOCATION else SummonedVexRenderer.Companion.VEX_LOCATION
  }

  companion object {
    private val VEX_LOCATION: ResourceLocation =
      ResourceLocation.withDefaultNamespace("textures/entity/illager/vex.png")
    private val VEX_CHARGING_LOCATION: ResourceLocation =
      ResourceLocation.withDefaultNamespace("textures/entity/illager/vex_charging.png")
  }
}
