package com.seancheey.scene.controller

import com.seancheey.game.*
import com.seancheey.gui.ComponentView
import com.seancheey.gui.DragDropGrid
import com.seancheey.gui.ModelSlot
import com.seancheey.gui.modelCopyFormat
import com.seancheey.scene.Scenes
import com.seancheey.scene.Stages
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.Node
import javafx.scene.control.TextField
import javafx.scene.input.ClipboardContent
import javafx.scene.input.TransferMode
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.HBox
import javafx.scene.layout.TilePane
import java.net.URL
import java.util.*

/**
 * Created by Seancheey on 20/05/2017.
 * GitHub: https://github.com/Seancheey
 */


/**
 * This BotEdit is used as a JavaFX controller
 * One should not create any new instance of this class
 */
class BotEdit : Initializable {
    /** generated by javafx, each time the pane is initialized, editController too **/
    companion object {
        var editController: BotEdit? = null
    }

    /**
     * backing field for companion object
     */
    var editController: BotEdit?
        get() = Companion.editController
        set(value) {
            BotEdit.editController = value
        }

    /**
     * Component panes for player to select component models
     */
    @FXML
    var blocksPane: TilePane? = null
    @FXML
    var weaponsPane: TilePane? = null
    /**
     * Pane for player to edit their ships
     */
    @FXML
    var editPane: AnchorPane? = null
    /**
     * TextField of robot name
     */
    @FXML
    var nameField: TextField? = null
    /**
     * RobotNode selection HBox
     */
    @FXML
    var botGroupBox: HBox? = null

    /**
     * Index of player's selected index of bot group and model
     */
    var selectBotGroupIndex: Int = 0
    var selectBotModelIndex: Int = 0
    val editingRobot: RobotModel
        get() = Config.player.robotGroups[selectBotGroupIndex][selectBotModelIndex]


    override fun initialize(location: URL?, resources: ResourceBundle?) {
        editController = this
        initModelFlowPanes()
        initEditPane()
        initBotGroup()
        setEditingRobot(0)
    }

    private fun initModelFlowPanes() {
        for (component in Models.BLOCKS) {
            val componentSlot = ModelSlot(component)
            componentSlot.setOnDragDetected { event ->
                dragComponentStart(component, componentSlot)
                event.consume()
            }
            blocksPane!!.children.add(componentSlot)
        }
    }

    private fun initEditPane() {
        val size = Config.botGridNum * Config.botGridSize
        editPane!!.minWidth = size
        editPane!!.maxWidth = size
        // add grid to edit pane
        val grids = arrayListOf<DragDropGrid>()
        for (y in 0 until Config.botGridNum) {
            for (x in 0 until Config.botGridNum) {
                val grid = DragDropGrid(x, y, { x, y, model ->
                    putComponent(model, x, y)
                    updateRobot()
                })
                AnchorPane.setTopAnchor(grid, Config.botGridSize * y)
                AnchorPane.setLeftAnchor(grid, Config.botGridSize * x)
                grids.add(grid)
            }
        }
        editPane!!.children.addAll(grids)
        nameField!!.setMaxSize(size, size)
    }

    private fun initBotGroup() {
        // select player's first BotGroup to initialize
        val models = Config.player.robotGroups[0]
        for ((i, model) in models.withIndex()) {
            val robotModelSlot = ModelSlot(model)
            robotModelSlot.setOnAction { setEditingRobot(i) }
            botGroupBox!!.children.add(robotModelSlot)
        }
    }

    fun setEditingRobot(index: Int) {
        selectBotModelIndex = index
        // change nameField
        editController!!.nameField!!.text = editingRobot.name
        // change components on grid
        editController!!.clearComponents()
        for ((model, x, y) in editingRobot.components) {
            editController!!.putComponent(model, x, y)
        }
        setGridsInRangeIsEnabled(0, 0, Config.botGridNum, Config.botGridNum, true)
    }

    fun saveRobot() {
        Config.player.saveData()
    }

    fun updateRobot() {
        Config.player.robotGroups[selectBotGroupIndex][selectBotModelIndex] = getRobotModel()
        botGroupBox!!.children.clear()
        initBotGroup()
    }

    fun clearComponents() {
        val toDelete = editPane!!.children.filterIsInstance<ComponentView>()
        for (view in toDelete) {
            removeComponent(view)
        }
    }

    fun getRobotModel(): RobotModel {
        val components = arrayListOf<Component<ComponentModel>>()
        for (node in editPane!!.children) {
            if (node is ComponentView)
                components.add(node.toComponent())
        }

        return RobotModel(nameField!!.text, components)
    }

    fun putComponent(componentModel: ComponentModel, x: Int, y: Int) {
        val componentView = ComponentView(componentModel, x, y, { compView ->
            dragComponentStart(compView.componentModel, compView)
            removeComponent(compView)
            updateRobot()
        })
        AnchorPane.setLeftAnchor(componentView, x * Config.botGridSize)
        AnchorPane.setTopAnchor(componentView, y * Config.botGridSize)
        editPane!!.children.add(componentView)
        setGridsInRangeIsEnabled(x, y, componentModel.gridWidth, componentModel.gridHeight, false)
    }

    fun removeComponent(componentView: ComponentView): Boolean {
        if (editPane!!.children.contains(componentView)) {
            setGridsInRangeIsEnabled(componentView.x, componentView.y, componentView.componentModel.gridWidth, componentView.componentModel.gridHeight, true)
            editPane!!.children.remove(componentView)
            return true
        } else {
            return false
        }
    }

    fun setGridsInRangeIsEnabled(x: Int, y: Int, width: Int, height: Int, value: Boolean) {
        for (y2 in y until y + height) {
            for (x2 in x until x + width) {
                val compGrid = getComponentGridAt(x2, y2)
                if (compGrid != null) {
                    compGrid.enabled = value
                }
            }
        }
    }

    private fun getComponentGridAt(x: Int, y: Int): DragDropGrid? {
        if (x < Config.botGridSize || y < Config.botGridSize) {
            for (node in editPane!!.children) {
                if (node is DragDropGrid) {
                    if (node.x == x && node.y == y) {
                        return node
                    }
                }
            }
        }
        return null
    }

    fun dragComponentStart(componentModel: ComponentModel, node: Node): Unit {
        // start drag with any transfer mode
        val db = node.startDragAndDrop(TransferMode.COPY, TransferMode.LINK, TransferMode.MOVE)
        // put the componentModel into clipboard
        val clipboard = ClipboardContent()
        clipboard.put(modelCopyFormat, componentModel)
        db.setContent(clipboard)
        // set mouse holding image and offsets
        db.dragView = componentModel.image
        db.dragViewOffsetX = (componentModel.gridWidth - 1) * componentModel.image.width / componentModel.gridWidth / 2
        db.dragViewOffsetY = -(componentModel.gridHeight - 1) * componentModel.image.height / componentModel.gridHeight / 2
    }

    fun menu() {
        Stages.switchScene(Scenes.menu)
    }
}

