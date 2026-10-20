package com.azizoglu.grisilith.profile

import com.badlogic.gdx.Application
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Preferences
import java.lang.reflect.Proxy

/** Isolated, in-memory preferences: never reads or overwrites the developer's actual wallet. */
fun main() {
    val stored=mutableMapOf<String,Any>()
    lateinit var prefs:Preferences
    prefs=Proxy.newProxyInstance(Preferences::class.java.classLoader,arrayOf(Preferences::class.java)) {_,method,args->
        val a=args?:emptyArray()
        when(method.name) {
            "contains"->stored.containsKey(a[0])
            "getInteger"->stored[a[0]]?:a.getOrElse(1){0}
            "getString"->stored[a[0]]?:a.getOrElse(1){""}
            "putInteger","putString"->{stored[a[0] as String]=a[1];prefs}
            "clear"->{stored.clear();null}
            "flush"->null
            else->error("Unexpected preferences method: ${method.name}")
        }
    } as Preferences
    val previous=Gdx.app
    Gdx.app=Proxy.newProxyInstance(Application::class.java.classLoader,arrayOf(Application::class.java)) {_,method,_->
        when(method.name){"getPreferences"->prefs;else->null}
    } as Application
    try {
        PlayerProfileRepository.load()
        check(PlayerProfileRepository.profile.grisilithCoins==1000)
        check(stored["grisilith_coins"]==1000)
        check(PlayerProfileRepository.spendGrisilithCoins(125))
        PlayerProfileRepository.load()
        check(PlayerProfileRepository.profile.grisilithCoins==875)
        stored["grisilith_coins"]=0
        PlayerProfileRepository.load()
        check(PlayerProfileRepository.profile.grisilithCoins==0)
        stored.clear();stored["version"]=4 // A legacy profile without a coin field is not a new player.
        PlayerProfileRepository.load()
        check(PlayerProfileRepository.profile.grisilithCoins==0)
        PlayerProfileRepository.resetForBeta()
        check(PlayerProfileRepository.profile.grisilithCoins==1000)
        PlayerProfileRepository.load()
        check(PlayerProfileRepository.profile.grisilithCoins==1000)
        println("BETA_COINS_PASS: new=1000, spent balance/zero/legacy preserved, explicit beta reset=1000")
    } finally {Gdx.app=previous}
}
