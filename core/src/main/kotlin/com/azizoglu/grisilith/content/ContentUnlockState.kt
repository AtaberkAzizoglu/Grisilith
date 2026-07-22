package com.azizoglu.grisilith.content

enum class ContentUnlockState {
    /*
     * Ana mağazadan henüz satın alınmamış.
     * Run içi markette ve sandıklarda çıkamaz.
     */
    LOCKED,

    /*
     * Kalıcı olarak açılmış.
     * Run içi market ve sandık havuzuna girebilir.
     */
    UNLOCKED,

    /*
     * Oyuncunun kalıcı koleksiyonunda bulunuyor.
     * Şimdilik UNLOCKED ile benzer davranacak;
     * ileride collection sistemi farkı kullanacak.
     */
    OWNED
}
