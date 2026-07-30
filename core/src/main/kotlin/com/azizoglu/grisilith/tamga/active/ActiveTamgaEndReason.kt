package com.azizoglu.grisilith.tamga.runtime.active

enum class ActiveTamgaEndReason {

    /*
     * Skill normal süresini tamamladı.
     */
    COMPLETED,

    /*
     * Skill kendi başarısızlık koşulu
     * nedeniyle yarıda kesildi.
     */
    INTERRUPTED,

    /*
     * Run yeniden başladı, Tamga çıkarıldı
     * veya sistem dışarıdan sıfırlandı.
     */
    RESET
}
