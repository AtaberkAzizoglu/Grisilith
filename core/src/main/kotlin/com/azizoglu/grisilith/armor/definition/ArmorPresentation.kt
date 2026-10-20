package com.azizoglu.grisilith.armor.definition

/** Presentation only: base spell protection is not the Magic Resistance stat. */
object ArmorPresentation {
    fun stats(armor:ArmorDefinition):String=buildString {
        appendLine("Base Health: ${armor.baseHealth}")
        appendLine("Base Armor: ${armor.baseArmor}")
        appendLine("Base Spell Protection: ${armor.baseMagicResistance}")
        armor.allStatModifiers().sortedBy {it.statType.displayName}.forEach {
            appendLine("${it.statType.displayName}: ${if(it.amount>0) "+" else ""}${it.amount}")
        }
    }.trimEnd()
    fun hover(armor:ArmorDefinition):String=stats(armor)
}
