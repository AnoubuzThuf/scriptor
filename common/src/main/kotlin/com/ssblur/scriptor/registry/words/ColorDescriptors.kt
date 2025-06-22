package com.ssblur.scriptor.registry.words

import com.ssblur.scriptor.registry.words.WordRegistry.register
import com.ssblur.scriptor.word.descriptor.color.ColorDescriptor

@Suppress("unused")
object ColorDescriptors {
  val WHITE = register(
    "white",
    ColorDescriptor(0xe4e4e4,
    "white",)
  )
  val LIGHT_GRAY = register(
    "light_gray",
    ColorDescriptor(0xa0a7a7,
    "light_gray",)
  )
  val DARK_GRAY = register(
    "dark_gray",
    ColorDescriptor(0x414141,
    "dark_gray",)
  )
  val BLACK = register(
    "black",
    ColorDescriptor(0x181414,
    "black",)
  )
  val RED = register(
    "red",
    ColorDescriptor(0x9e2b27,
    "red",)
  )
  val ORANGE = register(
    "orange",
    ColorDescriptor(0xea7e35,
    "orange",)
  )
  val YELLOW = register(
    "yellow",
    ColorDescriptor(0xc2b51c,
    "yellow",)
  )
  val LIME_GREEN = register(
    "lime_green",
    ColorDescriptor(0x39ba2e,
    "lime_green",)
  )
  val GREEN = register(
    "green",
    ColorDescriptor(0x364b18,
    "green",)
  )
  val LIGHT_BLUE = register(
    "light_blue",
    ColorDescriptor(0x6387d2,
    "light_blue",)
  )
  val CYAN = register(
    "cyan",
    ColorDescriptor(0x267191,
    "cyan",)
  )
  val BLUE = register(
    "blue",
    ColorDescriptor(0x253193,
    "blue",)
  )
  val PURPLE = register(
    "purple",
    ColorDescriptor(0x7e34bf,
    "purple",)
  )
  val MAGENTA = register(
    "magenta",
    ColorDescriptor(0xbe49c9,
    "magenta",)
  )
  val PINK = register(
    "pink",
    ColorDescriptor(0xd98199,
    "pink",)
  )
  val BROWN = register(
    "brown",
    ColorDescriptor(0x56331c,
    "brown",)
  )
  val RAINBOW = register(
    "rainbow",
    ColorDescriptor(-1,
    "rainbow",)
  )
}
