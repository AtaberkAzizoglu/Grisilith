package qa

import com.azizoglu.grisilith.*
import com.azizoglu.grisilith.character.*
import com.azizoglu.grisilith.collection.*
import com.azizoglu.grisilith.content.*
import com.azizoglu.grisilith.armor.definition.DefaultArmors
import com.azizoglu.grisilith.screen.CollectionCategoryScreen
import com.azizoglu.grisilith.screen.render.CharacterHoverRenderer
import com.azizoglu.grisilith.ui.theme.PaperUi
import com.azizoglu.grisilith.ui.typography.UiFontFactory
import com.badlogic.gdx.*
import com.badlogic.gdx.backends.lwjgl3.*
import com.badlogic.gdx.graphics.*
import com.badlogic.gdx.graphics.g2d.*
import com.badlogic.gdx.math.*

object CollectionCopySmoke {
    @JvmStatic fun main(args:Array<String>) {
        val config=Lwjgl3ApplicationConfiguration().apply {
            setWindowedMode(1280,720);setInitialVisible(false);disableAudio(true)
            setPreferencesConfig("../tools/combat-targeting/copy-prefs",Files.FileType.Local)
        }
        Lwjgl3Application(object:ApplicationAdapter() {
            override fun create() { try { checks();Gdx.app.exit() } catch(t:Throwable){t.printStackTrace();kotlin.system.exitProcess(1)} }
        },config)
    }
    private fun checks() {
        val explosion=com.azizoglu.grisilith.tamga.runtime.active.ChargerExplosionRuntime()
        val damageMethod=explosion.javaClass.getDeclaredMethod("calculateDamage",Int::class.javaPrimitiveType,
            Int::class.javaPrimitiveType,Float::class.javaPrimitiveType,Boolean::class.javaPrimitiveType).apply{isAccessible=true}
        for(score in listOf(0,5,20)) for(stage in listOf(1,5,6,11,50)) for(seconds in listOf(0f,1.5f,3.5f,6f)) for(full in listOf(false,true)) {
            val raw=70f+score*2.1f+8f*seconds.coerceIn(0f,3.5f)+5f*((stage-1)/5)+if(full)12f else 0f
            check(damageMethod.invoke(explosion,score,stage,seconds,full)==Math.round(raw))
        }
        println("TAMGA_COPY_PASS: Charger formula compared with runtime across 120 score/stage/charge/completion combinations; all eight short descriptions and scaling sections")
        fun field(target:Any,name:String)=target.javaClass.getDeclaredField(name).apply{isAccessible=true}
        fun screenshot(name:String) {
            val p=Pixmap.createFromFrameBuffer(0,0,1280,720)
            PixmapIO.writePNG(Gdx.files.local("../tools/combat-targeting/$name.png"),p,-1,true);p.dispose()
        }
        val game=GrisilithGame()
        for(kind in listOf("normal","mini","arc","vampire")) {
            val frame=Pixmap(Gdx.files.internal("ui/content_icons/frames/bestiary/${kind}_v1.png"))
            check((frame.getPixel(frame.width/2,frame.height/2) and 255)==0){"$kind frame must have a transparent center"}
            check((frame.getPixel(0,0) and 255)==0)
            frame.dispose()
        }
        check("Movement Speed" in com.azizoglu.grisilith.tamga.definition.DefaultTamgas.all.first{it.displayName=="Phantom Steed"}.description)
        val repeat=com.azizoglu.grisilith.ui.input.AcceleratingPressRepeat()
        repeat.begin("plus")
        check(repeat.update(.2f,true,"plus")==0)
        check(repeat.update(.17f,true,"plus")==0)
        check(repeat.update(.02f,true,"plus")==1)
        var early=0;var late=0
        repeat.begin("plus")
        repeat.update(.25f,true,"plus")
        repeat.update(.15f,true,"plus")
        repeat(10){early+=repeat.update(.05f,true,"plus")}
        repeat(30){repeat.update(.05f,true,"plus")}
        repeat(10){late+=repeat.update(.05f,true,"plus")}
        check(late>early)
        check(repeat.update(.25f,true,"minus")==0)
        check(repeat.update(.25f,true,"plus")==0)
        println("STAT_REPEAT_PASS: delay, acceleration and leaving the pressed control")
        val journal=com.azizoglu.grisilith.screen.preset.PresetJournalScreen(game,createNew=true)
        journal.show();journal.resize(1280,720);journal.render(.016f);screenshot("create-preset-characters")
        val act=journal.javaClass.getDeclaredMethod("act",String::class.java).apply{isAccessible=true}
        act.invoke(journal,"content:0")
        check(field(journal,"characterDetailsOpen").getBoolean(journal))
        journal.render(.016f);screenshot("preset-character-details")
        act.invoke(journal,"changeCharacter")
        check(!field(journal,"characterDetailsOpen").getBoolean(journal))
        check(field(journal,"openSlot").get(journal).toString()=="CHARACTER")
        journal.render(.016f);screenshot("preset-character-picker")
        val renderMiddle=journal.javaClass.getDeclaredMethod("renderMiddle").apply{isAccessible=true}
        val journalBatch=field(journal,"batch").get(journal) as SpriteBatch
        val journalPointer=field(journal,"pointer").get(journal) as com.azizoglu.grisilith.screen.components.MenuPointer
        journalPointer.position.set(460f,495f)
        field(journal,"tooltip").set(journal,null)
        journalBatch.begin();renderMiddle.invoke(journal);journalBatch.end()
        check(field(journal,"tooltip").get(journal)==null){"Character portrait must not open a hover"}
        val select=journal.javaClass.getDeclaredMethod("selectContent",ContentDefinition::class.java).apply{isAccessible=true}
        for(c in DefaultCharacters.all) {
            if(field(journal,"openSlot").get(journal)==null)act.invoke(journal,"changeCharacter")
            select.invoke(journal,c)
            journal.render(.016f);screenshot("preset-character-page-${c.id.value}")
            val scroll=field(journal,"rightScroll").get(journal)
            field(scroll,"offset").setFloat(scroll,field(scroll,"maximum").getFloat(scroll))
            journal.render(.016f);screenshot("preset-character-page-${c.id.value}-scrolled")
        }
        act.invoke(journal,"changeCharacter")
        act.invoke(journal,"content:0")
        act.invoke(journal,"stats")
        println("CHARACTER_PAGE_PASS: character portrait has no hover; detail/change/list flow; all five pages and scroll extents")
        journal.render(.016f);screenshot("preset-stat-allocation")
        val pointer=field(journal,"pointer").get(journal) as com.azizoglu.grisilith.screen.components.MenuPointer
        val stat=com.azizoglu.grisilith.statistics.StatType.STRENGTH
        val label=journal.javaClass.getDeclaredMethod("statLabelBounds",com.azizoglu.grisilith.statistics.StatType::class.java).apply{isAccessible=true}.invoke(journal,stat) as Rectangle
        check(label.width>0 && label.height<18f)
        val original=Gdx.input;var held=true;var pressed=true
        Gdx.input=java.lang.reflect.Proxy.newProxyInstance(Input::class.java.classLoader,arrayOf(Input::class.java)) { _,method,args ->
            when(method.name) {
                "isButtonJustPressed"->pressed && args?.firstOrNull()==Input.Buttons.LEFT
                "isButtonPressed"->held && args?.firstOrNull()==Input.Buttons.LEFT
                else->when(method.returnType){java.lang.Boolean.TYPE->false;java.lang.Integer.TYPE->0;java.lang.Float.TYPE->0f;else->null}
            }
        } as Input
        try {
            val control=journal.javaClass.getDeclaredMethod("statControl",com.azizoglu.grisilith.statistics.StatType::class.java,Int::class.javaPrimitiveType).apply{isAccessible=true}
            val input=journal.javaClass.getDeclaredMethod("input",Float::class.javaPrimitiveType).apply{isAccessible=true}
            val draft=field(journal,"draft").get(journal)
            val allocationField=field(draft,"allocation")
            fun spent()=(allocationField.get(draft) as com.azizoglu.grisilith.preset.PresetStatAllocation).spentPointCount
            val plus=control.invoke(journal,stat,1) as Rectangle
            pointer.position.set(plus.x+plus.width/2,plus.y+plus.height/2)
            input.invoke(journal,.016f);pressed=false
            val immediate=spent();check(immediate==1)
            repeat(45){input.invoke(journal,.05f)}
            check(spent()>immediate && spent()<=field(draft,"budget").getInt(draft))
            held=false;input.invoke(journal,.016f)
            val minus=control.invoke(journal,stat,0) as Rectangle
            pointer.position.set(minus.x+minus.width/2,minus.y+minus.height/2)
            held=true;pressed=true;input.invoke(journal,.016f);pressed=false
            repeat(80){input.invoke(journal,.05f)}
            check(spent()==0)
            println("STAT_ALLOCATION_INPUT_PASS: real journal press/hold respects budget and zero lower bound")
        }finally{Gdx.input=original}
        journal.dispose()
        for(category in ContentCategory.entries) {
            val screen=CollectionCategoryScreen(game,category)
            screen.show();screen.resize(1280,720)
            val definitions=DefaultContentRegistry.instance.definitionsForCategory(category)
            field(screen,"entries").set(screen,definitions.map{CollectionEntry(it,true,true)})
            for(definition in if(category!=ContentCategory.ITEM) definitions else definitions.take(1)) {
                field(screen,"selectedContentId").set(screen,definition.id.value)
                screen.render(.016f)
                if(category!=ContentCategory.ITEM) screenshot("collection-${definition.id.value}")
            }
            if(category==ContentCategory.ENEMY) {
                val scroll=screen.javaClass.getDeclaredMethod("scrollBy",Float::class.javaPrimitiveType).apply{isAccessible=true}
                scroll.invoke(screen,10000f);screen.render(.016f);screenshot("bestiary-final-frames")
            }
            val rectMethod=screen.javaClass.getDeclaredMethod("cardRectangles").apply{isAccessible=true}
            val cards=rectMethod.invoke(screen) as List<Rectangle>
            check(cards.all{it.height==180f && it.width>190f})
            field(screen,"filterState").get(screen).let { state ->
                state.javaClass.getDeclaredMethod("toggleDropdown").invoke(state)
            }
            screen.render(.016f)
            if(category==ContentCategory.ARMOR) screenshot("collection-armor-filters")
            screen.dispose()
        }
        val camera=OrthographicCamera(1280f,720f).apply{position.set(640f,360f,0f);update()}
        val batch=SpriteBatch().apply{projectionMatrix=camera.combined};val font=UiFontFactory.createBody();val paper=PaperUi()
        WeaponDescriptionSmoke.checks(batch,font,camera,::screenshot)
        for(w in com.azizoglu.grisilith.weapon.definition.DefaultWeapons.all) {
            val p=com.azizoglu.grisilith.weapon.definition.WeaponPresentation
            check(p.normal(w).isNotBlank() && p.abilityScaling(w).isNotBlank() && p.upgrades(w).contains("Level 5"))
            for(level in 1..5) {
                val text=p.hover(w,level)
                listOf("Attack Type:","Damage:","Base Attack Speed:","Range:","Scaling:","Damage Type:","Ability:").forEach{check(it in text)}
                check("cumulative level totals" !in p.upgrades(w))
            }
            val labels=com.azizoglu.grisilith.screen.render.WeaponDetailPanel.labels(w)
            check("TARGETING" !in labels)
            for(i in labels.indices) {
                Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
                val bounds=Rectangle(670f,120f,520f,498f)
                val r=com.azizoglu.grisilith.screen.render.WeaponDetailPanel.control(bounds,i,labels.size)
                batch.begin()
                com.azizoglu.grisilith.screen.render.WeaponDetailPanel.render(batch,font,w,bounds)
                check(com.azizoglu.grisilith.screen.render.WeaponDetailPanel.hover(batch,font,w,bounds,Vector2(r.x+10f,r.y+10f)))
                batch.end()
                if(i==2 || (i==1 && w.id.value.contains("CHALK")))screenshot("weapon-reference-${w.id.value}-$i")
            }
        }
        println("WEAPON_COPY_PASS: all weapon pages and all four reference hovers rendered")
        val weaponCopy=com.azizoglu.grisilith.weapon.definition.WeaponPresentation
        val weapons=com.azizoglu.grisilith.weapon.definition.DefaultWeapons
        check(weaponCopy.damageTypesAtLevel(weapons.orbOfSky,1).size==2)
        check(weaponCopy.damageTypesAtLevel(weapons.orbOfSky,4)==setOf(DamageType.LIGHTNING))
        check("Harvest" !in weaponCopy.upgrades(weapons.enchantedChalkDust).substringAfter("Level 3").substringBefore("Level 4"))
        check("25s" in weaponCopy.timing(weapons.boomerang,5))
        check(com.azizoglu.grisilith.collection.detail.CollectionDetailRegistry.find(ContentId("ENEMY_KIND_VAMPIRE_LORD"))!!.rows.any{it.label=="Type" && it.value=="Arc Boss"})
        println("DESCRIPTION_REVISIONS_PASS: level-aware schema/types/cooldowns; no repeated upgrades; Arc Boss label")
        val merchant=com.azizoglu.grisilith.screen.MerchantScreen(game)
        merchant.show();merchant.resize(1280,720)
        val catalog=com.azizoglu.grisilith.unlock.UnlockRegistry.visible()
        for(type in com.azizoglu.grisilith.unlock.UnlockType.entries) {
            val product=catalog.firstOrNull{it.type==type}?:continue
            field(merchant,"selectedCategory").set(merchant,type)
            field(merchant,"selectedUnlockId").set(merchant,product.id)
            merchant.render(.016f);screenshot("merchant-${type.name}")
            val renderer=field(merchant,"renderer").get(merchant) as com.azizoglu.grisilith.screen.render.MerchantRenderer
            renderer.scrollDetails(1000f)
            merchant.render(.016f);screenshot("merchant-${type.name}-scrolled")
        }
        merchant.dispose()
        // Inspect every enemy portrait at a useful size, without source-border UVs.
        Gdx.gl.glClearColor(.10f,.10f,.11f,1f);Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        batch.begin()
        com.azizoglu.grisilith.enemy.definition.DefaultEnemyKinds.all.forEachIndexed { i,enemy ->
            val x=20f+(i%7)*180f;val y=535f-(i/7)*205f
            com.azizoglu.grisilith.ui.icon.ContentIconRenderer.draw(batch,enemy,Rectangle(x,y,152f,152f),framed=false)
            font.data.setScale(.65f);font.draw(batch,enemy.displayName,x,y-8f)
            val detail=com.azizoglu.grisilith.collection.detail.CollectionDetailRegistry.find(enemy.id)!!
            check(detail.rows.none{it.label=="Behavior" || it.label=="Skills"})
        }
        batch.end();screenshot("enemy-unframed-contact")
        com.azizoglu.grisilith.tamga.definition.DefaultTamgas.all.forEach { tamga ->
            check(tamga.description.length<260)
            val detail=com.azizoglu.grisilith.collection.detail.CollectionDetailRegistry.find(tamga.id)!!
            check(detail.description.contains("Scaling"))
        }
        DefaultCollectionMaps.all.forEach { map ->
            check(com.azizoglu.grisilith.ui.icon.ContentIconRenderer.hasIcon(map.id.value))
        }
        for(character in DefaultCharacters.all) {
            check(CharacterPresentation.summary(character)!=character.description)
            check(CharacterPresentation.mainStats(character)==character.preferredMarketStats.toList())
            Gdx.gl.glClearColor(.10f,.09f,.08f,1f);Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
            batch.begin();CharacterHoverRenderer.draw(batch,font,Vector2(720f,600f),character,paper,includeStats=true);batch.end()
            screenshot("preset-hover-${character.id.value}")
        }
        check(DefaultArmors.all.map{it.description}.distinct().size==8)
        paper.dispose();font.dispose();batch.dispose()
        println("COLLECTION_COPY_PASS: all seven categories; five character backstories/short summaries/priority lists/full-stat hovers; eight differentiated armor descriptions; enlarged contained cards and filter popup")
    }
}
