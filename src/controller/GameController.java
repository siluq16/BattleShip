package controller;

import ai.BattleStrategy;
import ai.HumanStrategy;
import ai.SmartAI;
import logic.GameEngine;
import logic.GameListener;
import model.Board;
import model.CellState;
import model.Ship;
import network.NetworkManager;
import utils.SoundManager;
import view.*;

import javax.swing.*;
import java.awt.*;

public class GameController implements GameListener, NetworkManager.NetworkListener {

    private final JFrame         frame;
    private final JPanel         mainContainer;
    private final CardLayout     cardLayout;
    private final MainMenuPanel  menuPanel;
    private final SetupPanel     setupPanel;
    private final GamePlayPanel  gamePlayPanel;

    private GameEngine     engine;
    private NetworkManager netManager;

    private boolean isAnimating           = false;
    private boolean isEvEMode             = false;
    private boolean isLanMode             = false;
    private boolean isGameOverBySurrender = false;
    private boolean lanReadyToSend        = false;

    private Board  lanEnemyBoard = null;
    private JDialog waitDialog   = null;

    private String myLanName    = "Bạn";
    private String enemyLanName = "Đối Thủ";
    private String aiSubtitle   = "Máy";

    private static final int BOT_THINK_MS = 1200;
    private Timer botTimer;


    public GameController(JFrame frame,
                          JPanel mainContainer,
                          CardLayout cardLayout,
                          MainMenuPanel menuPanel,
                          SetupPanel setupPanel,
                          GamePlayPanel gamePlayPanel) {
        this.frame         = frame;
        this.mainContainer = mainContainer;
        this.cardLayout    = cardLayout;
        this.menuPanel     = menuPanel;
        this.setupPanel    = setupPanel;
        this.gamePlayPanel = gamePlayPanel;

        initBotTimer();
        wireMenuListeners();
        wireSetupListeners();
        wireGamePlayListeners();
    }

    private void wireMenuListeners() {
        menuPanel.setListener(new MainMenuPanel.MainMenuListener() {
            @Override public void onPlayPvE()  { startSetupPhase(false); }
            @Override public void onHostLAN()  { hostLAN(); }
            @Override public void onJoinLAN()  { joinLAN(); }
            @Override public void onPlayEvE()  { startGameEvE(); }
        });
    }

    private void wireSetupListeners() {
        setupPanel.setListener(new SetupPanel.SetupListener() {
            @Override
            public void onStart() {
                if (isLanMode) {
                    lanReadyToSend = true;
                    setupPanel.showWaitingOverlay();
                    sendBoardOverLan();
                    checkLanStart();
                } else {
                    startGamePvE();
                }
            }

            @Override
            public void onExit() {
                if (isLanMode && netManager != null) {
                    isGameOverBySurrender = true;
                    netManager.sendMessage("QUIT");
                    netManager.disconnect();
                }
                cardLayout.show(mainContainer, "MENU");
            }
        });
    }

    private void wireGamePlayListeners() {
        gamePlayPanel.setListener(new GamePlayPanel.GamePlayListener() {
            @Override
            public void onShootAction(int x, int y) {
                handlePlayerShoot(x, y);
            }

            @Override
            public void onPauseResume(boolean isResuming) {
                if (isResuming) {
                    if (engine != null && !engine.isGameOver()) {
                        gamePlayPanel.resumeTurnTimer();
                        scheduleBotShot();
                    }
                } else {
                    botTimer.stop();
                    gamePlayPanel.pauseTurnTimer();
                }
            }

            @Override
            public void onSurrenderOrExit() {
                stopAllTimers();
                if (isLanMode && netManager != null) {
                    isGameOverBySurrender = true;
                    netManager.sendMessage(engine != null ? "SURRENDER" : "QUIT");
                    netManager.disconnect();
                }
                cardLayout.show(mainContainer, "MENU");
            }

            @Override
            public void onTurnTimeout() {
                handleTurnTimeout();
            }

            @Override
            public void onChatSend(String message) {
                if (isLanMode && netManager != null)
                    netManager.sendMessage("CHAT:" + message);
            }
        });
    }

    private void initBotTimer() {
        botTimer = new Timer(BOT_THINK_MS, e -> {
            botTimer.stop();
            if (engine == null || engine.isGameOver() || isAnimating) return;
            boolean isBotTurn = isEvEMode || (!isLanMode && !engine.isTurnPlayer1());
            if (isBotTurn) engine.shoot();
        });
        botTimer.setRepeats(false);
    }

    private void scheduleBotShot() {
        if (engine == null || engine.isGameOver()) return;
        boolean isBotTurn = isEvEMode || (!isLanMode && !engine.isTurnPlayer1());
        if (isBotTurn && !botTimer.isRunning()) botTimer.restart();
    }

    private void handlePlayerShoot(int x, int y) {
        if (isEvEMode) return;
        if (isAnimating || engine == null || engine.isGameOver()) return;
        if (!engine.isTurnPlayer1()) return;
        if (engine.getBoard2().getCellState(x, y) != model.CellState.WATER) return;

        SoundManager.playSound("shoot");
        if (isLanMode && netManager != null)
            netManager.sendMessage("SHOOT:" + x + "," + y);
        engine.manualShoot(x, y);
    }

    private void handleTurnTimeout() {
        if (engine != null && !engine.isGameOver() && !isAnimating) {
            engine.switchTurn();
            gamePlayPanel.updateBoardsAndBanner(engine.isTurnPlayer1(), isEvEMode);
            Timer delay = new Timer(1500, e2 -> {
                ((Timer) e2.getSource()).stop();
                if (!isEvEMode) gamePlayPanel.startTurnTimer();
                scheduleBotShot();
            });
            delay.setRepeats(false);
            delay.start();
        }
    }

    public void startSetupPhase(boolean isLan) {
        isLanMode = isLan;
        setupPanel.reset();
        setupPanel.setLanMode(isLan);
        setupPanel.startCountdown();
        cardLayout.show(mainContainer, "SETUP");
    }


    public void hostLAN() {
        String name = UIComponents.askPlayerNameDialog(frame, "Tên của bạn (Host):");
        if (name == null) return;
        myLanName = name.trim().isEmpty() ? "Host" : name.trim();

        netManager = new NetworkManager(this);
        netManager.hostGameWithCode();

        new Timer(50, e -> {
            ((Timer) e.getSource()).stop();
            String code = netManager.getRoomCode();
            waitDialog = UIComponents.lanWaitDialog(
                    frame, code != null ? code : "...",
                    () -> netManager.disconnect());
            waitDialog.setVisible(true);
        }).start();
    }

    public void joinLAN() {
        String[] result = UIComponents.joinLanDialog(frame);
        if (result != null && result[0] != null && !result[0].trim().isEmpty()) {
            myLanName = (result[1] != null && !result[1].trim().isEmpty())
                    ? result[1].trim()
                    : "Người Chơi";
            netManager = new NetworkManager(this);
            netManager.joinGameWithCode(result[0].trim());
        }
    }


    private void startGamePvE() {
        isEvEMode = false;
        Board botBoard = new Board();
        botBoard.placeShipsRandomly();

        ai.AIDifficulty diff     = setupPanel.getSelectedDifficulty();
        BattleStrategy  aiStrat  = diff.createStrategy("Máy");
        aiSubtitle = "Máy · " + difficultyLabel(diff);

        engine = new GameEngine(
                new HumanStrategy("Bạn"), aiStrat,
                setupPanel.getBoard(), botBoard);
        launchGame();
    }

    private void startGameEvE() {
        isEvEMode = true;
        Board b1 = new Board(); b1.placeShipsRandomly();
        Board b2 = new Board(); b2.placeShipsRandomly();
        engine = new GameEngine(
                new SmartAI("Bot Alpha"), new SmartAI("Bot Beta"), b1, b2);
        aiSubtitle = "Máy · Khó";
        launchGame();
    }

    private void launchGame() {
        engine.setListener(this);
        boolean isGuest = isLanMode && netManager != null && !netManager.isHost();
        gamePlayPanel.setupGame(engine, isLanMode, isGuest);

        if (isEvEMode) {
            gamePlayPanel.setPlayerNames("Bot Alpha", "Bot Beta");
            gamePlayPanel.setAvatarSubtitles("Máy · Khó", "Máy · Khó");
            gamePlayPanel.revealAllForGameOver();
        } else if (isLanMode) {
            gamePlayPanel.setPlayerNames(myLanName, enemyLanName);
            gamePlayPanel.setAvatarSubtitles("Chế độ LAN", "Chế độ LAN");
            gamePlayPanel.setChatEnabled(true, myLanName, enemyLanName);
        } else {
            gamePlayPanel.setPlayerNames("Bạn", "Máy");
            gamePlayPanel.setAvatarSubtitles("Một người chơi", aiSubtitle);
            gamePlayPanel.setChatEnabled(false, "Bạn", "Máy");
        }

        gamePlayPanel.updateBoardsAndBanner(engine.isTurnPlayer1(), isEvEMode);
        if (!isEvEMode) gamePlayPanel.startTurnTimer();
        cardLayout.show(mainContainer, "GAME");
        scheduleBotShot();
    }

    private void sendBoardOverLan() {
        StringBuilder sb = new StringBuilder("BOARD:");
        for (Ship s : setupPanel.getBoard().getShips()) {
            sb.append(s.getX()).append(",").append(s.getY()).append(",")
              .append(s.getLength()).append(",").append(s.isHorizontal()).append(";");
        }
        netManager.sendMessage(sb.toString());
    }

    private void checkLanStart() {
        if (lanReadyToSend && lanEnemyBoard != null) {
            setupPanel.hideWaitingOverlay();
            isEvEMode = false;

            engine = new GameEngine(
                    new HumanStrategy(myLanName),
                    new HumanStrategy(enemyLanName),
                    setupPanel.getBoard(), lanEnemyBoard);

            if (netManager != null && !netManager.isHost()) engine.switchTurn();

            launchGame();
        }
    }

    private void stopAllTimers() {
        if (botTimer      != null) botTimer.stop();
        if (gamePlayPanel != null) gamePlayPanel.stopTurnTimer();
    }

    @Override
    public void onUpdate(int defenderId, int x, int y, CellState result) {
        gamePlayPanel.stopTurnTimer();
        gamePlayPanel.repaintBoards();

        if      (result == CellState.HIT)    SoundManager.playSound("hit");
        else if (result == CellState.MISSED) SoundManager.playSound("miss");
        else if (result == CellState.SUNK)   SoundManager.playSound("hit");

        if (result == CellState.MISSED) {
            engine.switchTurn();
            gamePlayPanel.updateBoardsAndBanner(engine.isTurnPlayer1(), isEvEMode);
            if (!isEvEMode) gamePlayPanel.resetTurnTimer();
            scheduleBotShot();

        } else if (result == CellState.HIT && !engine.isGameOver()) {
            gamePlayPanel.updateBoardsAndBanner(engine.isTurnPlayer1(), isEvEMode);
            gamePlayPanel.resetTurnTimer();
            scheduleBotShot();

        } else if (result == CellState.SUNK && !engine.isGameOver()) {
            isAnimating = true;
            Timer sunkDelay = new Timer(300, e -> {
                ((Timer) e.getSource()).stop();
                if (engine != null && !engine.isGameOver()) {
                    gamePlayPanel.updateBoardsAndBanner(engine.isTurnPlayer1(), isEvEMode);
                    gamePlayPanel.resetTurnTimer();
                    isAnimating = false;
                    scheduleBotShot();
                }
            });
            sunkDelay.setRepeats(false);
            sunkDelay.start();
        }
    }

    @Override
    public void onGameOver(String winner) {
        stopAllTimers();
        isAnimating = false;

        boolean isWin  = winner.equals(myLanName) || winner.equals("Bạn");
        boolean isLose = winner.equals("Máy") || winner.equals("Đối Thủ") || winner.equals(enemyLanName);

        String bannerText;
        Color  bannerColor;
        if (isWin && !isLose) {
            bannerText  = "CHIẾN THẮNG!";
            bannerColor = UITheme.SUCCESS;
            SoundManager.playSound("win");
        } else if (isLose) {
            bannerText  = "THẤT BẠI!";
            bannerColor = UITheme.DANGER;
            SoundManager.playSound("win");
        } else {
            bannerText  = winner + " THẮNG!";
            bannerColor = UITheme.WARNING;
        }

        gamePlayPanel.revealAllForGameOver();
        if (netManager != null) netManager.disconnect();

        new Timer(1500, e -> {
            ((Timer) e.getSource()).stop();
            showGameOverDialog(winner, bannerText, bannerColor);
        }).start();
    }

    @Override
    public void onConnected() {
        netManager.sendMessage("NAME:" + myLanName);
        SwingUtilities.invokeLater(() -> {
            if (waitDialog != null) { waitDialog.dispose(); waitDialog = null; }
            lanEnemyBoard      = null;
            lanReadyToSend     = false;
            isGameOverBySurrender = false;
            startSetupPhase(true);
        });
    }

    @Override
    public void onError(String err) {
        SwingUtilities.invokeLater(() -> {
            if (waitDialog != null) { waitDialog.dispose(); waitDialog = null; }
            stopAllTimers();
            if (netManager != null) netManager.disconnect();
            if (!isGameOverBySurrender)
                showLanResultDialog("📡  MẤT KẾT NỐI",
                        "Kết nối bị gián đoạn.\n" + err, UITheme.DANGER);
            else
                cardLayout.show(mainContainer, "MENU");
        });
    }

    @Override
    public void onMessageReceived(String msg) {
        SwingUtilities.invokeLater(() -> processNetworkMessage(msg));
    }

    private void processNetworkMessage(String msg) {
        if (msg.startsWith("NAME:")) {
            enemyLanName = msg.substring(5).trim();
            if (enemyLanName.isEmpty()) enemyLanName = "Đối Thủ";

        } else if (msg.startsWith("BOARD:")) {
            lanEnemyBoard = parseBoardMessage(msg.substring(6));
            checkLanStart();

        } else if (msg.startsWith("SHOOT:")) {
            if (engine.isTurnPlayer1()) engine.switchTurn();
            String[] p = msg.substring(6).split(",");
            engine.manualShoot(Integer.parseInt(p[0]), Integer.parseInt(p[1]));

        } else if (msg.startsWith("CHAT:")) {
            gamePlayPanel.addChatMessage(enemyLanName, msg.substring(5), false);

        } else if (msg.equals("SURRENDER")) {
            isGameOverBySurrender = true;
            stopAllTimers();
            if (netManager != null) netManager.disconnect();
            showLanResultDialog("CHIẾN THẮNG!",
                    enemyLanName + " đã đầu hàng — bạn là người chiến thắng!",
                    UITheme.SUCCESS);

        } else if (msg.equals("QUIT")) {
            isGameOverBySurrender = true;
            stopAllTimers();
            if (netManager != null) netManager.disconnect();
            boolean inGame = engine != null && !engine.isGameOver();
            showLanResultDialog(
                    inGame ? "CHIẾN THẮNG!" : "PHÒNG ĐÃ ĐÓNG",
                    enemyLanName + (inGame
                            ? " đã rời trận — bạn là người chiến thắng!"
                            : " đã thoát khỏi phòng."),
                    inGame ? UITheme.SUCCESS : UITheme.WARNING);
        }
    }

    private Board parseBoardMessage(String data) {
        Board board = new Board();
        for (String sData : data.split(";")) {
            if (sData.isEmpty()) continue;
            String[] p = sData.split(",");
            board.placeShip(
                    Integer.parseInt(p[0]), Integer.parseInt(p[1]),
                    Integer.parseInt(p[2]), Boolean.parseBoolean(p[3]));
        }
        return board;
    }

    private void showLanResultDialog(String title, String message, Color accent) {
        UIComponents.showDialog(frame, title, message, accent,
                new Object[]{"Về Menu", (Runnable) () -> cardLayout.show(mainContainer, "MENU")});
    }

    private void showGameOverDialog(String winner, String bannerText, Color accent) {
        int[] stats = (engine != null) ? new int[]{
                engine.getShots(0), engine.getHits(0),
                engine.getShots(1), engine.getHits(1)
        } : null;

        UIComponents.showGameOverDialog(
                frame, bannerText, winner, accent, stats,
                () -> {
                    if (isEvEMode)       startGameEvE();
                    else if (isLanMode)  { showLanResultDialog("LAN MODE",
                            "Tạo/vào lại phòng để chơi ván mới!", UITheme.PRIMARY);
                                           cardLayout.show(mainContainer, "MENU"); }
                    else                 startSetupPhase(false);
                },
                () -> cardLayout.show(mainContainer, "MENU"),
                () -> System.exit(0)
        );
    }

    public void handleAppExit() {
        if (isLanMode && netManager != null && !isGameOverBySurrender) {
            netManager.sendMessage("QUIT");
            netManager.disconnect();
        }
        System.exit(0);
    }

    private String difficultyLabel(ai.AIDifficulty diff) {
        if (diff == null) return "";
        switch (diff.name()) {
            case "EASY":   return "Dễ";
            case "MEDIUM": return "Trung bình";
            case "HARD":   return "Khó";
            default:       return diff.toString();
        }
    }
}