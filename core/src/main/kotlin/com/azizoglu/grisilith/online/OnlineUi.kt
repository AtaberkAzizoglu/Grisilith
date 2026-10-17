package com.azizoglu.grisilith.online

import com.azizoglu.grisilith.content.ContentCategory
import com.azizoglu.grisilith.ui.icon.ContentIconRenderer
import com.azizoglu.grisilith.ui.theme.PanelVisualStyle
import com.azizoglu.grisilith.ui.theme.UiComponentRenderer
import com.azizoglu.grisilith.ui.typography.UiFontFactory
import com.azizoglu.grisilith.screen.components.MenuButton
import com.azizoglu.grisilith.screen.components.MenuPointer
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Disposable
import com.badlogic.gdx.utils.JsonValue

/** Owned by a screen: no global GL resources and no player-name markup interpretation. */
class OnlineUi : Disposable {
    // The packaged sans-serif face keeps names, UUIDs and scores easy to read.
    val font = UiFontFactory.createNumeric(20).also { it.data.markupEnabled = false }
    private val pixel = Pixmap(1,1,Pixmap.Format.RGBA8888).let { p ->
        p.setColor(Color.WHITE); p.fill(); Texture(p).also { p.dispose() }
    }

    fun text(batch: SpriteBatch, value: String, x: Float, y: Float, width: Float, size: Float=1f,
             color: Color=Color(0.94f,0.88f,0.72f,1f), align: Int=Align.left) {
        val oldX=font.data.scaleX; val oldY=font.data.scaleY; val oldColor=Color(font.color)
        font.data.setScale(size)
        val measured=GlyphLayout(font,value).width
        if (measured>width && measured>0) font.data.setScale(size*width/measured)
        font.color=color; font.draw(batch,value,x,y,width,align,false)
        font.data.setScale(oldX,oldY); font.color=oldColor
    }

    fun connection(batch: SpriteBatch, backend: OnlineBackend?, x: Float, y: Float, warning: String?=null) {
        val ready=backend?.status==OnlineBackend.Status.READY
        val old=Color(batch.color)
        batch.color=if(ready) Color(.45f,.84f,.58f,1f) else Color(.8f,.52f,.4f,1f)
        // Standard ascending signal bars. A diagonal slash explicitly marks offline.
        repeat(3) { i -> batch.draw(pixel,x+i*6f,y,4f,5f+i*5f) }
        if(!ready) batch.draw(pixel,x-2f,y-2f,0f,0f,25f,2f,1f,1f,43f,0,0,1,1,false,false)
        batch.color=old
        text(batch,if(ready) "ONLINE" else if(backend?.status==OnlineBackend.Status.CONNECTING) "CONNECTING" else "OFFLINE",
            x+25f,y+15f,150f,.68f)
        if(warning!=null) text(batch,warning,x,y-7f,340f,.66f,Color(1f,.72f,.4f,1f))
    }

    fun leaderboard(batch: SpriteBatch, data: JsonValue?, backend: OnlineBackend?, loading: Boolean, failed: Boolean) {
        val b=Rectangle(38f,102f,730f,548f)
        batch.color=Color(.025f,.025f,.035f,.82f); batch.draw(pixel,b.x,b.y,b.width,b.height); batch.color=Color.WHITE
        text(batch,"LEADERBOARD",60f,624f,690f,1.25f)
        text(batch,"ALL MAPS · TOP 10 RUNS",60f,596f,690f,.65f,Color.LIGHT_GRAY)
        if(backend?.status!=OnlineBackend.Status.READY) {
            text(batch,"Unable to connect. Offline play is available.",60f,540f,680f,.85f); return
        }
        if(data==null) {
            text(batch,if(failed) "Leaderboard unavailable. Retrying shortly." else if(loading) "Loading runs…" else "No runs yet.",60f,540f,680f,.85f)
            return
        }
        text(batch,"RANK",55f,566f,43f,.6f)
        text(batch,"NAME",105f,566f,390f,.6f)
        text(batch,"SCORE",509f,566f,90f,.6f,align=Align.right)
        text(batch,"HERO",608f,566f,45f,.55f)
        text(batch,"MAP",671f,566f,65f,.55f)
        val rows=data.get("top")
        rows?.forEachIndexed { index,row -> row(batch,row,537f-index*36f,index%2==0) }
        if(rows==null || rows.size==0) text(batch,"Be the first to finish an online run.",60f,535f,680f,.85f)
        val footer=data.get("footer")?.takeUnless { it.isNull }
        text(batch,if(data.getString("footer_kind","")=="next") "NEXT RUN" else "YOUR BEST RUN",60f,156f,680f,.62f)
        if(footer!=null) row(batch,footer,132f,true)
        else text(batch,if(data.getString("footer_kind","")=="next") "No additional run yet." else "No eligible run yet.",105f,132f,580f,.72f,Color.LIGHT_GRAY)
    }

    private fun row(batch: SpriteBatch, row: JsonValue, y: Float, shaded: Boolean) {
        if(shaded || row.getBoolean("is_self",false)) {
            batch.color=if(row.getBoolean("is_self",false)) Color(.3f,.45f,.5f,.35f) else Color(.6f,.55f,.4f,.09f)
            batch.draw(pixel,49f,y-27f,706f,35f); batch.color=Color.WHITE
        }
        text(batch,row.getLong("rank",0).toString(),56f,y,42f,.8f)
        text(batch,row.getString("display_name",""),105f,y,390f,.86f)
        text(batch,String.format(java.util.Locale.ROOT,"%,d",row.getLong("score",0)),502f,y,97f,.85f,align=Align.right)
        ContentIconRenderer.draw(batch,row.getString("character_id",""),ContentCategory.CHARACTER,Rectangle(612f,y-24f,30f,30f))
        ContentIconRenderer.draw(batch,"MAP_"+row.getString("map_id",""),ContentCategory.MAP,Rectangle(674f,y-24f,54f,30f),framed=false)
    }

    fun dim(batch: SpriteBatch) {
        batch.color=Color(0f,0f,0f,.75f);batch.draw(pixel,0f,0f,1280f,720f);batch.color=Color.WHITE
    }
    override fun dispose() { font.dispose(); pixel.dispose() }
}

/** One modal editor reused for first online entry and voluntary Settings renames. */
class OnlineNameDialog(private val backend: OnlineBackend, private val ui: OnlineUi) : Disposable {
    var open=false; private set
    private var initial=false
    private var input=""
    private var cursor=0
    private var selectAll=false
    private var busy=false
    private var error:String?=null
    private var previous:InputProcessor?=null
    private var generation=0
    private var checkedInput:String?=null
    private var lastEditedAt=0L
    private var observedInput=""
    private var onClose:()->Unit={}
    private val field=Rectangle(350f,357f,580f,58f)
    private val save=MenuButton("CONFIRM",515f,265f,250f,58f) { submit() }
    private val cancel=MenuButton("CANCEL",680f,265f,250f,58f) { if(!busy) close() }
    private val keyboard=object:InputAdapter() {
        override fun keyTyped(character:Char):Boolean {
            if(!open || busy) return true
            if(character>=' ' && character!='\u007f') insert(character.toString())
            return true
        }
        override fun keyDown(keycode:Int):Boolean {
            if(busy) return true
            val control=Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)||Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT)
            when {
                control && keycode==Input.Keys.V -> insert(Gdx.app.clipboard.contents ?: "")
                control && keycode==Input.Keys.A -> selectAll=true
                keycode==Input.Keys.BACKSPACE -> { if(selectAll) {input="";cursor=0} else if(cursor>0) {input=input.removeRange(cursor-1,cursor);cursor--};selectAll=false;error=null }
                keycode==Input.Keys.FORWARD_DEL -> { if(selectAll) {input="";cursor=0} else if(cursor<input.length) input=input.removeRange(cursor,cursor+1); selectAll=false;error=null }
                keycode==Input.Keys.LEFT -> {cursor=(cursor-1).coerceAtLeast(0);selectAll=false}
                keycode==Input.Keys.RIGHT -> {cursor=(cursor+1).coerceAtMost(input.length);selectAll=false}
                keycode==Input.Keys.HOME -> {cursor=0;selectAll=false}
                keycode==Input.Keys.END -> {cursor=input.length;selectAll=false}
                keycode==Input.Keys.ENTER || keycode==Input.Keys.NUMPAD_ENTER -> submit()
                keycode==Input.Keys.ESCAPE && !initial -> close()
            }
            return true
        }
    }

    fun show(firstTime:Boolean,onClosed:()->Unit={}) {
        if(open) return
        initial=firstTime;input="";cursor=0;selectAll=false;error=null;busy=false;onClose=onClosed
        save.bounds.x=if(firstTime) 515f else 350f
        checkedInput=null;observedInput="";lastEditedAt=System.nanoTime()/1_000_000L
        previous=Gdx.input.inputProcessor;Gdx.input.inputProcessor=keyboard
        open=true;generation++;Gdx.input.setOnscreenKeyboardVisible(true)
    }
    private fun insert(value:String) {
        val upper=PlayerNamePolicy.uppercase(value)
        if(upper.any { it !in 'A'..'Z' && it !in '0'..'9' && it !in "ÇĞİÖŞÜ" && it !in PlayerNamePolicy.SYMBOLS }) {
            error="Unsupported character.";return
        }
        val proposed=if(selectAll) upper else input.substring(0,cursor)+upper+input.substring(cursor)
        if(proposed.length>24) {error="Use at most 24 characters.";return}
        if(selectAll) cursor=0
        input=proposed;cursor+=upper.length;selectAll=false;error=null
        lastEditedAt=System.nanoTime()/1_000_000L
    }
    private fun submit() {
        if(!open||busy) return
        error=PlayerNamePolicy.packaged.error(input)
        if(error!=null) return
        if(backend.status!=OnlineBackend.Status.READY) {close();return}
        if(!busy && checkedInput!=input && System.nanoTime()/1_000_000L-lastEditedAt>=500L &&
            PlayerNamePolicy.packaged.error(input)==null) {
            val requested=input;val requestGeneration=generation
            checkedInput=requested
            backend.nameAvailable(requested) { result ->
                if(open && requestGeneration==generation && input==requested && !busy) {
                    if(result.getOrNull()==false) error="This name is already taken."
                }
            }
        }
        busy=true
        val requestGeneration=generation
        backend.setDisplayName(input) { result ->
            if(!open || requestGeneration!=generation) return@setDisplayName
            busy=false
            if(result.isSuccess) close() else {
                if(backend.status!=OnlineBackend.Status.READY) {close();return@setDisplayName}
                error=when((result.exceptionOrNull() as? OnlineRequestFailure)?.reason) {
                    "display_name_taken" -> "This name is already taken."
                    "blocked_display_name" -> "This name contains a blocked expression."
                    "invalid_display_name" -> "Use a valid 3–24 character name."
                    else -> "Could not save the name. Please try again."
                }
            }
        }
    }
    fun update(pointer:MenuPointer) {
        if(!open) return
        if(backend.status!=OnlineBackend.Status.READY) {close();return}
        if(observedInput!=input) {observedInput=input;lastEditedAt=System.nanoTime()/1_000_000L}
        if(!busy) {
            listOfNotNull(save,cancel.takeUnless {initial}).forEach {
                it.update(pointer.position,pointer.justPressed,pointer.justReleased)
            }
            if(pointer.justReleased && field.contains(pointer.position)) Gdx.input.setOnscreenKeyboardVisible(true)
        }
    }
    fun render(batch:SpriteBatch,pointer:MenuPointer) {
        if(!open) return
        ui.dim(batch)
        UiComponentRenderer.drawStonePanel(batch,Rectangle(300f,205f,680f,330f))
        ui.text(batch,if(initial) "CHOOSE YOUR NAME" else "CHANGE YOUR NAME",335f,500f,610f,1.25f,align=Align.center)
        ui.text(batch,"3–24 characters · Empty = your Unique ID",335f,458f,610f,.75f,align=Align.center)
        UiComponentRenderer.drawPanel(batch,field,PanelVisualStyle.INNER)
        val display=if(input.isEmpty()) PlayerNamePolicy.uppercase(backend.playerId ?: "") else input
        ui.text(batch,display,365f,395f,550f,.95f,if(input.isEmpty()) Color.LIGHT_GRAY else Color.WHITE)
        if(input.isNotEmpty() && !busy) ui.text(batch,if(selectAll) "ALL SELECTED" else "|",365f,372f,550f,.5f)
        error?.let {ui.text(batch,it,335f,439f,610f,.76f,Color(1f,.38f,.32f,1f),Align.center)}
        if(busy) ui.text(batch,"Saving…",335f,301f,610f,1f,align=Align.center)
        else listOfNotNull(save,cancel.takeUnless {initial}).forEach { it.render(batch,ui.font,pointer.position,pointer.isHeld) }
    }
    fun close() {
        if(!open)return
        open=false;generation++
        if(Gdx.input.inputProcessor===keyboard) Gdx.input.inputProcessor=previous
        previous=null;Gdx.input.setOnscreenKeyboardVisible(false);onClose()
    }
    override fun dispose() = close()
}
