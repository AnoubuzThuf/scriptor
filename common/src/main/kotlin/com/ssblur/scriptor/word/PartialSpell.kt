package com.ssblur.scriptor.word

import com.ssblur.scriptor.ScriptorMod
import com.ssblur.scriptor.api.ScriptorRegistry
import com.ssblur.scriptor.api.word.Action
import com.ssblur.scriptor.api.word.Descriptor
import com.ssblur.scriptor.registry.words.Actions
import com.ssblur.scriptor.registry.words.WordRegistry
import org.spongepowered.asm.mixin.injection.Desc

fun toString(pSpell: PartialSpell): String {
  var pSpellString: String = "A:" + pSpell.action.registryKey + ","
  for (currentDescriptor in pSpell.descriptors) {
    pSpellString += "D:" + currentDescriptor.registryKey + ","
  }
  return pSpellString
}

fun fromString(pSpellString: String?): PartialSpell? {
  if (pSpellString != null) {
    val actionString = Regex("^A:([A-Za-z_0-9]+?),").find(pSpellString)
    val descriptorStringMatches: Sequence<MatchResult> = Regex("D:([A-Za-z_0-9]+?),").findAll(pSpellString)
    val descriptorStrings: List<String> = descriptorStringMatches.map {it.groupValues[1]}.toList()
    if ((actionString != null && actionString.groupValues.size == 2)) {
      val action: Action? = WordRegistry.actionRegistry.get(actionString.groupValues[1])
      if (action != null) {
        var descriptors: ArrayList<Descriptor> = arrayListOf<Descriptor>()
        for (d in descriptorStrings) {
          val descriptor: Descriptor? = WordRegistry.descriptorRegistry.get(d)
          if (descriptor == null) {
            return null
          }
          descriptors.add(descriptor)
        }
        return PartialSpell(action, *descriptors.toTypedArray())
      }
    }
  }
  return null
}

class PartialSpell(val action: Action, vararg val descriptors: Descriptor) {
  fun deduplicatedDescriptors(): Array<Descriptor> {
    val out = ArrayList<Descriptor>()
    for (descriptor in descriptors) {
      if (descriptor.allowsDuplicates() || !out.contains(descriptor)) out.add(descriptor)
    }
    return out.toTypedArray()
  }
}
