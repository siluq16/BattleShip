package model;

public class Cell {
    private int x, y;
    private CellState state;

    public Cell(int x, int y) {
        this.x = x;
        this.y = y;
        this.state = CellState.WATER;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public CellState getState() { return state; }
    public void setState(CellState state) { this.state = state; }
}