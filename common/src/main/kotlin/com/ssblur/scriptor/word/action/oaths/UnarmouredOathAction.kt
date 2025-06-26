package com.ssblur.scriptor.word.action.oaths

import com.ssblur.scriptor.effect.ScriptorEffects.UNARMOURED_OATH_EFFECT

class UnarmouredOathAction(registryKey: String): OathAction(UNARMOURED_OATH_EFFECT.ref(), 0.0, Cost(4.0, COSTTYPE.ADDITIVE), null, registryKey) {
}
