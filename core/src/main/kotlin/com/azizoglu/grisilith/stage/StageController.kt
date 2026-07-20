package com.azizoglu.grisilith.stage

class StageController(
    private val normalStageDurationSeconds: Float,
    private val miniBossStageDurationSeconds: Float,
    private val finalBossStageDurationSeconds: Float,
    val maximumStage: Int
) {
    init {
        require(normalStageDurationSeconds > 0f)
        require(miniBossStageDurationSeconds > 0f)
        require(finalBossStageDurationSeconds > 0f)
        require(maximumStage > 0)
    }

    var currentStage: Int = 1
        private set

    var elapsedStageTime: Float = 0f
        private set

    val currentStageType: StageType
        get() = determineStageType(currentStage)

    val currentStageDuration: Float
        get() = when (currentStageType) {
            StageType.NORMAL ->
                normalStageDurationSeconds

            StageType.MINI_BOSS ->
                miniBossStageDurationSeconds

            StageType.FINAL_BOSS ->
                finalBossStageDurationSeconds
        }

    val remainingStageTime: Float
        get() = (
            currentStageDuration - elapsedStageTime
            ).coerceAtLeast(0f)

    val isFinalStage: Boolean
        get() = currentStage == maximumStage

    val isRunComplete: Boolean
        get() = currentStage > maximumStage

    fun update(delta: Float): Boolean {
        if (isRunComplete) {
            return false
        }

        elapsedStageTime += delta

        if (elapsedStageTime < currentStageDuration) {
            return false
        }

        elapsedStageTime -= currentStageDuration
        currentStage++

        return true
    }

    private fun determineStageType(
        stage: Int
    ): StageType {
        return when {
            stage == maximumStage ->
                StageType.FINAL_BOSS

            stage > 0 && stage % 5 == 0 ->
                StageType.MINI_BOSS

            else ->
                StageType.NORMAL
        }
    }
}
