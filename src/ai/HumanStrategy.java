package ai;

import model.CellState;
import model.Point;
import java.util.List;

public class HumanStrategy implements BattleStrategy {
    private String name;

    public HumanStrategy(String name) {
        this.name = name;
    }

    @Override
    public int[] makeMove() {
        return null; 
    }

    @Override
    public void processResult(int x, int y, CellState result, List<Point> sunkShipCoords) {
    }

    @Override
    public String getName() {
        return name;
    }
}