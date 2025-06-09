package com.ssblur.scriptor.word

import com.ssblur.scriptor.ScriptorMod.LOGGER
import com.ssblur.scriptor.advancement.ScriptorAdvancements
import com.ssblur.scriptor.api.word.Descriptor
import com.ssblur.scriptor.api.word.Subject
import com.ssblur.scriptor.api.word.Word
import com.ssblur.scriptor.api.word.Word.COSTTYPE
import com.ssblur.scriptor.effect.ScriptorEffects.MUTE
import com.ssblur.scriptor.helpers.targetable.EntityTargetable
import com.ssblur.scriptor.helpers.targetable.Targetable
import com.ssblur.scriptor.network.client.ParticleNetwork
import com.ssblur.scriptor.word.descriptor.AfterCastDescriptor
import com.ssblur.scriptor.word.descriptor.CastDescriptor
import com.ssblur.scriptor.word.descriptor.focus.FocusDescriptor
import com.ssblur.scriptor.word.descriptor.focus.MultiTargetFocusDescriptor
import com.ssblur.scriptor.word.descriptor.target.GeometricTargetDescriptor
import com.ssblur.scriptor.word.descriptor.target.TargetDescriptor
import net.minecraft.ChatFormatting
import net.minecraft.core.Direction
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.Vec3
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException
import kotlin.math.abs
import kotlin.math.pow

/**
 * A record used to represent a complete spell.
 * @param subject The target of a spell
 * @param spells Groups of descriptors and actions
 */
class Spell(val subject: Subject, vararg val spells: PartialSpell) {

  fun correctInitialTargetPositions(originalTargets: List<Targetable>): List<Targetable> {
    var adjustedList: MutableList<Targetable> = mutableListOf()
    for (i in 0..originalTargets.size-1) {
      var adjustedTarget = originalTargets[i]
      var adjusted: Boolean = false
      val targetDirection = adjustedTarget.direction
      if (adjustedTarget !is EntityTargetable) {
        for (coord in listOf(adjustedTarget.targetPos.x, adjustedTarget.targetPos.y, adjustedTarget.targetPos.z)) {
          val check = abs(coord % 1)
          if (check == 0.0) {
            if (originalTargets[i].facing == Direction.NORTH) {
              adjustedTarget = Targetable(adjustedTarget.level, adjustedTarget.targetPos.add(Vec3(0.0, 0.0, -1.0)), targetDirection)
            } else if (originalTargets[i].facing == Direction.WEST) {
              adjustedTarget = Targetable(adjustedTarget.level, adjustedTarget.targetPos.add(Vec3(-1.0, 0.0, 0.0)), targetDirection)
            } else if (originalTargets[i].facing == Direction.DOWN) {
              adjustedTarget =
                Targetable(adjustedTarget.level, adjustedTarget.targetPos.add(Vec3(0.0, -1.0, 0.0)), targetDirection)
            }
            adjusted = true
            break
          }
        }
        if (!adjusted) {
          val newVec = Vec3(adjustedTarget.beforeBlockPos.x.toDouble(), adjustedTarget.beforeBlockPos.y.toDouble(), adjustedTarget.beforeBlockPos.z.toDouble())
          adjustedTarget = Targetable(adjustedTarget.level, newVec, targetDirection)
        }
      }
      adjustedList.add(adjustedTarget)
    }
    return adjustedList.toList()
  }


  fun castOnTargets(originalCaster: Targetable, originalTargets: List<Targetable>) {
    assert(spells.isNotEmpty())
    val adjustedList = this.correctInitialTargetPositions(originalTargets)
//    if (originalCaster is EntityTargetable && originalCaster.targetEntity is Player) {
//      if (originalCaster.level.server != null) {
//        originalCaster.level.server!!.sendSystemMessage(
//          Component.literal(originalTargets.first().targetPos.toString())
//        )
//        originalCaster.level.server!!.sendSystemMessage(
//          Component.literal((originalTargets.first().targetPos.x % 1).toString())
//        )
//      }
//    }
    for (spell in spells) {
      var caster = originalCaster
      var targets = adjustedList
      val deduplicatedDescriptors = spell.deduplicatedDescriptors()
      for (i in 0..deduplicatedDescriptors.size-1) {
        val descriptor = deduplicatedDescriptors.get(i)
        targets = when (descriptor) {
          is TargetDescriptor -> descriptor.modifyTargets(targets, caster)
          is MultiTargetFocusDescriptor -> descriptor.modifyTargetsFocus(targets, caster)
          is GeometricTargetDescriptor -> descriptor.modifyTargets(targets, caster, i, deduplicatedDescriptors)
          else -> targets
        }
        if (descriptor is FocusDescriptor) caster = descriptor.modifyFocus(caster)
      }
      for (target in targets)
        spell.action.apply(caster, target, spell.deduplicatedDescriptors(), words())
    }
  }

  fun createFuture(caster: Targetable): CompletableFuture<List<Targetable>> {
    val targetFuture = CompletableFuture<List<Targetable>>()
    targetFuture.whenComplete { targets, throwable -> if (throwable == null) castOnTargets(caster, targets) }
    return targetFuture
  }

  /**
   * Casts this spell.
   * @param originalCaster The entity which cast this spell.
   */
  fun cast(originalCaster: Targetable) {
    var entity: Entity? = null
    var caster = originalCaster
    if (caster is EntityTargetable) {
      entity = caster.targetEntity
      if (entity is LivingEntity)
        if (entity.hasEffect(MUTE)) {
          if (entity is Player) entity.sendSystemMessage(Component.translatable("extra.scriptor.mute"))
          return
        }
    }

    assert(spells.isNotEmpty())
    for (descriptor in spells[0].deduplicatedDescriptors()) {
      if (descriptor is CastDescriptor)
        if (descriptor.cannotCast(caster)) {
          if (entity is Player) {
            entity.sendSystemMessage(Component.translatable("extra.scriptor.condition_not_met"))
            ScriptorAdvancements.FIZZLE.get().trigger(entity as ServerPlayer)
          }
          if (!caster.level.isClientSide) ParticleNetwork.fizzle(caster.level, caster.targetBlockPos)
          return
        }
      if (descriptor is FocusDescriptor) caster = descriptor.modifyFocus(caster)
    }
    val targetFuture = subject.getTargets(caster, this)
    for (descriptor in spells[0].deduplicatedDescriptors())
      if (descriptor is AfterCastDescriptor) descriptor.afterCast(caster)

    if (targetFuture.isDone) {
      try {
        castOnTargets(caster.finalTargetable!!, targetFuture.get())
      } catch (e: InterruptedException) {
        LOGGER.error(e)
      } catch (e: ExecutionException) {
        LOGGER.error(e)
      }
    } else targetFuture.whenComplete { targets, throwable ->
      throwable ?: castOnTargets(
        caster.finalTargetable!!,
        targets
      )
    }
  }

  /**
   * Casts this spell with a specified list of Targetables.
   * @param caster The entity which cast this spell.
   */
  fun cast(caster: Targetable, vararg targetables: Targetable) = castOnTargets(caster, targetables.toList())

  /**
   * The cost for this spell, generally affects cooldowns / material cost.
   * @return A number representing cost.
   */
  fun cost(): Double {
    var total: Double = 0.0
    for (subspell in spells) {
      total += subspellCost(subspell)
    }
//    Small inefficiency for using multiple actions.
    total += spells.size
    return total * finalDiscountMultiplier()
  }

  fun subspellCost(spell: PartialSpell): Double {
    var sum = 0.0
    var scalar = 1.0
    var discount = 0.0
    var subCount = 0.0
    var sub = 0.0

    val action = spell.action
    val descriptors = spell.deduplicatedDescriptors()

    fun handleCost(word: Word?) {
      val cost = word!!.cost()
      when (cost.type) {
        COSTTYPE.ADDITIVE -> {
          if (sum < 0) {
            subCount++
            sub += cost.cost
          } else sum += cost.cost
        }
//        Skip discount multipliers here, then apply once at the end.
        COSTTYPE.MULTIPLICATIVE -> if (cost.cost >= 1.0) scalar *= cost.cost
        COSTTYPE.ADDITIVE_POST -> discount += cost.cost
      }
    }
    handleCost(action)
    handleCost(subject)
    for (d in descriptors) {
      handleCost(d)
    }

    if (subCount > 0) {
      val squeeze = 0.5
      val denominator = 1 - (squeeze - squeeze.pow(subCount))
      sum -= sub / denominator
    }

    return sum * scalar + discount
  }

  private fun finalDiscountMultiplier(): Double {
    var allDiscounts = arrayListOf<Word>()
    var finalDiscountMulti = 1.0
    for (spell in spells) {
      val descriptors = spell.deduplicatedDescriptors()
      for (word in descriptors) {
        if (word!!.cost().type == COSTTYPE.MULTIPLICATIVE && word!!.cost().cost < 1.0) {
          if (word.allowsDuplicates()) {
            allDiscounts.add(word)
            finalDiscountMulti *= word.cost().cost
          } else if (!allDiscounts.contains(word)) {
            allDiscounts.add(word)
            finalDiscountMulti *= word.cost().cost
          }
        }
      }
    }
    return finalDiscountMulti
  }

  private fun words(): Array<Word?> {
    var length = 1
    var index = 1
    for (spell in spells) length += spell.deduplicatedDescriptors().size + 1

    val words = arrayOfNulls<Word>(length)
    words[0] = subject

    for (spell in spells) {
      words[index] = spell.action
      index++

      val descriptors = spell.deduplicatedDescriptors()
      System.arraycopy(descriptors, 0, words, index, descriptors.size)
      index += descriptors.size
    }
    return words
  }

  fun deduplicatedDescriptorsForSubjects(): Array<Descriptor> =
    spells.flatMap { it.descriptors.toList() }.distinct().toTypedArray()
}
