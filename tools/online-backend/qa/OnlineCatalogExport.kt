package qa

import com.azizoglu.grisilith.content.DefaultContentRegistry
import com.azizoglu.grisilith.content.ContentCategory
import com.azizoglu.grisilith.item.definition.ItemDefinition
import com.azizoglu.grisilith.preset.PresetItemRules

object OnlineCatalogExport {
    @JvmStatic fun main(args: Array<String>) {
        val categories = mapOf(ContentCategory.WEAPON to "weapon", ContentCategory.ARMOR to "armor",
            ContentCategory.ITEM to "item", ContentCategory.TAMGA to "tamga", ContentCategory.CHARACTER to "character")
        println("-- Generated from the v0.3 content registry; stable IDs only.")
        println("begin;")
        println("insert into grisilith_private.content_catalog(content_id,category,starting_allowed) values")
        println(DefaultContentRegistry.instance.allDefinitions.filter { it.category in categories }
            .sortedBy { it.id.value }.joinToString(",\n", postfix = ";") {
                "('${it.id.value}','${categories.getValue(it.category)}',${it !is ItemDefinition || PresetItemRules.canStartWith(it)})"
            })
        println("commit;")
    }
}
