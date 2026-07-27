package com.azizoglu.grisilith.chest

data class ChestRewardChoiceSet(
    val choices:
    List<ChestRewardRoll>
) {

    companion object {
        const val CHOICE_COUNT =
            3
    }

    init {
        require(
            choices.size ==
                CHOICE_COUNT
        ) {
            "A chest must contain exactly " +
                "$CHOICE_COUNT reward choices."
        }
    }
}
