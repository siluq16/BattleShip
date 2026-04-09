package view;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.Point;

public final class UIComponents {

    private UIComponents() {}

    public static JButton primaryButton(String text) {
        return buildButton(text, UITheme.PRIMARY, UITheme.PRIMARY_DARK, UITheme.TEXT_ON_PRIMARY, UITheme.RADIUS_PILL);
    }

    public static JButton successButton(String text) {
        return buildButton(text, UITheme.SUCCESS, UITheme.SUCCESS_DARK, UITheme.TEXT_ON_PRIMARY, UITheme.RADIUS_PILL);
    }

    public static JButton dangerButton(String text) {
        return buildButton(text, UITheme.DANGER, UITheme.DANGER_DARK, UITheme.TEXT_ON_PRIMARY, UITheme.RADIUS_PILL);
    }

    public static JButton outlineButton(String text) {
        JButton btn = new JButton(text) {
            private boolean hovered = false;
            {
                addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) { hovered = true;  repaint(); }
                    public void mouseExited (MouseEvent e) { hovered = false; repaint(); }
                });
            }
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                int r = UITheme.RADIUS_PILL;
                if (hovered) {
                    g2.setColor(new Color(37, 99, 235, 40));
                    g2.fill(new RoundRectangle2D.Float(0, 0, w, h, r, r));
                }
                g2.setColor(hovered ? UITheme.PRIMARY_LIGHT : UITheme.PRIMARY);
                g2.setStroke(new BasicStroke(1.8f));
                g2.draw(new RoundRectangle2D.Float(1, 1, w - 2, h - 2, r, r));
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int tx = (w - fm.stringWidth(getText())) / 2;
                int ty = (h + fm.getAscent() - fm.getDescent()) / 2;
                g2.setColor(hovered ? UITheme.PRIMARY_LIGHT : UITheme.TEXT_PRIMARY);
                g2.drawString(getText(), tx, ty);
                g2.dispose();
            }
        };
        styleButtonBase(btn);
        btn.setForeground(UITheme.TEXT_PRIMARY);
        return btn;
    }

    public static JButton iconButton(String icon, String tooltip) {
        JButton btn = new JButton(icon) {
            private boolean hovered = false;
            {
                addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) { hovered = true;  repaint(); }
                    public void mouseExited (MouseEvent e) { hovered = false; repaint(); }
                });
            }
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                if (hovered) {
                    g2.setColor(new Color(255, 255, 255, 25));
                    g2.fill(new RoundRectangle2D.Float(0, 0, w, h, UITheme.RADIUS_MD, UITheme.RADIUS_MD));
                }
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                g2.setColor(UITheme.TEXT_SECONDARY);
                g2.drawString(getText(), (w - fm.stringWidth(getText())) / 2,
                        (h + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        btn.setFont(UITheme.body(16, Font.PLAIN));
        btn.setPreferredSize(new Dimension(36, 36));
        btn.setToolTipText(tooltip);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public static JLabel heroLabel(String text) {
        JLabel lbl = new JLabel(text, SwingConstants.CENTER);
        lbl.setFont(UITheme.display(38, Font.BOLD));
        lbl.setForeground(UITheme.TEXT_PRIMARY);
        return lbl;
    }

    public static JLabel titleLabel(String text) {
        JLabel lbl = new JLabel(text, SwingConstants.CENTER);
        lbl.setFont(UITheme.display(26, Font.BOLD));
        lbl.setForeground(UITheme.TEXT_PRIMARY);
        return lbl;
    }

    public static JLabel subtitleLabel(String text) {
        JLabel lbl = new JLabel(text, SwingConstants.CENTER);
        lbl.setFont(UITheme.body(13, Font.PLAIN));
        lbl.setForeground(UITheme.TEXT_SECONDARY);
        return lbl;
    }

    public static JLabel captionLabel(String text) {
        JLabel lbl = new JLabel(text, SwingConstants.CENTER);
        lbl.setFont(UITheme.body(11, Font.PLAIN));
        lbl.setForeground(UITheme.TEXT_MUTED);
        return lbl;
    }

    public static JLabel badgeLabel(String text, Color bg) {
        JLabel lbl = new JLabel(text, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), UITheme.RADIUS_PILL, UITheme.RADIUS_PILL));
                super.paintComponent(g2);
                g2.dispose();
            }
        };
        lbl.setFont(UITheme.mono(12, Font.BOLD));
        lbl.setForeground(Color.WHITE);
        lbl.setOpaque(false);
        lbl.setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 10));
        return lbl;
    }

    public static JPanel transparentPanel(LayoutManager layout) {
        JPanel p = new JPanel(layout);
        p.setOpaque(false);
        return p;
    }

    public static JPanel cardPanel(int radius) {
        JPanel p = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                UITheme.paintCard(g2, 0, 0, getWidth(), getHeight(), radius);
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        return p;
    }

    public static JPanel divider(int width) {
        JPanel d = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                UITheme.paintDivider((Graphics2D) g, 0, getHeight() / 2, getWidth());
            }
        };
        d.setOpaque(false);
        d.setPreferredSize(new Dimension(width, 16));
        return d;
    }

    public static JDialog lanWaitDialog(JFrame parent, String code, Runnable onCancel) {
        JDialog dialog = new JDialog(parent, true);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0, 0, 0, 0));
        dialog.setSize(400, 320);
        dialog.setLocationRelativeTo(parent);

        JPanel root = buildFloatingPanel();
        root.setLayout(new GridBagLayout());
        root.setBorder(BorderFactory.createEmptyBorder(28, 36, 28, 36));

        makeDraggable(dialog, root);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1;

        gbc.gridy = 0; gbc.insets = new Insets(0, 0, 14, 0);
        root.add(buildAccentTitle("TẠO PHÒNG MẠNG LAN"), gbc);

        gbc.gridy = 1; gbc.insets = new Insets(0, 0, 6, 0);
        root.add(subtitleLabel("Đang chờ đối thủ kết nối..."), gbc);

        gbc.gridy = 2; gbc.insets = new Insets(4, 0, 15, 0);
        JLabel codeLabel = new JLabel(code, SwingConstants.CENTER);
        codeLabel.setFont(UITheme.display(62, Font.BOLD));
        codeLabel.setForeground(UITheme.ACCENT);
        root.add(codeLabel, gbc);

        gbc.gridy = 4; gbc.insets = new Insets(0, 0, 16, 0);
        root.add(divider(320), gbc);

        gbc.gridy = 5; gbc.insets = new Insets(0, 40, 0, 40);
        JButton btnCancel = dangerButton("Hủy Tạo Phòng");
        btnCancel.addActionListener(e -> { onCancel.run(); dialog.dispose(); });
        root.add(btnCancel, gbc);

        dialog.setContentPane(root);
        return dialog;
    }

    public static String[] joinLanDialog(JFrame parent) {
        String[] result = { null, null }; 

        JDialog dialog = new JDialog(parent, true);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0, 0, 0, 0));
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(parent);

        JPanel root = buildFloatingPanel();
        root.setLayout(new GridBagLayout());
        root.setBorder(BorderFactory.createEmptyBorder(28, 36, 28, 36));

        makeDraggable(dialog, root);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1;

        gbc.gridy = 0; gbc.insets = new Insets(0, 0, 18, 0);
        root.add(buildAccentTitle("THAM GIA TRẬN CHIẾN"), gbc);

        gbc.gridy = 1; gbc.insets = new Insets(0, 0, 8, 0);
        root.add(subtitleLabel("Nhập mã phòng từ người tạo:"), gbc);

        gbc.gridy = 2; gbc.insets = new Insets(0, 20, 10, 20);
        JTextField codeField = buildCodeInput();
        root.add(codeField, gbc);

        gbc.gridy = 4; gbc.insets = new Insets(0, 0, 6, 0);
        root.add(subtitleLabel("Tên của bạn:"), gbc);

        gbc.gridy = 5; gbc.insets = new Insets(0, 20, 0, 20);
        JTextField nameField = buildNameInput("Nhập tên...");
        root.add(nameField, gbc);

        gbc.gridy = 6; gbc.insets = new Insets(10, 0, 14, 0);
        root.add(divider(320), gbc);

        gbc.gridy = 7; gbc.insets = new Insets(0, 0, 0, 0);
        JPanel btnRow = transparentPanel(new GridLayout(1, 2, 12, 0));

        JButton btnCancel = outlineButton("Hủy");
        btnCancel.addActionListener(e -> dialog.dispose());

        JButton btnJoin = successButton("Tham Gia");
        Runnable doJoin = () -> {
            String code = codeField.getText().trim();
            String name = nameField.getText().trim();
            if (name.isEmpty()) name = "Người Chơi";
            if (!code.isEmpty()) {
                result[0] = code;
                result[1] = name;
                dialog.dispose();
            } else {
                codeField.setBackground(new Color(239, 68, 68, 40));
                codeField.requestFocus();
            }
        };
        btnJoin.addActionListener(e -> doJoin.run());

        codeField.addActionListener(e -> doJoin.run());
        nameField.addActionListener(e -> doJoin.run());

        btnRow.add(btnCancel);
        btnRow.add(btnJoin);
        root.add(btnRow, gbc);

        dialog.setContentPane(root);
        return result[0] != null ? result : null;
    }


    public static String askPlayerNameDialog(JFrame parent, String prompt) {
        String[] result = { null };

        JDialog dialog = new JDialog(parent, true);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0, 0, 0, 0));
        dialog.setSize(380, 230);
        dialog.setLocationRelativeTo(parent);

        JPanel root = buildFloatingPanel();
        root.setLayout(new GridBagLayout());
        root.setBorder(BorderFactory.createEmptyBorder(28, 32, 28, 32));
        makeDraggable(dialog, root);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1;

        gbc.gridy = 0; gbc.insets = new Insets(0, 0, 16, 0);
        root.add(buildAccentTitle("NHẬP TÊN NGƯỜI CHƠI"), gbc);

        gbc.gridy = 1; gbc.insets = new Insets(0, 0, 6, 0);
        root.add(subtitleLabel(prompt), gbc);

        gbc.gridy = 2; gbc.insets = new Insets(0, 0, 16, 0);
        JTextField nameField = buildNameInput("VD: Nguyễn Văn A...");
        root.add(nameField, gbc);

        gbc.gridy = 3; gbc.insets = new Insets(0, 0, 0, 0);
        JPanel btnRow = transparentPanel(new GridLayout(1, 2, 12, 0));

        JButton btnCancel = outlineButton("Hủy");
        btnCancel.addActionListener(e -> dialog.dispose());

        JButton btnOK = primaryButton("Xác Nhận");
        Runnable confirm = () -> {
            result[0] = nameField.getText().trim().isEmpty() ? "Người Chơi" : nameField.getText().trim();
            dialog.dispose();
        };
        btnOK.addActionListener(e -> confirm.run());
        nameField.addActionListener(e -> confirm.run());

        btnRow.add(btnCancel);
        btnRow.add(btnOK);
        root.add(btnRow, gbc);

        dialog.setContentPane(root);
        SwingUtilities.invokeLater(() -> nameField.requestFocusInWindow());
        dialog.setVisible(true);
        return result[0];
    }

    public static void showPauseDialog(java.awt.Component owner, boolean isLanMode,
                                       boolean isMuted,
                                       Runnable onResume,
                                       java.util.function.Consumer<Boolean> onMuteToggle,
                                       Runnable onExit) {
        Frame frame = (Frame) SwingUtilities.getWindowAncestor(owner);
        JDialog dlg = new JDialog(frame, true);
        dlg.setUndecorated(true);
        dlg.setBackground(new Color(0, 0, 0, 0));
        dlg.setSize(360, 300);
        dlg.setLocationRelativeTo(owner);
        dlg.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        JPanel root = buildFloatingPanel();
        root.setLayout(new GridBagLayout());
        root.setBorder(BorderFactory.createEmptyBorder(26, 28, 26, 28));
        makeDraggable(dlg, root);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1;

        gbc.gridy = 0; gbc.insets = new Insets(0, 0, 14, 0);
        root.add(buildAccentTitle(isLanMode ? "CÀI ĐẶT" : "TẠM DỪNG"), gbc);

        gbc.gridy = 1; gbc.insets = new Insets(0, 0, 12, 0);
        root.add(divider(300), gbc);

        gbc.gridy = 2; gbc.insets = new Insets(4, 0, 5, 0);
        JButton btnResume = primaryButton(isLanMode ? "Đóng (Quay Lại Game)" : "Tiếp Tục Chiến Đấu");
        btnResume.addActionListener(e -> { dlg.dispose(); onResume.run(); });
        root.add(btnResume, gbc);

        gbc.gridy = 3; gbc.insets = new Insets(0, 0, 5, 0);
        boolean[] mutedState = { isMuted };
        JButton btnSound = outlineButton(isMuted ? "Bật Tiếng" : "Tắt Tiếng");
        btnSound.addActionListener(e -> {
            mutedState[0] = !mutedState[0];
            btnSound.setText(mutedState[0] ? "Bật Tiếng" : "Tắt Tiếng");
            onMuteToggle.accept(mutedState[0]);
        });
        root.add(btnSound, gbc);

        gbc.gridy = 4; gbc.insets = new Insets(6, 0, 0, 0);
        JButton btnExit = dangerButton(isLanMode ? "Đầu Hàng & Rời Phòng" : "Thoát Về Menu");
        btnExit.addActionListener(e -> { dlg.dispose(); onExit.run(); });
        root.add(btnExit, gbc);

        dlg.setContentPane(root);
        dlg.setVisible(true);
    }

    
    public static void showDialog(JFrame parent, String title, String message,
                                  Color accent, Object[]... buttons) {
        JDialog dlg = new JDialog(parent, true);
        dlg.setUndecorated(true);
        dlg.setBackground(new Color(0, 0, 0, 0));
        dlg.setSize(420, 80 + 60 + buttons.length * 52);
        dlg.setLocationRelativeTo(parent);

        JPanel root = buildFloatingPanelWithAccent(accent);
        root.setLayout(new GridBagLayout());
        root.setBorder(BorderFactory.createEmptyBorder(24, 32, 24, 32));
        makeDraggable(dlg, root);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1;

        gbc.gridy = 0; gbc.insets = new Insets(0, 0, 10, 0);
        JLabel titleLbl = new JLabel(title, SwingConstants.CENTER);
        titleLbl.setFont(UITheme.display(18, Font.BOLD));
        titleLbl.setForeground(accent);
        root.add(titleLbl, gbc);

        gbc.gridy = 1; gbc.insets = new Insets(0, 0, 16, 0);
        JLabel msgLbl = new JLabel(
            "<html><div style='text-align:center;'>" + message.replace("\n", "<br>") + "</div></html>",
            SwingConstants.CENTER);
        msgLbl.setFont(UITheme.body(13, Font.PLAIN));
        msgLbl.setForeground(UITheme.TEXT_SECONDARY);
        root.add(msgLbl, gbc);

        gbc.gridy = 2; gbc.insets = new Insets(0, 0, 12, 0);
        root.add(divider(340), gbc);

        for (int i = 0; i < buttons.length; i++) {
            String  label  = (String)   buttons[i][0];
            Runnable action = (Runnable) buttons[i][1];
            gbc.gridy = 3 + i; gbc.insets = new Insets(0, 20, 8, 20);
            JButton btn = primaryButton(label);
            btn.addActionListener(e -> { dlg.dispose(); action.run(); });
            root.add(btn, gbc);
        }

        dlg.setContentPane(root);
        dlg.setVisible(true);
    }

    public static void showGameOverDialog(JFrame parent, String bannerText, String winner,
                                          Color accent, int[] stats,
                                          Runnable onReplay, Runnable onMenu, Runnable onExit) {
        int height = stats != null ? 400 : 320;
        JDialog dlg = new JDialog(parent, true);
        dlg.setUndecorated(true);
        dlg.setBackground(new Color(0, 0, 0, 0));
        dlg.setSize(460, height);
        dlg.setLocationRelativeTo(parent);

        JPanel root = buildFloatingPanelWithAccent(accent);
        root.setLayout(new GridBagLayout());
        root.setBorder(BorderFactory.createEmptyBorder(24, 32, 24, 32));
        makeDraggable(dlg, root);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1;

        gbc.gridy = 0; gbc.insets = new Insets(0, 0, 4, 0);
        JLabel iconLbl = new JLabel(bannerText, SwingConstants.CENTER);
        iconLbl.setFont(UITheme.display(22, Font.BOLD));
        iconLbl.setForeground(accent);
        root.add(iconLbl, gbc);

        gbc.gridy = 1; gbc.insets = new Insets(0, 0, 12, 0);
        JLabel sub = new JLabel(winner + " là người chiến thắng trận này!", SwingConstants.CENTER);
        sub.setFont(UITheme.body(13, Font.PLAIN));
        sub.setForeground(UITheme.TEXT_SECONDARY);
        root.add(sub, gbc);

        if (stats != null) {
            gbc.gridy = 2; gbc.insets = new Insets(0, 0, 10, 0);
            root.add(buildStatsPanel(stats), gbc);
        }

        gbc.gridy = 3; gbc.insets = new Insets(0, 0, 12, 0);
        root.add(divider(380), gbc);

        gbc.gridy = 4; gbc.insets = new Insets(0, 0, 6, 0);
        JButton btnReplay = primaryButton("Chơi Lại");
        btnReplay.addActionListener(e -> { dlg.dispose(); onReplay.run(); });
        root.add(btnReplay, gbc);

        gbc.gridy = 5; gbc.insets = new Insets(0, 0, 6, 0);
        JButton btnMenu = outlineButton("Về Menu");
        btnMenu.addActionListener(e -> { dlg.dispose(); onMenu.run(); });
        root.add(btnMenu, gbc);

        gbc.gridy = 6; gbc.insets = new Insets(0, 0, 0, 0);
        JButton btnExit = dangerButton("Thoát Game");
        btnExit.addActionListener(e -> onExit.run());
        root.add(btnExit, gbc);

        dlg.setContentPane(root);
        dlg.setVisible(true);
    }

    private static JPanel buildStatsPanel(int[] s) {
        // s = [p1Shots, p1Hits, p2Shots, p2Hits]
        JPanel panel = new JPanel(new GridLayout(1, 2, 12, 0));
        panel.setOpaque(false);
        panel.add(buildStatCard("BẠN / P1", s[0], s[1], UITheme.PRIMARY));
        panel.add(buildStatCard("ĐỊCH / P2", s[2], s[3], UITheme.DANGER));
        return panel;
    }

    private static JPanel buildStatCard(String label, int shots, int hits, Color accent) {
        JPanel card = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 25));
                g2.fill(new java.awt.geom.RoundRectangle2D.Float(0,0,getWidth(),getHeight(),
                        UITheme.RADIUS_MD, UITheme.RADIUS_MD));
                g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 80));
                g2.setStroke(new BasicStroke(1f));
                g2.draw(new java.awt.geom.RoundRectangle2D.Float(0.5f,0.5f,getWidth()-1,getHeight()-1,
                        UITheme.RADIUS_MD, UITheme.RADIUS_MD));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1;

        gbc.gridy = 0; gbc.insets = new Insets(0, 0, 6, 0);
        JLabel lbl = new JLabel(label, SwingConstants.CENTER);
        lbl.setFont(UITheme.body(11, Font.BOLD));
        lbl.setForeground(accent);
        card.add(lbl, gbc);

        double acc = shots == 0 ? 0.0 : (double) hits / shots * 100;

        String[][] rows = {
            { "Số lượt bắn", String.valueOf(shots) },
            { "Bắn trúng",   String.valueOf(hits)  },
            { "Độ chính xác", String.format("%.1f%%", acc) },
        };
        for (int i = 0; i < rows.length; i++) {
            gbc.gridy = i + 1; gbc.insets = new Insets(2, 0, 2, 0);
            JPanel row = transparentPanel(new BorderLayout());
            JLabel k = new JLabel(rows[i][0]);
            k.setFont(UITheme.body(11, Font.PLAIN));
            k.setForeground(UITheme.TEXT_SECONDARY);
            JLabel v = new JLabel(rows[i][1], SwingConstants.RIGHT);
            v.setFont(UITheme.mono(12, Font.BOLD));
            v.setForeground(UITheme.TEXT_PRIMARY);
            row.add(k, BorderLayout.WEST);
            row.add(v, BorderLayout.EAST);
            card.add(row, gbc);
        }

        gbc.gridy = rows.length + 1; gbc.insets = new Insets(6, 0, 0, 0);
        JPanel bar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.BG_ELEVATED);
                g2.fill(new java.awt.geom.RoundRectangle2D.Float(0,0,getWidth(),getHeight(),4,4));
                int filled = (int)(getWidth() * acc / 100.0);
                if (filled > 0) {
                    g2.setColor(accent);
                    g2.fill(new java.awt.geom.RoundRectangle2D.Float(0,0,filled,getHeight(),4,4));
                }
                g2.dispose();
            }
        };
        bar.setOpaque(false);
        bar.setPreferredSize(new Dimension(0, 8));
        card.add(bar, gbc);

        return card;
    }

    // ══════════════════════════════════════════
    //  PRIVATE DIALOG HELPERS
    // ══════════════════════════════════════════

    private static JPanel buildFloatingPanel() {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int r = UITheme.RADIUS_LG;
                for (int i = 4; i >= 1; i--) {
                    g2.setColor(new Color(0, 0, 0, 18 * i));
                    g2.fill(new RoundRectangle2D.Float(i, i + 2, getWidth() - i, getHeight() - i, r, r));
                }
                // Body
                g2.setColor(UITheme.BG_SURFACE);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), r, r));
                // Border
                g2.setColor(UITheme.BORDER);
                g2.setStroke(new BasicStroke(1.2f));
                g2.draw(new RoundRectangle2D.Float(0.6f, 0.6f, getWidth()-1.2f, getHeight()-1.2f, r, r));
                // Top accent gradient
                UITheme.paintTopAccent(g2, r, 0, getWidth() - r * 2, r);
                g2.dispose();
            }
        };
    }

    private static JPanel buildAccentTitle(String text) {
        JPanel p = transparentPanel(new BorderLayout(10, 0));
        // Accent dot
        JPanel dot = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, UITheme.ACCENT, 0, getHeight(), UITheme.PRIMARY);
                g2.setPaint(gp);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 3, 3));
                g2.dispose();
            }
        };
        dot.setOpaque(false);
        dot.setPreferredSize(new Dimension(4, 28));
        JLabel lbl = new JLabel(text, SwingConstants.LEFT);
        lbl.setFont(UITheme.display(16, Font.BOLD));
        lbl.setForeground(UITheme.TEXT_PRIMARY);
        p.add(dot, BorderLayout.WEST);
        p.add(lbl, BorderLayout.CENTER);
        return p;
    }

    private static JTextField buildCodeInput() {
        JTextField tf = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Nền
                g2.setColor(UITheme.BG_ELEVATED);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), UITheme.RADIUS_MD, UITheme.RADIUS_MD));
                // Viền dưới accent (Material style)
                g2.setColor(isFocusOwner() ? UITheme.ACCENT : UITheme.BORDER);
                g2.setStroke(new BasicStroke(isFocusOwner() ? 2f : 1.2f));
                g2.drawLine(4, getHeight()-2, getWidth()-4, getHeight()-2);
                super.paintComponent(g);
                g2.dispose();
            }
        };
        tf.setFont(UITheme.display(28, Font.BOLD));
        tf.setForeground(UITheme.ACCENT);
        tf.setCaretColor(UITheme.ACCENT);
        tf.setHorizontalAlignment(SwingConstants.CENTER);
        tf.setOpaque(false);
        tf.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        tf.setPreferredSize(new Dimension(0, 58));
        tf.addKeyListener(new KeyAdapter() {
            @Override public void keyTyped(KeyEvent e) {
                if (tf.getText().length() >= 4 || !Character.isDigit(e.getKeyChar())) e.consume();
            }
        });
        return tf;
    }

    private static JTextField buildNameInput(String placeholder) {
        JTextField tf = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.BG_ELEVATED);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), UITheme.RADIUS_MD, UITheme.RADIUS_MD));
                g2.setColor(isFocusOwner() ? UITheme.ACCENT : UITheme.BORDER);
                g2.setStroke(new BasicStroke(isFocusOwner() ? 2f : 1.2f));
                g2.drawLine(4, getHeight()-2, getWidth()-4, getHeight()-2);
                // Placeholder text
                if (getText().isEmpty() && !isFocusOwner()) {
                    g2.setFont(UITheme.body(13, Font.PLAIN));
                    g2.setColor(UITheme.TEXT_MUTED);
                    g2.drawString(placeholder, 14, getHeight()/2 + g2.getFontMetrics().getAscent()/2);
                }
                super.paintComponent(g);
                g2.dispose();
            }
        };
        tf.setFont(UITheme.body(14, Font.PLAIN));
        tf.setForeground(UITheme.TEXT_PRIMARY);
        tf.setCaretColor(UITheme.ACCENT);
        tf.setOpaque(false);
        tf.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        tf.setPreferredSize(new Dimension(0, 42));
        tf.addKeyListener(new KeyAdapter() {
            @Override public void keyTyped(KeyEvent e) {
                if (tf.getText().length() >= 20) e.consume();
            }
        });
        return tf;
    }

    private static JPanel buildFloatingPanelWithAccent(Color accent) {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int r = UITheme.RADIUS_LG;
                for (int i = 4; i >= 1; i--) {
                    g2.setColor(new Color(0, 0, 0, 18 * i));
                    g2.fill(new RoundRectangle2D.Float(i, i + 2, getWidth() - i, getHeight() - i, r, r));
                }
                g2.setColor(UITheme.BG_SURFACE);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), r, r));
                g2.setColor(UITheme.BORDER);
                g2.setStroke(new BasicStroke(1.2f));
                g2.draw(new RoundRectangle2D.Float(0.6f, 0.6f, getWidth() - 1.2f, getHeight() - 1.2f, r, r));
                GradientPaint gp = new GradientPaint(r, 0, accent, getWidth() - r, 0, accent.darker());
                g2.setPaint(gp);
                g2.setStroke(new BasicStroke(3f));
                g2.drawLine(r, 0, getWidth() - r, 0);
                g2.dispose();
            }
        };
    }


    private static void makeDraggable(JDialog dialog, JPanel panel) {
        int[] dragStart = new int[2];
        panel.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                dragStart[0] = e.getX();
                dragStart[1] = e.getY();
            }
        });
        panel.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                Point loc = dialog.getLocation();
                dialog.setLocation(loc.x + e.getX() - dragStart[0],
                                   loc.y + e.getY() - dragStart[1]);
            }
        });
    }


    // ══════════════════════════════════════════
    //  PRIVATE HELPERS
    // ══════════════════════════════════════════

    private static JButton buildButton(String text, Color base, Color hover, Color fg, int radius) {
        JButton btn = new JButton(text) {
            private boolean hovered = false;
            private boolean pressed = false;
            {
                addMouseListener(new MouseAdapter() {
                    public void mouseEntered (MouseEvent e){ hovered = true;          repaint(); }
                    public void mouseExited  (MouseEvent e){ hovered = false; pressed = false; repaint(); }
                    public void mousePressed (MouseEvent e){ pressed = true;           repaint(); }
                    public void mouseReleased(MouseEvent e){ pressed = false;          repaint(); }
                });
            }
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();

                if (!pressed) {
                    g2.setColor(new Color(0, 0, 0, 40));
                    g2.fill(new RoundRectangle2D.Float(2, 3, w - 2, h, radius, radius));
                }
                Color face = pressed ? hover.darker() : (hovered ? hover : base);
                g2.setColor(face);
                g2.fill(new RoundRectangle2D.Float(0, pressed ? 2 : 0, w, h - (pressed ? 2 : 0), radius, radius));

                if (!pressed) {
                    g2.setColor(new Color(255, 255, 255, 30));
                    g2.fill(new RoundRectangle2D.Float(2, 1, w - 4, h / 2, radius, radius));
                }

                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int tx = (w - fm.stringWidth(getText())) / 2;
                int ty = (h + fm.getAscent() - fm.getDescent()) / 2 + (pressed ? 1 : 0);
                g2.setColor(new Color(0, 0, 0, 60));
                g2.drawString(getText(), tx + 1, ty + 1);
                g2.setColor(fg);
                g2.drawString(getText(), tx, ty);
                g2.dispose();
            }
        };
        styleButtonBase(btn);
        return btn;
    }

    private static void styleButtonBase(JButton btn) {
        btn.setFont(UITheme.body(14, Font.BOLD));
        btn.setPreferredSize(new Dimension(200, 44));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setOpaque(false);
    }
}