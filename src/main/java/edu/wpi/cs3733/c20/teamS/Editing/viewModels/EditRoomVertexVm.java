package edu.wpi.cs3733.c20.teamS.Editing.viewModels;

import edu.wpi.cs3733.c20.teamS.Settings;
import edu.wpi.cs3733.c20.teamS.collisionMasks.Room;
import edu.wpi.cs3733.c20.teamS.utilities.numerics.Vector2;
import edu.wpi.cs3733.c20.teamS.utilities.rx.RxAdaptors;
import io.reactivex.rxjava3.core.Observable;
import javafx.collections.ListChangeListener;
import javafx.scene.Parent;

public class EditRoomVertexVm extends Parent {
    private final Room room;
    private final int vertexIndex;
    private final Observable<Vector2> dragged;
    private final Observable<Vector2> released;

    public EditRoomVertexVm(Room room, int vertexIndex) {
        this.room = room;
        this.vertexIndex = vertexIndex;
        Vector2 position = room.vertices().get(vertexIndex);
        setTranslateX(position.x());
        setTranslateY(position.y());

        HighlightCircle mask = new HighlightCircle(5, 9);
        mask.setTranslateX(0);
        mask.setTranslateY(0);
        mask.setNormalFillColor(Settings.get().editRoomColorNormal());
        mask.setHighlightFillColor(Settings.get().editRoomColorHighlight());
        getChildren().add(mask);

        dragged = RxAdaptors.eventStream(this::setOnMouseDragged)
                .map(e -> localToParent(e.getX(), e.getY()))
                .map(point -> new Vector2(point.getX(), point.getY()));
        released = RxAdaptors.eventStream(this::setOnMouseReleased)
                .map(e -> localToParent(e.getX(), e.getY()))
                .map(point -> new Vector2(point.getX(), point.getY()));

        room.vertices().addListener((ListChangeListener<? super Vector2>) change -> {
            setTranslateX(room.vertices().get(vertexIndex).x());
            setTranslateY(room.vertices().get(vertexIndex).y());
        });
    }

    public Observable<Vector2> dragged() {
        return dragged;
    }
    public Observable<Vector2> released() {
        return released;
    }
    public Room room() {
        return room;
    }
    public int vertexIndex() {
        return vertexIndex;
    }
}
