package qa

import com.azizoglu.grisilith.weapon.runtime.*
import com.azizoglu.grisilith.weapon.definition.DefaultWeapons
import com.azizoglu.grisilith.screen.pause.*
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.utils.viewport.FitViewport
import com.badlogic.gdx.math.Vector2

object PauseInventorySmoke {
    fun checks() {
        val inv=WeaponInventory();val state=PauseInteractionState();var changes=0
        val drops=PauseWeaponInventoryController(inv,state){changes++}
        val sword=WeaponInstance(DefaultWeapons.doubleSwords,2)
        val bow=WeaponInstance(DefaultWeapons.longbow,1)
        inv.addWeapon(sword);inv.addWeapon(bow)
        drops.drop(0,1);check(inv.weaponAt(1)===sword && inv.weaponAt(0)===bow)
        drops.drop(1,4);check(inv.weaponAt(4)===sword && inv.weaponAt(1)==null)
        val count=changes;drops.drop(4,-1);drops.drop(4,4);check(changes==count)
        inv.weaponSlots[2].weapon=WeaponInstance(DefaultWeapons.doubleSwords,1)
        drops.drop(2,4);check(sword.level==3 && inv.weaponAt(2)==null && inv.weaponAt(4)===sword)
        inv.weaponSlots[2].weapon=WeaponInstance(DefaultWeapons.doubleSwords,2)
        drops.drop(2,4);check(state.confirmationType==PauseConfirmationType.MERGE_WEAPONS && sword.level==3)
        state.closeAllModals();check(sword.level==3 && inv.weaponAt(2)!=null)
        drops.drop(2,4);drops.confirm();check(sword.level==4 && inv.weaponAt(2)==null)
        inv.weaponSlots[2].weapon=WeaponInstance(DefaultWeapons.doubleSwords,2)
        drops.drop(2,4);inv.swapSlots(2,3);drops.confirm();check(sword.level==4 && inv.weaponAt(3)!=null)
        inv.weaponSlots[2].weapon=WeaponInstance(DefaultWeapons.doubleSwords,1)
        drops.drop(2,4);check(sword.level==5)
        inv.weaponSlots[2].weapon=WeaponInstance(DefaultWeapons.doubleSwords,1)
        drops.drop(2,4);check(sword.level==5 && inv.weaponAt(2)!=null && !state.isAnyModalOpen)

        // Drive the real pause input controller through press, drag and release frames.
        val camera=OrthographicCamera();val viewport=FitViewport(1280f,720f,camera)
        viewport.update(Gdx.graphics.width,Gdx.graphics.height,true)
        val layout=PauseLayout.create(640f,360f,1280f,720f)
        val original=Gdx.input;var held=false;var mx=0;var my=0
        val input=java.lang.reflect.Proxy.newProxyInstance(Input::class.java.classLoader,arrayOf(Input::class.java)) { _,method,args ->
            when(method.name) {
                "getX"->mx;"getY"->my
                "isButtonPressed"->held && args?.firstOrNull()==Input.Buttons.LEFT
                "isKeyJustPressed"->false
                else -> when(method.returnType) { java.lang.Boolean.TYPE->false; java.lang.Integer.TYPE->0;java.lang.Float.TYPE->0f;java.lang.Long.TYPE->0L; else->null }
            }
        } as Input
        val controller=PauseInputController(viewport,{layout},state,{inv.weaponAt(it)!=null},{0},drops::drop,drops::confirm)
        fun at(index:Int) {
            val rect=layout.weaponSlots[index]
            val p=Vector2(rect.x+rect.width/2,rect.y+rect.height/2);viewport.project(p)
            mx=p.x.toInt();my=Gdx.graphics.height-p.y.toInt()
        }
        try {
            Gdx.input=input
            at(0);held=true;controller.update();check(state.draggedWeaponSlotIndex==null)
            at(5);controller.update();check(state.draggedWeaponSlotIndex==0 && state.weaponDropTargetIndex==5)
            held=false;controller.update();check(inv.weaponAt(5)===bow && inv.weaponAt(0)==null && state.draggedWeaponSlotIndex==null)
            at(5);held=true;controller.update();mx=-200;my=-200;controller.update()
            held=false;controller.update();check(inv.weaponAt(5)===bow)
        } finally { Gdx.input=original;controller.reset() }
        println("PAUSE_INVENTORY_PASS: actual drag/drop, outside cancellation, empty/different swaps, merge, high-level confirmation/cancel, stale protection, level cap; weapon instances preserved")
    }
}
