package com.ssblur.scriptor.registry.words

import com.ssblur.scriptor.api.word.Word
import com.ssblur.scriptor.api.word.Word.COSTTYPE
import com.ssblur.scriptor.api.word.Word.Cost
import com.ssblur.scriptor.effect.ScriptorEffects
import com.ssblur.scriptor.effect.ScriptorEffects.UNARMOURED_OATH_EFFECT
import com.ssblur.scriptor.registry.words.WordRegistry.register
import com.ssblur.scriptor.word.action.oaths.OathAction
import com.ssblur.scriptor.word.action.potions.*
import net.minecraft.world.effect.MobEffects

@Suppress("unused")
object OathActions {
  val UNARMOURED_OATH_ACTION = register(
    "unarmoured_oath_action",
    OathAction(
      UNARMOURED_OATH_EFFECT.ref(),
      0.0,
      Cost(1.0, COSTTYPE.ADDITIVE),
      null,
      "unarmoured_oath_action")
  )
}
