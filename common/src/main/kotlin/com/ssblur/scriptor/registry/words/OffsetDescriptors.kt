package com.ssblur.scriptor.registry.words

import com.ssblur.scriptor.registry.words.WordRegistry.register
import com.ssblur.scriptor.word.descriptor.target.OffsetDescriptor

@Suppress("unused")
object OffsetDescriptors {
  val MOVE_RIGHT = register("move_right", OffsetDescriptor(1.05, "move_right").right())
  val COPY_RIGHT = register("copy_right", OffsetDescriptor(1.25, "copy_right").duplicate().right())
  val MOVE_LEFT = register("move_left", OffsetDescriptor(1.05, "move_left").left())
  val COPY_LEFT = register("copy_left", OffsetDescriptor(1.25, "copy_left").duplicate().left())
  val MOVE_FORWARDS = register("move_forwards", OffsetDescriptor(1.05, "move_forwards").forward())
  val COPY_FORWARDS = register("copy_forwards", OffsetDescriptor(1.25, "copy_forwards").duplicate().forward())
  val MOVE_BACKWARDS = register("move_backwards", OffsetDescriptor(1.05, "move_backwards").backwards())
  val COPY_BACKWARDS = register("copy_backwards", OffsetDescriptor(1.25, "copy_backwards").duplicate().backwards())
  val MOVE_UP = register("move_up", OffsetDescriptor(1.05, "move_up").up())
  val COPY_UP = register("copy_up", OffsetDescriptor(1.25, "copy_up").duplicate().up())
  val MOVE_DOWN = register("move_down", OffsetDescriptor(1.05, "move_down").down())
  val COPY_DOWN = register("copy_down", OffsetDescriptor(1.25, "copy_down").duplicate().down())
}
