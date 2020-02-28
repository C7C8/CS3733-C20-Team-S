package edu.wpi.cs3733.c20.teamS.Editing.viewModels;

import edu.wpi.cs3733.c20.teamS.Settings;
import edu.wpi.cs3733.c20.teamS.database.NodeData;
import edu.wpi.cs3733.c20.teamS.utilities.ReactiveProperty;
import javafx.scene.Parent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class NodeVm extends Parent {
    private final NodeData node;
    private final Circle visibleMask;
    private final Circle collisionMask;
    private final ReactiveProperty<Boolean> isMouseOver = new ReactiveProperty<>(false);
    private final ReactiveProperty<Boolean> highlightOnMouseOver = new ReactiveProperty<>(true);

    public NodeVm(NodeData node) {
        this.node = node;
        visibleMask = new Circle();
        visibleMask.setRadius(12);
        visibleMask.setMouseTransparent(true);
        visibleMask.setFill(Settings.get().nodeFillColorNormal());
        visibleMask.setStroke(Settings.get().nodeStrokeColorNormal());
        getChildren().add(visibleMask);

        collisionMask = new Circle();
        collisionMask.setRadius(20);
        collisionMask.setFill(Color.TRANSPARENT);
        collisionMask.setStroke(Color.TRANSPARENT);
        getChildren().add(collisionMask);

        updatePosition();

        collisionMask.setOnMouseEntered(e -> isMouseOver.setValue(true));
        collisionMask.setOnMouseExited(e -> isMouseOver.setValue(false));
        isMouseOver.changed().mergeWith(highlightOnMouseOver.changed())
                .subscribe(e -> updateHighlightState());

        isMouseOver.changed()
                .subscribe(huh -> visibleMask.setRadius(huh ? 20 : 12));
        node.positionChanged().subscribe(e -> updatePosition());
    }

    public boolean highlightOnMouseOver() {
        return highlightOnMouseOver.value();
    }
    public void setHighlightOnMouseOver(boolean value) {
        highlightOnMouseOver.setValue(value);
    }
    public double visibleRadius() {
        return visibleMask.getRadius();
    }
    public void setVisibleRadius(double value) {
        visibleMask.setRadius(value);
    }
    public double collisionRadius() {
        return collisionMask.getRadius();
    }
    public void setCollisionRadius(double value) {
        collisionMask.setRadius(value);
    }

    private void updateHighlightState() {
        if (highlightOnMouseOver.value() && isMouseOver.value())
            visibleMask.setFill(Settings.get().nodeFillColorHighlight());
        else
            visibleMask.setFill(Settings.get().nodeFillColorNormal());
    }

    private void updatePosition() {
        visibleMask.setCenterX(node.getxCoordinate());
        visibleMask.setCenterY(node.getyCoordinate());
        collisionMask.setCenterX(node.getxCoordinate());
        collisionMask.setCenterY(node.getyCoordinate());
    }
}