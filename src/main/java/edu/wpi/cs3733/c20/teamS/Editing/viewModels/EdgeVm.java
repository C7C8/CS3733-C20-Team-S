package edu.wpi.cs3733.c20.teamS.Editing.viewModels;

import edu.wpi.cs3733.c20.teamS.Settings;
import edu.wpi.cs3733.c20.teamS.ThrowHelper;
import edu.wpi.cs3733.c20.teamS.database.NodeData;
import edu.wpi.cs3733.c20.teamS.utilities.rx.ReactiveProperty;
import javafx.scene.Parent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

public final class EdgeVm extends Parent {
    private final NodeData start;
    private final NodeData end;

    private final Line visibleMask;
    private final Line collisionMask;
    private final ReactiveProperty<Boolean> isMouseOver = new ReactiveProperty<>(false);
    private final ReactiveProperty<Boolean> highlightOnMouseOver = new ReactiveProperty<>(true);
    private final ReactiveProperty<Boolean> enlargeOnMouseOver = new ReactiveProperty<>(true);

    public EdgeVm(NodeData start, NodeData end) {
        if (start == null) ThrowHelper.illegalNull("start");
        if (end == null) ThrowHelper.illegalNull("end");

        this.start = start;
        this.end = end;

        visibleMask = new Line();
        updateLinePosition(visibleMask);
        visibleMask.setStrokeWidth(Settings.get().editEdgeStrokeWidth());
        visibleMask.setStroke(Settings.get().editEdgeColorNormal());
        visibleMask.setMouseTransparent(true);
        getChildren().add(visibleMask);

        collisionMask = new Line();
        updateLinePosition(collisionMask);
        collisionMask.setStroke(Color.TRANSPARENT);
        collisionMask.setStrokeWidth(30);
        getChildren().add(collisionMask);

        initEventHandlers(start, end);
    }

    private void initEventHandlers(NodeData start, NodeData end) {
        start.positionChanged()
                .mergeWith(end.positionChanged())
                .subscribe(e -> {
                    updateLinePosition(visibleMask);
                    updateLinePosition(collisionMask);
                });
        collisionMask.setOnMouseEntered(e -> isMouseOver.setValue(true));
        collisionMask.setOnMouseExited(e -> isMouseOver.setValue(false));
        isMouseOver.changed()
                .mergeWith(highlightOnMouseOver.changed())
                .subscribe(e -> updateHighlightState());
        isMouseOver.changed()
                .mergeWith(enlargeOnMouseOver.changed())
                .subscribe(e -> {
                    double width = isMouseOver.value() && enlargeOnMouseOver.value() ?
                            2.5 * Settings.get().editEdgeStrokeWidth() :
                            1.0 * Settings.get().editEdgeStrokeWidth();
                    visibleMask.setStrokeWidth(width);
                });
    }

    public boolean highlightOnMouseOver() {
        return highlightOnMouseOver.value();
    }
    public void setHighlightOnMouseOver(boolean value) {
        highlightOnMouseOver.setValue(value);
    }
    public boolean enlargeOnMouseOver() {
        return enlargeOnMouseOver.value();
    }
    public void setEnlargeOnMouseOver(boolean value) {
        enlargeOnMouseOver.setValue(value);
    }

    private void updateHighlightState() {
        Color stroke = isMouseOver.value() && highlightOnMouseOver.value() ?
                Settings.get().editEdgeColorHighlight() :
                Settings.get().editEdgeColorNormal();
        visibleMask.setStroke(stroke);
    }

    private void updateLinePosition(Line line) {
        line.setStartX(start.getxCoordinate());
        line.setStartY(start.getyCoordinate());
        line.setEndX(end.getxCoordinate());
        line.setEndY(end.getyCoordinate());
    }
}
