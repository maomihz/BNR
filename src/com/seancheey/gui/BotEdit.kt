package com.seancheey.gui

import com.seancheey.game.*
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.Node
import javafx.scene.control.TextField
import javafx.scene.image.ImageView
import javafx.scene.input.ClipboardContent
import javafx.scene.input.DataFormat
import javafx.scene.input.DragEvent
import javafx.scene.input.TransferMode
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.HBox
import javafx.scene.layout.StackPane
import javafx.scene.layout.TilePane
import java.net.URL
import java.util.*

/**
 * Created by Seancheey on 20/05/2017.
 * GitHub: https://github.com/Seancheey
 */

/** DataFormat of componentModel, used as a reference to componentModel in dragging clipboard **/
object modelFormat : DataFormat("object/componentModel")

/** generated by javafx, each time the pane is initialized, editController too **/
private var editController: BotEdit? = null

/**
 * This BotEdit is used as a JavaFX controller
 * One should not create any new instance of this class
 */
class BotEdit : Initializable {
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
        get() = Config.player.robots[selectBotGroupIndex][selectBotModelIndex]


    override fun initialize(location: URL?, resources: ResourceBundle?) {
        editController = this
        initModelFlowPanes()
        initEditPane()
        initBotGroup()
        setEditingRobot(0)
    }

    private fun initModelFlowPanes() {
        for (component in Models.BLOCKS) {
            blocksPane!!.children.add(ComponentModelSlot(component))
        }
    }

    private fun initEditPane() {
        val size = Config.botGridNum * Config.botGridWidth
        editPane!!.minWidth = size
        editPane!!.maxWidth = size
        // add grid to edit pane
        val grids = arrayListOf<ComponentGrid>()
        for (y in 0 until Config.botGridNum) {
            for (x in 0 until Config.botGridNum) {
                val grid = ComponentGrid(x, y)
                AnchorPane.setTopAnchor(grid, Config.botGridWidth * y)
                AnchorPane.setLeftAnchor(grid, Config.botGridWidth * x)
                grids.add(grid)
            }
        }
        editPane!!.children.addAll(grids)
        nameField!!.setMaxSize(size, size)
    }

    private fun initBotGroup() {
        // select player's first BotGroup to initialize
        val models = Config.player.robots[0]
        for ((i, model) in models.withIndex()) {
            val robotModelSlot = RobotModelSlot(model)
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
        Config.player.robots[selectBotGroupIndex][selectBotModelIndex] = getRobotModel()
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
        val componentView = ComponentView(componentModel, x, y)
        AnchorPane.setLeftAnchor(componentView, x * Config.botGridWidth)
        AnchorPane.setTopAnchor(componentView, y * Config.botGridWidth)
        editPane!!.children.add(componentView)
        setGridsInRangeIsEnabled(x, y, componentModel.width, componentModel.height, false)
    }

    fun removeComponent(componentView: ComponentView): Boolean {
        if (editPane!!.children.contains(componentView)) {
            setGridsInRangeIsEnabled(componentView.x, componentView.y, componentView.componentModel.width, componentView.componentModel.height, true)
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

    private fun getComponentGridAt(x: Int, y: Int): ComponentGrid? {
        if (x < Config.botGridWidth || y < Config.botGridWidth) {
            for (node in editPane!!.children) {
                if (node is ComponentGrid) {
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
        clipboard.put(modelFormat, componentModel)
        db.setContent(clipboard)
        // set mouse holding image and offsets
        db.dragView = componentModel.image
        db.dragViewOffsetX = (componentModel.width - 1) * componentModel.image.width / componentModel.width / 2
        db.dragViewOffsetY = -(componentModel.height - 1) * componentModel.image.height / componentModel.height / 2
    }

    fun menu() {
        Stages.switchScene(Scenes.menu)
    }


}

/**
 * A component of a robot
 * It is created when a player drags component componentModel from a ComponentModelSlot to any grid
 */
class ComponentView(val componentModel: ComponentModel, val x: Int, val y: Int) : ImageView(componentModel.image) {
    init {
        setOnDragDetected { event ->
            editController!!.dragComponentStart(componentModel, this)
            editController!!.removeComponent(this)
            event.consume()
        }
    }

    fun toComponent(): Component<ComponentModel> {
        return Component(componentModel, x, y, null)
    }
}

/**
 * A grid on editPane waiting to be filled by a component
 * It is created when BotEdit pane is initialized
 */
class ComponentGrid(val x: Int, val y: Int, componentModel: ComponentModel? = null) : StackPane() {

    var componentModel: ComponentModel? = null
    var enabled = true

    init {
        this.componentModel = componentModel

        minWidth = Config.botGridWidth
        minHeight = Config.botGridWidth
        maxWidth = Config.botGridWidth
        maxHeight = Config.botGridWidth

        setOnDragOver {
            event ->
            if (enabled) {
                event.acceptTransferModes(TransferMode.MOVE, TransferMode.LINK, TransferMode.COPY)
                event.consume()
            }
        }
        setOnDragDropped { event ->
            if (enabled) dragComponentEnd(x, y, event)
        }
    }

    fun dragComponentEnd(x: Int, y: Int, event: DragEvent) {
        if (event.dragboard.hasContent(modelFormat)) {

            editController!!.putComponent(event.dragboard.getContent(modelFormat) as ComponentModel, x, y)
            editController!!.updateRobot()

            event.isDropCompleted = true
            event.consume()
        }
    }
}

/**
 * A componentModel slot as an option to add to robot
 * It is created when BotEdit pane is initialized
 * ComponentModel slots are initialized according to data files
 */
class ComponentModelSlot(val componentModel: ComponentModel) : ImageView(componentModel.image) {

    init {
        // bind mouse location to hoverView
        setOnDragDetected { event ->
            editController!!.dragComponentStart(componentModel, this)
            event.consume()
        }
    }
}
