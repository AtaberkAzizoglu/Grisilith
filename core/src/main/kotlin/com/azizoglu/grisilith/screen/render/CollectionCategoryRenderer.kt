package com.azizoglu.grisilith.screen.render

import com.azizoglu.grisilith.collection.CollectionEntry
import com.azizoglu.grisilith.content.ContentCategory
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.utils.Align

class CollectionCategoryRenderer {

    companion object {

        private const val VIEWPORT_WIDTH =
            1280f

        private const val TITLE_Y =
            665f

        private const val SUBTITLE_Y =
            615f

        private const val CARD_NAME_HEIGHT =
            42f
    }

    fun renderCardBackgrounds(
        shapeRenderer: ShapeRenderer,
        entries: List<CollectionEntry>,
        cardBounds: List<Rectangle>,
        selectedLocalIndex: Int?
    ) {

        shapeRenderer.begin(
            ShapeRenderer.ShapeType.Filled
        )

        entries.forEachIndexed {
                index,
                entry ->

            val bounds =
                cardBounds[index]

            shapeRenderer.color =
                when {

                    entry.isUnknown ->
                        Color(
                            0.09f,
                            0.09f,
                            0.09f,
                            1f
                        )

                    entry.unlocked ->
                        Color(
                            0.12f,
                            0.10f,
                            0.17f,
                            1f
                        )

                    else ->
                        Color(
                            0.075f,
                            0.07f,
                            0.085f,
                            1f
                        )
                }

            shapeRenderer.rect(
                bounds.x,
                bounds.y,
                bounds.width,
                bounds.height
            )

            shapeRenderer.color =
                when {

                    entry.isUnknown ->
                        Color(
                            0.13f,
                            0.13f,
                            0.13f,
                            1f
                        )

                    entry.unlocked ->
                        Color(
                            0.18f,
                            0.14f,
                            0.24f,
                            1f
                        )

                    else ->
                        Color(
                            0.10f,
                            0.09f,
                            0.11f,
                            1f
                        )
                }

            shapeRenderer.rect(
                bounds.x,
                bounds.y,
                bounds.width,
                CARD_NAME_HEIGHT
            )
        }

        shapeRenderer.end()

        Gdx.gl.glLineWidth(
            3f
        )

        shapeRenderer.begin(
            ShapeRenderer.ShapeType.Line
        )

        entries.forEachIndexed {
                index,
                _ ->

            val bounds =
                cardBounds[index]

            shapeRenderer.color =
                if (
                    selectedLocalIndex ==
                    index
                ) {
                    Color.GOLD
                } else {
                    Color(
                        0.42f,
                        0.37f,
                        0.52f,
                        1f
                    )
                }

            shapeRenderer.rect(
                bounds.x,
                bounds.y,
                bounds.width,
                bounds.height
            )
        }

        shapeRenderer.end()

        Gdx.gl.glLineWidth(
            1f
        )
    }

    fun renderDetailBackground(
        shapeRenderer: ShapeRenderer,
        panelBounds: Rectangle,
        entry: CollectionEntry
    ) {

        shapeRenderer.begin(
            ShapeRenderer.ShapeType.Filled
        )

        shapeRenderer.color =
            Color(
                0.025f,
                0.018f,
                0.04f,
                0.98f
            )

        shapeRenderer.rect(
            panelBounds.x,
            panelBounds.y,
            panelBounds.width,
            panelBounds.height
        )

        /*
         * Geçici büyük görsel alanı.
         */
        shapeRenderer.color =
            Color(
                0.12f,
                0.10f,
                0.16f,
                1f
            )

        shapeRenderer.rect(
            panelBounds.x +
                30f,
            panelBounds.y +
                panelBounds.height -
                190f,
            150f,
            150f
        )

        /*
         * Başlık arka planı.
         */
        shapeRenderer.color =
            if (
                entry.unlocked
            ) {
                Color(
                    0.18f,
                    0.14f,
                    0.25f,
                    1f
                )
            } else {
                Color(
                    0.11f,
                    0.10f,
                    0.13f,
                    1f
                )
            }

        shapeRenderer.rect(
            panelBounds.x +
                205f,
            panelBounds.y +
                panelBounds.height -
                90f,
            panelBounds.width -
                235f,
            60f
        )

        shapeRenderer.end()

        Gdx.gl.glLineWidth(
            3f
        )

        shapeRenderer.begin(
            ShapeRenderer.ShapeType.Line
        )

        shapeRenderer.color =
            if (
                entry.unlocked
            ) {
                Color(
                    0.55f,
                    0.40f,
                    0.78f,
                    1f
                )
            } else {
                Color.GRAY
            }

        shapeRenderer.rect(
            panelBounds.x,
            panelBounds.y,
            panelBounds.width,
            panelBounds.height
        )

        shapeRenderer.rect(
            panelBounds.x +
                30f,
            panelBounds.y +
                panelBounds.height -
                190f,
            150f,
            150f
        )

        shapeRenderer.end()

        Gdx.gl.glLineWidth(
            1f
        )
    }

    fun renderTexts(
        batch: SpriteBatch,
        font: BitmapFont,
        category: ContentCategory,
        entries: List<CollectionEntry>,
        cardBounds: List<Rectangle>,
        currentPage: Int,
        pageCount: Int,
        selectedEntry: CollectionEntry?,
        detailPanelBounds: Rectangle
    ) {

        val oldScaleX =
            font.data.scaleX

        val oldScaleY =
            font.data.scaleY

        val oldColor =
            Color(
                font.color
            )

        if (
            selectedEntry == null
        ) {
            renderTitle(
                batch =
                    batch,

                font =
                    font,

                category =
                    category
            )

            renderCards(
                batch =
                    batch,

                font =
                    font,

                entries =
                    entries,

                cardBounds =
                    cardBounds
            )

            if (
                pageCount > 1
            ) {
                renderPageIndicator(
                    batch =
                        batch,

                    font =
                        font,

                    currentPage =
                        currentPage,

                    pageCount =
                        pageCount
                )
            }
        } else {
            renderDetailTexts(
                batch =
                    batch,

                font =
                    font,

                entry =
                    selectedEntry,

                panelBounds =
                    detailPanelBounds
            )
        }

        font.data.setScale(
            oldScaleX,
            oldScaleY
        )

        font.color =
            oldColor
    }

    private fun renderTitle(
        batch: SpriteBatch,
        font: BitmapFont,
        category: ContentCategory
    ) {

        font.data.setScale(
            2f
        )

        font.color =
            Color.WHITE

        font.draw(
            batch,
            categoryTitle(
                category
            ),
            0f,
            TITLE_Y,
            VIEWPORT_WIDTH,
            Align.center,
            false
        )

        font.data.setScale(
            0.85f
        )

        font.color =
            Color.LIGHT_GRAY

        font.draw(
            batch,
            "Select an entry to view its details.",
            0f,
            SUBTITLE_Y,
            VIEWPORT_WIDTH,
            Align.center,
            false
        )
    }

    private fun renderCards(
        batch: SpriteBatch,
        font: BitmapFont,
        entries: List<CollectionEntry>,
        cardBounds: List<Rectangle>
    ) {

        entries.forEachIndexed {
                index,
                entry ->

            val bounds =
                cardBounds[index]

            font.data.setScale(
                if (
                    entry.unlocked
                ) {
                    1.05f
                } else {
                    1.25f
                }
            )

            font.color =
                if (
                    entry.unlocked
                ) {
                    Color.LIGHT_GRAY
                } else {
                    Color.GRAY
                }

            font.draw(
                batch,
                when {

                    entry.isUnknown ->
                        "????"

                    entry.unlocked ->
                        categoryPlaceholder(
                            entry.definition.category
                        )

                    else ->
                        "LOCKED"
                },

                bounds.x,
                bounds.y +
                    CARD_NAME_HEIGHT +
                    47f,
                bounds.width,
                Align.center,
                false
            )

            font.data.setScale(
                0.72f
            )

            font.color =
                Color.WHITE

            font.draw(
                batch,
                if (
                    entry.isUnknown
                ) {
                    "????"
                } else {
                    entry.definition.displayName
                },
                bounds.x +
                    6f,
                bounds.y +
                    CARD_NAME_HEIGHT -
                    10f,
                bounds.width -
                    12f,
                Align.center,
                true
            )
        }
    }

    private fun renderDetailTexts(
        batch: SpriteBatch,
        font: BitmapFont,
        entry: CollectionEntry,
        panelBounds: Rectangle
    ) {

        val definition =
            entry.definition

        /*
         * Geçici büyük görsel alanı yazısı.
         */
        font.data.setScale(
            if (
                entry.unlocked
            ) {
                0.9f
            } else {
                1.2f
            }
        )

        font.color =
            if (
                entry.unlocked
            ) {
                Color.LIGHT_GRAY
            } else {
                Color.GRAY
            }

        font.draw(
            batch,
            if (
                entry.unlocked
            ) {
                categoryPlaceholder(
                    definition.category
                )
            } else {
                "LOCKED"
            },
            panelBounds.x +
                30f,
            panelBounds.y +
                panelBounds.height -
                110f,
            150f,
            Align.center,
            false
        )

        /*
         * İsim.
         */
        font.data.setScale(
            1.35f
        )

        font.color =
            Color.WHITE

        font.draw(
            batch,
            definition.displayName,
            panelBounds.x +
                225f,
            panelBounds.y +
                panelBounds.height -
                51f,
            panelBounds.width -
                275f,
            Align.left,
            false
        )

        /*
         * Kategori.
         */
        font.data.setScale(
            0.85f
        )

        font.color =
            Color.LIGHT_GRAY

        font.draw(
            batch,
            categoryTitle(
                definition.category
            ),
            panelBounds.x +
                225f,
            panelBounds.y +
                panelBounds.height -
                115f
        )

        /*
         * Kilit durumu.
         */
        font.color =
            if (
                entry.unlocked
            ) {
                Color(
                    0.35f,
                    0.85f,
                    0.45f,
                    1f
                )
            } else {
                Color(
                    0.90f,
                    0.55f,
                    0.35f,
                    1f
                )
            }

        font.draw(
            batch,
            if (
                entry.unlocked
            ) {
                "UNLOCKED"
            } else {
                "LOCKED"
            },
            panelBounds.x +
                225f,
            panelBounds.y +
                panelBounds.height -
                150f
        )

        /*
         * Açıklama.
         */
        font.data.setScale(
            0.82f
        )

        font.color =
            Color.WHITE

        font.draw(
            batch,
            definition.description,
            panelBounds.x +
                225f,
            panelBounds.y +
                panelBounds.height -
                195f,
            panelBounds.width -
                270f,
            Align.left,
            true
        )

        /*
         * Tag listesi.
         */
        font.data.setScale(
            0.72f
        )

        font.color =
            Color.LIGHT_GRAY

        val tagText =
            if (
                definition.tags.isEmpty()
            ) {
                "Tags: None"
            } else {
                "Tags: " +
                    definition.tags
                        .joinToString(
                            separator =
                                ", "
                        ) { tag ->
                            tag.name
                                .replace(
                                    "_",
                                    " "
                                )
                        }
            }

        font.draw(
            batch,
            tagText,
            panelBounds.x +
                30f,
            panelBounds.y +
                75f,
            panelBounds.width -
                60f,
            Align.left,
            true
        )

    }

    private fun renderPageIndicator(
        batch: SpriteBatch,
        font: BitmapFont,
        currentPage: Int,
        pageCount: Int
    ) {

        font.data.setScale(
            0.9f
        )

        font.color =
            Color.LIGHT_GRAY

        font.draw(
            batch,
            "${currentPage + 1} / $pageCount",
            560f,
            73f,
            160f,
            Align.center,
            false
        )
    }

    private fun categoryTitle(
        category: ContentCategory
    ): String {

        return when (
            category
        ) {

            ContentCategory.CHARACTER ->
                "CHARACTERS"

            ContentCategory.WEAPON ->
                "WEAPONS"

            ContentCategory.ARMOR ->
                "ARMORS"

            ContentCategory.ITEM ->
                "ITEMS"

            ContentCategory.TAMGA ->
                "TAMGAS"

            ContentCategory.ENEMY ->
                "ENEMIES"

            ContentCategory.MAP ->
                "MAPS"
        }
    }

    private fun categoryPlaceholder(
        category: ContentCategory
    ): String {

        return when (
            category
        ) {

            ContentCategory.CHARACTER ->
                "CHARACTER"

            ContentCategory.WEAPON ->
                "WEAPON"

            ContentCategory.ARMOR ->
                "ARMOR"

            ContentCategory.ITEM ->
                "ITEM"

            ContentCategory.TAMGA ->
                "TAMGA"

            ContentCategory.ENEMY ->
                "ENEMY"

            ContentCategory.MAP ->
                "MAP"
        }
    }
}
