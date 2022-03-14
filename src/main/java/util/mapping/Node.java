package util.mapping;

import environment.Coordinate;

public class Node {
    private Coordinate position;

    public Node(Coordinate position) {
        this.position = position;
    }

    public int getX() {
        return position.getX();
    }

    public int getY() {
        return position.getY();
    }
}
