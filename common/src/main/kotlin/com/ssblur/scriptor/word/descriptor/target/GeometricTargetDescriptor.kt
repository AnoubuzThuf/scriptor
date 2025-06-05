package com.ssblur.scriptor.word.descriptor.target

import com.ssblur.scriptor.api.word.Descriptor
import com.ssblur.scriptor.helpers.targetable.Targetable
import net.minecraft.world.phys.Vec3


interface GeometricTargetDescriptor {
//    Pass in list of descriptors to allow counting the number of specific descriptors
    fun modifyTargets(originalTargetables: List<Targetable>, owner: Targetable, index: Int, descriptors: Array<Descriptor>): List<Targetable>
    fun replacesSubjectCost(): Boolean
    fun getUses(index: Int, descriptors: Array<Descriptor>): Int {
        val currentType = descriptors.get(index)::class
        if (descriptors.size-1 > index && descriptors.get(index + 1)::class == currentType) {
            return -1
        }
        var instances = 1
        var current = index - 1
        while (current >= 0) {
            if (descriptors.get(current)::class == currentType) {
                instances++
            } else {
                break
            }
            current--
        }
        return instances
    }
    fun deduplicate(points: List<Vec3>): List<Vec3> {
        var pointSet: MutableSet<Vec3> = mutableSetOf()
        for (point in points) {
            if (pointSet.none { it.distanceTo(point) < 0.1 }) {
//            if (pointSet.none { it.x == point.x && it.y == point.y && it.z == point.z }) {
                pointSet.add(point)
            }
        }
        return pointSet.toList()
    }
}
