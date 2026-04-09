package view;

import model.Board;
import utils.ImageLoader;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

public class SetupPanel extends JPanel {

    public interface SetupListener {
        void onStart();
        void onExit();
    }

    private BoardPanel  boardPanel;
    private Board       userBoard;
    private SetupListener listener;
    private boolean     isLanMode = false;

    private ai.AIDifficulty selectedDifficulty = ai.AIDifficulty.HARD;
    private JPanel difficultyPanel; 

    private static final int TOTAL_SECONDS = 60;
    private int     secondsLeft = TOTAL_SECONDS;
    private Timer   countdownTimer;
    private JLabel  timerLabel;

    private JPanel     waitingOverlay;
    private JLabel     waitingLabel;
    private JPanel     centerStack;
    private static final String CARD_BOARD   = "BOARD";
    private static final String CARD_WAITING = "WAITING";


    public SetupPanel(SetupListener listener) {
        this.listener = listener;
        setOpaque(false);
        setLayout(new BorderLayout(0, 0));

        userBoard = new Board();
        userBoard.placeShipsRandomly();

        buildUI();
        setupCountdown();
    }

    public SetupPanel(Runnable onStart) {
        this(new SetupListener() {
            public void onStart() { onStart.run(); }
            public void onExit()  { }
        });
    }


    private void buildUI() {
        add(buildHeader(), BorderLayout.NORTH);

        centerStack = new JPanel(new CardLayout());
        centerStack.setOpaque(false);
        centerStack.add(buildCenter(), CARD_BOARD);
        waitingOverlay = buildWaitingOverlay();
        centerStack.add(waitingOverlay, CARD_WAITING);
        add(centerStack, BorderLayout.CENTER);

        add(buildActionBar(), BorderLayout.SOUTH);
    }


    private JPanel buildHeader() {
        JPanel bar = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(0, 0, 0, 60));
                g2.fillRect(0, 0, getWidth(), getHeight());
                GradientPaint gp = new GradientPaint(0, getHeight()-2, UITheme.ACCENT,
                                                     getWidth(), getHeight()-2, UITheme.PRIMARY);
                g2.setPaint(gp);
                g2.setStroke(new BasicStroke(2f));
                g2.drawLine(0, getHeight()-2, getWidth(), getHeight()-2);
                g2.dispose();
            }
        };
        bar.setOpaque(false);
        bar.setBorder(BorderFactory.createEmptyBorder(12, 24, 12, 24));

        JPanel left = UIComponents.transparentPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        JLabel title = new JLabel("BỐ TRÍ HẠM ĐỘI");
        title.setFont(UITheme.display(20, Font.BOLD));
        title.setForeground(UITheme.TEXT_PRIMARY);
        left.add(title);

        JPanel right = UIComponents.transparentPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));

        timerLabel = new JLabel("1:00") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = secondsLeft <= 10 ? UITheme.DANGER : UITheme.BG_ELEVATED;
                g2.setColor(bg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(),
                        UITheme.RADIUS_PILL, UITheme.RADIUS_PILL));
                g2.setColor(secondsLeft <= 10 ? UITheme.DANGER_DARK : UITheme.BORDER);
                g2.setStroke(new BasicStroke(1.2f));
                g2.draw(new RoundRectangle2D.Float(0.6f, 0.6f, getWidth()-1.2f, getHeight()-1.2f,
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
        timerLabel.setFont(UITheme.mono(14, Font.BOLD));
        timerLabel.setOpaque(false);
        timerLabel.setPreferredSize(new Dimension(64, 30));

        JLabel badge = UIComponents.badgeLabel("SETUP PHASE", UITheme.PRIMARY);

        right.add(timerLabel);
        right.add(badge);

        bar.add(left, BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }


    private JPanel buildCenter() {
        JPanel center = UIComponents.transparentPanel(new GridBagLayout());
        center.setBorder(BorderFactory.createEmptyBorder(20, 24, 12, 24));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0; gbc.fill = GridBagConstraints.BOTH; gbc.weighty = 1;

        // Board card
        gbc.gridx = 0; gbc.weightx = 0; gbc.insets = new Insets(0, 0, 0, 20);
        center.add(buildBoardCard(), gbc);

        // Info panel
        gbc.gridx = 1; gbc.weightx = 1; gbc.insets = new Insets(0, 0, 0, 0);
        center.add(buildInfoPanel(), gbc);

        return center;
    }

    private JPanel buildBoardCard() {
        JPanel card = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                UITheme.paintCard(g2, 0, 0, getWidth(), getHeight(), UITheme.RADIUS_LG);
                UITheme.paintTopAccent(g2, UITheme.RADIUS_LG, 0,
                        getWidth() - UITheme.RADIUS_LG * 2, UITheme.RADIUS_LG);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        boardPanel = new BoardPanel(userBoard, true, true, null);
        boardPanel.setSetupMode(true);
        boardPanel.setCellSize(40);
        boardPanel.setGridColor(UITheme.ACCENT);
        card.add(boardPanel, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildInfoPanel() {
        JPanel info = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                UITheme.paintCard(g2, 0, 0, getWidth(), getHeight(), UITheme.RADIUS_LG);
                g2.dispose();
            }
        };
        info.setOpaque(false);
        info.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1;
        gbc.anchor = GridBagConstraints.NORTHWEST;

        gbc.gridy = 0; gbc.insets = new Insets(0, 0, 14, 0);
        JLabel infoTitle = new JLabel("HƯỚNG DẪN");
        infoTitle.setFont(UITheme.display(14, Font.BOLD));
        infoTitle.setForeground(UITheme.ACCENT);
        info.add(infoTitle, gbc);

        String[][] guides = {
            { "•",  "Kéo & thả tàu",          "Di chuyển tàu đến vị trí mong muốn" },
            { "•",  "Click phải để xoay",      "Đổi hướng tàu ngang ↔ dọc"         },
            { "•",  "Ngẫu Nhiên",              "Xếp tàu tự động ngẫu nhiên"         },
            { "•",  "Đếm ngược 60 giây",       "Hết giờ sẽ tự động bắt đầu"        },
        };
        for (int i = 0; i < guides.length; i++) {
            gbc.gridy = i + 1;
            gbc.insets = new Insets(0, 0, 10, 0);
            info.add(buildGuideRow(guides[i][0], guides[i][1], guides[i][2]), gbc);
        }

        gbc.gridy = guides.length + 1; gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        info.add(Box.createVerticalGlue(), gbc);

        gbc.gridy = guides.length + 2; gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 0, 0);
        info.add(buildFleetSummary(), gbc);

        return info;
    }

    private JPanel buildGuideRow(String icon, String title, String desc) {
        JPanel row = UIComponents.transparentPanel(new BorderLayout(10, 0));
        row.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));

        JLabel icLbl = new JLabel(icon);
        icLbl.setFont(UITheme.body(18, Font.PLAIN));
        icLbl.setPreferredSize(new Dimension(28, 28));

        JPanel text = UIComponents.transparentPanel(new GridLayout(2, 1, 0, 1));
        JLabel t = new JLabel(title);
        t.setFont(UITheme.body(12, Font.BOLD));
        t.setForeground(UITheme.TEXT_PRIMARY);
        JLabel d = new JLabel(desc);
        d.setFont(UITheme.body(11, Font.PLAIN));
        d.setForeground(UITheme.TEXT_SECONDARY);
        text.add(t); text.add(d);

        row.add(icLbl, BorderLayout.WEST);
        row.add(text,  BorderLayout.CENTER);
        return row;
    }

    private JPanel buildFleetSummary() {
        JPanel fleet = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.BG_ELEVATED);
                g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),
                        UITheme.RADIUS_MD, UITheme.RADIUS_MD));
                g2.dispose();
            }
        };
        fleet.setOpaque(false);
        fleet.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1;

        gbc.gridy = 0; gbc.insets = new Insets(0, 0, 8, 0);
        JLabel fTitle = new JLabel("HẠM ĐỘI CỦA BẠN");
        fTitle.setFont(UITheme.body(11, Font.BOLD));
        fTitle.setForeground(UITheme.TEXT_SECONDARY);
        fleet.add(fTitle, gbc);

        model.ShipConfig.ShipEntry[] entries = model.ShipConfig.getEntries();
        for (int i = 0; i < entries.length; i++) {
            gbc.gridy = i + 1; gbc.insets = new Insets(2, 0, 2, 0);
            fleet.add(buildShipRow(entries[i].length, entries[i].count, entries[i].name), gbc);
        }
        return fleet;
    }

    private JPanel buildShipRow(int len, int count, String name) {
        JPanel row = UIComponents.transparentPanel(new BorderLayout(8, 0));
        JLabel nameLbl = new JLabel(name + (count > 1 ? " x"+count : ""));
        nameLbl.setFont(UITheme.body(11, Font.PLAIN));
        nameLbl.setForeground(UITheme.TEXT_SECONDARY);
        // Hull blocks
        JPanel blocks = UIComponents.transparentPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        for (int i = 0; i < len; i++) {
            JPanel b = new JPanel() {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setColor(UITheme.PRIMARY);
                    g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),2,2));
                    g2.dispose();
                }
            };
            b.setOpaque(false);
            b.setPreferredSize(new Dimension(12, 8));
            blocks.add(b);
        }
        row.add(nameLbl, BorderLayout.CENTER);
        row.add(blocks,  BorderLayout.EAST);
        return row;
    }


    private JPanel buildActionBar() {
        JPanel bar = new JPanel(new BorderLayout(12, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(0,0,0,60));
                g2.fillRect(0,0,getWidth(),getHeight());
                GradientPaint gp = new GradientPaint(0,0,UITheme.PRIMARY,getWidth(),0,UITheme.ACCENT);
                g2.setPaint(gp);
                g2.setStroke(new BasicStroke(2f));
                g2.drawLine(0,0,getWidth(),0);
                g2.dispose();
            }
        };
        bar.setOpaque(false);
        bar.setBorder(BorderFactory.createEmptyBorder(14, 24, 16, 24));

        JButton btnExit = UIComponents.dangerButton("Thoát");
        btnExit.setPreferredSize(new Dimension(120, 44));
        btnExit.addActionListener(e -> {
            stopCountdown();
            if (listener != null) listener.onExit();
        });

        JPanel diffPanel = UIComponents.transparentPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        JLabel diffLabel = new JLabel("Độ khó:");
        diffLabel.setFont(UITheme.body(13, Font.BOLD));
        diffLabel.setForeground(UITheme.TEXT_SECONDARY);

        JButton diffBtn = UIComponents.outlineButton(selectedDifficulty.label + "  ▲");
        diffBtn.setPreferredSize(new Dimension(240, 44));

        JPopupMenu popup = new JPopupMenu() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.BG_SURFACE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), UITheme.RADIUS_MD, UITheme.RADIUS_MD);
                g2.setColor(UITheme.BORDER);
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, UITheme.RADIUS_MD, UITheme.RADIUS_MD);
                g2.dispose();
            }
        };
        popup.setOpaque(false);
        popup.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        popup.setBackground(UITheme.BG_SURFACE);

        for (ai.AIDifficulty diff : ai.AIDifficulty.values()) {
            JMenuItem item = new JMenuItem(diff.label) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    if (getModel().isArmed() || diff == selectedDifficulty) {
                        g2.setColor(UITheme.PRIMARY);
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), UITheme.RADIUS_SM, UITheme.RADIUS_SM);
                    }
                    g2.setFont(getFont());
                    FontMetrics fm = g2.getFontMetrics();
                    g2.setColor(UITheme.TEXT_PRIMARY);
                    g2.drawString(getText(), 12, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                    g2.dispose();
                }
            };
            item.setFont(UITheme.body(13, Font.PLAIN));
            item.setForeground(UITheme.TEXT_PRIMARY);
            item.setOpaque(false);
            item.setBackground(new Color(0,0,0,0));
            item.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
            item.setPreferredSize(new Dimension(230, 36));
            item.addActionListener(e -> {
                selectedDifficulty = diff;
                diffBtn.setText(diff.label + "  ▲");
                diffBtn.repaint();
            });
            popup.add(item);
        }

        diffBtn.addActionListener(e -> {
            popup.show(diffBtn, 0, -popup.getPreferredSize().height - 4);
        });

        diffPanel.add(diffLabel);
        diffPanel.add(diffBtn);
        difficultyPanel = diffPanel; 
        
        JPanel rightBtns = UIComponents.transparentPanel(new GridLayout(1, 2, 12, 0));

        JButton btnRandom = UIComponents.outlineButton("Ngẫu Nhiên");
        btnRandom.addActionListener(e -> {
            userBoard.placeShipsRandomly();
            boardPanel.repaint();
        });

        JButton btnStart = UIComponents.successButton("VÀO TRẬN");
        btnStart.addActionListener(e -> handleStart());

        rightBtns.add(btnRandom);
        rightBtns.add(btnStart);

        bar.add(btnExit,    BorderLayout.WEST);
        bar.add(diffPanel,  BorderLayout.CENTER);
        bar.add(rightBtns,  BorderLayout.EAST);
        return bar;
    }

    private JPanel buildWaitingOverlay() {
        JPanel overlay = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(5, 15, 45, 230));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        overlay.setOpaque(false);

        JPanel card = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                UITheme.paintCard(g2, 0, 0, getWidth(), getHeight(), UITheme.RADIUS_LG);
                UITheme.paintTopAccent(g2, UITheme.RADIUS_LG, 0,
                        getWidth() - UITheme.RADIUS_LG * 2, UITheme.RADIUS_LG);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(40, 48, 40, 48));
        card.setPreferredSize(new Dimension(460, 290));

        GridBagConstraints cgbc = new GridBagConstraints();
        cgbc.gridx = 0; cgbc.fill = GridBagConstraints.HORIZONTAL; cgbc.weightx = 1;
        cgbc.insets = new Insets(8, 0, 8, 0);

        cgbc.gridy = 1;
        JLabel title = new JLabel("ĐANG CHỜ ĐỐI THỦ", SwingConstants.CENTER);
        title.setFont(UITheme.display(22, Font.BOLD));
        title.setForeground(UITheme.TEXT_PRIMARY);
        card.add(title, cgbc);

        cgbc.gridy = 2;
        waitingLabel = new JLabel("Bản đồ đã gửi — chờ đối thủ xếp xong...", SwingConstants.CENTER);
        waitingLabel.setFont(UITheme.body(13, Font.PLAIN));
        waitingLabel.setForeground(UITheme.TEXT_SECONDARY);
        card.add(waitingLabel, cgbc);

        cgbc.gridy = 3; cgbc.insets = new Insets(4, 0, 4, 0);
        card.add(UIComponents.divider(360), cgbc);

        cgbc.gridy = 4; cgbc.insets = new Insets(8, 20, 0, 20);
        JButton btnCancel = UIComponents.dangerButton("Hủy & Thoát Phòng");
        btnCancel.addActionListener(e -> {
            hideWaitingOverlay();
            if (listener != null) listener.onExit();
        });
        card.add(btnCancel, cgbc);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        overlay.add(card, gbc);
        return overlay;
    }

    private void setupCountdown() {
        countdownTimer = new Timer(1000, e -> {
            secondsLeft--;
            updateTimerLabel();
            if (secondsLeft <= 0) {
                stopCountdown();
                userBoard.placeShipsRandomly();
                boardPanel.repaint();
                handleStart();
            }
        });
    }

    private void updateTimerLabel() {
        int m = secondsLeft / 60, s = secondsLeft % 60;
        timerLabel.setText(String.format("%d:%02d", m, s));
        timerLabel.repaint();
    }

    private void stopCountdown() {
        if (countdownTimer != null) countdownTimer.stop();
    }

    private void handleStart() {
        stopCountdown();
        if (listener != null) listener.onStart();
    }


    public void showWaitingOverlay() {
        ((CardLayout) centerStack.getLayout()).show(centerStack, CARD_WAITING);
    }

    public void hideWaitingOverlay() {
        ((CardLayout) centerStack.getLayout()).show(centerStack, CARD_BOARD);
    }

    public void setWaitingText(String text) {
        if (waitingLabel != null) waitingLabel.setText(text);
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
        g2.dispose();
    }


    public Board getBoard() { return userBoard; }

    public ai.AIDifficulty getSelectedDifficulty() { return selectedDifficulty; }

    public void reset() {
        secondsLeft = TOTAL_SECONDS;
        updateTimerLabel();
        userBoard = new Board();
        userBoard.placeShipsRandomly();
        boardPanel.setBoard(userBoard);
        hideWaitingOverlay();
    }

    public void startCountdown() {
        secondsLeft = TOTAL_SECONDS;
        updateTimerLabel();
        countdownTimer.start();
    }

    public void setLanMode(boolean lan) {
        this.isLanMode = lan;
        if (difficultyPanel != null) difficultyPanel.setVisible(!lan);
    }
}