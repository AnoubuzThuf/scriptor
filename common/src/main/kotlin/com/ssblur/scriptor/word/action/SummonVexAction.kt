package com.ssblur.scriptor.word.action

import com.ssblur.scriptor.api.word.Action
import com.ssblur.scriptor.api.word.Descriptor
import com.ssblur.scriptor.api.word.Word
import com.ssblur.scriptor.entity.ScriptorEntities
import com.ssblur.scriptor.entity.ScriptorEntities.SUMMONED_VEX
import com.ssblur.scriptor.entity.SummonedVex
import com.ssblur.scriptor.helpers.targetable.EntityTargetable
import com.ssblur.scriptor.helpers.targetable.Targetable
import com.ssblur.scriptor.word.descriptor.duration.DurationDescriptor
import com.ssblur.scriptor.word.descriptor.power.StrengthDescriptor
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.MobSpawnType
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.gameevent.GameEvent
import net.minecraft.world.phys.Vec3



class SummonVexAction: Action() {
  override fun apply(caster: Targetable, targetable: Targetable, descriptors: Array<Descriptor>, words: Array<Word?>) {
    val level = targetable.level as ServerLevel
    var strength = 1.0
    var duration = 10.0
    for (d in descriptors) {
      if (d is StrengthDescriptor) strength += d.strengthModifier()
      if (d is DurationDescriptor) duration += d.durationModifier()
    }
    if (caster is EntityTargetable) {
      if (caster.targetEntity is LivingEntity) {
        val l = caster.targetEntity as LivingEntity
        val blockPos: BlockPos = targetable.targetBlockPos
        val blockState: BlockState = level.getBlockState(blockPos)
        val blockPos2 = if (blockState.getCollisionShape(level, blockPos).isEmpty()) {
          blockPos
        } else {
          blockPos.relative(targetable.facing)
        }
        val summonedVex: SummonedVex? = SUMMONED_VEX.get().spawn(
          level, null, null, blockPos2, MobSpawnType.MOB_SUMMONED, true, blockPos.equals(blockPos2) && targetable.facing == Direction.UP
        )
        if (summonedVex != null) {
//          summonedVex.moveTo(targetable.targetBlockPos, 0f, 0f)
          summonedVex.finalizeSpawn(level, level.getCurrentDifficultyAt(targetable.targetBlockPos), MobSpawnType.MOB_SUMMONED, null)
//          summonedVex.setPos(targetable.targetBlockPos.x.toDouble(), targetable.targetBlockPos.y.toDouble(), targetable.targetBlockPos.z.toDouble())
          val tag = CompoundTag()
          summonedVex.setup(l, tag, duration.toInt() * 10, true, strength.toInt())
          summonedVex.setBoundOrigin(blockPos2)
          level.addFreshEntity(summonedVex)
          level.gameEvent(GameEvent.ENTITY_PLACE, blockPos2,  GameEvent.Context.of(l))
        }
      }
    }
  }
  override fun cost() = Cost(8.0, COSTTYPE.ADDITIVE)
}
