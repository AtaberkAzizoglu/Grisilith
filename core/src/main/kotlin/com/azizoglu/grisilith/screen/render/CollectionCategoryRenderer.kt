package com.azizoglu.grisilith.screen.render

import com.azizoglu.grisilith.collection.CollectionEntry
import com.azizoglu.grisilith.collection.detail.CollectionDetailDefinition
import com.azizoglu.grisilith.collection.detail.CollectionDetailRegistry
import com.azizoglu.grisilith.collection.filter.CollectionFilterOption
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

    fun renderFilterBackgrounds(
        shapeRenderer: ShapeRenderer,
        headerBounds: Rectangle,
        optionBounds: List<Rectangle>,
        dropdownOpen: Boolean,
        selectedOptionIds: Set<String>,
        options: List<CollectionFilterOption>
    ) {

        shapeRenderer.begin(
            ShapeRenderer.ShapeType.Filled
        )

        shapeRenderer.color =
            Color(
                0.12f,
                0.10f,
                0.17f,
                1f
            )

        shapeRenderer.rect(
            headerBounds.x,
            headerBounds.y,
            headerBounds.width,
            headerBounds.height
        )

        if (
            dropdownOpen
        ) {
            optionBounds.forEachIndexed {
                    index,
                    bounds ->

                val selected =
                    options[index].id in
                        selectedOptionIds

                shapeRenderer.color =
                    if (
                        selected
                    ) {
                        Color(
                            0.20f,
                            0.30f,
                            0.22f,
                            1f
                        )
                    } else {
                        Color(
                            0.065f,
                            0.055f,
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
                    if (
                        selected
                    ) {
                        Color(
                            0.35f,
                            0.85f,
                            0.45f,
                            1f
                        )
                    } else {
                        Color(
                            0.18f,
                            0.16f,
                            0.22f,
                            1f
                        )
                    }

                shapeRenderer.rect(
                    bounds.x +
                        10f,
                    bounds.y +
                        9f,
                    20f,
                    20f
                )
            }
        }

        shapeRenderer.end()

        Gdx.gl.glLineWidth(
            2f
        )

        shapeRenderer.begin(
            ShapeRenderer.ShapeType.Line
        )

        shapeRenderer.color =
            Color(
                0.42f,
                0.37f,
                0.52f,
                1f
            )

        shapeRenderer.rect(
            headerBounds.x,
            headerBounds.y,
            headerBounds.width,
            headerBounds.height
        )

        if (
            dropdownOpen
        ) {
            optionBounds.forEach {
                    bounds ->

                shapeRenderer.rect(
                    bounds.x,
                    bounds.y,
                    bounds.width,
                    bounds.height
                )
            }
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
        detailPanelBounds: Rectangle,
        filterOptions: List<CollectionFilterOption>,
        filterHeaderBounds: Rectangle,
        filterOptionBounds: List<Rectangle>,
        filterDropdownOpen: Boolean,
        selectedFilterIds: Set<String>
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
                entries.isEmpty()
            ) {
                renderEmptyFilterResult(
                    batch =
                        batch,

                    font =
                        font
                )
            }

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

    fun renderFilterTexts(
        batch: SpriteBatch,
        font: BitmapFont,
        options: List<CollectionFilterOption>,
        headerBounds: Rectangle,
        optionBounds: List<Rectangle>,
        dropdownOpen: Boolean,
        selectedOptionIds: Set<String>
    ) {

        font.data.setScale(
            0.78f
        )

        font.color =
            Color.WHITE

        val headerText =
            if (
                selectedOptionIds.isEmpty()
            ) {
                "FILTER: ALL"
            } else {
                "FILTER: ${selectedOptionIds.size} ACTIVE"
            }

        font.draw(
            batch,
            headerText,
            headerBounds.x +
                12f,
            headerBounds.y +
                28f,
            headerBounds.width -
                24f,
            Align.left,
            false
        )

        font.data.setScale(
            0.70f
        )

        font.color =
            Color.LIGHT_GRAY

        font.draw(
            batch,
            if (
                dropdownOpen
            ) {
                "^"
            } else {
                "v"
            },
            headerBounds.x +
                headerBounds.width -
                28f,
            headerBounds.y +
                28f
        )

        if (
            !dropdownOpen
        ) {
            return
        }

        optionBounds.forEachIndexed {
                index,
                bounds ->

            val option =
                options[index]

            val selected =
                option.id in
                    selectedOptionIds

            font.data.setScale(
                0.70f
            )

            font.color =
                option.textColor

            font.draw(
                batch,
                option.displayName,
                bounds.x +
                    40f,
                bounds.y +
                    25f,
                bounds.width -
                    50f,
                Align.left,
                false
            )

            if (
                selected
            ) {
                font.data.setScale(
                    0.72f
                )

                font.color =
                    Color.WHITE

                font.draw(
                    batch,
                    "X",
                    bounds.x +
                        15f,
                    bounds.y +
                        25f
                )
            }
        }
    }

    private fun renderEmptyFilterResult(
        batch: SpriteBatch,
        font: BitmapFont
    ) {

        font.data.setScale(
            1f
        )

        font.color =
            Color.GRAY

        font.draw(
            batch,
            "NO MATCHING CONTENT",
            0f,
            355f,
            VIEWPORT_WIDTH,
            Align.center,
            false
        )
    }

    private fun renderDetailTexts(
        batch: SpriteBatch,
        font: BitmapFont,
        entry: CollectionEntry,
        panelBounds: Rectangle
    ) {

        val definition =
            entry.definition

        val collectionDetail =
            CollectionDetailRegistry.find(
                definition.id
            )

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

        renderCustomCollectionDetail(
            batch =
                batch,

            font =
                font,

            detail =
                collectionDetail,

            panelBounds =
                panelBounds
        )
    }

    private fun renderCustomCollectionDetail(
        batch: SpriteBatch,
        font: BitmapFont,
        detail: CollectionDetailDefinition?,
        panelBounds: Rectangle
    ) {

        if (
            detail == null
        ) {
            font.data.setScale(
                1.1f
            )

            font.color =
                Color.GRAY

            font.draw(
                batch,
                "COMING SOON",
                panelBounds.x +
                    225f,
                panelBounds.y +
                    panelBounds.height -
                    235f,
                panelBounds.width -
                    270f,
                Align.left,
                false
            )

            return
        }

        /*
         * Elle yazılmış ana açıklama.
         */
        font.data.setScale(
            0.82f
        )

        font.color =
            Color.WHITE

        font.draw(
            batch,
            detail.description,
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

        var currentY =
            panelBounds.y +
                panelBounds.height -
                285f

        /*
         * Özel bilgi satırları.
         */
        detail.rows.forEach {
                row ->

            font.data.setScale(
                0.76f
            )

            font.color =
                Color.LIGHT_GRAY

            font.draw(
                batch,
                "${row.label}:",
                panelBounds.x +
                    225f,
                currentY,
                180f,
                Align.left,
                false
            )

            font.color =
                Color.WHITE

            font.draw(
                batch,
                row.value,
                panelBounds.x +
                    405f,
                currentY,
                panelBounds.width -
                    450f,
                Align.left,
                true
            )

            currentY -=
                30f
        }

        /*
         * Ek notlar.
         */
        if (
            detail.notes.isNotEmpty()
        ) {
            currentY -=
                12f

            detail.notes.forEach {
                    note ->

                font.data.setScale(
                    0.70f
                )

                font.color =
                    Color.LIGHT_GRAY

                font.draw(
                    batch,
                    "- $note",
                    panelBounds.x +
                        225f,
                    currentY,
                    panelBounds.width -
                        270f,
                    Align.left,
                    true
                )

                currentY -=
                    34f
            }
        }
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
