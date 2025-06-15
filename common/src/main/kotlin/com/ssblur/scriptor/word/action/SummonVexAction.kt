package com.ssblur.scriptor.word.action

import com.ssblur.scriptor.api.word.Action
import com.ssblur.scriptor.api.word.Descriptor
import com.ssblur.scriptor.api.word.Word
import com.ssblur.scriptor.color.CustomColors.getColor
import com.ssblur.scriptor.entity.SUMMON_PROPERTIES
import com.ssblur.scriptor.entity.ScriptorEntities.SUMMONED_SKELETON
import com.ssblur.scriptor.entity.ScriptorEntities.SUMMONED_VEX
import com.ssblur.scriptor.entity.SummonedVex
import com.ssblur.scriptor.helpers.targetable.EntityTargetable
import com.ssblur.scriptor.helpers.targetable.Targetable
import com.ssblur.scriptor.word.descriptor.duration.DurationDescriptor
import com.ssblur.scriptor.word.descriptor.power.StrengthDescriptor
import com.ssblur.scriptor.word.descriptor.summon.SummonBehaviourDescriptor
import com.ssblur.scriptor.word.descriptor.summon.SummonPropertyDescriptor
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.MobSpawnType
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.gameevent.GameEvent
import net.minecraft.world.phys.Vec3



class SummonVexAction: Action() {
  override fun apply(caster: Targetable, targetable: Targetable, descriptors: Array<Descriptor>, words: Array<Word?>) {
    val level = targetable.level as ServerLevel
    var strength = 0.0
    var duration = 10.0
    for (d in descriptors) {
      if (d is StrengthDescriptor) strength += d.strengthModifier()
      if (d is DurationDescriptor) duration += d.durationModifier()
    }
    val behaviourDescriptors: List<SummonBehaviourDescriptor> = descriptors.filter{it is SummonBehaviourDescriptor}.map{it as SummonBehaviourDescriptor}

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
        val vecPos: Vec3 = Vec3(blockPos2.x.toDouble(), blockPos2.y.toDouble(), blockPos2.z.toDouble())
        val summonedVex: SummonedVex = SUMMONED_VEX.get().create(
          level, null, blockPos2, MobSpawnType.MOB_SUMMONED, false, false
        )!!
        summonedVex.setSummonParams(l,  duration.toInt() * 20, true, strength.toInt(), getColor(descriptors), behaviourDescriptors, level)

        summonedVex.finalizeSpawn(level, level.getCurrentDifficultyAt(blockPos2), MobSpawnType.MOB_SUMMONED, null)
        summonedVex.setPos(vecPos)
        level.addFreshEntity(summonedVex)
        level.gameEvent(GameEvent.ENTITY_PLACE, blockPos2,  GameEvent.Context.of(l))
        summonedVex.setPos(vecPos)
      }
    }
  }
  override fun cost() = Cost(4.0, COSTTYPE.ADDITIVE)
}
