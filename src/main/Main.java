package main;

import view.BattleshipGUI;

import javax.swing.SwingUtilities;

import utils.ImageLoader;

public class Main {
    public static void main(String[] args) {
    	ImageLoader.loadImages();
        SwingUtilities.invokeLater(() -> {
            new BattleshipGUI(); 
        });
    }
}