package com.azizoglu.grisilith.chest

/*
 * Yeni sistemde chest toplandığı anda ödül üretilmez.
 *
 * Bu sınıf yalnızca stage içinde toplanan ve henüz
 * işlenmemiş chest sayısını tutar.
 *
 * Ödül, chest ekranında ilk kilit tıklamasında üretilir.
 */
class ChestRewardQueue {

    private var pendingChestCount =
        0

    val rewardCount: Int
        get() =
            pendingChestCount

    val isEmpty: Boolean
        get() =
            pendingChestCount <= 0

    val isNotEmpty: Boolean
        get() =
            pendingChestCount > 0

    fun addChest() {
        pendingChestCount +=
            1
    }

    /*
     * Stage sonu ekranı açılırken bütün chest sayısını
     * tek seferde Controller'a aktarır ve kuyruğu temizler.
     */
    fun takeAll():
        Int {

        val result =
            pendingChestCount

        pendingChestCount =
            0

        return result
    }

    fun clear() {
        pendingChestCount =
            0
    }
}
