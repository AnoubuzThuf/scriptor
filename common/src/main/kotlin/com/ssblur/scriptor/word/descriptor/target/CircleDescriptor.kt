package com.ssblur.scriptor.word.descriptor.target

import com.ssblur.scriptor.api.word.Descriptor
import com.ssblur.scriptor.helpers.MathHelper
import com.ssblur.scriptor.helpers.targetable.Targetable
import net.minecraft.core.Direction
import net.minecraft.world.phys.Vec2

class CircleDescriptor: Descriptor(), GeometricTargetDescriptor {
  override fun modifyTargets(originalTargetables: List<Targetable>, owner: Targetable, index: Int, descriptors: Array<Descriptor>): List<Targetable> {
    val uses = getUses(index, descriptors)
    val radius = uses
    if (uses < 1) {
      return originalTargetables
    }
    val targetables = originalTargetables.toMutableList()
    if (targetables.isEmpty()) return targetables
    val circle_points = MathHelper.get_circle_coords(radius)
    val targetable = targetables.last()
    val axis = targetable.facing.axis

    val translatedPoints = circle_points.map { point ->
      if (axis === Direction.Axis.Y) {
        Vec2(point.x + radius, point.y)
      } else {
        Vec2(point.x, point.y + radius)
      }
    }
    val transformedPoints = deduplicate(translatedPoints.map{ MathHelper.player_view_transform_point(targetable, owner, it) })
    for (point in transformedPoints) {
      targetables.add(Targetable(targetable.level, point).setFacing(targetable.facing))
    }
    return targetables
  }

  override fun replacesSubjectCost() = false
  override fun allowsDuplicates() = true
  override fun cost() = Cost(1.25, COSTTYPE.MULTIPLICATIVE)

}
