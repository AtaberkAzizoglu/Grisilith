package com.azizoglu.grisilith.ui.stat

import com.azizoglu.grisilith.statistics.StatType
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.Align
import java.util.EnumMap

/** Shared stat icon and label presentation used by menus and run UI. */
object StatSymbolRenderer {

    private const val ASSET_ROOT = "ui/stat_symbols"

    private val assetNames = mapOf(
        StatType.MAX_HP to "stat_max_hp.png",
        StatType.ARMOR_CLASS to "stat_armor_class.png",
        StatType.STRENGTH to "stat_strength.png",
        StatType.DEXTERITY to "stat_dexterity.png",
        StatType.WISDOM to "stat_wisdom.png",
        StatType.HASTE to "stat_haste.png",
        StatType.ATTACK_SPEED to "stat_attack_speed.png",
        StatType.CRIT_RATE to "stat_crit_rate.png",
        StatType.CRIT_DAMAGE to "stat_crit_damage.png",
        StatType.MOVEMENT_SPEED to "stat_movement_speed.png",
        StatType.LUCK to "stat_luck.png",
        StatType.HARVEST to "stat_harvest.png",
        StatType.ELEMENTAL_DAMAGE to "stat_elemental_damage.png",
        StatType.MIGHT_OF_NATURE to "stat_might_of_nature.png",
        StatType.SHEPHERD to "stat_shepherd.png",
        StatType.PIERCING to "stat_piercing.png",
        StatType.ENEMY_COUNT to "stat_enemy_count.png",
        StatType.HP_REGENERATION to "stat_hp_regeneration.png",
        StatType.LIFE_STEAL to "stat_life_steal.png",
        StatType.MAGIC_RESISTANCE to "stat_magic_resistance.png"
    )

    private val textures = EnumMap<StatType, Texture>(StatType::class.java)

    private val statNamePattern = Regex(
        StatType.alphabetical
            .sortedByDescending { it.displayName.length }
            .joinToString("|", prefix = "(", postfix = ")") { Regex.escape(it.displayName) }
    )

    fun drawIcon(
        batch: SpriteBatch,
        statType: StatType,
        x: Float,
        y: Float,
        size: Float
    ) {
        val texture = textures.getOrPut(statType) {
            Texture(Gdx.files.internal("$ASSET_ROOT/${assetNames.getValue(statType)}")).also {
                it.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
            }
        }
        val previousColor = Color(batch.color)
        batch.color = Color(1f, 1f, 1f, previousColor.a)
        batch.draw(texture, x, y, size, size)
        batch.color = previousColor
    }

    /** Draws the required red, visually bold stat name with its symbol. */
    fun drawLabel(
        batch: SpriteBatch,
        font: BitmapFont,
        statType: StatType,
        x: Float,
        baselineY: Float,
        iconSize: Float = 18f,
        width: Float = 0f,
        align: Int = Align.left
    ) {
        val previousColor = Color(font.color)
        val oldX=font.data.scaleX;val oldY=font.data.scaleY
        font.data.setScale(oldX+.10f,oldY+.10f)
        val labelX = x + iconSize + 4f
        drawIcon(batch, statType, x, baselineY - iconSize + 4f, iconSize)
        font.color = Color(0.92f, 0.18f, 0.16f, previousColor.a)
        if (width > 0f) {
            font.draw(batch, statType.displayName, labelX + 0.7f, baselineY, width - iconSize - 4f, align, false)
            font.draw(batch, statType.displayName, labelX, baselineY, width - iconSize - 4f, align, false)
        } else {
            font.draw(batch, statType.displayName, labelX + 0.7f, baselineY)
            font.draw(batch, statType.displayName, labelX, baselineY)
        }
        font.color = previousColor
        font.data.setScale(oldX,oldY)
    }

    fun drawModifiersHorizontal(
        batch: SpriteBatch,
        font: BitmapFont,
        modifiers: List<Pair<StatType, Int>>,
        x: Float,
        baselineY: Float,
        iconSize: Float = 15f,
        gap: Float = 10f
    ) {
        var cursorX = x
        modifiers.sortedBy { it.first.displayName }.forEach { (statType, amount) ->
            drawIcon(batch, statType, cursorX, baselineY - iconSize + 4f, iconSize)
            cursorX += iconSize + 3f
            font.color = Color(0.92f, 0.18f, 0.16f, 1f)
            font.draw(batch, statType.displayName, cursorX + 0.7f, baselineY)
            font.draw(batch, statType.displayName, cursorX, baselineY)
            cursorX += GlyphLayout(font, statType.displayName).width + 4f
            font.color = Color.WHITE
            val amountText = if (amount > 0) "+$amount" else amount.toString()
            font.draw(batch, amountText, cursorX, baselineY)
            cursorX += GlyphLayout(font, amountText).width + gap
        }
    }

    fun drawModifiersVertical(
        batch: SpriteBatch,
        font: BitmapFont,
        modifiers: List<Pair<StatType, Int>>,
        x: Float,
        firstBaselineY: Float,
        lineHeight: Float = 25f,
        iconSize: Float = 20f
    ) {
        modifiers.sortedBy { it.first.displayName }.forEachIndexed { index, (statType, amount) ->
            val baselineY = firstBaselineY - index * lineHeight
            drawLabel(batch, font, statType, x, baselineY, iconSize)
            font.color = Color.WHITE
            val amountText = if (amount > 0) "+$amount" else amount.toString()
            font.draw(batch, amountText, x + 180f, baselineY)
        }
    }

    /** Draws prose while replacing every exact stat name with icon + red bold text. */
    fun drawRichText(
        batch: SpriteBatch,
        font: BitmapFont,
        text: String,
        x: Float,
        firstBaselineY: Float,
        width: Float,
        lineHeight: Float = font.lineHeight * 1.15f,
        iconSize: Float = font.lineHeight * 0.9f,
        measureOnly: Boolean = false
    ): Float {
        val originalColor = Color(font.color)
        val baseX=font.data.scaleX;val baseY=font.data.scaleY
        var cursorX = x
        var baselineY = firstBaselineY

        val lexicalPattern = Regex("\\n|[^\\S\\n]+|${SemanticSymbolRenderer.lexicalFragment}|${statNamePattern.pattern}")
        var sourceIndex = 0
        val tokens = buildList {
            lexicalPattern.findAll(text).forEach { match ->
                if (match.range.first > sourceIndex) add(text.substring(sourceIndex, match.range.first))
                add(match.value)
                sourceIndex = match.range.last + 1
            }
            if (sourceIndex < text.length) add(text.substring(sourceIndex))
        }

        fun nextLine() {
            cursorX = x
            baselineY -= lineHeight
        }

        var section:String?=null
        tokens.forEach { token ->
            if (token == "\n") {
                section=null
                nextLine()
                return@forEach
            }
            SemanticSymbolRenderer.sectionFor(token)?.let{section=it}
            val decoration=SemanticSymbolRenderer.decoration(token,section)
            val statType = StatType.alphabetical.firstOrNull { it.displayName == token }
            if(decoration!=null) {
                font.data.setScale(baseX+decoration.boost,baseY+decoration.boost)
                val labelWidth=GlyphLayout(font,token).width
                val size=if(decoration.icon<0)0f else iconSize+if(decoration.kind==SemanticSymbolRenderer.Kind.HEADING)2f else 0f
                val iconGap=if(decoration.icon<0)0f else 4f
                if(cursorX>x && cursorX+size+iconGap+labelWidth>x+width)nextLine()
                if(!measureOnly) {
                    if(decoration.icon>=0)SemanticSymbolRenderer.drawIcon(batch,decoration.icon,cursorX,baselineY-size+4f,size)
                    font.color=SemanticSymbolRenderer.readableColor(decoration, originalColor)
                    font.draw(batch,token,cursorX+size+iconGap,baselineY)
                }
                cursorX+=size+iconGap+labelWidth
                font.data.setScale(baseX,baseY)
            } else if (statType != null) {
                font.data.setScale(baseX+.10f,baseY+.10f)
                val labelWidth = GlyphLayout(font, token).width
                val tokenWidth = iconSize + 4f + labelWidth
                if (cursorX > x && cursorX + tokenWidth > x + width) nextLine()
                if(!measureOnly) drawIcon(batch, statType, cursorX, baselineY - iconSize + 4f, iconSize)
                cursorX += iconSize + 4f
                font.color = Color(0.92f, 0.18f, 0.16f, 1f)
                if(!measureOnly) {
                    font.draw(batch, token, cursorX + 0.7f, baselineY)
                    font.draw(batch, token, cursorX, baselineY)
                }
                cursorX += labelWidth
                font.data.setScale(baseX,baseY)
            } else {
                val normalized = if (token.all { it.isWhitespace() }) " " else token
                val tokenWidth = GlyphLayout(font, normalized).width
                if (cursorX > x && cursorX + tokenWidth > x + width && normalized.isNotBlank()) nextLine()
                if (!(cursorX == x && normalized.isBlank())) {
                    font.color = originalColor
                    if(!measureOnly) font.draw(batch, normalized, cursorX, baselineY)
                    cursorX += tokenWidth
                }
            }
        }
        font.color = originalColor
        font.data.setScale(baseX,baseY)
        return firstBaselineY - baselineY + lineHeight
    }
}
