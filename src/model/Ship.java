package model;
import java.util.ArrayList;
import java.util.List;

public class Ship {
    private List<Cell> cells = new ArrayList<>();
    private int hitCount = 0;

    private int startX;
    private int startY;
    private int length;
    private boolean isHorizontal;

    public void setShipInfo(int x, int y, int length, boolean horizontal) {
        this.startX = x;
        this.startY = y;
        this.length = length;
        this.isHorizontal = horizontal;
    }

    public void addCell(Cell cell) { 
        cells.add(cell); 
    }
    
    public boolean contains(int x, int y) {
        for (Cell c : cells) {
            if (c.getX() == x && c.getY() == y) return true;
        }
        return false;
    }
    
    public void hit() { 
        hitCount++; 
    }

    public boolean isSunk() { 
        return hitCount >= cells.size(); 
    }

    public List<Cell> getCells() { 
        return cells; 
    }
    
    public List<Point> getCoordinates() {
        List<Point> points = new ArrayList<>();
        for (Cell c : cells) {
            points.add(new Point(c.getX(), c.getY()));
        }
        return points;
    }

    public int getX() { 
        return startX; 
    }

    public int getY() { 
        return startY; 
    }

    public int getLength() { 
        return length; 
    }

    public boolean isHorizontal() { 
        return isHorizontal; 
    }
}