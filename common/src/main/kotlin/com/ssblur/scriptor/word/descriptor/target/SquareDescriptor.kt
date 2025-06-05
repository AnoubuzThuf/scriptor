package com.ssblur.scriptor.word.descriptor.target

import com.ssblur.scriptor.api.word.Descriptor
import com.ssblur.scriptor.helpers.MathHelper
import com.ssblur.scriptor.helpers.targetable.Targetable

class SquareDescriptor: Descriptor(), GeometricTargetDescriptor {
  override fun modifyTargets(originalTargetables: List<Targetable>, owner: Targetable, index: Int, descriptors: Array<Descriptor>): List<Targetable> {
    val uses = getUses(index, descriptors)
    if (uses < 1) {
      return originalTargetables
    }
    val targetables = originalTargetables.toMutableList()
    if (targetables.isEmpty()) return targetables

    val square_points = MathHelper.get_square_coords(uses+1)
    val targetable = targetables.last()
    val transformed_points = deduplicate(square_points.map{ MathHelper.player_view_transform_point(targetable, owner, it) })
    for (point in transformed_points) {
      targetables.add(Targetable(targetable.level, point).setFacing(targetable.facing))
    }
    return targetables
  }

  override fun replacesSubjectCost() = false
  override fun allowsDuplicates() = true
  override fun cost() = Cost(1.25, COSTTYPE.MULTIPLICATIVE)

}
