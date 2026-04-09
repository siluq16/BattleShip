package logic;

import model.CellState;

public interface GameListener {
    void onUpdate(int botId, int x, int y, CellState result);
    
    void onGameOver(String winner);
}