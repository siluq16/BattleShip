package ai;
import model.CellState;
import model.Point;      // <--- THÊM DÒNG NÀY
import java.util.List;

public interface BattleStrategy {
    int[] makeMove(); 

    void processResult(int x, int y, CellState result, List<Point> sunkShipCoords);
    
    String getName();
}