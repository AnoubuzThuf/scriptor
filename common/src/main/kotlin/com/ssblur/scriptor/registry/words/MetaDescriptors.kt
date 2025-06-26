package com.ssblur.scriptor.registry.words

import com.ssblur.scriptor.registry.words.WordRegistry.register
import com.ssblur.scriptor.word.descriptor.focus.inventory.CasterFirstEmptySlotDescriptor
import com.ssblur.scriptor.word.descriptor.focus.inventory.CasterFirstFilledSlotDescriptor
import com.ssblur.scriptor.word.descriptor.focus.inventory.CasterIgnoreTargetedSlotDescriptor
import com.ssblur.scriptor.word.descriptor.focus.inventory.CasterInventoryDescriptor
import com.ssblur.scriptor.word.descriptor.meta.DescriptorMultiplier
import com.ssblur.scriptor.word.descriptor.target.inventory.FirstEmptySlotDescriptor
import com.ssblur.scriptor.word.descriptor.target.inventory.FirstFilledSlotDescriptor
import com.ssblur.scriptor.word.descriptor.target.inventory.IgnoreTargetedSlotDescriptor
import com.ssblur.scriptor.word.descriptor.target.inventory.InventoryDescriptor

@Suppress("unused")
object MetaDescriptors {
    val DESCRIPTOR_MULTIPLIER__TWO = register(
        "descriptor_multiplier__two",
        DescriptorMultiplier(2,
            "descriptor_multiplier__two")
    )
    val DESCRIPTOR_MULTIPLIER__THREE = register(
        "descriptor_multiplier__three",
        DescriptorMultiplier(3,
            "descriptor_multiplier__three")
    )
    val DESCRIPTOR_MULTIPLIER__FIVE = register(
        "descriptor_multiplier__five",
        DescriptorMultiplier(5,
            "descriptor_multiplier__five")
    )
    val DESCRIPTOR_MULTIPLIER__SEVEN = register(
        "descriptor_multiplier__seven",
        DescriptorMultiplier(7,
            "descriptor_multiplier__seven")
    )
}
