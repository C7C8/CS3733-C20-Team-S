package edu.wpi.cs3733.c20.teamS.Editing;

import com.google.common.graph.EndpointPair;
import com.google.common.graph.MutableGraph;
import edu.wpi.cs3733.c20.teamS.Editing.tools.IEditingTool;
import edu.wpi.cs3733.c20.teamS.Editing.tools.ObservableGraph;
import edu.wpi.cs3733.c20.teamS.Editing.viewModels.EdgeVm;
import edu.wpi.cs3733.c20.teamS.Editing.viewModels.NodeVm;
import edu.wpi.cs3733.c20.teamS.Editing.viewModels.RoomVm;
import edu.wpi.cs3733.c20.teamS.ThrowHelper;
import edu.wpi.cs3733.c20.teamS.collisionMasks.Room;
import edu.wpi.cs3733.c20.teamS.database.NodeData;
import edu.wpi.cs3733.c20.teamS.pathDisplaying.FloorSelector;
import edu.wpi.cs3733.c20.teamS.pathDisplaying.MapZoomer;
import javafx.scene.Group;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MapEditor {
    private final ObservableGraph graph;
    private final FloorSelector floorSelector;
    private final Set<Room> rooms;
    private IEditingTool editingTool;
    private final ScrollPane scrollPane;
    private final ImageView mapImage;
    private final MapZoomer zoomer;
    private final Map<NodeData, NodeVm> nodeLookup = new HashMap<>();
    private final Map<EndpointPair<NodeData>, EdgeVm> edgeLookup = new HashMap<>();

    private final Group rootGroup;
    private final Group nodeGroup = new Group();
    private final Group edgeGroup = new Group();
    private final Group roomGroup = new Group();

    public MapEditor(
            MutableGraph<NodeData> graph,
            FloorSelector floorSelector, Set<Room> rooms,
            ScrollPane scrollPane, ImageView mapImage
    ) {
        if (graph == null) ThrowHelper.illegalNull("graph");
        if (floorSelector == null) ThrowHelper.illegalNull("floorSelector");
        if (rooms == null) ThrowHelper.illegalNull("rooms");

        this.floorSelector = floorSelector;
        this.rooms = rooms;
        this.editingTool = new IEditingTool() {};
        this.scrollPane = scrollPane;
        zoomer = new MapZoomer(this.scrollPane);
        this.rootGroup = new Group();
        this.mapImage = mapImage;

        this.graph = createGraph();
        graph.nodes().forEach(this.graph::addNode);
        graph.edges().forEach(edge -> this.graph.putEdge(edge.nodeU(), edge.nodeV()));

        rootGroup.setOnMouseClicked(e -> editingTool.onMapClicked(e));
        rootGroup.setOnMouseMoved(e -> editingTool.onMouseMoved(e));
        floorSelector.currentChanged()
                .subscribe(n -> {
                   mapImage.setImage(floorSelector.floor(n).image);
                   updateZoom();
                });

        updateZoom();
    }

    public IEditingTool editingTool() {
        return editingTool;
    }
    public void setEditingTool(IEditingTool value) {
        IEditingTool previous = this.editingTool;
        this.editingTool = value;
        if (previous == null)
            return;
        previous.onClosed();
    }
    public ObservableGraph graph() {
        return graph;
    }

    public boolean canZoomIn() {
        return zoomer.canZoomIn();
    }
    public boolean canZoomOut() {
        return zoomer.canZoomOut();
    }
    public void zoomIn() {
        zoomer.zoomIn();
        updateZoom();
    }
    public void zoomOut() {
        zoomer.zoomOut();
        updateZoom();
    }

    private void updateZoom() {
        double hval = scrollPane.getHvalue();
        double vval = scrollPane.getVvalue();

        rootGroup.getChildren().clear();
        rootGroup.getChildren().add(mapImage);
        rootGroup.getChildren().add(roomGroup);
        rootGroup.getChildren().add(edgeGroup);
        rootGroup.getChildren().add(nodeGroup);

        scrollPane.setContent(rootGroup);

        zoomer.zoomSet();
        scrollPane.setHvalue(hval);
        scrollPane.setVvalue(vval);
    }
    private ObservableGraph createGraph() {
        ObservableGraph graph = new ObservableGraph();

        graph.nodeAdded().subscribe(this::onNodeAdded);
        graph.nodeRemoved().subscribe(this::onNodeRemoved);
        graph.edgeAdded().subscribe(this::onEdgeAdded);
        graph.edgeRemoved().subscribe(this::onEdgeRemoved);

        return graph;
    }
    private NodeVm createNodeVm(NodeData node) {
        assert node != null : "node can't be null!";

        NodeVm result = new NodeVm(node);
        result.setOnMouseClicked(e -> editingTool.onNodeClicked(node, e));
        result.setOnMouseDragged(e -> editingTool.onNodeDragged(node, e));
        result.setOnMouseReleased(e -> editingTool.onNodeReleased(node, e));

        return result;
    }
    private EdgeVm createEdgeVm(NodeData start, NodeData end) {
        EdgeVm edgeVm = new EdgeVm(start, end);
        edgeVm.setOnMouseClicked(e -> {
            EndpointPair<NodeData> edge = EndpointPair.unordered(start, end);
            editingTool.onEdgeClicked(edge, e);
        });

        return edgeVm;
    }
    private RoomVm createRoomVm(Room room) {
        RoomVm result = new RoomVm(room);
        result.setOnMouseClicked(e -> editingTool.onRoomClicked(room, e));
        return result;
    }

    private void onNodeAdded(NodeData node) {
        NodeVm vm = createNodeVm(node);
        nodeLookup.put(node, vm);
        nodeGroup.getChildren().add(vm);
    }
    private void onNodeRemoved(NodeData node) {
        NodeVm remove = nodeLookup.get(node);
        nodeGroup.getChildren().remove(remove);
        nodeLookup.remove(node);
    }
    private void onEdgeAdded(EndpointPair<NodeData> edge) {
        EdgeVm vm = createEdgeVm(edge.nodeU(), edge.nodeV());
        edgeGroup.getChildren().add(vm);
        edgeLookup.put(edge, vm);
    }
    private void onEdgeRemoved(EndpointPair<NodeData> edge) {
        EdgeVm remove = edgeLookup.get(edge);
        edgeGroup.getChildren().remove(remove);
        edgeLookup.remove(edge);
    }
}
