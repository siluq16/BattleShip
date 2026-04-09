package ai;

import model.CellState;
import model.Point;
import java.util.*;

public class EasyAI implements BattleStrategy {
    private final String name;
    private final int boardSize = 10;
    private final Set<String> firedShots = new HashSet<>();
    private final Random rand = new Random();

    public EasyAI(String name) {
        this.name = name;
    }

    @Override
    public int[] makeMove() {
        int x, y;
        do {
            x = rand.nextInt(boardSize);
            y = rand.nextInt(boardSize);
        } while (firedShots.contains(x + "," + y));
        firedShots.add(x + "," + y);
        return new int[]{x, y};
    }

    @Override
    public void processResult(int x, int y, CellState result, List<Point> sunkShipCoords) {
    }

    @Override
    public String getName() { return name; }
}