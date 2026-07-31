package com.azizoglu.grisilith.screen

import com.azizoglu.grisilith.GrisilithGame
import com.azizoglu.grisilith.profile.PlayerProfileRepository
import com.azizoglu.grisilith.screen.components.MenuButton
import com.azizoglu.grisilith.screen.components.MenuPointer
import com.azizoglu.grisilith.screen.render.GrisilithCoinRenderer
import com.azizoglu.grisilith.screen.render.MerchantRenderer
import com.azizoglu.grisilith.screen.render.PurchasedStatPointRenderer
import com.azizoglu.grisilith.unlock.UnlockDefinition
import com.azizoglu.grisilith.unlock.UnlockManager
import com.azizoglu.grisilith.unlock.UnlockPurchaseResult
import com.azizoglu.grisilith.unlock.UnlockRegistry
import com.azizoglu.grisilith.unlock.UnlockType
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.utils.viewport.FitViewport
import kotlin.math.abs

class MerchantScreen(
    private val game: GrisilithGame
) : ScreenAdapter() {

    companion object {
        private const val VIEWPORT_WIDTH = 1280f
        private const val VIEWPORT_HEIGHT = 720f

        private const val COLUMN_WIDTH = 270f
        private const val COLUMN_GAP = 22f
        private const val COLUMN_LEFT_PADDING = 35f

        private const val COLUMN_VIEWPORT_LEFT = 35f
        private const val COLUMN_VIEWPORT_RIGHT = 1245f
        private const val COLUMN_VIEWPORT_BOTTOM = 145f
        private const val COLUMN_VIEWPORT_TOP = 565f

        private const val CARD_HEIGHT = 72f
        private const val CARD_GAP = 12f
        private const val CARD_TOP_PADDING = 58f
        private const val DRAG_THRESHOLD = 7f
    }

    private val camera = OrthographicCamera()

    private val viewport =
        FitViewport(
            VIEWPORT_WIDTH,
            VIEWPORT_HEIGHT,
            camera
        )

    private val batch = SpriteBatch()
    private val font = BitmapFont()
    private val pointer = MenuPointer(viewport)

    private val renderer =
        MerchantRenderer(
            grisilithCoinRenderer = GrisilithCoinRenderer(),
            purchasedStatPointRenderer = PurchasedStatPointRenderer()
        )

    private var message: String? = null
    private var selectedUnlockId: String? = null
    private var hoveredCardId: String? = null
    private var pressedCardId: String? = null

    private var horizontalScrollOffset = 0f
    private val columnScrolls =
        mutableMapOf<UnlockType, Float>()
    private var previousDragX = 0f
    private var accumulatedDragDistance = 0f
    private var isDraggingColumns = false

    private val backButton =
        MenuButton(
            text = "BACK",
            x = 35f,
            y = 35f,
            width = 175f,
            height = 58f
        ) {
            game.showMainMenu()
        }

    private val buyButton =
        MenuButton(
            text = "BUY SELECTED",
            x = 500f,
            y = 35f,
            width = 280f,
            height = 58f,
            isEnabled = false
        ) {
            purchaseSelectedUnlock()
            false
        }

    private val inputProcessor: InputProcessor =
        object : InputAdapter() {
            override fun scrolled(
                amountX: Float,
                amountY: Float
            ): Boolean {
                if (!columnViewportRectangle().contains(pointer.position)) {
                    return false
                }

                val products = availableProducts()

                val column =
                    hoveredColumn(products)
                        ?: return false

                scrollColumn(
                    type = column.type,
                    amount = amountY * 55f,
                    products = column.products
                )

                return true
            }
        }

    override fun show() {
        viewport.apply()

        camera.position.set(
            VIEWPORT_WIDTH / 2f,
            VIEWPORT_HEIGHT / 2f,
            0f
        )
        camera.update()

        pointer.reset()
        message = null
        selectedUnlockId = null
        hoveredCardId = null
        pressedCardId = null
        horizontalScrollOffset = 0f

        Gdx.input.inputProcessor = inputProcessor

        UnlockType.entries.forEach {
            columnScrolls[it] = 0f
        }
    }

    override fun render(delta: Float) {
        pointer.update()

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.showMainMenu()
            return
        }

        val products = availableProducts()

        normalizeSelection(products)
        updateHoveredCard(products)
        handleColumnDrag(products)

        val backScreenChanged =
            backButton.update(
                pointerPosition = pointer.position,
                pointerJustPressed = pointer.justPressed,
                pointerJustReleased = pointer.justReleased
            )

        if (backScreenChanged) {
            return
        }

        buyButton.isEnabled =
            selectedProduct(products) != null

        buyButton.update(
            pointerPosition = pointer.position,
            pointerJustPressed = pointer.justPressed,
            pointerJustReleased = pointer.justReleased
        )

        Gdx.gl.glClearColor(
            0.035f,
            0.025f,
            0.055f,
            1f
        )
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        batch.projectionMatrix = camera.combined
        batch.begin()

        val columns =
            merchantColumns(products)

        val columnRectangles =
            columnRectangles(columns)

        renderer.render(
            batch = batch,
            font = font,
            profile = PlayerProfileRepository.profile,
            columns = columns,
            columnRectangles = columnRectangles,
            cardRectanglesById =
                cardRectanglesById(
                    columns = columns,
                    columnRectangles = columnRectangles
            ),
            selectedUnlockId = selectedUnlockId,
            hoveredUnlockId = hoveredCardId,
            pressedUnlockId = pressedCardId,
            tooltipProduct = tooltipProduct(products),
            message = message,
            viewportRectangle = columnViewportRectangle(),
            horizontalScrollOffset = horizontalScrollOffset,
            maximumHorizontalScrollOffset = maximumHorizontalScrollOffset()
        )

        buyButton.render(
            batch = batch,
            font = font,
            pointerPosition = pointer.position,
            pointerHeld = pointer.isHeld
        )

        backButton.render(
            batch = batch,
            font = font,
            pointerPosition = pointer.position,
            pointerHeld = pointer.isHeld
        )

        batch.end()
    }

    private fun handleColumnDrag(
        products: List<UnlockDefinition>
    ) {
        if (
            pointer.justPressed &&
            columnViewportRectangle().contains(pointer.position)
        ) {
            previousDragX = pointer.position.x
            accumulatedDragDistance = 0f
            isDraggingColumns = false
            pressedCardId = cardAtPointer(products)?.id
        }

        val dragIsActive =
            pointer.isHeld &&
                (
                    pressedCardId != null ||
                        columnViewportRectangle().contains(pointer.position)
                    )

        if (dragIsActive) {
            val currentX = pointer.position.x
            val deltaX = currentX - previousDragX

            accumulatedDragDistance += abs(deltaX)

            if (accumulatedDragDistance >= DRAG_THRESHOLD) {
                isDraggingColumns = true
            }

            if (isDraggingColumns) {
                horizontalScrollOffset =
                    (horizontalScrollOffset - deltaX)
                        .coerceIn(
                            0f,
                            maximumHorizontalScrollOffset()
                        )
            }

            previousDragX = currentX
        }

        if (pointer.justReleased) {
            if (!isDraggingColumns) {
                val product = cardAtPointer(products)

                if (
                    product != null &&
                    product.id == pressedCardId
                ) {
                    selectedUnlockId = product.id
                    message = null
                }
            }

            pressedCardId = null
            isDraggingColumns = false
            accumulatedDragDistance = 0f
        }
    }

    private fun updateHoveredCard(
        products: List<UnlockDefinition>
    ) {
        hoveredCardId =
            if (columnViewportRectangle().contains(pointer.position)) {
                cardAtPointer(products)?.id
            } else {
                null
            }
    }

    private fun tooltipProduct(
        products: List<UnlockDefinition>
    ): UnlockDefinition? {
        val tooltipId =
            when {
                pointer.isHeld && pressedCardId != null ->
                    pressedCardId

                hoveredCardId != null ->
                    hoveredCardId

                else ->
                    null
            }
                ?: return null

        return products.firstOrNull {
            it.id == tooltipId
        }
    }

    private fun availableProducts(): List<UnlockDefinition> {
        return UnlockRegistry.availableForMerchant(
            PlayerProfileRepository.profile.unlockedIds
        )
    }

    private fun merchantColumns(
        products: List<UnlockDefinition>
    ): List<MerchantColumn> {
        return listOf(
            MerchantColumn(
                title = "STAT POINT",
                type = UnlockType.STAT_POINT,
                products = products.filter {
                    it.type == UnlockType.STAT_POINT
                },
                scrollOffset =
                    columnScrolls[UnlockType.STAT_POINT] ?: 0f
            ),
            MerchantColumn(
                title = "CHARACTERS",
                type = UnlockType.CHARACTER,
                products = products.filter {
                    it.type == UnlockType.CHARACTER
                },
                scrollOffset =
                    columnScrolls[UnlockType.CHARACTER] ?: 0f
            ),
            MerchantColumn(
                title = "WEAPONS",
                type = UnlockType.WEAPON,
                products = products.filter {
                    it.type == UnlockType.WEAPON
                },
                scrollOffset =
                    columnScrolls[UnlockType.WEAPON] ?: 0f
            ),
            MerchantColumn(
                title = "ARMORS",
                type = UnlockType.ARMOR,
                products = products.filter {
                    it.type == UnlockType.ARMOR
                },
                scrollOffset =
                    columnScrolls[UnlockType.ARMOR] ?: 0f
            ),
            MerchantColumn(
                title = "ITEMS",
                type = UnlockType.ITEM,
                products = products.filter {
                    it.type == UnlockType.ITEM
                },
                scrollOffset =
                    columnScrolls[UnlockType.ITEM] ?: 0f
            ),
            MerchantColumn(
                title = "TAMGAS",
                type = UnlockType.TAMGA,
                products = products.filter {
                    it.type == UnlockType.TAMGA
                },
                scrollOffset =
                    columnScrolls[UnlockType.TAMGA] ?: 0f
            )
        ).filter { column ->
            column.products.isNotEmpty()
        }
    }

    private fun columnRectangles(
        columns: List<MerchantColumn>
    ): List<Rectangle> {
        return List(columns.size) { index ->
            Rectangle(
                COLUMN_LEFT_PADDING +
                    index * (COLUMN_WIDTH + COLUMN_GAP) -
                    horizontalScrollOffset,
                COLUMN_VIEWPORT_BOTTOM,
                COLUMN_WIDTH,
                COLUMN_VIEWPORT_TOP - COLUMN_VIEWPORT_BOTTOM
            )
        }
    }

    private fun cardRectanglesById(
        columns: List<MerchantColumn>,
        columnRectangles: List<Rectangle>
    ): Map<String, Rectangle> {
        val result =
            mutableMapOf<String, Rectangle>()

        columns.forEachIndexed { columnIndex, column ->
            val columnRectangle =
                columnRectangles.getOrNull(columnIndex)
                    ?: return@forEachIndexed

            column.products.forEachIndexed { productIndex, product ->
                result[product.id] =
                    Rectangle(
                        columnRectangle.x + 12f,
                        columnRectangle.y +
                            columnRectangle.height -
                            CARD_TOP_PADDING -
                            CARD_HEIGHT -
                            productIndex * (CARD_HEIGHT + CARD_GAP) +
                            column.scrollOffset,
                        columnRectangle.width - 24f,
                        CARD_HEIGHT
                    )
            }
        }

        return result
    }

    private fun cardAtPointer(
        products: List<UnlockDefinition>
    ): UnlockDefinition? {
        val viewportRectangle =
            columnViewportRectangle()

        if (!viewportRectangle.contains(pointer.position)) {
            return null
        }

        val columns =
            merchantColumns(products)

        val columnRectangles =
            columnRectangles(columns)

        val rectangles =
            cardRectanglesById(
                columns = columns,
                columnRectangles = columnRectangles
            )

        return products.firstOrNull { product ->
            val card =
                rectangles[product.id]
                    ?: return@firstOrNull false

            val cardIsInsideViewport =
                card.y >= viewportRectangle.y &&
                    card.y + card.height <=
                    viewportRectangle.y +
                    viewportRectangle.height

            cardIsInsideViewport &&
                card.contains(pointer.position)
        }
    }

    private fun selectedProduct(
        products: List<UnlockDefinition>
    ): UnlockDefinition? {
        val id = selectedUnlockId ?: return null

        return products.firstOrNull {
            it.id == id
        }
    }

    private fun normalizeSelection(
        products: List<UnlockDefinition>
    ) {
        val selectedExists =
            selectedUnlockId != null &&
                products.any {
                    it.id == selectedUnlockId
                }

        if (!selectedExists) {
            selectedUnlockId = null
        }
    }

    private fun purchaseSelectedUnlock(): Boolean {
        val product =
            selectedProduct(availableProducts())
                ?: return false

        val result =
            UnlockManager.purchase(product.id)

        message =
            when (result) {
                UnlockPurchaseResult.SUCCESS ->
                    "PURCHASE COMPLETE"

                UnlockPurchaseResult.NOT_ENOUGH_COINS ->
                    "NOT ENOUGH GRISILITH COIN"

                UnlockPurchaseResult.ALREADY_UNLOCKED ->
                    "ALREADY PURCHASED"

                UnlockPurchaseResult.LOCKED ->
                    "THIS UNLOCK IS LOCKED"

                UnlockPurchaseResult.UNKNOWN_UNLOCK ->
                    "UNKNOWN UNLOCK"
            }

        if (
            result == UnlockPurchaseResult.SUCCESS &&
            product.type != UnlockType.STAT_POINT
        ) {
            selectedUnlockId = null
        }

        return false
    }

    private fun scrollColumn(
        type: UnlockType,
        amount: Float,
        products: List<UnlockDefinition>
    ) {
        val current =
            columnScrolls[type] ?: 0f

        val columnHeight =
            COLUMN_VIEWPORT_TOP -
                COLUMN_VIEWPORT_BOTTOM

        val visibleCardAreaHeight =
            columnHeight -
                CARD_TOP_PADDING -
                12f

        val totalCardsHeight =
            if (products.isEmpty()) {
                0f
            } else {
                products.size * CARD_HEIGHT +
                    (products.size - 1) * CARD_GAP
            }

        val maximumScroll =
            (totalCardsHeight - visibleCardAreaHeight)
                .coerceAtLeast(0f)

        columnScrolls[type] =
            (current + amount)
                .coerceIn(
                    0f,
                    maximumScroll
                )
    }

    private fun hoveredColumn(
        products: List<UnlockDefinition>
    ): MerchantColumn? {

        val columns =
            merchantColumns(products)

        val rectangles =
            columnRectangles(columns)

        return columns.zip(rectangles)
            .firstOrNull {
                it.second.contains(pointer.position)
            }
            ?.first
    }

    private fun maximumHorizontalScrollOffset(): Float {
        val columnCount =
            merchantColumns(
                availableProducts()
            ).size

        if (columnCount <= 0) {
            return 0f
        }

        val totalWidth =
            COLUMN_LEFT_PADDING +
                columnCount * COLUMN_WIDTH +
                (columnCount - 1) * COLUMN_GAP +
                COLUMN_LEFT_PADDING

        val viewportWidth =
            COLUMN_VIEWPORT_RIGHT -
                COLUMN_VIEWPORT_LEFT

        return (totalWidth - viewportWidth)
            .coerceAtLeast(0f)
    }

    private fun columnViewportRectangle(): Rectangle {
        return Rectangle(
            COLUMN_VIEWPORT_LEFT,
            COLUMN_VIEWPORT_BOTTOM,
            COLUMN_VIEWPORT_RIGHT - COLUMN_VIEWPORT_LEFT,
            COLUMN_VIEWPORT_TOP - COLUMN_VIEWPORT_BOTTOM
        )
    }

    override fun resize(
        width: Int,
        height: Int
    ) {
        if (width <= 0 || height <= 0) {
            return
        }

        viewport.update(
            width,
            height,
            true
        )
    }

    override fun hide() {
        if (Gdx.input.inputProcessor == inputProcessor) {
            Gdx.input.inputProcessor = null
        }
    }

    override fun dispose() {
        renderer.dispose()
        batch.dispose()
        font.dispose()
    }
}

data class MerchantColumn(
    val title: String,
    val type: UnlockType,
    val products: List<UnlockDefinition>,
    var scrollOffset: Float = 0f
)
