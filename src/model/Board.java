package model;
import java.util.*;


public class Board {
    public static final int SIZE = 10;
    private Cell[][] grid;
    private List<Ship> ships;
    private Ship lastSunkShip = null;

    public Board() {
        grid = new Cell[SIZE][SIZE];
        ships = new ArrayList<>();
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                grid[y][x] = new Cell(x, y);
            }
        }
    }

    public CellState receiveShot(int x, int y) {
        lastSunkShip = null;
        Cell cell = grid[y][x];
        if (cell.getState() != CellState.WATER) return cell.getState(); 

        for (Ship s : ships) {
            if (s.contains(x, y)) {
                s.hit();
                cell.setState(CellState.HIT);
                if (s.isSunk()) {
                    lastSunkShip = s;
                    markShipSunk(s);
                    return CellState.SUNK;
                }
                return CellState.HIT;
            }
        }
        cell.setState(CellState.MISSED);
        return CellState.MISSED;
    }
    
    private void markShipSunk(Ship s) {
        for (Cell c : s.getCells()) {
            grid[c.getY()][c.getX()].setState(CellState.SUNK);
        }
    }

    public boolean allShipsSunk() {
        return ships.stream().allMatch(Ship::isSunk);
    }
    
    public Ship getLastSunkShip() { return lastSunkShip; }
    public List<Ship> getShips() { return ships; }
    public CellState getCellState(int x, int y) { return grid[y][x].getState(); }
    public boolean hasShip(int x, int y) { return isShipAt(x, y); }

    public Ship getShipAt(int x, int y) {
        for (Ship s : ships) {
            if (s.contains(x, y)) return s;
        }
        return null;
    }

    public void removeShip(Ship s) {
        if (s == null) return;
        ships.remove(s); 
    }
    
    public boolean placeShip(int x, int y, int length, boolean horizontal) {
        if (canPlaceShip(x, y, length, horizontal)) {
            Ship newShip = new Ship();
            newShip.setShipInfo(x, y, length, horizontal);
            for (int i = 0; i < length; i++) {
                int curX = x + (horizontal ? i : 0);
                int curY = y + (horizontal ? 0 : i);
                newShip.addCell(grid[curY][curX]);
            }
            ships.add(newShip);
            return true;
        }
        return false;
    }

    public void placeShipsRandomly() {
        ships.clear();
        Random rand = new Random();
        int[] shipSizes = ShipConfig.getSizesFlat();
        for (int length : shipSizes) {
            boolean placed = false;
            while (!placed) {
                int x = rand.nextInt(SIZE);
                int y = rand.nextInt(SIZE);
                boolean horizontal = rand.nextBoolean();
                if (placeShip(x, y, length, horizontal)) placed = true;
            }
        }
    }

    private boolean canPlaceShip(int x, int y, int length, boolean horizontal) {
        for (int i = 0; i < length; i++) {
            int curX = x + (horizontal ? i : 0);
            int curY = y + (horizontal ? 0 : i);
            
            if (curX < 0 || curX >= SIZE || curY < 0 || curY >= SIZE) return false;
            if (isShipAt(curX, curY)) return false;
        }
        return true;
    }

    private boolean isShipAt(int x, int y) {
        for (Ship s : ships) {
            if (s.contains(x, y)) return true;
        }
        return false;
    }
}