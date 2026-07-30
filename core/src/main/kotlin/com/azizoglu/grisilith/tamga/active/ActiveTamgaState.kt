package com.azizoglu.grisilith.tamga.runtime.active

enum class ActiveTamgaState {

    /*
     * Skill kullanılmaya hazır.
     */
    READY,

    /*
     * Skill şu anda çalışıyor.
     */
    ACTIVE,

    /*
     * Skill bittikten veya bozulduktan sonra
     * yeniden kullanılamadığı dönem.
     */
    COOLDOWN
}
