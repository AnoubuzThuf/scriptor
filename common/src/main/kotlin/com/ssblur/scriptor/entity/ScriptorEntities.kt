package com.ssblur.scriptor.entity

import com.ssblur.scriptor.ScriptorMod
import com.ssblur.scriptor.entity.renderers.ColorfulSheepRenderer
import com.ssblur.scriptor.entity.renderers.ScriptorProjectileRenderer
import com.ssblur.scriptor.entity.renderers.SummonedVexRenderer
import com.ssblur.unfocused.entity.EntityAttributes.registerEntityAttributes
import com.ssblur.unfocused.rendering.EntityRendering.registerEntityRenderer
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.renderer.entity.SkeletonRenderer
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.MobCategory
import net.minecraft.world.entity.animal.Sheep
import net.minecraft.world.entity.monster.Skeleton
import net.minecraft.world.entity.monster.Vex


object ScriptorEntities {
  val PROJECTILE_TYPE = ScriptorMod.registerEntity(
    "projectile"
  ) {
    EntityType.Builder.of(
      { entityType, level -> ScriptorProjectile(entityType, level) },
      MobCategory.MISC
    )
      .clientTrackingRange(8)
      .sized(0.25f, 0.25f)
      .build("projectile")
  }
  val COLORFUL_SHEEP_TYPE = ScriptorMod.registerEntity(
    "colorful_sheep"
  ) {
    EntityType.Builder.of(
      { entityType, level -> ColorfulSheep(entityType, level) },
      MobCategory.CREATURE
    )
      .clientTrackingRange(10)
      .sized(0.9f, 1.3f)
      .build("colorful_sheep")
  }
  val SUMMONED_VEX = ScriptorMod.registerEntity(
    "summoned_vex"
  ) {
    EntityType.Builder.of(
      { entityType, level -> SummonedVex(entityType, level) },
      MobCategory.CREATURE
    )
      .clientTrackingRange(64)
      .sized(0.4f, 0.8f)
      .build("summoned_vex")
  }
  val SUMMONED_SKELETON = ScriptorMod.registerEntity(
    "summoned_skeleton"
  ) {
    EntityType.Builder.of(
      { entityType, level -> SummonedSkeleton(entityType, level) },
      MobCategory.CREATURE
    )
      .sized(1.0F, 1.8F).clientTrackingRange(10)
      .build("summoned_skeleton")
  }

  @Environment(EnvType.CLIENT)
  fun registerRenderers() {
    ScriptorMod.registerEntityRenderer(PROJECTILE_TYPE) { ScriptorProjectileRenderer(it) }
    ScriptorMod.registerEntityRenderer(COLORFUL_SHEEP_TYPE) { ColorfulSheepRenderer(it) }
    ScriptorMod.registerEntityRenderer(SUMMONED_VEX) { SummonedVexRenderer(it) }
    ScriptorMod.registerEntityRenderer(SUMMONED_SKELETON) { SkeletonRenderer(it) }
  }

  fun register() {
    ScriptorMod.registerEntityAttributes(COLORFUL_SHEEP_TYPE) { Sheep.createAttributes() }
    ScriptorMod.registerEntityAttributes(SUMMONED_VEX) { Vex.createAttributes() }
    ScriptorMod.registerEntityAttributes(SUMMONED_SKELETON) { Skeleton.createAttributes() }
  }
}
