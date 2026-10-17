package com.azizoglu.grisilith.screen.render

import com.azizoglu.grisilith.collection.CollectionEntry
import com.azizoglu.grisilith.collection.detail.CollectionDetailRegistry
import com.azizoglu.grisilith.collection.filter.CollectionFilterOption
import com.azizoglu.grisilith.content.ContentCategory
import com.azizoglu.grisilith.ui.icon.ContentIconRenderer
import com.azizoglu.grisilith.ui.theme.ButtonVisualState
import com.azizoglu.grisilith.ui.theme.PanelVisualStyle
import com.azizoglu.grisilith.ui.theme.UiComponentRenderer
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.math.Vector2
import com.azizoglu.grisilith.character.CharacterDefinition
import com.azizoglu.grisilith.character.CharacterPresentation
import com.azizoglu.grisilith.armor.definition.ArmorDefinition

class CollectionCategoryRenderer {

    private val archiveBackground = Texture(
        Gdx.files.internal("ui/backgrounds/chronicle_archive.png")
    ).apply {
        setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
    }

    fun renderShell(
        batch: SpriteBatch,
        font: BitmapFont,
        category: ContentCategory,
        gridBounds: Rectangle,
        detailBounds: Rectangle
    ) {
        val previous = Color(batch.color)
        batch.color = Color(0.55f, 0.55f, 0.55f, 1f)
        batch.draw(archiveBackground, 0f, 0f, 1280f, 720f)
        batch.color = previous
        UiComponentRenderer.drawStonePanel(batch, Rectangle(22f, 22f, 1236f, 676f))

        val oldScaleX = font.data.scaleX
        val oldScaleY = font.data.scaleY
        val oldColor = Color(font.color)
        font.data.setScale(1.62f)
        font.color = Color(0.94f, 0.91f, 0.84f, 1f)
        com.azizoglu.grisilith.ui.ScreenTitleArt.draw(batch,categoryTitle(category),Rectangle(290f,644f,700f,44f))

        UiComponentRenderer.drawSummarySectionPanel(batch, Rectangle(gridBounds.x - 10f, gridBounds.y - 10f, gridBounds.width + 20f, gridBounds.height + 20f))
        UiComponentRenderer.drawSummarySectionPanel(batch, detailBounds)

        font.data.setScale(0.70f)
        font.color = Color(0.77f, 0.58f, 0.28f, 1f)
        font.draw(batch, "ARCHIVE ENTRIES", gridBounds.x+12f, gridBounds.y + gridBounds.height - 2f)
        font.draw(batch, "DETAILS", detailBounds.x + 18f, detailBounds.y + detailBounds.height - 19f)
        font.data.setScale(oldScaleX, oldScaleY)
        font.color = oldColor
    }

    fun renderToolbar(
        batch: SpriteBatch,
        font: BitmapFont,
        searchText: String,
        searchFocused: Boolean,
        stateLabel: String,
        sortLabel: String,
        filterLabel: String,
        searchBounds: Rectangle,
        stateBounds: Rectangle,
        sortBounds: Rectangle,
        filterBounds: Rectangle,
        hasCategoryFilters: Boolean,
        filterOpen: Boolean
    ) {
        UiComponentRenderer.drawButton(batch, searchBounds, ButtonVisualState.NORMAL)
        UiComponentRenderer.drawButton(batch, stateBounds, ButtonVisualState.NORMAL)
        UiComponentRenderer.drawButton(batch, sortBounds, ButtonVisualState.NORMAL)
        if (hasCategoryFilters) UiComponentRenderer.drawDropdown(batch, filterBounds, filterOpen)

        val oldScaleX = font.data.scaleX
        val oldScaleY = font.data.scaleY
        val oldColor = Color(font.color)
        font.data.setScale(0.76f)
        font.color = if (searchText.isBlank()) Color.GRAY else Color.WHITE
        val searchLabel = if (searchText.isBlank()) "SEARCH ARCHIVE..." else searchText + if (searchFocused) " |" else ""
        fittedText(batch,font,searchLabel,Rectangle(searchBounds.x+24f,searchBounds.y+8f,searchBounds.width-48f,searchBounds.height-16f),.86f,Align.left)
        font.color = Color(0.92f, 0.89f, 0.82f, 1f)
        fittedText(batch,font,stateLabel,Rectangle(stateBounds.x+20f,stateBounds.y+8f,stateBounds.width-40f,stateBounds.height-16f),.86f)
        fittedText(batch,font,sortLabel,Rectangle(sortBounds.x+18f,sortBounds.y+8f,sortBounds.width-36f,sortBounds.height-16f),.86f)
        if (hasCategoryFilters) {
            fittedText(batch,font,filterLabel,Rectangle(filterBounds.x+24f,filterBounds.y+8f,filterBounds.width-48f,filterBounds.height-16f),.86f)
        }
        font.data.setScale(oldScaleX, oldScaleY)
        font.color = oldColor
    }

    fun renderCards(
        batch: SpriteBatch,
        font: BitmapFont,
        category: ContentCategory,
        entries: List<CollectionEntry>,
        bounds: List<Rectangle>,
        selectedId: String?,
        hoveredId: String?,
        pressedId: String?
    ) {
        entries.forEachIndexed { index, entry ->
            val card = bounds[index]
            val id = entry.definition.id.value
            val selected = id == selectedId
            val hovered = id == hoveredId
            val plainArt=category==ContentCategory.ENEMY || category==ContentCategory.MAP || category==ContentCategory.SUMMON
            if(category!=ContentCategory.MAP) UiComponentRenderer.drawPanel(batch, card, if (selected) PanelVisualStyle.MODAL else PanelVisualStyle.INNER)

            val iconSize = if(category==ContentCategory.MAP)104f else 88f
            val iconWidth = if(category==ContentCategory.MAP)card.width-32f else iconSize
            val iconBounds = Rectangle(
                card.x + (card.width - iconWidth) / 2f,
                card.y + card.height - iconSize - 22f,
                iconWidth,
                iconSize
            )
            if(!plainArt) UiComponentRenderer.drawSlot(batch, iconBounds, selected = selected || hovered, locked = !entry.unlocked || entry.isUnknown)
            if (entry.isUnknown) {
                drawUnknown(batch, font, iconBounds)
            } else {
                val enemy=entry.definition as? com.azizoglu.grisilith.enemy.definition.EnemyKindDefinition
                val artworkBounds=if(enemy!=null) {
                    val boss=enemy.allowedTypes.any{it==com.azizoglu.grisilith.enemy.EnemyType.MINI_BOSS || it==com.azizoglu.grisilith.enemy.EnemyType.FINAL_BOSS}
                    val inset=if(boss)6f else 3f
                    Rectangle(iconBounds.x+inset,iconBounds.y+inset,iconBounds.width-2*inset,iconBounds.height-2*inset)
                }else iconBounds
                val drawn = ContentIconRenderer.draw(batch, entry.definition, artworkBounds, locked = !entry.unlocked,framed=!plainArt)
                if (!drawn) drawFallback(batch, font, entry.definition.displayName, iconBounds, !entry.unlocked)
                if(enemy!=null)ContentIconRenderer.drawEnemyArchiveFrame(batch,enemy,iconBounds)
            }

            val oldScaleX = font.data.scaleX
            val oldScaleY = font.data.scaleY
            val oldColor = Color(font.color)
            font.data.setScale(0.70f)
            font.color = if (entry.isUnknown) Color.GRAY else if(plainArt && (selected || hovered)) Color.GOLD else Color(0.94f, 0.91f, 0.84f, 1f)
            val name = if (entry.isUnknown) "UNKNOWN" else entry.definition.displayName
            fittedText(batch,font,name,Rectangle(card.x+14f,card.y+if(category==ContentCategory.MAP)27f else 35f,card.width-28f,36f),.70f,wrap=true)
            font.data.setScale(0.55f)
            font.color = statusColor(entry)
            fittedText(batch,font,metadata(entry),Rectangle(card.x+16f,card.y+16f,card.width-32f,18f),.55f)
            if (id == pressedId) {
                font.color = Color(1f, 0.82f, 0.38f, 1f)
                font.draw(batch, "SELECT", card.x + 8f, card.y + card.height - 8f, card.width - 16f, Align.center, false)
            }
            font.data.setScale(oldScaleX, oldScaleY)
            font.color = oldColor
        }
    }

    fun renderDetails(batch: SpriteBatch, font: BitmapFont, entry: CollectionEntry?, bounds: Rectangle) {
        val oldScaleX = font.data.scaleX
        val oldScaleY = font.data.scaleY
        val oldColor = Color(font.color)
        if (entry == null) {
            font.data.setScale(0.62f)
            font.color = Color.GRAY
            font.draw(batch, "NO ENTRY SELECTED", bounds.x + 24f, bounds.y + bounds.height / 2f, bounds.width - 48f, Align.center, false)
            restoreFont(font, oldScaleX, oldScaleY, oldColor)
            return
        }

        if (entry.isUnknown) {
            val icon = Rectangle(bounds.x + (bounds.width - 118f) / 2f, bounds.y + bounds.height - 166f, 118f, 118f)
            if(entry.definition.category != ContentCategory.ENEMY && entry.definition.category != ContentCategory.MAP) UiComponentRenderer.drawSlot(batch, icon, locked = true)
            drawUnknown(batch, font, icon)
            font.data.setScale(0.94f)
            font.color = Color.GRAY
            font.draw(batch, "UNKNOWN", bounds.x + 20f, bounds.y + bounds.height - 195f, bounds.width - 40f, Align.center, false)
            font.data.setScale(0.53f)
            font.draw(
                batch,
                "Encounter this entry during a run to reveal its archive record.",
                bounds.x + 28f,
                bounds.y + bounds.height - 250f,
                bounds.width - 56f,
                Align.center,
                true
            )
            restoreFont(font, oldScaleX, oldScaleY, oldColor)
            return
        }

        if(entry.definition.category == ContentCategory.MAP) {
            val art=Rectangle(bounds.x+24f,bounds.y+bounds.height-274f,bounds.width-48f,228f)
            ContentIconRenderer.draw(batch,entry.definition,art,framed=false)
            font.color=Color.WHITE
            fittedText(batch,font,entry.definition.displayName,Rectangle(bounds.x+24f,art.y-48f,bounds.width-48f,36f),1.02f)
            font.color=Color.LIGHT_GRAY
            fittedText(batch,font,entry.definition.description,Rectangle(bounds.x+24f,bounds.y+26f,bounds.width-48f,art.y-bounds.y-88f),.84f,Align.left,true,topAligned=true)
            restoreFont(font,oldScaleX,oldScaleY,oldColor)
            return
        }
        if(entry.definition.category == ContentCategory.SUMMON) {
            val art = Rectangle(bounds.x+24f,bounds.y+bounds.height-148f,112f,112f)
            ContentIconRenderer.draw(batch,entry.definition,art)
            font.color=Color.GOLD
            fittedText(batch,font,entry.definition.displayName,Rectangle(bounds.x+154f,art.y+24f,bounds.width-178f,56f),1.02f,Align.left,true)
            font.color=Color.LIGHT_GRAY
            fittedText(batch,font,entry.definition.description,Rectangle(bounds.x+24f,bounds.y+24f,bounds.width-48f,bounds.height-192f),.80f,Align.left,true,topAligned=true)
            restoreFont(font,oldScaleX,oldScaleY,oldColor)
            return
        }
        if(entry.definition is com.azizoglu.grisilith.weapon.definition.WeaponDefinition) {
            WeaponDetailPanel.render(batch,font,entry.definition,bounds)
            restoreFont(font,oldScaleX,oldScaleY,oldColor);return
        }

        val icon = Rectangle(bounds.x + 24f, bounds.y + bounds.height - 118f, 80f, 80f)
        val plainArt=entry.definition.category==ContentCategory.ENEMY || entry.definition.category==ContentCategory.SUMMON
        if(!plainArt) UiComponentRenderer.drawSlot(batch, icon, locked = !entry.unlocked)
        if (!ContentIconRenderer.draw(batch, entry.definition, icon, locked = !entry.unlocked,framed=!plainArt)) {
            drawFallback(batch, font, entry.definition.displayName, icon, !entry.unlocked)
        }

        font.data.setScale(1.02f)
        font.color = Color(0.94f, 0.91f, 0.84f, 1f)
        fittedText(batch,font,entry.definition.displayName,Rectangle(bounds.x+118f,bounds.y+bounds.height-84f,bounds.width-142f,44f),1.02f,Align.left,true)
        font.data.setScale(0.64f)
        font.color = statusColor(entry)
        font.draw(batch, if (entry.unlocked) "UNLOCKED" else "ENCOUNTERED - LOCKED", bounds.x+118f,bounds.y+bounds.height-103f,bounds.width-142f,Align.left,false)

        val detail = CollectionDetailRegistry.find(entry.definition.id)
        font.data.setScale(0.76f)
        font.color = Color(0.88f, 0.87f, 0.84f, 1f)
        val character=entry.definition as? CharacterDefinition
        if(character!=null) {
            val traitText="${character.trait.displayName}: ${character.trait.description}"
            font.data.setScale(.73f)
            val traitHeight=com.azizoglu.grisilith.ui.stat.StatSymbolRenderer.drawRichText(batch,font,traitText,0f,0f,bounds.width-48f,measureOnly=true)
            font.data.setScale(.66f)
            val mainText=CharacterPresentation.mainStatsText(character)
            val mainHeight=com.azizoglu.grisilith.ui.stat.StatSymbolRenderer.drawRichText(batch,font,mainText,0f,0f,bounds.width-48f,measureOnly=true)
            val statsPanel=characterStatsBounds(bounds)
            val mainTop=statsPanel.y+statsPanel.height+mainHeight+16f
            val traitTop=mainTop+32f+traitHeight+12f
            val traitHeading=traitTop+28f
            val storyTop=bounds.y+bounds.height-135f
            fittedText(batch,font,character.description,Rectangle(bounds.x+24f,traitHeading+16f,bounds.width-48f,storyTop-traitHeading-16f),.76f,Align.left,true,topAligned=true)
            font.color=Color.GOLD;font.data.setScale(.94f)
            font.draw(batch,"Trait",bounds.x+24f,traitHeading)
            font.color=Color.LIGHT_GRAY;font.data.setScale(.73f)
            com.azizoglu.grisilith.ui.stat.StatSymbolRenderer.drawRichText(batch,font,traitText,bounds.x+24f,traitTop,bounds.width-48f)
            font.color=Color.GOLD;font.data.setScale(.78f)
            font.draw(batch,"MAIN STATS (PRIORITY ORDER)",bounds.x+24f,mainTop+27f)
            font.color=Color.LIGHT_GRAY;font.data.setScale(.66f)
            com.azizoglu.grisilith.ui.stat.StatSymbolRenderer.drawRichText(batch,font,mainText,bounds.x+24f,mainTop,bounds.width-48f)
            UiComponentRenderer.drawSummarySectionPanel(batch,statsPanel)
            font.color=Color.WHITE
            fittedText(batch,font,"BASE STATS — HOVER TO VIEW",Rectangle(statsPanel.x+16f,statsPanel.y+6f,statsPanel.width-32f,statsPanel.height-12f),.74f)
        } else {
            val rows=detail?.rows.orEmpty().take(when(entry.definition.category) {
                ContentCategory.ARMOR -> 5
                ContentCategory.TAMGA -> 2
                else -> 4
            })
            val rowHeight=if(entry.definition is ArmorDefinition)43f else 44f
            val rowTop=bounds.y+28f+rows.size*rowHeight
            fittedText(batch,font,detail?.description ?: entry.definition.description,
                Rectangle(bounds.x+24f,rowTop+18f,bounds.width-48f,bounds.height-135f-(rowTop-bounds.y)-18f),.78f,Align.left,true,topAligned=true)
            rows.forEachIndexed { index,row ->
                val y=rowTop-index*rowHeight
                font.color=Color(.80f,.67f,.41f,1f)
                val stat=com.azizoglu.grisilith.statistics.StatType.entries.firstOrNull{it.displayName.equals(row.label,true)}
                if(stat!=null) {
                    font.data.setScale(.64f)
                    com.azizoglu.grisilith.ui.stat.StatSymbolRenderer.drawLabel(batch,font,stat,bounds.x+24f,y-6f,15f)
                } else fittedText(batch,font,row.label.uppercase(),Rectangle(bounds.x+24f,y-27f,158f,32f),.70f,Align.left,true)
                font.color=Color.LIGHT_GRAY
                fittedText(batch,font,row.value,Rectangle(bounds.x+190f,y-27f,bounds.width-214f,32f),.73f,Align.left,true)
            }
        }
        restoreFont(font, oldScaleX, oldScaleY, oldColor)
    }

    fun characterStatsBounds(bounds: Rectangle)=Rectangle(bounds.x+24f,bounds.y+20f,bounds.width-48f,40f)

    fun renderDetailHover(batch:SpriteBatch,font:BitmapFont,entry:CollectionEntry?,bounds:Rectangle,pointer:Vector2) {
        if(entry==null || entry.isUnknown) return
        (entry.definition as? com.azizoglu.grisilith.weapon.definition.WeaponDefinition)?.let {
            WeaponDetailPanel.hover(batch,font,it,bounds,pointer);return
        }
        val character=entry.definition as? CharacterDefinition
        if(character!=null && characterStatsBounds(bounds).contains(pointer)) {
            CharacterHoverRenderer.draw(batch,font,pointer,character,statsOnly=true);return
        }
        val armor=entry.definition as? ArmorDefinition ?: return
        val rows=CollectionDetailRegistry.find(armor.id)?.rows.orEmpty()
        val top=bounds.y+28f+rows.size*43f
        val index=rows.indices.firstOrNull { Rectangle(bounds.x+24f,top-it*43f-27f,bounds.width-48f,32f).contains(pointer) } ?: return
        val title=rows[index].label
        val copy=when(title) {
            "Base Health" -> "Maximum HP = Armor's Base Health × Max HP stat.\n\nFor this armor: ${armor.baseHealth} × Max HP stat. Item effects can further modify the result; maximum HP is rounded and cannot fall below 1."
            "Base Armor" -> "Physical damage reduction = Armor's Base Armor × Armor Class stat.\n\nFor this armor: ${armor.baseArmor} × Armor Class stat. Incoming Piercing reduces this protection."
            "Magic Resistance" -> "Magical damage reduction = Armor's Base Magic Resistance × Magic Resistance stat.\n\nFor this armor: ${armor.baseMagicResistance} × Magic Resistance stat. Piercing does not bypass magical protection."
            else -> return
        }
        AdaptiveTooltipRenderer.draw(batch,font,Rectangle(0f,0f,1280f,720f),pointer,title,copy,520f,.90f,.76f)
    }

    private fun fittedText(batch:SpriteBatch,font:BitmapFont,text:String,bounds:Rectangle,scale:Float,align:Int=Align.center,wrap:Boolean=false,topAligned:Boolean=false) {
        var size=scale
        var layout:GlyphLayout
        do {
            font.data.setScale(size)
            layout=GlyphLayout(font,text,font.color,bounds.width,align,wrap)
            val richHeight=if(wrap && align!=Align.center)com.azizoglu.grisilith.ui.stat.StatSymbolRenderer.drawRichText(batch,font,text,0f,0f,bounds.width,measureOnly=true) else layout.height
            if(layout.width<=bounds.width+.5f && richHeight<=bounds.height+.5f) break
            size-=.01f
        } while(size>.45f)
        val y=if(topAligned)bounds.y+bounds.height else bounds.y+(bounds.height+layout.height)/2f
        if(wrap && align!=Align.center)com.azizoglu.grisilith.ui.stat.StatSymbolRenderer.drawRichText(batch,font,text,bounds.x,y,bounds.width)
        else font.draw(batch,text,bounds.x,y,bounds.width,align,wrap)
    }

    fun renderFilterDropdown(
        batch: SpriteBatch,
        font: BitmapFont,
        options: List<CollectionFilterOption>,
        optionBounds: List<Rectangle>,
        selectedIds: Set<String>
    ) {
        if (optionBounds.isEmpty()) return
        val panel = Rectangle(
            optionBounds.first().x - 5f,
            optionBounds.last().y - 5f,
            optionBounds.first().width + 10f,
            optionBounds.first().y + optionBounds.first().height - optionBounds.last().y + 10f
        )
        UiComponentRenderer.drawPanel(batch, panel, PanelVisualStyle.TOOLTIP)
        val oldScaleX = font.data.scaleX
        val oldScaleY = font.data.scaleY
        val oldColor = Color(font.color)
        optionBounds.forEachIndexed { index, bounds ->
            val option = options[index]
            val selected = option.id in selectedIds
            UiComponentRenderer.drawDropdownRow(batch, bounds, selected)
            UiComponentRenderer.drawCheckbox(batch, Rectangle(bounds.x + 12f, bounds.y + (bounds.height-26f)/2f, 26f, 26f), selected)
            font.data.setScale(0.74f)
            font.color = if (selected) option.textColor else Color.LIGHT_GRAY
            fittedText(batch,font,option.displayName,Rectangle(bounds.x+48f,bounds.y+7f,bounds.width-65f,bounds.height-14f),.84f,Align.left)
        }
        restoreFont(font, oldScaleX, oldScaleY, oldColor)
    }

    private fun drawUnknown(batch: SpriteBatch, font: BitmapFont, bounds: Rectangle) {
        val oldScaleX = font.data.scaleX
        val oldScaleY = font.data.scaleY
        val oldColor = Color(font.color)
        font.data.setScale(1.45f)
        font.color = Color(0.35f, 0.35f, 0.37f, 1f)
        font.draw(batch, "?", bounds.x, bounds.y + bounds.height * 0.67f, bounds.width, Align.center, false)
        restoreFont(font, oldScaleX, oldScaleY, oldColor)
    }

    private fun drawFallback(batch: SpriteBatch, font: BitmapFont, name: String, bounds: Rectangle, locked: Boolean) {
        val oldScaleX = font.data.scaleX
        val oldScaleY = font.data.scaleY
        val oldColor = Color(font.color)
        font.data.setScale(1.05f)
        font.color = if (locked) Color.DARK_GRAY else Color(0.77f, 0.58f, 0.28f, 1f)
        font.draw(batch, name.take(1).uppercase(), bounds.x, bounds.y + bounds.height * 0.64f, bounds.width, Align.center, false)
        restoreFont(font, oldScaleX, oldScaleY, oldColor)
    }

    private fun metadata(entry: CollectionEntry): String = when {
        entry.isUnknown -> "UNDISCOVERED"
        entry.definition is com.azizoglu.grisilith.summon.SummonCollectionDefinition ->
            if(entry.definition.type.isFriendlyToPlayer) "ALLY" else "NEUTRAL"
        entry.definition is com.azizoglu.grisilith.enemy.definition.EnemyKindDefinition -> entry.definition.enemyTags.joinToString(" / ") { if(it.name=="MONSTER")"MONSTROSITY" else it.name }
        entry.unlocked -> entry.definition.tags.firstOrNull()?.name?.replace('_', ' ') ?: "UNLOCKED"
        else -> "LOCKED"
    }

    private fun statusColor(entry: CollectionEntry): Color = when {
        entry.isUnknown -> Color.GRAY
        entry.unlocked -> Color(0.42f, 0.78f, 0.40f, 1f)
        else -> Color(0.78f, 0.48f, 0.28f, 1f)
    }

    private fun categoryTitle(category: ContentCategory): String = when (category) {
        ContentCategory.CHARACTER -> "CHARACTER ARCHIVE"
        ContentCategory.WEAPON -> "WEAPON ARCHIVE"
        ContentCategory.ARMOR -> "ARMOR ARCHIVE"
        ContentCategory.ITEM -> "ITEM ARCHIVE"
        ContentCategory.TAMGA -> "TAMGA ARCHIVE"
        ContentCategory.SUMMON -> "SUMMON ARCHIVE"
        ContentCategory.ENEMY -> "BESTIARY"
        ContentCategory.MAP -> "MAP ARCHIVE"
    }

    private fun restoreFont(font: BitmapFont, x: Float, y: Float, color: Color) {
        font.data.setScale(x, y)
        font.color = color
    }

    fun dispose() {
        archiveBackground.dispose()
    }
}
