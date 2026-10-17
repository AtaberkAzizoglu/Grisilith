package com.azizoglu.grisilith.enemy.visual

import com.azizoglu.grisilith.enemy.definition.EnemyKindIds

/** Alpha-component bounds measured from the original art, not guessed grid cuts. */
object CorrectedEnemySpriteSheets {
    private fun animations(frames: List<EnemySpriteFrame>, seconds: Float) =
        EnemyVisualDirection.entries.mapIndexed { row, direction ->
            val poses = frames.subList(row * 5, row * 5 + 5)
            direction to EnemySpriteAnimationDefinition(poses.first(), poses.drop(1), seconds)
        }.toMap()

    val bear = EnemySpriteSheetDefinition(EnemyKindIds.GRIZZLY_BEAR,
        "enemies/beast/grizzly_bear.png", 612, 408, referenceSide = 83f,
        animations = animations(listOf(
            EnemySpriteFrame(201,28,53,72), EnemySpriteFrame(282,30,54,71), EnemySpriteFrame(362,31,55,70), EnemySpriteFrame(444,31,59,70), EnemySpriteFrame(530,31,61,68),
            EnemySpriteFrame(196,124,66,53), EnemySpriteFrame(272,128,72,49), EnemySpriteFrame(352,129,72,48), EnemySpriteFrame(435,129,76,48), EnemySpriteFrame(522,128,77,49),
            EnemySpriteFrame(196,209,67,53), EnemySpriteFrame(272,215,72,47), EnemySpriteFrame(356,215,70,48), EnemySpriteFrame(436,215,77,47), EnemySpriteFrame(522,215,76,47),
            EnemySpriteFrame(201,284,50,83), EnemySpriteFrame(283,286,48,81), EnemySpriteFrame(366,286,46,81), EnemySpriteFrame(451,287,46,79), EnemySpriteFrame(537,287,47,80)
        ), .15f))

    val crocodile = EnemySpriteSheetDefinition(EnemyKindIds.CROCODILE,
        "enemies/beast/crocodile.png", 1402, 1122, referenceSide = 288f,
        animations = animations(listOf(
            EnemySpriteFrame(77,38,187,263), EnemySpriteFrame(352,45,185,262), EnemySpriteFrame(611,43,182,261), EnemySpriteFrame(873,48,194,258), EnemySpriteFrame(1170,43,176,259),
            EnemySpriteFrame(14,356,286,162), EnemySpriteFrame(308,353,266,167), EnemySpriteFrame(580,357,261,161), EnemySpriteFrame(840,356,271,165), EnemySpriteFrame(1120,350,268,168),
            EnemySpriteFrame(24,588,281,169), EnemySpriteFrame(309,587,265,170), EnemySpriteFrame(576,585,269,172), EnemySpriteFrame(854,587,262,170), EnemySpriteFrame(1119,589,264,165),
            EnemySpriteFrame(64,791,192,288), EnemySpriteFrame(345,800,196,273), EnemySpriteFrame(603,798,191,275), EnemySpriteFrame(879,802,197,276), EnemySpriteFrame(1155,797,190,275)
        ), .18f))

    private fun strip(path: String, width: Int, height: Int, poses: List<EnemySpriteFrame>) =
        EnemySpriteAnimationDefinition(poses.first(), poses.drop(1), .13f,
            texturePath = path, sourceWidth = width, sourceHeight = height)

    val spearman: EnemySpriteSheetDefinition = run {
        val front = strip("enemies/humanoid/spearman_front.png",2172,724,listOf(
            EnemySpriteFrame(64,39,357,622), EnemySpriteFrame(504,40,351,621), EnemySpriteFrame(911,45,363,616), EnemySpriteFrame(1335,45,352,617), EnemySpriteFrame(1751,47,355,614)))
        val left = strip("enemies/humanoid/spearman_left.png",2172,724,listOf(
            EnemySpriteFrame(56,108,351,525), EnemySpriteFrame(480,108,381,521), EnemySpriteFrame(894,109,402,522), EnemySpriteFrame(1301,99,418,530), EnemySpriteFrame(1742,118,387,511)))
        val back = strip("enemies/humanoid/spearman_back.png",2043,770,listOf(
            EnemySpriteFrame(45,61,341,632), EnemySpriteFrame(450,60,350,623), EnemySpriteFrame(868,60,353,633), EnemySpriteFrame(1269,63,357,634), EnemySpriteFrame(1676,60,345,635)))
        EnemySpriteSheetDefinition(EnemyKindIds.SPEARMAN,"enemies/humanoid/spearman_front.png",2172,724,
            animations = mapOf(EnemyVisualDirection.DOWN to front, EnemyVisualDirection.LEFT to left,
                EnemyVisualDirection.RIGHT to left.copy(flipX = true), EnemyVisualDirection.UP to back))
    }
}
