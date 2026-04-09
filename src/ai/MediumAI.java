package ai;

import model.CellState;
import model.Point;
import java.util.*;

public class MediumAI implements BattleStrategy {
    private final String name;
    private final int boardSize = 10;
    private final Set<String> firedShots = new HashSet<>();
    private final LinkedList<Point> targets = new LinkedList<>();
    private final Random rand = new Random();

    private List<Point> hitChain = new ArrayList<>();

    public MediumAI(String name) {
        this.name = name;
    }

    @Override
    public int[] makeMove() {
        while (!targets.isEmpty()) {
            Point p = targets.removeFirst();
            if (!isFired(p.x, p.y)) {
                fire(p.x, p.y);
                return new int[]{p.x, p.y};
            }
        }
        int x, y;
        do {
            x = rand.nextInt(boardSize);
            y = rand.nextInt(boardSize);
        } while (isFired(x, y));
        fire(x, y);
        return new int[]{x, y};
    }

    @Override
    public void processResult(int x, int y, CellState result, List<Point> sunkShipCoords) {
        if (result == CellState.HIT) {
            hitChain.add(new Point(x, y));
            addNeighborTargets(x, y);
        } else if (result == CellState.SUNK) {
            hitChain.clear();
            targets.clear();
        }
    }

    private void addNeighborTargets(int x, int y) {
        targets.clear();
        if (hitChain.size() >= 2) {
            hitChain.sort((a, b) -> a.x != b.x ? a.x - b.x : a.y - b.y);
            Point first = hitChain.get(0);
            Point last  = hitChain.get(hitChain.size() - 1);
            boolean horiz = (first.y == last.y);
            if (horiz) {
                addTarget(first.x - 1, first.y);
                addTarget(last.x  + 1, last.y);
            } else {
                addTarget(first.x, first.y - 1);
                addTarget(last.x,  last.y  + 1);
            }
        } else {
            addTarget(x - 1, y);
            addTarget(x + 1, y);
            addTarget(x, y - 1);
            addTarget(x, y + 1);
        }
    }

    private void addTarget(int x, int y) {
        if (isValid(x, y) && !isFired(x, y))
            targets.addLast(new Point(x, y));
    }

    private boolean isValid(int x, int y) { return x >= 0 && x < boardSize && y >= 0 && y < boardSize; }
    private boolean isFired(int x, int y)  { return firedShots.contains(x + "," + y); }
    private void fire(int x, int y)         { firedShots.add(x + "," + y); }

    @Override
    public String getName() { return name; }
}