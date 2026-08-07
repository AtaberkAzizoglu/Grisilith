package com.azizoglu.grisilith.stage

class StageController(
    private val normalStageDurationSeconds: Float,
    private val miniBossStageDurationSeconds: Float,
    private val finalBossStageDurationSeconds: Float,
    val maximumStage: Int
) {

    init {
        /*
         * Constructor parameters are kept for source compatibility.
         * The actual Stage duration table is resolved centrally below.
         */
        require(normalStageDurationSeconds > 0f)
        require(miniBossStageDurationSeconds > 0f)
        require(finalBossStageDurationSeconds > 0f)
        require(maximumStage > 0)
    }

    var currentStage: Int =
        1
        private set

    var elapsedStageTime: Float =
        0f
        private set

    val currentStageType: StageType
        get() =
            determineStageType(
                currentStage
            )

    val currentStageDuration: Float
        get() =
            durationFor(
                stage =
                    currentStage,

                stageType =
                    currentStageType
            )

    val hasTimeLimit: Boolean
        get() =
            currentStageType !=
                StageType.FINAL_BOSS

    val remainingStageTime: Float
        get() =
            if (
                !hasTimeLimit
            ) {
                Float.POSITIVE_INFINITY
            } else {
                (
                    currentStageDuration -
                        elapsedStageTime
                    )
                    .coerceAtLeast(
                        0f
                    )
            }

    val isRunComplete: Boolean
        get() =
            currentStage >
                maximumStage

    /*
     * NORMAL ve MINI_BOSS Stage süre dolduğunda tamamlanır.
     *
     * MINI_BOSS'un ölmesi Stage'i erken bitirmez.
     * FINAL_BOSS Stage'in süre sınırı yoktur ve yalnızca
     * Final Boss öldürüldüğünde dışarıdan tamamlanır.
     */
    fun update(
        delta: Float
    ): Boolean {
        if (
            isRunComplete ||
            !hasTimeLimit
        ) {
            return false
        }

        elapsedStageTime +=
            delta.coerceAtLeast(
                0f
            )

        return elapsedStageTime >=
            currentStageDuration
    }

    fun completeCurrentStage() {
        if (
            isRunComplete
        ) {
            return
        }

        currentStage++
        elapsedStageTime =
            0f
    }

    fun restoreCheckpointStage(
        checkpointStage: Int
    ) {
        require(
            checkpointStage in
                1..maximumStage
        ) {
            "Checkpoint stage must be between 1 and $maximumStage."
        }

        currentStage =
            checkpointStage

        elapsedStageTime =
            0f
    }

    private fun durationFor(
        stage: Int,
        stageType: StageType
    ): Float {
        return when (
            stageType
        ) {
            StageType.FINAL_BOSS -> {
                Float.POSITIVE_INFINITY
            }

            StageType.MINI_BOSS -> {
                MINI_BOSS_STAGE_DURATION_SECONDS
            }

            StageType.NORMAL -> {
                normalDurationForStage(
                    stage
                )
            }
        }
    }

    private fun normalDurationForStage(
        stage: Int
    ): Float {
        return when {
            stage <= 1 -> {
                30f
            }

            stage <= 4 -> {
                30f +
                    (stage - 1) *
                    5f
            }

            stage in 6..9 -> {
                45f +
                    (stage - 6) *
                    5f
            }

            else -> {
                60f
            }
        }
    }

    private fun determineStageType(
        stage: Int
    ): StageType {
        return when {
            stage ==
                maximumStage -> {
                StageType.FINAL_BOSS
            }

            stage >
                0 &&
                stage %
                5 ==
                0 -> {
                StageType.MINI_BOSS
            }

            else -> {
                StageType.NORMAL
            }
        }
    }

    companion object {

        private const val MINI_BOSS_STAGE_DURATION_SECONDS =
            90f
    }
}
