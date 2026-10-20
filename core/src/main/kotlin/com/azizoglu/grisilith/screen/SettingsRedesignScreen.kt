package com.azizoglu.grisilith.screen

import com.azizoglu.grisilith.GrisilithGame
import com.azizoglu.grisilith.input.RunInputAction
import com.azizoglu.grisilith.input.RunInputBindings
import com.azizoglu.grisilith.screen.components.MenuButton
import com.azizoglu.grisilith.screen.components.MenuButtonVisualStyle
import com.azizoglu.grisilith.screen.components.MenuPointer
import com.azizoglu.grisilith.settings.GameSettings
import com.azizoglu.grisilith.ui.theme.PanelVisualStyle
import com.azizoglu.grisilith.ui.theme.UiComponentRenderer
import com.azizoglu.grisilith.ui.typography.UiFontFactory
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.viewport.FitViewport
import kotlin.math.roundToInt
import com.azizoglu.grisilith.online.OnlineUi
import com.azizoglu.grisilith.online.OnlineNameDialog
import com.azizoglu.grisilith.online.OnlineBackend
import com.azizoglu.grisilith.audio.GameAudio
import com.azizoglu.grisilith.audio.UiSound

class SettingsScreen(
    @Suppress("unused") private val game: GrisilithGame,
    private val onBack: () -> Unit
) : ScreenAdapter() {

    private companion object {
        const val WIDTH = 1280f
        const val HEIGHT = 720f
        val MAIN_PANEL = Rectangle(55f, 31f, 1170f, 658f)
        val NAV_PANEL = Rectangle(70f, 82f, 240f, 520f)
    }

    private enum class Page { GENERAL, CONTROLS, COMBAT, ACCOUNT }
    private enum class ResetTarget { ALL, CONTROLS }
    private var resetConfirmation: ResetTarget? = null
    private var displayBefore: GameSettings.Snapshot? = null
    private var displaySeconds = 0f
    private var resolutionOpen=false
    private var resolutionOffset=0
    private val resolutionBounds=Rectangle(940f,384f,228f,56f)
    private fun resolutionRows():List<Rectangle> = GameSettings.resolutions().drop(resolutionOffset).take(5).indices.map {
        Rectangle(resolutionBounds.x,resolutionBounds.y-38f*(it+1),resolutionBounds.width,38f)
    }
    private fun resolutionLabel(value:GameSettings.Resolution):String =
        if(value.width==0 && !draftSettings.fullscreen) "Default (1280 x 720)" else value.label
    private enum class VolumeSlider { MASTER, MUSIC, EFFECTS }

    private val camera = OrthographicCamera()
    private val viewport = FitViewport(WIDTH, HEIGHT, camera)
    private val batch = SpriteBatch()
    private val font: BitmapFont = UiFontFactory.createBody()
    private val titleFont: BitmapFont = UiFontFactory.createDisplay(28)
    private val pointer = MenuPointer(viewport)
    private val background = Texture(Gdx.files.internal("ui/backgrounds/pause_stone_table.png")).also {
        it.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
    }
    private val settingsPanel = Texture(Gdx.files.internal("ui/backgrounds/settings_panel_hd.png")).also {
        it.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
    }
    private val solidPixel = Pixmap(1, 1, Pixmap.Format.RGBA8888).let { pixmap ->
        pixmap.setColor(Color.WHITE)
        pixmap.fill()
        Texture(pixmap).also { pixmap.dispose() }
    }

    private var page = Page.GENERAL
    private val onlineUi = OnlineUi()
    private var nameDialog: OnlineNameDialog? = null
    private var buttons: List<MenuButton> = emptyList()
    private var listeningAction: RunInputAction? = null
    private var exitConfirmationOpen = false
    private var draggingSlider: VolumeSlider? = null
    private var previousInputProcessor: InputProcessor? = null
    private lateinit var appliedSettings: GameSettings.Snapshot
    private lateinit var draftSettings: GameSettings.Snapshot
    private lateinit var appliedBindings: Map<RunInputAction, Int>
    private lateinit var draftBindings: MutableMap<RunInputAction, Int>

    private val keyboardInput = object : InputAdapter() {
        override fun scrolled(amountX:Float,amountY:Float):Boolean {
            if(!resolutionOpen)return false
            resolutionOffset=(resolutionOffset+amountY.toInt()).coerceIn(0,(GameSettings.resolutions().size-5).coerceAtLeast(0))
            rebuildButtons();return true
        }
        override fun keyDown(keycode: Int): Boolean {
            listeningAction?.let { action ->
                if (keycode == Input.Keys.ESCAPE) {
                    listeningAction = null
                } else {
                    assignDraftBinding(action, keycode)
                    listeningAction = null
                }
                rebuildButtons()
                return true
            }
            if (keycode == Input.Keys.ESCAPE) {
                GameAudio.play(UiSound.CLICK)
                if(resolutionOpen) {
                    resolutionOpen=false;rebuildButtons()
                } else if (displayBefore != null) {
                    finishDisplayPreview(false)
                } else if (resetConfirmation != null) {
                    resetConfirmation = null
                    rebuildButtons()
                } else if (exitConfirmationOpen) {
                    exitConfirmationOpen = false
                    rebuildButtons()
                } else {
                    requestExit()
                }
                return true
            }
            return false
        }
    }

    override fun show() {
        viewport.apply()
        camera.position.set(WIDTH / 2f, HEIGHT / 2f, 0f)
        camera.update()
        pointer.reset()
        resetConfirmation = null
        resolutionOpen=false
        appliedSettings = GameSettings.snapshot()
        draftSettings = appliedSettings
        appliedBindings = RunInputBindings.snapshot()
        draftBindings = appliedBindings.toMutableMap()
        previewAudio()
        previousInputProcessor = Gdx.input.inputProcessor
        Gdx.input.inputProcessor = keyboardInput
        if (nameDialog == null) game.online?.let { nameDialog = OnlineNameDialog(it, onlineUi) }
        rebuildButtons()
    }

    override fun hide() {
        if(displayBefore!=null) finishDisplayPreview(false)
        GameAudio.clearPreview(this)
        nameDialog?.close()
        if (Gdx.input.inputProcessor === keyboardInput) {
            Gdx.input.inputProcessor = previousInputProcessor
        }
        previousInputProcessor = null
    }

    override fun render(delta: Float) {
        if(displayBefore!=null) {
            displaySeconds-=delta.coerceAtLeast(0f)
            if(displaySeconds<=0f) finishDisplayPreview(false)
        }
        pointer.update()
        val dismissedResolution=resolutionOpen && pointer.justPressed && !resolutionBounds.contains(pointer.position) &&
            resolutionRows().none {it.contains(pointer.position)}
        if(dismissedResolution) {resolutionOpen=false;rebuildButtons()}
        nameDialog?.update(pointer)
        if (nameDialog?.open != true) handleVolumeSliders()
        previewAudio()
        val currentButtons = if(nameDialog?.open == true || dismissedResolution) emptyList() else buttons
        currentButtons.forEach { button ->
            if (button.update(pointer.position, pointer.justPressed, pointer.justReleased)) return
        }

        Gdx.gl.glClearColor(0.02f, 0.018f, 0.016f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        batch.projectionMatrix = camera.combined
        batch.begin()
        batch.color = Color.WHITE
        batch.draw(background, 0f, 0f, WIDTH, HEIGHT)
        batch.color = Color(0.035f, 0.03f, 0.028f, 0.52f)
        batch.draw(solidPixel, 0f, 0f, WIDTH, HEIGHT)
        batch.color = Color.WHITE

        batch.draw(settingsPanel, MAIN_PANEL.x, MAIN_PANEL.y, MAIN_PANEL.width, MAIN_PANEL.height)
        UiComponentRenderer.drawSimpleVerticalSeparator(batch, Rectangle(310f, 92f, 7f, 510f))
        drawHeader()
        drawNavigationText()
        when (page) {
            Page.GENERAL -> drawGeneralText()
            Page.CONTROLS -> drawControlsText()
            Page.COMBAT -> drawCombatText()
            Page.ACCOUNT -> drawAccountText()
        }
        if (page == Page.GENERAL) drawVolumeSliders()

        if (listeningAction != null) drawListeningModal()
        if (exitConfirmationOpen) drawExitConfirmation()
        if (resetConfirmation != null) drawResetConfirmation()
        if(displayBefore!=null) {
            drawModalBackdrop()
            UiComponentRenderer.drawPanel(batch,Rectangle(330f,205f,620f,290f),PanelVisualStyle.MODAL)
            font.data.setScale(1.05f);font.color=Color.WHITE
            font.draw(batch,"KEEP DISPLAY SETTINGS?",355f,442f,570f,Align.center,false)
            font.data.setScale(.85f)
            font.draw(batch,"Reverting in ${kotlin.math.ceil(displaySeconds).toInt()} seconds",355f,383f,570f,Align.center,false)
        }
        if(resolutionOpen) {
            regularButtons().forEach {it.render(batch,font,pointer.position,false)}
            val rows=resolutionRows()
            val bottom=rows.lastOrNull()?.y ?: resolutionBounds.y
            batch.color=Color(.035f,.03f,.025f,1f)
            batch.draw(solidPixel,resolutionBounds.x-5f,bottom-5f,resolutionBounds.width+10f,resolutionBounds.y-bottom+10f)
            batch.color=Color.WHITE
            UiComponentRenderer.drawPanel(batch,Rectangle(resolutionBounds.x-5f,bottom-5f,resolutionBounds.width+10f,resolutionBounds.y-bottom+10f),PanelVisualStyle.TOOLTIP)
            val total=GameSettings.resolutions().size
            if(total>5)UiComponentRenderer.drawChronicleScrollbar(batch,Rectangle(resolutionBounds.x+resolutionBounds.width+4f,bottom,5f,190f),5f/total,resolutionOffset.toFloat()/(total-5))
        }
        currentButtons.forEach { it.render(batch, font, pointer.position, pointer.isHeld) }
        if(page==Page.COMBAT && resetConfirmation==null && !exitConfirmationOpen)drawCombatTooltip()
        nameDialog?.render(batch,pointer)
        batch.end()
    }

    private fun drawHeader() {
        val old = titleFont.data.scaleX
        titleFont.data.setScale(1.32f)
        titleFont.color = Color(0.94f, 0.90f, 0.82f, 1f)
        com.azizoglu.grisilith.ui.ScreenTitleArt.draw(batch,"SETTINGS",Rectangle(370f,608f,540f,52f))
        titleFont.data.setScale(old)
    }

    private fun drawNavigationText() {
        font.data.setScale(0.90f)
        font.color = Color(0.68f, 0.69f, 0.70f, 1f)
        font.draw(batch, "CATEGORIES", NAV_PANEL.x + 18f, NAV_PANEL.y + NAV_PANEL.height - 20f)
        font.data.setScale(0.78f)
        font.color = if (hasPendingChanges()) Color(0.94f, 0.20f, 0.17f, 1f)
        else Color(0.92f, 0.72f, 0.24f, 1f)
        font.draw(batch, if (hasPendingChanges()) "UNSAVED CHANGES" else "ALL CHANGES SAVED", NAV_PANEL.x + 12f, 310f, NAV_PANEL.width - 24f, Align.center, false)
    }

    private fun drawGeneralText() {
        sectionHeader("AUDIO", 348f, 564f, 386f)
        sectionHeader("DISPLAY", 770f, 564f, 390f)
        sectionHeader("GAMEPLAY & HUD", 348f, 360f, 812f)
        label("Master Volume", 350f, 525f)
        label("Music Volume", 350f, 481f)
        label("Sound Effects", 350f, 437f)
        label("Directional Stereo", 350f, 389f)
        stateLabel(if(draftSettings.directionalStereo) "ON" else "OFF", 535f, 389f)
        label("Display Mode", 772f, 525f)
        label("VSYNC", 772f, 473f)
        label("Resolution",772f,421f)
        label("Screen Shake", 350f, 318f)
        label("Damage Numbers", 350f, 266f)
        label("Score Display", 350f, 214f)
        label("Game Speed Button", 772f, 318f)
        label("Shortcut Hints", 772f, 266f)
        label("Cooldown Numbers", 772f, 214f)
        stateLabel(if (draftSettings.fullscreen) "FULLSCREEN" else "WINDOWED", 968f, 525f)
        stateLabel(if (draftSettings.vSync) "ON" else "OFF", 1014f, 473f)
        stateLabel(if (draftSettings.screenShake) "ON" else "OFF", 535f, 318f)
        stateLabel(if (draftSettings.damageNumbers) "ON" else "OFF", 535f, 266f)
        stateLabel(if (draftSettings.showScore) "ON" else "OFF", 535f, 214f)
        stateLabel(if (draftSettings.showGameSpeedButton) "ON" else "OFF", 985f, 318f)
        stateLabel(if (draftSettings.showAbilityShortcuts) "ON" else "OFF", 985f, 266f)
        stateLabel(if (draftSettings.showAbilityCooldowns) "ON" else "OFF", 985f, 214f)
    }

    private fun drawAccountText() {
        sectionHeader("PLAYER IDENTITY",348f,564f,812f)
        onlineUi.connection(batch,game.online,350f,510f)
        label("Display Name",350f,465f)
        onlineUi.text(batch,game.online?.displayName ?: "Connect to create your identity.",350f,425f,790f,1.2f)
        label("Unique ID (permanent)",350f,365f)
        onlineUi.text(batch,game.online?.playerId?.let(com.azizoglu.grisilith.online.PlayerNamePolicy::uppercase) ?: "Available after your first connection.",
            350f,328f,790f,.95f)
        onlineUi.text(batch,"Renaming updates your previous leaderboard entries too.",350f,267f,790f,.78f)
    }

    private fun drawControlsText() {
        sectionHeader("MOVEMENT", 348f, 564f, 386f)
        sectionHeader("WEAPON SLOTS", 770f, 564f, 390f)
        sectionHeader("COMBAT", 348f, 322f, 386f)
        val leftMovement = RunInputAction.entries.filter { it.groupName == "MOVEMENT" }
        val leftCombat = RunInputAction.entries.filter { it.groupName == "COMBAT" }
        val rightWeapons = RunInputAction.entries.filter { it.groupName == "WEAPON SLOTS" }
        leftMovement.forEachIndexed { index, action -> label(action.displayName, 350f, 525f - index * 58f) }
        leftCombat.forEachIndexed { index, action -> label(action.displayName, 350f, 284f - index * 58f) }
        rightWeapons.forEachIndexed { index, action -> label(action.displayName, 772f, 525f - index * 58f) }
    }

    private fun combatPanelWidth(): Float {
        val oldX=font.data.scaleX; val oldY=font.data.scaleY
        font.data.setScale(1.02f)
        val labels = GameSettings.Targeting.entries.map { it.label } +
            GameSettings.AbilityCasting.entries.map { it.label } +
            GameSettings.NormalRange.entries.map { it.label } + GameSettings.AbilityRange.entries.map { it.label }
        val width=(labels.maxOf { com.badlogic.gdx.graphics.g2d.GlyphLayout(font,it).width }+60f).coerceAtLeast(280f)
        font.data.setScale(oldX,oldY)
        return width
    }
    private fun combatPanelBounds(row: Int): Rectangle {
        val width=combatPanelWidth()
        return Rectangle(1110f-width,457f-row*108f,width,64f)
    }
    private fun drawCombatText() {
        sectionHeader("TARGETING & RANGE INDICATORS",348f,564f,812f)
        listOf("Targeting","Ability Targeting","Normal Attack Range","Ability Range").forEachIndexed { row,title ->
            label(title,350f,combatPanelBounds(row).y+42f)
        }
    }
    private fun combatTooltipRow(): Int? = (0..3).firstOrNull { combatPanelBounds(it).contains(pointer.position) }
    private fun drawCombatTooltip() {
        val row=combatTooltipRow() ?: return
        val title=listOf("Targeting","Ability Targeting","Normal Attack Range","Ability Range")[row]
        val body=when(row) {
            0 -> com.azizoglu.grisilith.settings.CombatSettingDescriptions.targeting(draftSettings.targeting)
            1 -> com.azizoglu.grisilith.settings.CombatSettingDescriptions.casting(draftSettings.abilityCasting)
            2 -> com.azizoglu.grisilith.settings.CombatSettingDescriptions.normalRange(draftSettings.normalRange)
            else -> com.azizoglu.grisilith.settings.CombatSettingDescriptions.abilityRange(draftSettings.abilityRange)
        }
        com.azizoglu.grisilith.screen.render.AdaptiveTooltipRenderer.draw(batch,font,
            Rectangle(0f,0f,WIDTH,HEIGHT),pointer.position,title,body,maxWidth=480f,titleScale=.85f,bodyScale=.70f)
    }

    private fun sectionHeader(text: String, x: Float, y: Float, width: Float) {
        font.data.setScale(1.00f)
        font.color = Color(0.92f, 0.72f, 0.24f, 1f)
        font.draw(batch, text, x, y)
        UiComponentRenderer.drawHorizontalSeparator(batch, Rectangle(x, y - 23f, width, 6f))
    }

    private fun label(text: String, x: Float, y: Float) {
        font.data.setScale(0.98f)
        font.color = Color(0.82f, 0.83f, 0.85f, 1f)
        font.draw(batch, text, x, y)
    }

    private fun stateLabel(text: String, x: Float, y: Float) {
        font.data.setScale(0.84f)
        font.color = if (text == "OFF" || text == "WINDOWED") Color(0.70f, 0.71f, 0.73f, 1f)
        else Color(0.82f, 0.92f, 1f, 1f)
        font.draw(batch, text, x, y, 105f, Align.right, false)
    }

    private fun rebuildButtons() {
        buttons = when {
            displayBefore != null -> listOf(
                button("KEEP",400f,250f,220f,58f){finishDisplayPreview(true)},
                button("REVERT",660f,250f,220f,58f){finishDisplayPreview(false)})
            resolutionOpen -> buildList {
                val selected=GameSettings.Resolution(draftSettings.resolutionWidth,draftSettings.resolutionHeight)
                add(dropdownRow(resolutionLabel(selected)+"  ^",resolutionBounds.x,resolutionBounds.y,resolutionBounds.width,resolutionBounds.height,false) {resolutionOpen=false;rebuildButtons()})
                GameSettings.resolutions().drop(resolutionOffset).take(5).forEachIndexed {index,value ->
                    val row=resolutionRows()[index]
                    add(dropdownRow(resolutionLabel(value),row.x,row.y,row.width,row.height,value==selected) {
                        resolutionOpen=false
                        if(value!=selected)previewDisplay(draftSettings.fullscreen,value.width,value.height) else rebuildButtons()
                    })
                }
            }
            resetConfirmation != null -> listOf(
                button("RESET", 400f, 235f, 220f, 58f) { confirmReset() },
                button("CANCEL", 660f, 235f, 220f, 58f) {
                    resetConfirmation=null;rebuildButtons()
                })
            listeningAction != null -> listOf(button("CANCEL", 540f, 232f, 200f, 48f) {
                listeningAction = null
                rebuildButtons()
            })
            exitConfirmationOpen -> confirmationButtons()
            else -> regularButtons()
        }
    }

    private fun regularButtons(): List<MenuButton> = buildList {
        add(navButton("GENERAL", 76f, 515f, 222f, 52f, page == Page.GENERAL) {
            page = Page.GENERAL; rebuildButtons()
        })
        add(navButton("KEY BINDINGS", 76f, 450f, 222f, 52f, page == Page.CONTROLS) {
            page = Page.CONTROLS; rebuildButtons()
        })
        add(navButton("COMBAT", 76f, 385f, 222f, 52f, page == Page.COMBAT) {
            page = Page.COMBAT; rebuildButtons()
        })
        add(navButton("ACCOUNT",76f,320f,222f,52f,page==Page.ACCOUNT) {
            page=Page.ACCOUNT;rebuildButtons()
        })
        add(button("RESET ALL", 76f, 225f, 222f, 58f) {
            resetConfirmation = ResetTarget.ALL
            rebuildButtons()
        })
        add(button("CONFIRM CHANGES", 76f, 150f, 222f, 58f, ::applyChanges))
        add(button("BACK", 76f, 75f, 222f, 58f, ::requestExit))
        when (page) {
            Page.GENERAL -> addGeneralButtons()
            Page.CONTROLS -> addControlButtons()
            Page.COMBAT -> addCombatButtons()
            Page.ACCOUNT -> add(button("CHANGE NAME / RESET",350f,175f,385f,58f) {
                if(game.online?.status==OnlineBackend.Status.READY) nameDialog?.show(false)
            })
        }
    }

    private fun MutableList<MenuButton>.addGeneralButtons() {
        addSettingToggle(650f, 365f, draftSettings.directionalStereo) {
            draftSettings = draftSettings.copy(directionalStereo = !draftSettings.directionalStereo)
        }
        add(toggleButton("", 1082f, 497f, 86f, 35f, draftSettings.fullscreen) {
            previewDisplay(!draftSettings.fullscreen,0,0)
        })
        val resolution=GameSettings.Resolution(draftSettings.resolutionWidth,draftSettings.resolutionHeight)
        add(dropdownRow(resolutionLabel(resolution)+"  v",resolutionBounds.x,resolutionBounds.y,resolutionBounds.width,resolutionBounds.height,false){
            resolutionOpen=true
            resolutionOffset=GameSettings.resolutions().indexOf(resolution).coerceAtLeast(0).coerceAtMost((GameSettings.resolutions().size-5).coerceAtLeast(0))
            rebuildButtons()
        })
        add(toggleButton("", 1082f, 445f, 86f, 35f, draftSettings.vSync) {
            draftSettings = draftSettings.copy(vSync = !draftSettings.vSync); rebuildButtons()
        })
        addSettingToggle(650f, 290f, draftSettings.screenShake) {
            draftSettings = draftSettings.copy(screenShake = !draftSettings.screenShake)
        }
        addSettingToggle(650f, 238f, draftSettings.damageNumbers) {
            draftSettings = draftSettings.copy(damageNumbers = !draftSettings.damageNumbers)
        }
        addSettingToggle(650f, 186f, draftSettings.showScore) {
            draftSettings = draftSettings.copy(showScore = !draftSettings.showScore)
        }
        addSettingToggle(1100f, 290f, draftSettings.showGameSpeedButton) {
            draftSettings = draftSettings.copy(showGameSpeedButton = !draftSettings.showGameSpeedButton)
        }
        addSettingToggle(1100f, 238f, draftSettings.showAbilityShortcuts) {
            draftSettings = draftSettings.copy(showAbilityShortcuts = !draftSettings.showAbilityShortcuts)
        }
        addSettingToggle(1100f, 186f, draftSettings.showAbilityCooldowns) {
            draftSettings = draftSettings.copy(showAbilityCooldowns = !draftSettings.showAbilityCooldowns)
        }
    }

    private fun MutableList<MenuButton>.addCombatButtons() {
        fun stepper(label: String, row: Int, change: (Int) -> Unit) {
            val panel=combatPanelBounds(row)
            add(MenuButton("",panel.x-52f,panel.y+11f,42f,42f,visualStyle=MenuButtonVisualStyle.ARROW_LEFT,
                onClick={change(-1);rebuildButtons()}))
            add(dropdownRow(label,panel.x,panel.y,panel.width,panel.height,false) {})
            add(MenuButton("",panel.x+panel.width+10f,panel.y+11f,42f,42f,visualStyle=MenuButtonVisualStyle.ARROW_RIGHT,
                onClick={change(1);rebuildButtons()}))
        }
        stepper(draftSettings.targeting.label, 0) { step ->
            val values = GameSettings.Targeting.entries
            draftSettings = draftSettings.copy(targeting = values[(draftSettings.targeting.ordinal + step).mod(values.size)])
        }
        stepper(draftSettings.abilityCasting.label, 1) { step ->
            val values = GameSettings.AbilityCasting.entries
            draftSettings = draftSettings.copy(abilityCasting = values[(draftSettings.abilityCasting.ordinal + step).mod(values.size)])
        }
        stepper(draftSettings.normalRange.label, 2) { step ->
            val values = GameSettings.NormalRange.entries
            draftSettings = draftSettings.copy(normalRange = values[(draftSettings.normalRange.ordinal + step).mod(values.size)])
        }
        stepper(draftSettings.abilityRange.label, 3) { step ->
            val values = GameSettings.AbilityRange.entries
            draftSettings = draftSettings.copy(abilityRange = values[(draftSettings.abilityRange.ordinal + step).mod(values.size)])
        }
    }

    private fun previewDisplay(fullscreen:Boolean,width:Int,height:Int) {
        resolutionOpen=false
        displayBefore=GameSettings.snapshot()
        displaySeconds=15f
        GameSettings.restore(displayBefore!!.copy(fullscreen=fullscreen,resolutionWidth=width,resolutionHeight=height),persist=false)
        draftSettings=draftSettings.copy(fullscreen=fullscreen,resolutionWidth=width,resolutionHeight=height)
        rebuildButtons()
    }

    private fun finishDisplayPreview(keep:Boolean) {
        val before=displayBefore ?: return
        val chosen=if(keep)GameSettings.snapshot() else before
        GameSettings.restore(chosen)
        appliedSettings=appliedSettings.copy(fullscreen=chosen.fullscreen,resolutionWidth=chosen.resolutionWidth,resolutionHeight=chosen.resolutionHeight)
        draftSettings=draftSettings.copy(fullscreen=chosen.fullscreen,resolutionWidth=chosen.resolutionWidth,resolutionHeight=chosen.resolutionHeight)
        displayBefore=null
        rebuildButtons()
    }


    private fun MutableList<MenuButton>.addSettingToggle(x: Float, y: Float, selected: Boolean, toggle: () -> Unit) {
        add(toggleButton("", x, y, 86f, 35f, selected) {
            toggle(); rebuildButtons()
        })
    }

    private fun MutableList<MenuButton>.addControlButtons() {
        val movement = RunInputAction.entries.filter { it.groupName == "MOVEMENT" }
        val combat = RunInputAction.entries.filter { it.groupName == "COMBAT" }
        val weapons = RunInputAction.entries.filter { it.groupName == "WEAPON SLOTS" }
        movement.forEachIndexed { index, action -> addBindingButton(action, 658f, 487f - index * 58f) }
        combat.forEachIndexed { index, action -> addBindingButton(action, 658f, 246f - index * 58f) }
        weapons.forEachIndexed { index, action -> addBindingButton(action, 1092f, 487f - index * 58f) }
        add(button("RESET CONTROLS", 875f, 112f, 285f, 58f) {
            resetConfirmation = ResetTarget.CONTROLS; rebuildButtons()
        })
    }

    private fun MutableList<MenuButton>.addBindingButton(action: RunInputAction, x: Float, y: Float) {
        add(keyButton(draftKeyLabel(action).uppercase(), x, y, 58f, 58f) {
            listeningAction = action
            rebuildButtons()
        })
    }

    private fun confirmationButtons(): List<MenuButton> = listOf(
        button("CONFIRM AND EXIT", 286f, 235f, 225f, 58f) {
            applyChanges()
            onBack()
        },
        button("UNDO CHANGES", 527f, 235f, 225f, 58f) {
            discardPendingChanges()
        },
        button("CANCEL", 768f, 235f, 225f, 58f) {
            exitConfirmationOpen = false
            rebuildButtons()
        }
    )

    private fun requestExit() {
        if (hasPendingChanges()) {
            exitConfirmationOpen = true
            rebuildButtons()
        } else {
            onBack()
        }
    }

    private fun hasPendingChanges(): Boolean {
        if (!::appliedSettings.isInitialized || !::appliedBindings.isInitialized) return false
        val settingsChangedExcludingDisplayMode =
            draftSettings.copy(fullscreen = appliedSettings.fullscreen) != appliedSettings
        return settingsChangedExcludingDisplayMode || draftBindings != appliedBindings
    }

    private fun applyChanges() {
        val fullscreen = GameSettings.fullscreen
        GameSettings.restore(draftSettings.copy(fullscreen = fullscreen))
        RunInputBindings.restore(draftBindings)
        appliedSettings = GameSettings.snapshot()
        draftSettings = appliedSettings
        appliedBindings = RunInputBindings.snapshot()
        draftBindings = appliedBindings.toMutableMap()
        previewAudio()
        GameAudio.play(UiSound.CONFIRM)
        rebuildButtons()
    }

    private fun discardPendingChanges() {
        draftSettings = appliedSettings.copy(fullscreen = GameSettings.fullscreen)
        draftBindings = appliedBindings.toMutableMap()
        listeningAction = null
        exitConfirmationOpen = false
        previewAudio()
        GameAudio.play(UiSound.CLICK)
        rebuildButtons()
    }

    private fun previewAudio() {
        if(::draftSettings.isInitialized) GameAudio.preview(this,draftSettings.masterVolume,draftSettings.musicVolume,draftSettings.soundEffectsVolume,draftSettings.directionalStereo)
    }

    private fun assignDraftBinding(action: RunInputAction, keyCode: Int) {
        val previousKey = draftBindings[action] ?: action.defaultKey
        val conflict = draftBindings.entries.firstOrNull { it.key != action && it.value == keyCode }?.key
        draftBindings[action] = keyCode
        if (conflict != null) draftBindings[conflict] = previousKey
    }

    private fun draftKeyLabel(action: RunInputAction): String =
        RunInputBindings.keyLabel(draftBindings[action] ?: action.defaultKey)

    private fun sliderBounds(slider: VolumeSlider): Rectangle = when (slider) {
        VolumeSlider.MASTER -> Rectangle(530f, 495f, 190f, 24f)
        VolumeSlider.MUSIC -> Rectangle(530f, 451f, 190f, 24f)
        VolumeSlider.EFFECTS -> Rectangle(530f, 407f, 190f, 24f)
    }

    private fun handleVolumeSliders() {
        if (page != Page.GENERAL || listeningAction != null || exitConfirmationOpen || resetConfirmation != null || displayBefore!=null || resolutionOpen) {
            draggingSlider = null
            return
        }
        if (pointer.justPressed) {
            draggingSlider = VolumeSlider.entries.firstOrNull { sliderBounds(it).contains(pointer.position) }
        }
        val active = draggingSlider
        if (active != null && pointer.isHeld) {
            val bounds = sliderBounds(active)
            val railInset = bounds.width * 0.105f
            val railWidth = bounds.width - railInset * 2f
            val value = ((pointer.position.x - bounds.x - railInset) / railWidth).coerceIn(0f, 1f)
            val rounded = (value * 100f).roundToInt() / 100f
            draftSettings = when (active) {
                VolumeSlider.MASTER -> draftSettings.copy(masterVolume = rounded)
                VolumeSlider.MUSIC -> draftSettings.copy(musicVolume = rounded)
                VolumeSlider.EFFECTS -> draftSettings.copy(soundEffectsVolume = rounded)
            }
        }
        if (!pointer.isHeld) {
            if(draggingSlider==VolumeSlider.EFFECTS || draggingSlider==VolumeSlider.MASTER) GameAudio.play(UiSound.CLICK)
            draggingSlider = null
        }
    }

    private fun drawVolumeSliders() {
        VolumeSlider.entries.forEach { slider ->
            val value = when (slider) {
                VolumeSlider.MASTER -> draftSettings.masterVolume
                VolumeSlider.MUSIC -> draftSettings.musicVolume
                VolumeSlider.EFFECTS -> draftSettings.soundEffectsVolume
            }
            val bounds = sliderBounds(slider)
            UiComponentRenderer.drawSlider(batch, bounds, value, bounds.contains(pointer.position))
            font.data.setScale(0.72f)
            font.color = Color.WHITE
            font.draw(batch, "${(value * 100f).roundToInt()}%", 472f, bounds.y + 21f, 48f, Align.right, false)
        }
    }

    private fun drawListeningModal() {
        drawModalBackdrop()
        val bounds = Rectangle(365f, 210f, 550f, 250f)
        UiComponentRenderer.drawPanel(batch, bounds, PanelVisualStyle.MODAL)
        font.data.setScale(1.02f)
        font.color = Color.WHITE
        font.draw(batch, "PRESS A KEY", bounds.x, bounds.y + 198f, bounds.width, Align.center, false)
        font.data.setScale(0.76f)
        font.color = Color(0.92f, 0.72f, 0.24f, 1f)
        font.draw(batch, listeningAction?.displayName ?: "", bounds.x, bounds.y + 152f, bounds.width, Align.center, false)
        font.data.setScale(0.60f)
        font.color = Color.LIGHT_GRAY
        font.draw(batch, "Escape cancels. An occupied key will swap with this action.", bounds.x + 30f, bounds.y + 112f, bounds.width - 60f, Align.center, true)
    }

    private fun drawExitConfirmation() {
        drawModalBackdrop()
        val bounds = Rectangle(270f, 205f, 740f, 285f)
        UiComponentRenderer.drawPanel(batch, bounds, PanelVisualStyle.MODAL)
        font.data.setScale(1.02f)
        font.color = Color.WHITE
        font.draw(batch, "There is unsaved changes.", bounds.x, bounds.y + 216f, bounds.width, Align.center, false)
    }

    private fun confirmReset() {
        val target=resetConfirmation ?: return
        if(target==ResetTarget.ALL)draftSettings=GameSettings.defaults()
        draftBindings=RunInputBindings.defaults().toMutableMap()
        resetConfirmation=null
        previewAudio()
        GameAudio.play(UiSound.CONFIRM)
        rebuildButtons()
    }

    private fun drawResetConfirmation() {
        drawModalBackdrop()
        val bounds=Rectangle(270f,205f,740f,285f)
        UiComponentRenderer.drawPanel(batch,bounds,PanelVisualStyle.MODAL)
        font.data.setScale(1.02f);font.color=Color.WHITE
        font.draw(batch,"Are you sure?",bounds.x,bounds.y+220f,bounds.width,Align.center,false)
        font.data.setScale(.82f)
        font.draw(batch,if(resetConfirmation==ResetTarget.ALL)
            "Reset all settings and key bindings to their defaults?"
            else "Reset all key bindings to their defaults?",
            bounds.x+30f,bounds.y+166f,bounds.width-60f,Align.center,true)
    }

    private fun drawModalBackdrop() {
        batch.color = Color(0f, 0f, 0f, 0.66f)
        batch.draw(solidPixel, 0f, 0f, WIDTH, HEIGHT)
        batch.color = Color.WHITE
    }

    private fun button(text: String, x: Float, y: Float, width: Float, height: Float, click: () -> Unit) =
        MenuButton(text, x, y, width, height, onClick = click)

    private fun keyButton(text: String, x: Float, y: Float, width: Float, height: Float, click: () -> Unit) =
        MenuButton(text, x, y, width, height, visualStyle = MenuButtonVisualStyle.KEY_SLOT, onClick = click)

    private fun dropdownRow(text: String, x: Float, y: Float, width: Float, height: Float, selected: Boolean, click: () -> Unit) =
        MenuButton(text, x, y, width, height, visualStyle = MenuButtonVisualStyle.DROPDOWN_ROW, isSelected = selected, onClick = click)

    private fun toggleButton(
        text: String,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        selected: Boolean,
        click: () -> Unit
    ) = MenuButton(
        text, x, y, width, height,
        visualStyle = MenuButtonVisualStyle.TOGGLE,
        isSelected = selected,
        onClick = click
    )

    private fun navButton(
        text: String,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        selected: Boolean,
        click: () -> Unit
    ) = MenuButton(
        text, x, y, width, height,
        visualStyle = MenuButtonVisualStyle.STANDARD,
        isSelected = selected,
        onClick = click
    )

    override fun resize(width: Int, height: Int) {
        if (width > 0 && height > 0) viewport.update(width, height, true)
    }

    override fun dispose() {
        hide()
        batch.dispose()
        font.dispose()
        titleFont.dispose()
        background.dispose()
        settingsPanel.dispose()
        solidPixel.dispose()
        nameDialog?.dispose()
        onlineUi.dispose()
    }
}
