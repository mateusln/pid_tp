package me.cassiano.tp_pid;

import javafx.scene.Group;

/**
 * Created by matheus on 5/11/15.
 */
public class Seed {

    public enum Type {
        Internal, External;
    }

    public enum Shape {
        Circle, Square;
    }

    private Group view;
    private Type type;
    private Shape shape;

    public Group getView() {
        return view;
    }

    public void setView(Group view) {
        this.view = view;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Shape getShape() {
        return shape;
    }

    public void setShape(Shape shape) {
        this.shape = shape;
    }
}
