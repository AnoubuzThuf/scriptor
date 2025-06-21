package com.ssblur.scriptor.word.action

import com.ssblur.scriptor.api.word.Action
import com.ssblur.scriptor.api.word.Descriptor
import com.ssblur.scriptor.api.word.Word
import com.ssblur.scriptor.helpers.targetable.EntityTargetable
import com.ssblur.scriptor.helpers.targetable.Targetable
import com.ssblur.scriptor.word.descriptor.power.StrengthDescriptor
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.item.FallingBlockEntity
import net.minecraft.world.level.block.FallingBlock
import net.minecraft.world.phys.Vec3

class LaunchAction: Action() {
  override fun apply(caster: Targetable, targetable: Targetable, descriptors: Array<Descriptor>, words: Array<Word?>) {
    var strength = 1.5
    for (d in descriptors) {
      if (d is StrengthDescriptor) strength += d.strengthModifier()
    }

    if (targetable is EntityTargetable) {
      val entity: LivingEntity = targetable.targetEntity as LivingEntity
      val lookAngle: Vec3 = entity.lookAngle
      val deltaMovement: Vec3 = entity.deltaMovement
      if (targetable.targetEntity is ServerPlayer) {
        entity.deltaMovement = deltaMovement.add(lookAngle.x * strength, lookAngle.y * strength, lookAngle.z * strength)
//        if (entity is Player) {
//          entity.sendSystemMessage(Component.literal(lookAngle.toString()))
//        }
        val serverPlayer: ServerPlayer = targetable.targetEntity as ServerPlayer
        serverPlayer.connection.send(ClientboundSetEntityMotionPacket(entity))
      } else {
        entity.deltaMovement = deltaMovement.add(0.0, 0.6 * strength, 0.0)
      }
      return
    }

    if (targetable.level.isClientSide) return
    val level = targetable.level as ServerLevel

    val pos = targetable.offsetBlockPos
    val blockState = targetable.level.getBlockState(pos)
    if (blockState.block is FallingBlock) {
      val fallingBlockEntity: FallingBlockEntity = FallingBlockEntity.fall(level, pos, blockState)
      fallingBlockEntity.setHurtsEntities(2.0f, 40)
      fallingBlockEntity.setDeltaMovement(fallingBlockEntity.deltaMovement.add(0.0, 0.6 * strength, 0.0))
      fallingBlockEntity.hurtMarked = true
      fallingBlockEntity.fallDistance = 0.0f
    }
  }

  override fun cost() = Cost(0.6, COSTTYPE.ADDITIVE)
}