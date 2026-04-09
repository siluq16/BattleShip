package view;

import logic.GameEngine;
import model.Board;
import model.Ship;
import utils.ImageLoader;
import utils.SoundManager;
import model.CellState;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.List;

public class GamePlayPanel extends JPanel {

    private JLayeredPane gameLayeredPane;
    private BoardPanel   leftBoardView;    
    private BoardPanel   rightBoardView; 
    private JLabel       leftBanner, rightBanner;
    private JLabel       turnBadge;

    private EnemyShipTracker enemyShipTracker;   
    private TurnCounter      turnCounter;
    private GameEngine       engine;
    private GamePlayListener listener;
    private boolean          isLanMode;

    private static final int TURN_SECONDS = 30;
    private int    turnSecondsLeft = TURN_SECONDS;
    private Timer  turnTimer;
    private JLabel turnTimerLabel;

    private PlayerAvatarCard avatarLeft;   
    private PlayerAvatarCard avatarRight;
    private String namePlayer1 = "BẠN";
    private String namePlayer2 = "ĐỊCH";
    private int    turnCount   = 0;

    public interface GamePlayListener {
        void onShootAction(int x, int y);
        void onPauseResume(boolean isResuming);
        void onSurrenderOrExit();
        void onTurnTimeout();
        void onChatSend(String message);
    }

    private JPanel     chatPanel;
    private JPanel     chatMessages;
    private JTextField chatInput;
    private boolean    chatVisible = false;
    private String     myName  = "Bạn";
    private String     oppName = "Đối Thủ";
    private JButton    chatButton;
    private int        unreadCount = 0;
    private boolean isMyTurn = true;

    private class EnemyShipTracker extends JPanel {
        private final int[] shipLengths = model.ShipConfig.getSizesFlat();
        private boolean[]   sunkFlags   = new boolean[shipLengths.length];
        private int[]       sunkCount   = new int[7];   
        private int[]       totalCount  = new int[7];  
        private int         totalRemaining = shipLengths.length;

        EnemyShipTracker() {
            setOpaque(false);
            for (int len : shipLengths) totalCount[Math.min(len, 6)]++;
        }

        void refresh(GameEngine eng) {
            if (eng == null) return;
            Board enemyBoard = eng.getBoard2();
            if (enemyBoard == null) return;
            List<Ship> ships = enemyBoard.getShips();
            sunkCount = new int[7];
            for (Ship s : ships) if (s.isSunk()) sunkCount[Math.min(s.getLength(), 6)]++;
            int[] usedSunk = new int[7];
            sunkFlags      = new boolean[shipLengths.length];
            totalRemaining = 0;
            for (int i = 0; i < shipLengths.length; i++) {
                int len = Math.min(shipLengths[i], 6);
                if (usedSunk[len] < sunkCount[len]) { sunkFlags[i] = true; usedSunk[len]++; }
                else totalRemaining++;
            }
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();

            g2.setColor(new Color(18, 32, 75, 220));
            g2.fill(new RoundRectangle2D.Float(0, 0, w, h, UITheme.RADIUS_MD, UITheme.RADIUS_MD));
            g2.setColor(new Color(200, 140, 50));
            g2.setStroke(new BasicStroke(1.5f));
            g2.draw(new RoundRectangle2D.Float(0.75f, 0.75f, w - 1.5f, h - 1.5f, UITheme.RADIUS_MD, UITheme.RADIUS_MD));

            int padX = 16;
            g2.setFont(UITheme.body(11, Font.BOLD));
            g2.setColor(UITheme.TEXT_PRIMARY); 
            g2.drawString("Hạm đội địch", padX, 22);

            java.util.List<Integer> uniqueLengths = new java.util.ArrayList<>();
            for (int i = 6; i >= 1; i--) { 
                if (totalCount[i] > 0) {
                    uniqueLengths.add(i);
                }
            }

            int n = uniqueLengths.size();
            if (n == 0) { g2.dispose(); return; }

            int blockW = 12; 
            int blockH = 12;
            int gapV   = 3;
            int colGap = 22; 

            int totalColsW = n * blockW + (n - 1) * colGap;
            int shipAreaStartX = (w - totalColsW) / 2;
            
            if (shipAreaStartX < padX + 85) shipAreaStartX = padX + 85; 

            int bottomY = h - 14; 
            g2.setFont(UITheme.body(10, Font.BOLD));
            FontMetrics numFm = g2.getFontMetrics();

            for (int i = 0; i < n; i++) {
                int len = uniqueLengths.get(i);
                int colX = shipAreaStartX + i * (blockW + colGap);

                int remaining = totalCount[len] - sunkCount[len];
                boolean isAllSunk = (remaining <= 0);

                String numLbl = String.valueOf(remaining);
                int numX = colX + (blockW - numFm.stringWidth(numLbl)) / 2;
                
                g2.setColor(isAllSunk ? new Color(220, 140, 55, 100) : new Color(220, 170, 80));
                g2.drawString(numLbl, numX, bottomY);

                int colH = len * blockH + (len - 1) * gapV;
                int startY = bottomY - numFm.getAscent() - 8 - colH; 

                for (int c = 0; c < len; c++) {
                    int by = startY + c * (blockH + gapV);
                    if (isAllSunk) {
                        g2.setColor(new Color(220, 140, 55, 150));
                        g2.setStroke(new BasicStroke(1.2f));
                        g2.draw(new RoundRectangle2D.Float(colX, by, blockW, blockH, 1, 1));
                    } else {
                        g2.setColor(new Color(220, 140, 55));
                        g2.fill(new RoundRectangle2D.Float(colX, by, blockW, blockH, 1, 1));
                    }
                }
            }

            g2.dispose();
        }
    }

    private class TurnCounter {
        void setTurn(boolean isMy, int cnt) {
            turnCount = cnt;
            if (enemyShipTracker != null) enemyShipTracker.repaint();
            if (turnBadge != null) turnBadge.repaint();
        }
    }

    private static class PlayerAvatarCard extends JPanel {
        private String  name;
        private Color   baseColor;
        private boolean isActive  = false;
        private String  subtitle  = "";
        private BufferedImage portrait;

        private static final Color[] PALETTE = {
            new Color(37,  99, 235),  
            new Color(100, 55,  18),   
        };

        PlayerAvatarCard(String name, int colorIndex) {
            this.name      = name;
            this.baseColor = PALETTE[colorIndex % PALETTE.length];
            setOpaque(false);
        }

        void setActive(boolean a)          { isActive = a;   repaint(); }
        void setDisplayName(String n)      { name = n;       repaint(); }
        void setPortrait(BufferedImage img){ portrait = img; repaint(); }
        void setSubtitle(String s)         { subtitle = s != null ? s : ""; repaint(); }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();

            Color bg = isActive
                ? new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), 60)
                : new Color(15, 28, 65, 210);
            g2.setColor(bg);
            g2.fill(new RoundRectangle2D.Float(0, 0, w, h, UITheme.RADIUS_SM, UITheme.RADIUS_SM));

            g2.setColor(isActive
                ? new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), 200)
                : new Color(50, 70, 120, 160));
            g2.setStroke(new BasicStroke(isActive ? 2f : 1f));
            g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 1, h - 1, UITheme.RADIUS_SM, UITheme.RADIUS_SM));

            int portW = Math.min(52, h - 8);
            int portH = h - 8;
            int px = 5, py = 4;
            g2.setColor(new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), 80));
            g2.fill(new RoundRectangle2D.Float(px, py, portW, portH, UITheme.RADIUS_SM, UITheme.RADIUS_SM));
            if (portrait != null) {
                g2.drawImage(portrait, px, py, portW, portH, null);
            } else {
                String init = getInitials(name);
                g2.setFont(UITheme.display(init.length() > 1 ? 14 : 18, Font.BOLD));
                FontMetrics fm = g2.getFontMetrics();
                g2.setColor(Color.WHITE);
                g2.drawString(init,
                    px + (portW - fm.stringWidth(init)) / 2,
                    py + (portH + fm.getAscent() - fm.getDescent()) / 2);
            }

            int tx = px + portW + 8;
            int textW = w - tx - 8;

            g2.setFont(UITheme.display(13, Font.BOLD));
            FontMetrics fm = g2.getFontMetrics();
            g2.setColor(UITheme.TEXT_PRIMARY);
            String dn = clipText(name, fm, textW);
            g2.drawString(dn, tx, py + fm.getAscent() + 2);

            if (!subtitle.isEmpty()) {
                g2.setFont(UITheme.body(10, Font.PLAIN));
                fm = g2.getFontMetrics();
                g2.setColor(UITheme.TEXT_SECONDARY);
                String sub = clipText(subtitle, fm, textW);
                g2.drawString(sub, tx, py + 18 + fm.getAscent());
            }

            if (isActive) {
                g2.setColor(UITheme.SUCCESS);
                g2.fillOval(w - 13, 6, 8, 8);
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawOval(w - 13, 6, 8, 8);
            }

            g2.dispose();
        }

        private String clipText(String text, FontMetrics fm, int maxW) {
            if (fm.stringWidth(text) <= maxW) return text;
            while (text.length() > 1 && fm.stringWidth(text + "…") > maxW)
                text = text.substring(0, text.length() - 1);
            return text + "…";
        }

        private String getInitials(String n) {
            if (n == null || n.isEmpty()) return "?";
            String[] words = n.trim().split("\\s+");
            if (words.length >= 2)
                return String.valueOf(words[0].charAt(0)).toUpperCase()
                     + String.valueOf(words[1].charAt(0)).toUpperCase();
            return n.substring(0, Math.min(2, n.length())).toUpperCase();
        }
    }

    public GamePlayPanel(GamePlayListener listener) {
        this.listener = listener;
        setLayout(new BorderLayout());
        setOpaque(false);

        add(buildTopBar(), BorderLayout.NORTH);

        gameLayeredPane = new JLayeredPane();
        gameLayeredPane.setOpaque(false);

        leftBoardView  = new BoardPanel(null, true, true, null);
        rightBoardView = new BoardPanel(null, false, true, (x, y) -> listener.onShootAction(x, y));
        leftBoardView.setCellSize(40);
        rightBoardView.setCellSize(40);

        leftBanner  = new JLabel(); leftBanner.setVisible(false);
        rightBanner = new JLabel(); rightBanner.setVisible(false);

        enemyShipTracker = new EnemyShipTracker();
        turnCounter      = new TurnCounter();

        turnBadge = buildTurnBadge();

        avatarLeft  = new PlayerAvatarCard("BẠN",  0);
        avatarRight = new PlayerAvatarCard("ĐỊCH", 1);

        gameLayeredPane.add(leftBoardView,    JLayeredPane.DEFAULT_LAYER);
        gameLayeredPane.add(rightBoardView,   JLayeredPane.DEFAULT_LAYER);
        gameLayeredPane.add(enemyShipTracker, JLayeredPane.PALETTE_LAYER);
        gameLayeredPane.add(turnBadge,        JLayeredPane.PALETTE_LAYER);
        gameLayeredPane.add(avatarLeft,       JLayeredPane.PALETTE_LAYER);
        gameLayeredPane.add(avatarRight,      JLayeredPane.PALETTE_LAYER);

        chatPanel = buildChatPanel();
        chatPanel.setVisible(false);
        gameLayeredPane.add(chatPanel, JLayeredPane.PALETTE_LAYER);

        add(gameLayeredPane, BorderLayout.CENTER);

        gameLayeredPane.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override public void componentResized(java.awt.event.ComponentEvent e) {
                layoutComponents();
            }
        });

        turnTimer = new Timer(1000, e -> {
            turnSecondsLeft--;
            updateTurnTimerLabel();
            if (turnSecondsLeft <= 0) {
                stopTurnTimer();
                listener.onTurnTimeout();
            }
        });
    }

    private void layoutComponents() {
        int W = gameLayeredPane.getWidth();
        int H = gameLayeredPane.getHeight();
        if (W == 0 || H == 0) return;

        int margin  = 12;
        int avatarH = 68;
        int gap     = 6;

        int maxLen   = 0;
        for (int len : model.ShipConfig.getSizesFlat()) if (len > maxLen) maxLen = len;
        int trackerH = maxLen * 12 + (maxLen - 1) * 3 + 45;
        int availH = H - margin - avatarH - gap - trackerH - gap - margin;
        int halfW  = (W - margin * 3) / 2;

        int baseCell = Math.min(halfW - 42, availH - 42) / 10;
        baseCell = Math.max(22, Math.min(baseCell, 48));

        int myCell  = Math.max(20, (int)(baseCell * 0.92));   // 92% — nhỏ hơn địch 1 chút
        int oppCell = Math.max(24, Math.min((int)(baseCell * 1.08), 52)); // 108%

        int myBW  = myCell  * 10 + 32 + 10;
        int myBH  = myCell  * 10 + 32 + 10;
        int oppBW = oppCell * 10 + 32 + 10;
        int oppBH = oppCell * 10 + 32 + 10;

        int leftX  = margin;
        int rightX = W - margin - oppBW;

        int topY = margin;

        avatarLeft.setBounds(leftX, topY, myBW, avatarH);
        avatarRight.setBounds(rightX, topY, oppBW, avatarH);

        int badgeX = leftX + myBW + gap;
        int badgeW = rightX - badgeX - gap;
        if (badgeW > 20) {
            turnBadge.setBounds(badgeX, topY, badgeW, avatarH);
            turnBadge.setVisible(true);
        } else {
            turnBadge.setVisible(false);
        }

        int trackerY = topY + avatarH + gap;
        
        int trackerW = (int)(myBW * 0.85);
        int trackerX = leftX + (myBW - trackerW) / 2;
        enemyShipTracker.setBounds(trackerX, trackerY, trackerW, trackerH);

        int myBoardY = trackerY + trackerH + gap;
        leftBoardView.setCellSize(myCell);
        leftBoardView.setBounds(leftX, myBoardY, myBW, myBH);

        int oppBoardY = trackerY; 
        rightBoardView.setCellSize(oppCell);
        rightBoardView.setBounds(rightX, oppBoardY, oppBW, oppBH);

        chatPanel.setBounds(W - 312, H - 362, 302, 352);

        gameLayeredPane.revalidate();
        gameLayeredPane.repaint();
    }

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                GradientPaint gp = new GradientPaint(0, getHeight()-1, UITheme.ACCENT,
                        getWidth(), getHeight()-1, UITheme.PRIMARY);
                g2.setPaint(gp);
                g2.setStroke(new BasicStroke(2f));
                g2.drawLine(0, getHeight()-1, getWidth(), getHeight()-1);
                g2.dispose();
            }
        };
        bar.setOpaque(false);
        bar.setBorder(BorderFactory.createEmptyBorder(10, 20, 8, 20));

        turnTimerLabel = new JLabel("0:30") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = turnSecondsLeft <= 10 ? UITheme.DANGER : UITheme.BG_ELEVATED;
                g2.setColor(bg);
                g2.fill(new java.awt.geom.RoundRectangle2D.Float(0,0,getWidth(),getHeight(),
                        UITheme.RADIUS_PILL, UITheme.RADIUS_PILL));
                g2.setColor(turnSecondsLeft <= 10 ? UITheme.DANGER_DARK : UITheme.BORDER);
                g2.setStroke(new BasicStroke(1.2f));
                g2.draw(new java.awt.geom.RoundRectangle2D.Float(0.6f,0.6f,getWidth()-1.2f,getHeight()-1.2f,
                        UITheme.RADIUS_PILL, UITheme.RADIUS_PILL));
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                String t = getText();
                g2.setColor(Color.WHITE);
                g2.drawString(t, (getWidth()-fm.stringWidth(t))/2,
                        (getHeight()+fm.getAscent()-fm.getDescent())/2);
                g2.dispose();
            }
        };
        turnTimerLabel.setFont(UITheme.mono(14, Font.BOLD));
        turnTimerLabel.setOpaque(false);
        turnTimerLabel.setPreferredSize(new Dimension(70, 32));
        bar.add(turnTimerLabel, BorderLayout.WEST);

        JLabel title = new JLabel("CHIẾN HẠM ĐẠI DƯƠNG", SwingConstants.CENTER);
        title.setFont(UITheme.display(20, Font.BOLD));
        title.setForeground(UITheme.TEXT_PRIMARY);
        bar.add(title, BorderLayout.CENTER);

        JPanel rightBtns = UIComponents.transparentPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        chatButton = UIComponents.outlineButton("Chat");
        chatButton.setPreferredSize(new Dimension(110, 36));
        chatButton.setVisible(false);
        chatButton.addActionListener(e -> toggleChat());
        JButton btnPause = UIComponents.outlineButton("Cài Đặt");
        btnPause.setPreferredSize(new Dimension(150, 36));
        btnPause.addActionListener(e -> showPauseMenu());
        rightBtns.add(chatButton);
        rightBtns.add(btnPause);
        bar.add(rightBtns, BorderLayout.EAST);

        return bar;
    }

    private JLabel buildBannerLabel() {
        JLabel lbl = new JLabel();
        lbl.setVisible(false);
        return lbl;
    }

    private JLabel buildTurnBadge() {
        JLabel lbl = new JLabel("", SwingConstants.CENTER) {
            @Override protected void paintComponent(Graphics g) {
                if (getWidth() < 10) return;
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();

                g2.setColor(new Color(18, 32, 75, 210));
                g2.fill(new RoundRectangle2D.Float(0, 0, w, h, UITheme.RADIUS_MD, UITheme.RADIUS_MD));
                Color borderC = turnCount > 0 && isMyTurn ? UITheme.PRIMARY : UITheme.DANGER;
                g2.setColor(new Color(borderC.getRed(), borderC.getGreen(), borderC.getBlue(), 160));
                g2.setStroke(new BasicStroke(1.5f));
                g2.draw(new RoundRectangle2D.Float(0.75f, 0.75f, w-1.5f, h-1.5f,
                        UITheme.RADIUS_MD, UITheme.RADIUS_MD));

                g2.setFont(UITheme.body(9, Font.BOLD));
                g2.setColor(UITheme.TEXT_SECONDARY);
                FontMetrics fm = g2.getFontMetrics();
                String lblTxt = "Lượt";
                g2.drawString(lblTxt, (w - fm.stringWidth(lblTxt)) / 2, 16);

                g2.setFont(UITheme.display(28, Font.BOLD));
                fm = g2.getFontMetrics();
                Color numCol = isMyTurn ? UITheme.ACCENT_LIGHT : UITheme.DANGER;
                g2.setColor(numCol);
                String num = String.valueOf(turnCount);
                g2.drawString(num, (w - fm.stringWidth(num)) / 2, 16 + fm.getAscent() + 2);

                String pillTxt = isMyTurn ? "LƯỢT BẠN" : "LƯỢT ĐỊCH";
                g2.setFont(UITheme.body(8, Font.BOLD));
                fm = g2.getFontMetrics();
                int pw = fm.stringWidth(pillTxt) + 10, ph = 14;
                int px = (w - pw) / 2, py = h - ph - 8;
                Color pc = isMyTurn ? UITheme.PRIMARY : UITheme.DANGER_DARK;
                g2.setColor(new Color(pc.getRed(), pc.getGreen(), pc.getBlue(), 200));
                g2.fill(new RoundRectangle2D.Float(px, py, pw, ph, UITheme.RADIUS_PILL, UITheme.RADIUS_PILL));
                g2.setColor(Color.WHITE);
                g2.drawString(pillTxt, px + (pw - fm.stringWidth(pillTxt)) / 2, py + fm.getAscent() + 1);

                g2.dispose();
            }
        };
        lbl.setOpaque(false);
        return lbl;
    }

    private void showPauseMenu() {
        listener.onPauseResume(false);
        UIComponents.showPauseDialog(
            this, isLanMode, SoundManager.isMuted,
            () -> listener.onPauseResume(true),
            muted -> SoundManager.isMuted = muted,
            () -> listener.onSurrenderOrExit()
        );
    }

    public void startTurnTimer() {
        turnSecondsLeft = TURN_SECONDS;
        updateTurnTimerLabel();
        turnTimer.start();
    }
    
    public void resetTurnTimer() {
        turnSecondsLeft = TURN_SECONDS; 
        updateTurnTimerLabel();        
        turnTimer.restart();         
    }
    
    public void stopTurnTimer()   { turnTimer.stop(); }
    public void pauseTurnTimer()  { turnTimer.stop(); }
    public void resumeTurnTimer() { if (turnSecondsLeft > 0) turnTimer.start(); }

    private void updateTurnTimerLabel() {
        if (turnTimerLabel == null) return;
        int m = turnSecondsLeft / 60, s = turnSecondsLeft % 60;
        turnTimerLabel.setText(String.format("%d:%02d", m, s));
        turnTimerLabel.repaint();
    }

    public void setupGame(GameEngine engine, boolean isLanMode) {
        this.engine    = engine;
        this.isLanMode = isLanMode;
        turnCount = 0;

        leftBoardView.setBoard(engine.getBoard1());
        rightBoardView.setBoard(engine.getBoard2());
        leftBoardView.setOwnerMode(true);
        rightBoardView.setOwnerMode(false);
        leftBoardView.setGridColor(UITheme.PRIMARY);      
        rightBoardView.setGridColor(UITheme.DANGER);    

        enemyShipTracker.refresh(engine);
        layoutComponents();
    }

    public void setupGame(GameEngine engine, boolean isLanMode, boolean isGuest) {
        setupGame(engine, isLanMode);
    }

    public void setPlayerNames(String p1, String p2) {
        if (p1 != null && !p1.isEmpty()) { namePlayer1 = p1; avatarLeft.setDisplayName(p1); }
        if (p2 != null && !p2.isEmpty()) { namePlayer2 = p2; avatarRight.setDisplayName(p2); }
    }

    public void setAvatarSubtitles(String sub1, String sub2) {
        if (avatarLeft  != null) avatarLeft.setSubtitle(sub1);
        if (avatarRight != null) avatarRight.setSubtitle(sub2);
    }

    public void updateBoardsAndBanner(boolean isMyTurn, boolean isEvEMode) {
    	this.isMyTurn = isMyTurn;
        turnCount++;
        if (isMyTurn) {
            rightBoardView.setEnabled(true);
            avatarLeft.setActive(true);
            avatarRight.setActive(false);
        } else {
            rightBoardView.setEnabled(false);
            avatarLeft.setActive(false);
            avatarRight.setActive(true);
        }
        if (turnBadge != null) turnBadge.repaint();
        if (turnCounter != null) turnCounter.setTurn(isMyTurn, turnCount);
        enemyShipTracker.refresh(engine);
        leftBoardView.repaint();
        rightBoardView.repaint();
    }

    public void showTurnTransition(boolean isMyTurn, boolean isEvEMode) {
        updateBoardsAndBanner(isMyTurn, isEvEMode);
    }

    public void hideTurnTransitionAndShowBoard() { }

    public void repaintBoards() {
        leftBoardView.repaint();
        rightBoardView.repaint();
        if (engine != null) enemyShipTracker.refresh(engine);
    }

    public void revealAllForGameOver() {
        leftBoardView.setOwnerMode(true);
        rightBoardView.setOwnerMode(true);
        leftBoardView.repaint();
        rightBoardView.repaint();
    }

    private JPanel buildChatPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int r = UITheme.RADIUS_LG;
                for (int i = 4; i >= 1; i--) {
                    g2.setColor(new Color(0,0,0,18*i));
                    g2.fill(new java.awt.geom.RoundRectangle2D.Float(i,i+2,getWidth()-i,getHeight()-i,r,r));
                }
                g2.setColor(new Color(UITheme.BG_SURFACE.getRed(), UITheme.BG_SURFACE.getGreen(),
                        UITheme.BG_SURFACE.getBlue(), 230));
                g2.fill(new java.awt.geom.RoundRectangle2D.Float(0,0,getWidth(),getHeight(),r,r));
                g2.setColor(UITheme.BORDER);
                g2.setStroke(new java.awt.BasicStroke(1.2f));
                g2.draw(new java.awt.geom.RoundRectangle2D.Float(0.6f,0.6f,getWidth()-1.2f,getHeight()-1.2f,r,r));
                UITheme.paintTopAccent(g2, r, 0, getWidth()-r*2, r);
                g2.dispose();
            }
        };
        panel.setOpaque(false);

        JPanel header = UIComponents.transparentPanel(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(10, 14, 8, 10));
        JLabel hTitle = new JLabel("CHAT", SwingConstants.LEFT);
        hTitle.setFont(UITheme.display(13, Font.BOLD));
        hTitle.setForeground(UITheme.ACCENT_LIGHT);
        JButton closeBtn = UIComponents.iconButton("X", "Đóng chat");
        closeBtn.addActionListener(e -> toggleChat());
        header.add(hTitle,   BorderLayout.CENTER);
        header.add(closeBtn, BorderLayout.EAST);
        panel.add(header, BorderLayout.NORTH);

        chatMessages = new JPanel();
        chatMessages.setLayout(new BoxLayout(chatMessages, BoxLayout.Y_AXIS));
        chatMessages.setOpaque(false);
        chatMessages.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

        JScrollPane scroll = new JScrollPane(chatMessages) {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(new Color(0,0,0,0)); g.fillRect(0,0,getWidth(),getHeight());
            }
        };
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        panel.add(scroll, BorderLayout.CENTER);

        JPanel inputRow = UIComponents.transparentPanel(new BorderLayout(6, 0));
        inputRow.setBorder(BorderFactory.createEmptyBorder(6, 8, 10, 8));

        chatInput = new JTextField() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.BG_ELEVATED);
                g2.fill(new java.awt.geom.RoundRectangle2D.Float(0,0,getWidth(),getHeight(),
                        UITheme.RADIUS_MD, UITheme.RADIUS_MD));
                g2.setColor(isFocusOwner() ? UITheme.ACCENT : UITheme.BORDER);
                g2.setStroke(new java.awt.BasicStroke(isFocusOwner() ? 2f : 1f));
                g2.draw(new java.awt.geom.RoundRectangle2D.Float(0.6f,0.6f,getWidth()-1.2f,getHeight()-1.2f,
                        UITheme.RADIUS_MD, UITheme.RADIUS_MD));
                super.paintComponent(g);
                g2.dispose();
            }
        };
        chatInput.setFont(UITheme.body(12, Font.PLAIN));
        chatInput.setForeground(UITheme.TEXT_PRIMARY);
        chatInput.setCaretColor(UITheme.ACCENT);
        chatInput.setOpaque(false);
        chatInput.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        chatInput.addActionListener(e -> sendChatMessage());

        JButton sendBtn = UIComponents.primaryButton("Gửi");
        sendBtn.setPreferredSize(new Dimension(42, 32));
        sendBtn.addActionListener(e -> sendChatMessage());

        inputRow.add(chatInput, BorderLayout.CENTER);
        inputRow.add(sendBtn,   BorderLayout.EAST);
        panel.add(inputRow, BorderLayout.SOUTH);

        return panel;
    }

    private void sendChatMessage() {
        String text = chatInput.getText().trim();
        if (text.isEmpty()) return;
        chatInput.setText("");
        addChatMessage(myName, text, true);
        listener.onChatSend(text);
    }

    private void toggleChat() {
        chatVisible = !chatVisible;
        chatPanel.setVisible(chatVisible);
        if (chatVisible) {
            unreadCount = 0;
            updateChatBadge();
            chatInput.requestFocusInWindow();
        }
    }

    private void updateChatBadge() {
        if (chatButton == null) return;
        chatButton.setText(unreadCount > 0 ? "Chat  (" + unreadCount + ")" : "Chat");
    }

    public void addChatMessage(String sender, String text, boolean isMine) {
        JPanel bubble = UIComponents.transparentPanel(new FlowLayout(
            isMine ? FlowLayout.RIGHT : FlowLayout.LEFT, 0, 2));
        Color bgColor = isMine ? UITheme.PRIMARY : UITheme.BG_ELEVATED;

        JPanel msgCard = new JPanel(new BorderLayout(0, 2)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bgColor);
                g2.fill(new java.awt.geom.RoundRectangle2D.Float(0, 0, getWidth(), getHeight(),
                        UITheme.RADIUS_MD, UITheme.RADIUS_MD));
                g2.dispose();
            }
            @Override public Dimension getPreferredSize() {
                Dimension d = super.getPreferredSize();
                int maxW = chatMessages.getWidth() > 0 ? (int)(chatMessages.getWidth()*0.72) : 180;
                d.width = Math.min(d.width, maxW);
                return d;
            }
        };
        msgCard.setOpaque(false);
        msgCard.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        JLabel senderLbl = new JLabel(sender);
        senderLbl.setFont(UITheme.body(10, Font.BOLD));
        senderLbl.setForeground(isMine ? UITheme.ACCENT_LIGHT : UITheme.TEXT_SECONDARY);

        JTextArea textArea = new JTextArea(text);
        textArea.setFont(UITheme.body(12, Font.PLAIN));
        textArea.setForeground(UITheme.TEXT_PRIMARY);
        textArea.setOpaque(false);
        textArea.setEditable(false);
        textArea.setFocusable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setBorder(null);

        msgCard.add(senderLbl, BorderLayout.NORTH);
        msgCard.add(textArea,  BorderLayout.CENTER);
        bubble.add(msgCard);
        chatMessages.add(bubble);
        chatMessages.revalidate();
        chatMessages.repaint();

        javax.swing.SwingUtilities.invokeLater(() -> {
            JScrollPane sp = (JScrollPane) chatMessages.getParent().getParent();
            sp.getVerticalScrollBar().setValue(sp.getVerticalScrollBar().getMaximum());
        });

        if (!chatVisible && !isMine) {
            unreadCount++;
            updateChatBadge();
        }
    }

    public void setChatEnabled(boolean enabled, String myPlayerName, String opponentName) {
        myName  = myPlayerName  != null ? myPlayerName  : "Bạn";
        oppName = opponentName  != null ? opponentName  : "Đối Thủ";
        if (chatButton != null) chatButton.setVisible(enabled);
        unreadCount = 0;
        updateChatBadge();
        if (!enabled && chatVisible) {
            chatVisible = false;
            chatPanel.setVisible(false);
        }
        if (chatMessages != null) {
            chatMessages.removeAll();
            chatMessages.revalidate();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();

        java.awt.Image bg = ImageLoader.getGif("bggif");
        if (bg != null) {
            g2.drawImage(bg, 0, 0, getWidth(), getHeight(), this);
            g2.setColor(new Color(5, 15, 45, 160));
            g2.fillRect(0, 0, getWidth(), getHeight());
        } else {
            UITheme.paintBackground(g2, getWidth(), getHeight());
        }

        RadialGradientPaint vig = new RadialGradientPaint(
            new java.awt.Point(getWidth()/2, getHeight()/2),
            Math.max(getWidth(), getHeight()) * 0.65f,
            new float[]{ 0.4f, 1.0f },
            new Color[]{ new Color(0,0,0,0), new Color(0,0,0,120) }
        );
        g2.setPaint(vig);
        g2.fillRect(0, 0, getWidth(), getHeight());

        GradientPaint accent = new GradientPaint(0, 0, UITheme.ACCENT, getWidth(), 0, UITheme.PRIMARY);
        g2.setPaint(accent);
        g2.setStroke(new BasicStroke(2f));
        g2.drawLine(0, 0, getWidth(), 0);
        g2.drawLine(0, getHeight()-1, getWidth(), getHeight()-1);

        g2.dispose();
    }
}