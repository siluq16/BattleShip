package ai;

import model.CellState;
import model.Point;
import model.ShipConfig;
import java.util.*;

public class SmartAI implements BattleStrategy {
    private String name;
    private LinkedList<Point> targets = new LinkedList<>(); 
    private Set<String> firedShots = new HashSet<>();
    private List<Point> currentChain = new ArrayList<>(); 
    private List<Point> unresolvedHits = new ArrayList<>();
    
    private List<Integer> enemyShips = ShipConfig.getSizesList();

    private int boardSize = 10;
    private Random rand = new Random();

    public SmartAI(String name) {
    	
        this.name = name;
    }

    @Override
    public int[] makeMove() {
        while (!targets.isEmpty()) {
            Point p = targets.removeFirst();
            if (!isFired(p.x, p.y)) {
                registerShot(p.x, p.y);
                return new int[]{p.x, p.y};
            }
        }

        Point bestMove = getBestMoveFromHeatmap();
        registerShot(bestMove.x, bestMove.y);
        return new int[]{bestMove.x, bestMove.y};
    }

    @Override
    public void processResult(int x, int y, CellState result, List<Point> sunkShipCoords) {
        if (result == CellState.HIT) {
        	unresolvedHits.add(new Point(x, y));
            currentChain.add(new Point(x, y));
            planNextShotsSmartly(); 
        } 
        else if (result == CellState.SUNK) {
            if (sunkShipCoords != null) {
                int size = sunkShipCoords.size();
                enemyShips.remove((Integer) size); 
                for (Point p : sunkShipCoords) {
                    unresolvedHits.removeIf(uh -> uh.x == p.x && uh.y == p.y);
                }
            }
            currentChain.clear(); 
            targets.clear();
            if (!unresolvedHits.isEmpty()) {
                currentChain.addAll(unresolvedHits);
                planNextShotsSmartly();
            }
        }
    }

    private void planNextShotsSmartly() {
        targets.clear();
        
        if (currentChain.size() == 1) {
            Point p = currentChain.get(0);
            List<PointScore> moves = new ArrayList<>();
            
            moves.add(evaluateTarget(p.x, p.y - 1, 0, -1, p));
            moves.add(evaluateTarget(p.x, p.y + 1, 0, 1, p));  
            moves.add(evaluateTarget(p.x - 1, p.y, -1, 0, p));
            moves.add(evaluateTarget(p.x + 1, p.y, 1, 0, p)); 
            
            moves.sort((a, b) -> a.score - b.score);
            for (PointScore ps : moves) {
                if (ps.isValid) forceAddTarget(ps.point.x, ps.point.y);
            }
        } else {
            sortCurrentChain();
            Point first = currentChain.get(0);
            Point last = currentChain.get(currentChain.size() - 1);
            boolean isHorizontal = (first.y == last.y);
            
            List<PointScore> moves = new ArrayList<>();
            if (isHorizontal) {
                moves.add(evaluateTarget(first.x - 1, first.y, -1, 0, first));
                moves.add(evaluateTarget(last.x + 1, last.y, 1, 0, last));
            } else {
                moves.add(evaluateTarget(first.x, first.y - 1, 0, -1, first));
                moves.add(evaluateTarget(last.x, last.y + 1, 0, 1, last));
            }
            
            moves.sort((a, b) -> a.score - b.score);
            for (PointScore ps : moves) {
                if (ps.isValid) forceAddTarget(ps.point.x, ps.point.y);
            }
        }
    }

    private void forceAddTarget(int x, int y) {
        if (isValid(x, y) && !isFired(x, y)) {
            targets.removeIf(p -> p.x == x && p.y == y);
            targets.addFirst(new Point(x, y));
        }
    }

    private Point getBestMoveFromHeatmap() {
        int[][] probabilityMap = new int[boardSize][boardSize];
        int maxScore = -1;
        List<Point> candidates = new ArrayList<>();


        for (int shipLen : enemyShips) {
            for (int y = 0; y < boardSize; y++) {
                for (int x = 0; x <= boardSize - shipLen; x++) {
                    if (canShipFit(x, y, shipLen, true)) {
                        for (int k = 0; k < shipLen; k++) probabilityMap[y][x + k]++;
                    }
                }
            }
            for (int x = 0; x < boardSize; x++) {
                for (int y = 0; y <= boardSize - shipLen; y++) {
                    if (canShipFit(x, y, shipLen, false)) {
                        for (int k = 0; k < shipLen; k++) probabilityMap[y + k][x]++;
                    }
                }
            }
        }

        int minShipSize = Collections.min(enemyShips); 

        for (int y = 0; y < boardSize; y++) {
            for (int x = 0; x < boardSize; x++) {
                if (isFired(x, y)) continue;
                
                int spaceX = countContinuousSpace(x, y, true);
                int spaceY = countContinuousSpace(x, y, false);
                if (spaceX < minShipSize && spaceY < minShipSize) continue;

                if (minShipSize >= 2 && (x + y) % 2 != 0) continue; 

                int score = probabilityMap[y][x];

                int distToEdgeX = Math.min(x, boardSize - 1 - x);
                int distToEdgeY = Math.min(y, boardSize - 1 - y);
                int distToEdge = Math.min(distToEdgeX, distToEdgeY);
                
                int edgeBonus = (5 - distToEdge) * 3; 
                score += edgeBonus;

                if (score > maxScore) {
                    maxScore = score;
                    candidates.clear(); 
                    candidates.add(new Point(x, y));
                } else if (score == maxScore) {
                    candidates.add(new Point(x, y));
                }
            }
        }

        return candidates.get(rand.nextInt(candidates.size()));
    }
    
    private int countContinuousSpace(int x, int y, boolean horizontal) {
        int count = 0;

        int dx = horizontal ? 1 : 0;
        int dy = horizontal ? 0 : 1;
        int cx = x, cy = y;
        while (isValid(cx, cy) && !isFired(cx, cy)) {
            count++;
            cx += dx;
            cy += dy;
        }

        dx = horizontal ? -1 : 0;
        dy = horizontal ? 0 : -1;
        cx = x - dx; 
        cy = y - dy;
        while (isValid(cx, cy) && !isFired(cx, cy)) {
            count++;
            cx += dx;
            cy += dy;
        }

        return count;
    }
    private PointScore evaluateTarget(int targetX, int targetY, int dx, int dy, Point origin) {
        if (!isValid(targetX, targetY) || isFired(targetX, targetY)) 
            return new PointScore(new Point(targetX, targetY), -1, false);

        int minShip = enemyShips.isEmpty() ? 1 : Collections.min(enemyShips);
        
        int spaceBehind = 0;
        int spaceAhead = 0;
        
        int backX = origin.x - dx;
        int backY = origin.y - dy;
        while (isValid(backX, backY) && (!isFired(backX, backY) || isKnownHit(backX, backY))) {
            spaceBehind++;
            backX -= dx;
            backY -= dy;
        }
        
        int forwardX = targetX;
        int forwardY = targetY;
        while (isValid(forwardX, forwardY) && (!isFired(forwardX, forwardY) || isKnownHit(forwardX, forwardY))) {
            spaceAhead++;
            forwardX += dx;
            forwardY += dy;
        }
        
        int totalSpace = spaceBehind + spaceAhead + 1;
        
        if (totalSpace < minShip) {
            return new PointScore(new Point(targetX, targetY), -1, false);
        }
        
        int score = 0;
        for (int shipLen : enemyShips) {
            if (totalSpace >= shipLen) {
                score += (totalSpace - shipLen + 1);
            }
        }
        
        return new PointScore(new Point(targetX, targetY), score, true);
    }

    private boolean isKnownHit(int x, int y) {
        for (Point p : currentChain) if (p.x == x && p.y == y) return true;
        
        if (unresolvedHits != null) {
            for (Point p : unresolvedHits) if (p.x == x && p.y == y) return true;
        }
        return false;
    }

    private boolean canShipFit(int x, int y, int length, boolean horizontal) {
        for (int k = 0; k < length; k++) {
            int cx = x + (horizontal ? k : 0); 
            int cy = y + (horizontal ? 0 : k);
            if (isFired(cx, cy)) return false;
        }
        return true;
    }

    private void sortCurrentChain() {
        currentChain.sort((p1, p2) -> 
            (p1.x != p2.x) ? p1.x - p2.x : p1.y - p2.y
        );
    }

    private boolean isValid(int x, int y) { 
        return x >= 0 && x < boardSize && y >= 0 && y < boardSize; 
    }

    private void registerShot(int x, int y) { 
        firedShots.add(x + "," + y); 
    }

    private boolean isFired(int x, int y) { 
        return firedShots.contains(x + "," + y); 
    }

    @Override 
    public String getName() { 
        return name; 
    }
    
    private class PointScore { 
        Point point; 
        int score; 
        boolean isValid; 
        PointScore(Point p, int s, boolean v) { 
            point = p; 
            score = s; 
            isValid = v; 
        } 
    }
}