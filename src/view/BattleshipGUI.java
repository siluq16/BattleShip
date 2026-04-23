package view;

import controller.GameController;
import utils.ImageLoader;

import javax.swing.*;
import java.awt.*;

public class BattleshipGUI extends JFrame {

    private final JPanel      mainContainer;
    private final CardLayout  cardLayout;
    private final GameController controller;

    public BattleshipGUI() {
        setTitle("Battleship — Chiến Hạm Đại Dương");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1000, 750);
        setLocationRelativeTo(null);

        getContentPane().setBackground(UITheme.BG_PRIMARY);

        cardLayout    = new CardLayout();
        mainContainer = new JPanel(cardLayout);
        mainContainer.setOpaque(false);

        JPanel root = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                UITheme.paintBackground((Graphics2D) g, getWidth(), getHeight());
            }
        };
        root.setOpaque(true);
        root.add(mainContainer, BorderLayout.CENTER);
        setContentPane(root);

        MainMenuPanel  menuPanel     = new MainMenuPanel(null);
        SetupPanel     setupPanel    = new SetupPanel((SetupPanel.SetupListener) null);
        GamePlayPanel  gamePlayPanel = new GamePlayPanel(null);

        mainContainer.add(menuPanel,     "MENU");
        mainContainer.add(setupPanel,    "SETUP");
        mainContainer.add(gamePlayPanel, "GAME");

        controller = new GameController(
                this, mainContainer, cardLayout,
                menuPanel, setupPanel, gamePlayPanel);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosing(java.awt.event.WindowEvent e) {
                controller.handleAppExit();
            }
        });

        setVisible(true);
    }
}