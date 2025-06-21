package com.ssblur.scriptor.word.action

import com.ssblur.scriptor.api.word.Action
import com.ssblur.scriptor.api.word.Descriptor
import com.ssblur.scriptor.api.word.Word
import com.ssblur.scriptor.color.CustomColors.getColor
import com.ssblur.scriptor.entity.SUMMON_BEHAVIOURS
import com.ssblur.scriptor.entity.SUMMON_PROPERTIES
import com.ssblur.scriptor.entity.ScriptorEntities.SUMMONED_SKELETON
import com.ssblur.scriptor.helpers.targetable.EntityTargetable
import com.ssblur.scriptor.helpers.targetable.Targetable
import com.ssblur.scriptor.word.descriptor.duration.DurationDescriptor
import com.ssblur.scriptor.word.descriptor.duration.PermanentDurationDescriptor
import com.ssblur.scriptor.word.descriptor.power.StrengthDescriptor
import com.ssblur.scriptor.word.descriptor.summon.SummonBehaviourDescriptor
import com.ssblur.scriptor.word.descriptor.summon.SummonPropertyDescriptor
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.MobSpawnType
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.gameevent.GameEvent
import net.minecraft.world.phys.Vec3


class SummonSkeletonAction: Action() {
  override fun apply(caster: Targetable, targetable: Targetable, descriptors: Array<Descriptor>, words: Array<Word?>) {
    val level = targetable.level as ServerLevel
    var strength = 0.0
    var duration: Double = 20.0
    for (d in descriptors) {
      if (d is StrengthDescriptor) strength += d.strengthModifier()
      if (d is DurationDescriptor) duration += d.durationModifier()
    }
    val finalDuration = if (descriptors.none {it is PermanentDurationDescriptor }) duration.toInt() * 20 else null

    val behaviourDescriptors: List<SummonBehaviourDescriptor> = descriptors.filter{it is SummonBehaviourDescriptor}.map{it as SummonBehaviourDescriptor}
    val summonProperties: List<SUMMON_PROPERTIES> = descriptors.filter{it is SummonPropertyDescriptor}.map{it as SummonPropertyDescriptor}.map{it.summonProperty}
    val isSentry: Boolean = behaviourDescriptors.any{ it.behaviour == SUMMON_BEHAVIOURS.SENTRY  }

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
        val summonedSkeleton = SUMMONED_SKELETON.get().create(level, null, blockPos2, MobSpawnType.MOB_SUMMONED, false, false)!!
        summonedSkeleton.setSummonParams(l,  finalDuration, strength.toInt(), getColor(descriptors), behaviourDescriptors, level,
          SUMMON_PROPERTIES.RANGED in summonProperties, SUMMON_PROPERTIES.INVISIBLE in summonProperties)

        summonedSkeleton.finalizeSpawn(level, level.getCurrentDifficultyAt(blockPos2), MobSpawnType.MOB_SUMMONED, null)
        summonedSkeleton.setPos(vecPos)
        if (isSentry) {
          summonedSkeleton.restrictTo(blockPos2, 1)
        }
        level.addFreshEntity(summonedSkeleton)
        level.gameEvent(GameEvent.ENTITY_PLACE, blockPos2,  GameEvent.Context.of(l))
        summonedSkeleton.setPos(vecPos)
      }
    }
  }
  override fun cost() = Cost(12.0, COSTTYPE.ADDITIVE)
}
