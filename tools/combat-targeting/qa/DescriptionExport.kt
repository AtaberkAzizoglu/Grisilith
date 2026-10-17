package qa

import com.azizoglu.grisilith.content.*
import com.azizoglu.grisilith.character.DefaultCharacters
import com.azizoglu.grisilith.collection.detail.DefaultCollectionDetails
import com.azizoglu.grisilith.config.GameMap
import com.azizoglu.grisilith.statistics.StatType
import com.azizoglu.grisilith.statistics.StatDetailPresentation
import com.azizoglu.grisilith.settings.*
import com.azizoglu.grisilith.unlock.UnlockRegistry
import com.azizoglu.grisilith.weapon.glyph.ChalkDustSpellChoice
import com.azizoglu.grisilith.weapon.summon.IvySummonChoice
import java.io.File

/** Exports the actual runtime catalog, including generated Collection/Merchant variants. */
object DescriptionExport {
    @JvmStatic fun main(args: Array<String>) {
        val content = DefaultContentRegistry.instance.allDefinitions
        val details = DefaultCollectionDetails.all.associateBy { it.contentId }
        val text = buildString {
            appendLine("# Faz 7f — Mevcut oyun açıklamaları")
            appendLine()
            appendLine("Bu dosya oyunda bulunan İngilizce metinleri değiştirmeden aktarır. Başlıkların altındaki açıklamaları düzenleyebilirsin; kimlikleri koruman hangi içeriği değiştirdiğini eşleştirmemi sağlar. Collection bilgi satırları ve seviye notları da dahil edilmiştir. Sayısal oyuncu değerleri, sahip olunan adetler ve ilerleme miktarları oyun sırasında hesaplandığından burada canlı bir kayıt olarak verilmez.")
            appendLine()
            appendLine("Kapsam: ${content.size} kayıtlı içerik, karakter özellikleri ve yardımcıları, Chalk büyüleri, Ivy summon seçimleri, 4 harita, ${StatType.entries.size} stat, büyü reaksiyonları, Merchant metinleri ve Combat ayar açıklamaları. Salt buton etiketleri, hata/onay mesajları açıklama kataloğuna dahil değildir.")
            appendLine()
            fun block(title: String, body: String) {
                appendLine("#### $title"); appendLine(); appendLine(body); appendLine()
            }
            val categoryNames = mapOf("CHARACTER" to "Karakterler", "WEAPON" to "Silahlar", "ARMOR" to "Zırhlar", "ITEM" to "Item'ler", "TAMGA" to "Tamga'lar", "ENEMY" to "Düşmanlar", "MAP" to "Collection haritaları")
            content.groupBy { it.category }.forEach { (category, definitions) ->
                appendLine("## ${categoryNames[category.name] ?: category.name}"); appendLine()
                definitions.sortedBy { it.displayName }.forEach { definition ->
                    appendLine("### ${definition.displayName}"); appendLine()
                    appendLine("Kimlik: `${definition.id.value}`"); appendLine()
                    block("Ana açıklama", definition.description)
                    if(definition is com.azizoglu.grisilith.weapon.definition.WeaponDefinition) {
                        val p=com.azizoglu.grisilith.weapon.definition.WeaponPresentation
                        block("Weapon — kısa hover",p.hover(definition))
                        block("Weapon — normal saldırı",p.normal(definition))
                        block("Weapon — ability",p.ability(definition)+"\n"+p.timing(definition))
                        block("Weapon — base stats",p.baseStats(definition))
                        block("Weapon — scaling",p.scaling(definition))
                        block("Weapon — level upgrades",p.upgrades(definition))
                        if(p.hasChoices(definition))block("Weapon — spells / summons",p.choices(definition))
                    }
                    details[definition.id]?.let { detail ->
                        if (detail.description != definition.description) block("Collection açıklaması", detail.description)
                        if (detail.rows.isNotEmpty()) {
                            appendLine("#### Collection bilgi satırları"); appendLine()
                            detail.rows.forEach { appendLine("- ${it.label}: ${it.value}") }; appendLine()
                        }
                        if (detail.notes.isNotEmpty()) block("Collection ek notları / seviye geliştirmeleri", detail.notes.joinToString("\n\n"))
                    }
                    DefaultCharacters.all.firstOrNull { it.id == definition.id }?.let { character ->
                        block("Collection / preset kısa hover açıklaması", com.azizoglu.grisilith.character.CharacterPresentation.summary(character))
                        block("Karakter özelliği — ${character.trait.displayName}", character.trait.description)
                    }
                }
            }
            appendLine("## Chalk Dust — büyü seçimleri"); appendLine()
            ChalkDustSpellChoice.entries.forEach { appendLine("### ${it.displayName} (`${it.name}`)\n\n${it.description}\n") }
            appendLine("## Ivy Staff — summon seçimleri"); appendLine()
            IvySummonChoice.entries.forEach { appendLine("### ${it.displayName} (`${it.name}`)\n\n${it.description}\n") }
            appendLine("## Play — harita hikâyeleri"); appendLine()
            GameMap.entries.forEach { appendLine("### ${it.displayName} (`${it.name}`)\n\n${it.description}\n") }
            appendLine("## Stat açıklamaları"); appendLine()
            StatType.alphabetical.forEach { appendLine("### ${it.displayName} (`${it.name}`)\n\n${StatDetailPresentation.description(it)}\n") }
            appendLine("## Büyü reaksiyonları"); appendLine()
            DefaultSpellReactions.all.forEach { appendLine("### ${it.displayName} (`${it.type.name}`)\n\n${it.description}\n") }
            appendLine("## Merchant — satın alma açıklamaları"); appendLine()
            UnlockRegistry.all().forEach { appendLine("### ${it.displayName} (`${it.id}`)\n\n${it.description}\n") }
            appendLine("## Combat ayarları — seçenek bazında hover açıklamaları"); appendLine()
            appendLine("### Targeting\n")
            GameSettings.Targeting.entries.forEach { block(it.label, CombatSettingDescriptions.targeting(it)) }
            appendLine("### Ability Targeting\n")
            GameSettings.AbilityCasting.entries.forEach { block(it.label, CombatSettingDescriptions.casting(it)) }
            appendLine("### Normal Attack Range\n")
            GameSettings.NormalRange.entries.forEach { block(it.label, CombatSettingDescriptions.normalRange(it)) }
            appendLine("### Ability Range\n")
            GameSettings.AbilityRange.entries.forEach { block(it.label, CombatSettingDescriptions.abilityRange(it)) }
            appendLine("## Ayrı açıklama metni bulunmayan içerikler\n")
            appendLine("Condition türlerinin ayrı bir yazılı açıklama alanı yoktur; etkileri oyun mantığında tanımlıdır. Yeni açıklama yazmak istersen mevcut adları şunlardır:\n")
            ConditionType.entries.forEach { appendLine("- ${it.displayName} (`${it.name}`)") }
            appendLine()
            appendLine("Silah ability'lerinin ayrı bir genel açıklama alanı bulunmaz; mevcut isimleri, cooldown bilgileri ve geliştirme notları yukarıdaki silahların Collection satırlarında yer alır. Summon seçimleri ve karakter yardımcıları için mevcut açıklamalar yukarıdadır; her summon türü için ayrı bir açıklama kataloğu yoktur.")
        }
        val output = File("../docs", args.firstOrNull() ?: "Faz-7f-Mevcut-Aciklamalar.md")
        output.parentFile.mkdirs()
        output.writeText(text, Charsets.UTF_8)
        check(content.all { text.contains("`${it.id.value}`") })
        println("DESCRIPTION EXPORT: ${content.size} content entries, ${text.length} characters -> ${output.canonicalPath}")
    }
}
