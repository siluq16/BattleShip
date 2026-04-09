package logic;

import ai.BattleStrategy;
import model.Board;
import model.CellState;
import model.Point; 
import model.Ship;  
import java.util.List; 

public class GameEngine {
    private Board board1; 
    private Board board2; 
    private BattleStrategy p1;
    private BattleStrategy p2;
    
    private boolean isTurnPlayer1 = true; 
    private GameListener listener;

    private int[] shots = {0, 0};
    private int[] hits  = {0, 0};

    public GameEngine(BattleStrategy p1, BattleStrategy p2, Board b1, Board b2) {
        this.p1 = p1;
        this.p2 = p2;
        this.board1 = b1;
        this.board2 = b2;
    }

    public void setListener(GameListener listener) { 
        this.listener = listener; 
    }

    public CellState shoot() {
        BattleStrategy attacker = isTurnPlayer1 ? p1 : p2;
        int[] move = attacker.makeMove();
        if (move == null) return CellState.WATER; 
        return processShot(move[0], move[1]);
    }
    
    public CellState manualShoot(int x, int y) {
        return processShot(x, y);
    }

    private CellState processShot(int x, int y) {
        BattleStrategy attacker = isTurnPlayer1 ? p1 : p2;
        Board defenderBoard   = isTurnPlayer1 ? board2 : board1;
        int defenderId        = isTurnPlayer1 ? 2 : 1;
        int attackerIdx       = isTurnPlayer1 ? 0 : 1;

        if (defenderBoard.getCellState(x, y) != CellState.WATER) {
            return defenderBoard.getCellState(x, y); 
        }

        CellState result = defenderBoard.receiveShot(x, y);
        List<Point> sunkCoords = null;

        shots[attackerIdx]++;
        if (result == CellState.HIT || result == CellState.SUNK) {
            hits[attackerIdx]++;
        }

        if (result == CellState.SUNK) {
            Ship s = defenderBoard.getLastSunkShip();
            if (s != null) sunkCoords = s.getCoordinates();
        }

        attacker.processResult(x, y, result, sunkCoords);

        if (listener != null) {
            listener.onUpdate(defenderId, x, y, result);
        }
        
        if (defenderBoard.allShipsSunk() && listener != null) {
            listener.onGameOver(attacker.getName());
        }

        return result; 
    }

    public void switchTurn() {
        isTurnPlayer1 = !isTurnPlayer1;
    }

    public boolean isTurnPlayer1() { 
        return isTurnPlayer1; 
    }

    public boolean isCurrentPlayerHuman() {
        return (isTurnPlayer1 && p1 instanceof ai.HumanStrategy) ||
               (!isTurnPlayer1 && p2 instanceof ai.HumanStrategy);
    }

    public boolean isGameOver() { 
        return board1.allShipsSunk() || board2.allShipsSunk(); 
    }

    public Board getBoard1() { return board1; }
    public Board getBoard2() { return board2; }
    public BattleStrategy getp1() { return p1; }
    public BattleStrategy getp2() { return p2; }

    public int getShots(int playerIndex) { return shots[playerIndex]; }

    public int getHits(int playerIndex) { return hits[playerIndex]; }

    public double getAccuracy(int playerIndex) {
        return shots[playerIndex] == 0 ? 0.0 : (double) hits[playerIndex] / shots[playerIndex];
    }}