package view;

import javax.swing.*;

import utils.ImageLoader;

import java.awt.*;

public class MainMenuPanel extends JPanel {

    public interface MainMenuListener {
        void onPlayPvE();
        void onHostLAN();
        void onJoinLAN();
        void onPlayEvE();
    }

    private final MainMenuListener listener;

    public MainMenuPanel(MainMenuListener listener) {
        this.listener = listener;
        setOpaque(false);
        setLayout(new BorderLayout());
        buildUI();
    }

    private void buildUI() {
        JPanel left = new JPanel(new GridBagLayout());
        left.setOpaque(false);
        left.setBorder(BorderFactory.createEmptyBorder(40, 48, 40, 40));
        left.setPreferredSize(new Dimension(320, 0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.anchor = GridBagConstraints.WEST;


        gbc.gridy = 1; gbc.insets = new Insets(0, 30, 8, 0);
        left.add(makeGradientLabel("BATTLESHIP", 30, UITheme.ACCENT_LIGHT, UITheme.PRIMARY_LIGHT), gbc);

        gbc.gridy = 2; gbc.insets = new Insets(0, 30, 20, 0);
        JLabel sub = new JLabel("NAVAL WARFARE  •  1941");
        sub.setFont(UITheme.body(12, Font.PLAIN));
        sub.setForeground(UITheme.TEXT_SECONDARY);
        left.add(sub, gbc);

        String[] labels = {
            "CHƠI VỚI MÁY",
            "TẠO PHÒNG LAN",
            "VÀO PHÒNG LAN",
            "MÁY VS MÁY",
        };
        Runnable[] actions = {
            listener::onPlayPvE,
            listener::onHostLAN,
            listener::onJoinLAN,
            listener::onPlayEvE,
        };
        for (int i = 0; i < labels.length; i++) {
            gbc.gridy = 3 + i;
            gbc.insets = new Insets(5, 0, 5, 0);
            JButton btn = UIComponents.primaryButton(labels[i]);
            final Runnable act = actions[i];
            btn.addActionListener(e -> act.run());
            left.add(btn, gbc);
        }


        add(left, BorderLayout.WEST);
    }


    private JLabel makeGradientLabel(String text, int size, Color from, Color to) {
        JLabel lbl = new JLabel(text, SwingConstants.LEFT) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                GradientPaint gp = new GradientPaint(0, 0, from, fm.stringWidth(text), 0, to);
                g2.setPaint(gp);
                g2.drawString(text, 0, fm.getAscent());
                g2.dispose();
            }
        };
        lbl.setFont(UITheme.display(size, Font.BOLD));
        return lbl;
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        java.awt.Image bg = ImageLoader.getGif("bggif");
        if (bg != null) {
            g2.drawImage(bg, 0, 0, getWidth(), getHeight(), this);
        } else {
            g2.setColor(new Color(8, 18, 55));
            g2.fillRect(0, 0, getWidth(), getHeight());
        }
        g2.dispose();
    }
}