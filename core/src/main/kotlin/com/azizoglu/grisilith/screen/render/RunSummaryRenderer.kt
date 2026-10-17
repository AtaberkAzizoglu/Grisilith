package com.azizoglu.grisilith.screen.render

import com.azizoglu.grisilith.content.ContentCategory
import com.azizoglu.grisilith.content.DamageType
import com.azizoglu.grisilith.statistics.career.HighScoreRepository
import com.azizoglu.grisilith.summary.RunSummarySnapshot
import com.azizoglu.grisilith.ui.icon.ContentIconRenderer
import com.azizoglu.grisilith.ui.theme.ButtonVisualState
import com.azizoglu.grisilith.ui.theme.UiComponentRenderer
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.viewport.Viewport
import java.util.Locale

class RunSummaryRenderer(
    private val camera: OrthographicCamera,
    private val cameraWidth: Float,
    private val cameraHeight: Float,
    private val viewport: Viewport,
    private val shapeRenderer: ShapeRenderer,
    private val batch: SpriteBatch,
    private val font: BitmapFont,
    private val titleFont: BitmapFont
) {
    companion object {
        // Run-sonu akış süreleri. Bu üç değer doğrudan buradan ayarlanabilir.
        const val OUTCOME_FADE_DURATION = 0.35f
        const val OUTCOME_DISPLAY_DURATION = 2.00f
        const val SUMMARY_FADE_DURATION = 2.00f
        const val SUMMARY_REVEAL_START = OUTCOME_DISPLAY_DURATION
        const val TRANSITION_DURATION = SUMMARY_REVEAL_START + SUMMARY_FADE_DURATION
    }

    private val victoryArtwork by lazy { texture("ui/run_summary/victory_emblem.png") }
    private val defeatArtwork by lazy { texture("ui/run_summary/defeat_emblem.png") }
    private val victoryBackground by lazy { texture("ui/run_summary/victory_background.png") }
    private val defeatBackground by lazy { texture("ui/run_summary/defeat_background.png") }
    private val grayFilter by lazy {
        val pixmap = Pixmap(1, 1, Pixmap.Format.RGBA8888)
        pixmap.setColor(Color.WHITE)
        pixmap.fill()
        Texture(pixmap).also { pixmap.dispose() }
    }
    private val pointer = Vector2()
    private val hoverTargets = mutableListOf<HoverTarget>()
    private var itemScrollOffset = 0f
    private var renderAlpha = 1f
    private var scoreElapsedSeconds = Float.POSITIVE_INFINITY

    data class Layout(
        val outerPanel: Rectangle,
        val leftColumn: Rectangle,
        val centerColumn: Rectangle,
        val rightColumn: Rectangle,
        val resultArtwork: Rectangle,
        val portrait: Rectangle,
        val scoreCard: Rectangle,
        val restartButton: Rectangle,
        val mainMenuButton: Rectangle
    )

    private val layout = Layout(Rectangle(), Rectangle(), Rectangle(), Rectangle(), Rectangle(), Rectangle(), Rectangle(), Rectangle(), Rectangle())

    fun currentLayout(): Layout {
        updateLayout()
        return layout
    }

    fun renderShapes(snapshot: RunSummarySnapshot) = Unit

    fun renderTexts(snapshot: RunSummarySnapshot, alpha: Float = 1f,
                    scoreElapsedSeconds: Float = Float.POSITIVE_INFINITY) {
        this.scoreElapsedSeconds=scoreElapsedSeconds
        updateLayout()
        updatePointer()
        hoverTargets.clear()
        renderAlpha = alpha.coerceIn(0f, 1f)
        val oldScaleX = font.data.scaleX
        val oldScaleY = font.data.scaleY
        val oldColor = Color(font.color)
        val oldBatchColor = Color(batch.color)
        batch.color = Color(1f, 1f, 1f, renderAlpha)

        UiComponentRenderer.withOpacity(renderAlpha) {
            drawBackground(if (snapshot.wasSuccessful) victoryBackground else defeatBackground, renderAlpha)
            drawSectionPanels()
            UiComponentRenderer.drawButton(batch, layout.restartButton, buttonState(layout.restartButton))
            UiComponentRenderer.drawButton(batch, layout.mainMenuButton, buttonState(layout.mainMenuButton))
            renderStatistics(snapshot)
            renderBuild(snapshot)
            renderButtonTexts()
        }
        // The outcome stays visually anchored while the rest of Summary fades in.
        drawOutcomeAnchor(snapshot, 1f)

        font.data.setScale(oldScaleX, oldScaleY)
        font.color = oldColor
        batch.color = oldBatchColor
    }

    fun renderTransitionOverlay(snapshot: RunSummarySnapshot, elapsed: Float) {
        updateLayout()
        if (elapsed >= SUMMARY_REVEAL_START) return
        val titleAlpha = (elapsed / OUTCOME_FADE_DURATION).coerceIn(0f, 1f)
        batch.projectionMatrix = camera.combined
        batch.begin()
        drawOutcomeAnchor(snapshot, titleAlpha)
        batch.end()
    }

    private fun drawBackground(image: Texture, alpha: Float = 1f) {
        val previous = Color(batch.color)
        batch.color = Color(1f, 1f, 1f, alpha)
        batch.draw(image, camera.position.x - cameraWidth / 2f, camera.position.y - cameraHeight / 2f, cameraWidth, cameraHeight)
        batch.color = Color(0.28f, 0.30f, 0.32f, 0.56f * alpha)
        batch.draw(grayFilter, camera.position.x - cameraWidth / 2f, camera.position.y - cameraHeight / 2f, cameraWidth, cameraHeight)
        batch.color = previous
    }

    private fun drawSectionPanels() {
        val bottom = screenBottom()
        UiComponentRenderer.drawSummarySectionPanel(batch, Rectangle(layout.leftColumn.x, bottom + 16f, layout.leftColumn.width, 618f))
        UiComponentRenderer.drawSummarySectionPanel(batch, Rectangle(layout.centerColumn.x + 6f, bottom + 76f, layout.centerColumn.width - 12f, 390f))
        UiComponentRenderer.drawSummarySectionPanel(batch, Rectangle(layout.rightColumn.x, bottom + 16f, layout.rightColumn.width, 618f))
    }

    private fun drawOutcomeAnchor(snapshot: RunSummarySnapshot, alpha: Float) {
        val bottom = screenBottom()
        val oldColor = Color(titleFont.color)
        val oldScaleX = titleFont.data.scaleX
        val oldScaleY = titleFont.data.scaleY
        titleFont.data.setScale(1.42f)
        titleFont.color = Color(resultAccent(snapshot.wasSuccessful)).also { it.a = alpha }
        titleFont.draw(batch, if (snapshot.wasSuccessful) "VICTORY" else "DEFEAT", layout.centerColumn.x, bottom + 665f, layout.centerColumn.width, Align.center, false)
        drawArtwork(snapshot.wasSuccessful, alpha)
        titleFont.data.setScale(oldScaleX, oldScaleY)
        titleFont.color = oldColor
    }

    private fun renderStatistics(snapshot: RunSummarySnapshot) {
        val bottom = screenBottom()
        font.data.setScale(1.10f)
        font.color = faded(Color.WHITE)
        font.draw(batch, "RUN STATISTICS", layout.leftColumn.x, bottom + 620f, layout.leftColumn.width, Align.center, false)
        UiComponentRenderer.drawHorizontalSeparator(batch, Rectangle(layout.leftColumn.x + 8f, bottom + 592f, layout.leftColumn.width - 16f, 7f))
        val physicalDamageTypes = setOf(DamageType.SLASHING, DamageType.PIERCING, DamageType.BLUDGEONING)
        val physicalDamage = snapshot.damageDealtByType
            .filterKeys { it in physicalDamageTypes }
            .values.sum()
        val magicalDamage = snapshot.damageDealtByType
            .filterKeys { it != DamageType.NONE && it !in physicalDamageTypes }
            .values.sum()
        val rows = listOf(
            SummaryRow("Difficulty", snapshot.difficultyLabel),
            SummaryRow("Map", snapshot.mapName),
            SummaryRow("Stage Reached", snapshot.stageReached.toString()),
            SummaryRow("Run Time", snapshot.formattedRunTime, isRunTimeRecord(snapshot)),
            SummaryRow("Enemies Killed", formatInt(snapshot.enemiesKilled)),
            SummaryRow("Damage Dealt", formatNumber(snapshot.totalDamageDealt), isDamageRecord(snapshot)),
            SummaryRow(
                "Physical Damage Dealt",
                formatNumber(physicalDamage),
                tooltipDescription = damageBreakdown(snapshot.damageDealtByType) { it in physicalDamageTypes }
            ),
            SummaryRow(
                "Magical Damage Dealt",
                formatNumber(magicalDamage),
                tooltipDescription = damageBreakdown(snapshot.damageDealtByType) {
                    it != DamageType.NONE && it !in physicalDamageTypes
                }
            ),
            SummaryRow(
                "Damage Taken",
                formatNumber(snapshot.totalDamageTaken),
                isDamageTakenRecord(snapshot),
                damageBreakdown(snapshot.damageTakenByType)
            ),
            SummaryRow(
                "Damage Reduced",
                formatNumber(snapshot.totalDamageReduced),
                tooltipDescription = damageBreakdown(snapshot.damageReducedByType)
            ),
            SummaryRow("Gold Collected", formatNumber(snapshot.goldCollected), isGoldRecord(snapshot)),
            SummaryRow("Grisilith Coins", snapshot.grisilithCoinsEarned.toString(), isGrisilithRecord(snapshot)),
            SummaryRow("Chests Opened", snapshot.chestsOpened.toString())
        )
        var y = bottom + 566f
        rows.forEach { row ->
            font.data.setScale(0.82f)
            font.color = faded(Color(0.82f, 0.84f, 0.88f, 1f))
            font.draw(batch, row.label, layout.leftColumn.x + 10f, y, 228f, Align.left, false)
            font.data.setScale(if (row.record) 0.92f else 0.82f)
            font.color = faded(if (row.record) Color(0.98f, 0.16f, 0.13f, 1f) else Color.WHITE)
            font.draw(batch, row.value, layout.leftColumn.x + 236f, y, layout.leftColumn.width - 246f, Align.right, false)
            row.tooltipDescription?.takeIf { it.isNotBlank() }?.let { description ->
                hoverTargets += HoverTarget(
                    Rectangle(layout.leftColumn.x + 6f, y - 23f, layout.leftColumn.width - 12f, 27f),
                    row.label,
                    description
                )
            }
            y -= 26f
        }
        ContentIconRenderer.drawCharacterPortrait(batch, snapshot.characterName, layout.portrait)
        font.data.setScale(0.69f)
        font.color = faded(Color.CYAN)
        font.draw(batch, snapshot.characterName, layout.portrait.x - 8f, layout.portrait.y + layout.portrait.height + 17f, layout.portrait.width + 16f, Align.center, false)
        renderScore(snapshot)
    }

    private fun renderScore(snapshot: RunSummarySnapshot) {
        val card=layout.scoreCard
        UiComponentRenderer.drawPanel(batch, card, com.azizoglu.grisilith.ui.theme.PanelVisualStyle.HORIZONTAL)
        UiComponentRenderer.drawHorizontalSeparator(batch, Rectangle(card.x + 8f, card.y + card.height - 40f, card.width - 16f, 7f))
        font.data.setScale(0.67f)
        font.color = faded(Color.LIGHT_GRAY)
        font.draw(batch, "SCORE", card.x, card.y + card.height - 25f, card.width, Align.center, false)
        val frame=com.azizoglu.grisilith.summary.ScoreReveal.sample(
            snapshot.scoreBeforeStageBonus,snapshot.score,scoreElapsedSeconds)
        val text=String.format(Locale.US,"%,d",frame.value)
        val inset=26f
        val valueWidth=card.width-2f*inset
        val numberScale=1.45f+frame.pulse*.08f
        font.data.setScale(numberScale)
        val naturalWidth=com.badlogic.gdx.graphics.g2d.GlyphLayout(font,text).width
        if(naturalWidth>valueWidth)font.data.setScale(numberScale*(valueWidth-2f)/naturalWidth)
        font.color = faded(if(frame.progress>0f && frame.progress<1f)Color(1f,.94f,.65f,1f) else Color.GOLD)
        font.draw(batch,text,card.x+inset,card.y+72f+frame.pulse*2f,valueWidth,Align.center,false)
        if(frame.multiplierAlpha>0f) {
            font.data.setScale(.67f+frame.pulse*.05f)
            font.color=faded(Color(.96f,.82f,.48f,frame.multiplierAlpha))
            val multiplier=String.format(Locale.US,"STAGE ×%.1f",snapshot.scoreStageMultiplier)
            font.draw(batch,multiplier,card.x+inset,card.y+42f+frame.pulse*3f,valueWidth,Align.center,false)
        }
        if(snapshot.scoreDifficultyMultiplier!=1f) {
            font.data.setScale(.42f);font.color=faded(Color.LIGHT_GRAY)
            val difficulty=String.format(Locale.US,"DIFFICULTY ×%.2f",snapshot.scoreDifficultyMultiplier)
            font.draw(batch,difficulty,card.x+inset,card.y+22f,valueWidth,Align.center,false)
        }
    }

    private fun drawArtwork(successful: Boolean, alpha: Float) {
        val image = if (successful) victoryArtwork else defeatArtwork
        val bounds = layout.resultArtwork
        val scale = minOf(bounds.width / image.width, bounds.height / image.height)
        val width = image.width * scale
        val height = image.height * scale
        val previous = Color(batch.color)
        batch.color = Color(1f, 1f, 1f, alpha)
        batch.draw(image, bounds.x + (bounds.width - width) / 2f, bounds.y + (bounds.height - height) / 2f, width, height)
        batch.color = previous
    }

    private fun renderBuild(snapshot: RunSummarySnapshot) {
        val bottom = screenBottom()
        val panel = layout.rightColumn
        val slotSize = 64f
        font.data.setScale(1.10f)
        font.color = faded(Color.WHITE)
        font.draw(batch, "BUILD", panel.x, bottom + 620f, panel.width, Align.center, false)
        sectionTitle("WEAPONS", bottom + 592f)
        val cellWidth = (panel.width - 24f) / 3f
        val strongest = snapshot.weapons.maxByOrNull { it.damageDealt }
        repeat(6) { index ->
            val column = index % 3
            val row = index / 3
            val cellX = panel.x + 12f + column * cellWidth
            val slot = Rectangle(cellX + (cellWidth - slotSize) / 2f, bottom + 500f - row * 108f, slotSize, slotSize)
            val weapon = snapshot.weapons.getOrNull(index)
            UiComponentRenderer.drawSlot(batch, slot, locked = weapon == null)
            if (weapon != null) {
                ContentIconRenderer.draw(batch, weapon.definitionId, ContentCategory.WEAPON, inset(slot, 3f))
                font.data.setScale(0.76f)
                font.color = faded(if (weapon === strongest && weapon.damageDealt > 0f) Color(0.98f, 0.16f, 0.13f, 1f) else Color.WHITE)
                val nameLayout = font.draw(batch, weapon.displayName, cellX, slot.y - 6f, cellWidth, Align.center, true)
                font.data.setScale(0.76f)
                font.color = faded(Color.LIGHT_GRAY)
                font.draw(batch, "${formatNumber(weapon.damageDealt)} dmg", cellX, slot.y - 11f - nameLayout.height, cellWidth, Align.center, false)
                hoverTargets += HoverTarget(Rectangle(slot.x, slot.y - 43f, slot.width, slot.height + 43f), weapon.displayName, weapon.description)
            }
        }
        sectionTitle("EQUIPMENTS", bottom + 350f)
        renderEquipment(snapshot, bottom + 278f, slotSize)
        sectionTitle("ITEMS", bottom + 214f)
        renderItems(snapshot, itemViewport(), slotSize)
    }

    private fun sectionTitle(text: String, y: Float) {
        font.data.setScale(0.76f)
        font.color = faded(Color.GOLD)
        font.draw(batch, text, layout.rightColumn.x + 12f, y)
    }

    private fun renderEquipment(snapshot: RunSummarySnapshot, y: Float, size: Float) {
        val entries = buildList {
            add(EquipmentEntry(snapshot.armor.definitionId, snapshot.armor.displayName, snapshot.armor.description, ContentCategory.ARMOR, null))
            snapshot.tamgas.take(2).forEach { add(EquipmentEntry(it.definitionId, it.displayName, it.description, ContentCategory.TAMGA, damageFor(snapshot, it.displayName))) }
        }
        val cellWidth = (layout.rightColumn.width - 24f) / 3f
        entries.forEachIndexed { index, entry ->
            val cellX = layout.rightColumn.x + 12f + index * cellWidth
            val slot = Rectangle(cellX + (cellWidth - size) / 2f, y, size, size)
            UiComponentRenderer.drawSlot(batch, slot, locked = entry.definitionId == null)
            entry.definitionId?.let { ContentIconRenderer.draw(batch, it, entry.category, inset(slot, 3f)) }
            font.data.setScale(0.76f)
            font.color = faded(Color.WHITE)
            val nameLayout = font.draw(batch, entry.name, cellX, slot.y - 6f, cellWidth, Align.center, true)
            entry.damage?.takeIf { it > 0f }?.let {
                font.data.setScale(0.76f)
                font.color = faded(Color.LIGHT_GRAY)
                font.draw(batch, "${formatNumber(it)} dmg", cellX, slot.y - 11f - nameLayout.height, cellWidth, Align.center, false)
            }
            hoverTargets += HoverTarget(Rectangle(slot.x, slot.y - 28f, slot.width, slot.height + 28f), entry.name, entry.description)
        }
    }

    private fun renderItems(snapshot: RunSummarySnapshot, clipBounds: Rectangle, size: Float) {
        val gap = 18f
        val columns = 4
        val gridWidth = columns * size + (columns - 1) * gap
        val startX = layout.rightColumn.x + (layout.rightColumn.width - gridWidth) / 2f
        val rowHeight = 92f
        val maximum = maximumItemScroll(snapshot.items.size, clipBounds)
        itemScrollOffset = itemScrollOffset.coerceIn(0f, maximum)
        val scissors = Rectangle()
        batch.flush()
        ScissorStack.calculateScissors(camera, batch.transformMatrix, clipBounds, scissors)
        val clipped = ScissorStack.pushScissors(scissors)
        snapshot.items.forEachIndexed { index, item ->
            val slot = Rectangle(
                startX + (index % columns) * (size + gap),
                clipBounds.y + clipBounds.height - size - (index / columns) * rowHeight + itemScrollOffset,
                size,
                size
            )
            if (!slot.overlaps(clipBounds)) return@forEachIndexed
            UiComponentRenderer.drawSlot(batch, slot)
            ContentIconRenderer.draw(batch, item.definitionId, ContentCategory.ITEM, inset(slot, 3f))
            if (item.count > 1) {
                font.data.setScale(0.45f)
                font.color = faded(Color.WHITE)
                font.draw(batch, item.count.toString(), slot.x, slot.y + 13f, slot.width - 4f, Align.right, false)
            }
            font.data.setScale(0.76f)
            font.color = faded(Color.WHITE)
            font.draw(batch, item.displayName, slot.x - 10f, slot.y - 7f, slot.width + 20f, Align.center, true)
            hoverTargets += HoverTarget(Rectangle(slot.x - 10f, slot.y - 30f, slot.width + 20f, slot.height + 30f), item.displayName, item.description)
        }
        if (clipped) {
            batch.flush()
            ScissorStack.popScissors()
        }
    }

    fun itemViewport(): Rectangle {
        updateLayout()
        return Rectangle(layout.rightColumn.x + 8f, screenBottom() + 20f, layout.rightColumn.width - 16f, 180f)
    }

    fun scrollItems(amount: Float, itemCount: Int) {
        itemScrollOffset = (itemScrollOffset + amount)
            .coerceIn(0f, maximumItemScroll(itemCount, itemViewport()))
    }

    private fun maximumItemScroll(itemCount: Int, viewport: Rectangle): Float {
        val rows = (itemCount + 3) / 4
        val contentHeight = if (rows == 0) 0f else 88f + (rows - 1) * 92f
        return (contentHeight - viewport.height).coerceAtLeast(0f)
    }

    fun renderHoveredTooltip() {
        if (renderAlpha < 0.999f) return
        hoverTargets.firstOrNull { it.bounds.contains(pointer) }?.let(::renderTooltip)
    }

    private fun renderTooltip(target: HoverTarget) {
        AdaptiveTooltipRenderer.draw(
            batch, font, camera, cameraWidth, cameraHeight, pointer,
            target.title, target.description, maxWidth = 430f
        )
    }

    private fun renderButtonTexts() {
        font.data.setScale(0.80f)
        font.color = faded(Color.WHITE)
        font.draw(batch, "RESTART  [R]", layout.restartButton.x, layout.restartButton.y + 37f, layout.restartButton.width, Align.center, false)
        font.draw(batch, "MAIN MENU  [ENTER]", layout.mainMenuButton.x, layout.mainMenuButton.y + 37f, layout.mainMenuButton.width, Align.center, false)
    }

    private fun buttonState(bounds: Rectangle) = if (bounds.contains(pointer)) ButtonVisualState.HOVERED else ButtonVisualState.NORMAL
    private fun updatePointer() { pointer.set(Gdx.input.x.toFloat(), Gdx.input.y.toFloat()); viewport.unproject(pointer) }
    private fun inset(bounds: Rectangle, amount: Float) = Rectangle(bounds.x + amount, bounds.y + amount, bounds.width - amount * 2f, bounds.height - amount * 2f)
    private fun screenBottom() = camera.position.y - cameraHeight / 2f
    private fun damageFor(snapshot: RunSummarySnapshot, name: String): Float? = snapshot.otherDamageSources.entries.firstOrNull { (source, _) -> source.equals(name, true) || source.contains(name, true) || name.contains(source, true) }?.value

    private fun isRunTimeRecord(snapshot: RunSummarySnapshot): Boolean {
        val seconds = snapshot.formattedRunTime.split(':').mapNotNull(String::toLongOrNull).fold(0L) { total, part -> total * 60L + part }
        return seconds > 0L && seconds >= HighScoreRepository.statistics.longestRunSeconds
    }
    private fun isDamageRecord(snapshot: RunSummarySnapshot) = snapshot.totalDamageDealt > 0f && snapshot.totalDamageDealt.toLong() >= HighScoreRepository.statistics.highestDamage
    private fun isDamageTakenRecord(snapshot: RunSummarySnapshot) = snapshot.totalDamageTaken > 0f && snapshot.totalDamageTaken.toLong() >= HighScoreRepository.statistics.mostDamageTaken
    private fun isGoldRecord(snapshot: RunSummarySnapshot) = snapshot.goldCollected > 0f && snapshot.goldCollected >= HighScoreRepository.statistics.mostGold
    private fun isGrisilithRecord(snapshot: RunSummarySnapshot) = snapshot.grisilithCoinsEarned > 0 && snapshot.grisilithCoinsEarned >= HighScoreRepository.statistics.mostGrisilithCoins
    private fun resultAccent(successful: Boolean) = if (successful) Color(0.96f, 0.78f, 0.20f, 1f) else Color(0.82f, 0.16f, 0.20f, 1f)
    private fun faded(color: Color) = Color(color).also { it.a *= renderAlpha }
    private fun formatInt(value: Int): String = String.format(Locale.US, "%,d", value)
    private fun formatNumber(value: Number): String = String.format(Locale.US, "%,.0f", value.toDouble())

    private fun damageBreakdown(
        values: Map<DamageType, Float>,
        include: (DamageType) -> Boolean = { it != DamageType.NONE }
    ): String? {
        val lines = DamageType.entries
            .filter { include(it) }
            .mapNotNull { type ->
                values[type]
                    ?.takeIf { it > 0f }
                    ?.let { "${type.displayName}: ${formatNumber(it)}" }
            }
        return lines.takeIf { it.isNotEmpty() }?.joinToString("\n")
    }

    private fun updateLayout() {
        val left = camera.position.x - cameraWidth / 2f
        val bottom = camera.position.y - cameraHeight / 2f
        layout.outerPanel.set(left, bottom, cameraWidth, cameraHeight)
        layout.leftColumn.set(left + 20f, bottom + 72f, 340f, 570f)
        layout.centerColumn.set(left + 375f, bottom + 72f, 530f, 570f)
        layout.rightColumn.set(left + 920f, bottom + 72f, 340f, 570f)
        layout.resultArtwork.set(layout.centerColumn.x + 36f, bottom + 486f, layout.centerColumn.width - 72f, 142f)
        layout.portrait.set(layout.leftColumn.x + 10f, bottom + 24f, 145f, 145f)
        layout.scoreCard.set(layout.leftColumn.x + 168f, bottom + 24f, 162f, 150f)
        layout.restartButton.set(camera.position.x - 235f, bottom + 12f, 218f, 58f)
        layout.mainMenuButton.set(camera.position.x + 17f, bottom + 12f, 218f, 58f)
    }

    private fun texture(path: String): Texture = Texture(Gdx.files.internal(path)).also { it.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear) }
    private data class SummaryRow(
        val label: String,
        val value: String,
        val record: Boolean = false,
        val tooltipDescription: String? = null
    )
    private data class HoverTarget(val bounds: Rectangle, val title: String, val description: String)
    private data class EquipmentEntry(val definitionId: String?, val name: String, val description: String, val category: ContentCategory, val damage: Float?)
}
