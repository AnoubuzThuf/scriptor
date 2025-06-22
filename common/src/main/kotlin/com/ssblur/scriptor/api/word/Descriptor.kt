package com.ssblur.scriptor.api.word

abstract class Descriptor(val registryKey: String): Word() {
  open fun allowsDuplicates(): Boolean {
    return false
  }
}
