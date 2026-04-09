package view;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import javax.swing.*;
import javax.swing.border.Border;

public final class UITheme {

    private UITheme() {}

    public static final Color BG_PRIMARY      = new Color(13, 27, 62);     
    public static final Color BG_SURFACE      = new Color(22, 42, 88);    
    public static final Color BG_ELEVATED     = new Color(30, 58, 118);  

    public static final Color PRIMARY         = new Color(37, 99, 235);  
    public static final Color PRIMARY_LIGHT   = new Color(96, 165, 250); 
    public static final Color PRIMARY_DARK    = new Color(29, 78, 216);  

    public static final Color ACCENT          = new Color(6, 182, 212);  
    public static final Color ACCENT_LIGHT    = new Color(103, 232, 249);

    public static final Color SUCCESS         = new Color(34, 197, 94);  
    public static final Color SUCCESS_DARK    = new Color(22, 163, 74);
    public static final Color DANGER          = new Color(239, 68, 68);  
    public static final Color DANGER_DARK     = new Color(220, 38, 38);
    public static final Color WARNING         = new Color(234, 179, 8);  

    public static final Color TEXT_PRIMARY    = new Color(241, 245, 249);
    public static final Color TEXT_SECONDARY  = new Color(148, 163, 184);
    public static final Color TEXT_MUTED      = new Color(71, 85, 105);  
    public static final Color TEXT_ON_PRIMARY = Color.WHITE;

    public static final Color BORDER          = new Color(51, 78, 140);
    public static final Color BORDER_LIGHT    = new Color(71, 105, 180);



    public static final String FONT_DISPLAY = "Bahnschrift"; 
    public static final String FONT_BODY    = "Segoe UI";
    public static final String FONT_MONO    = "Consolas";

    public static Font display(int size, int style) {
        return resolveFont(FONT_DISPLAY, style, size);
    }
    public static Font body(int size, int style) {
        return resolveFont(FONT_BODY, style, size);
    }
    public static Font mono(int size, int style) {
        return resolveFont(FONT_MONO, style, size);
    }

    private static Font resolveFont(String preferred, int style, int size) {
        String[] fallbacks = {"Segoe UI", "Calibri", "Tahoma", "SansSerif"};
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        java.util.Set<String> available = new java.util.HashSet<>(
                java.util.Arrays.asList(ge.getAvailableFontFamilyNames()));
        if (available.contains(preferred)) return new Font(preferred, style, size);
        for (String f : fallbacks) if (available.contains(f)) return new Font(f, style, size);
        return new Font("SansSerif", style, size);
    }



    public static final int  RADIUS_SM   = 6;
    public static final int  RADIUS_MD   = 12;
    public static final int  RADIUS_LG   = 18;
    public static final int  RADIUS_PILL = 50;

    public static final int  ELEVATION_1 = 2;
    public static final int  ELEVATION_2 = 4;
    public static final int  ELEVATION_3 = 8;



    public static void paintBackground(Graphics2D g2, int w, int h) {
        GradientPaint gp = new GradientPaint(0, 0, BG_PRIMARY, w * 0.4f, h, new Color(9, 18, 50));
        g2.setPaint(gp);
        g2.fillRect(0, 0, w, h);

        g2.setColor(new Color(255, 255, 255, 8));
        for (int x = 0; x < w; x += 28)
            for (int y = 0; y < h; y += 28)
                g2.fillOval(x - 1, y - 1, 2, 2);
    }

    public static void paintCard(Graphics2D g2, int x, int y, int w, int h, int radius) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // Shadow
        g2.setColor(new Color(0, 0, 0, 60));
        g2.fill(new RoundRectangle2D.Float(x + 3, y + 4, w, h, radius, radius));
        // Surface
        g2.setColor(BG_SURFACE);
        g2.fill(new RoundRectangle2D.Float(x, y, w, h, radius, radius));
        // Border
        g2.setColor(BORDER);
        g2.setStroke(new BasicStroke(1.2f));
        g2.draw(new RoundRectangle2D.Float(x + 0.6f, y + 0.6f, w - 1.2f, h - 1.2f, radius, radius));
    }

    public static void paintTopAccent(Graphics2D g2, int x, int y, int w, int radius) {
        GradientPaint gp = new GradientPaint(x, y, ACCENT, x + w, y, PRIMARY);
        g2.setPaint(gp);
        g2.setStroke(new BasicStroke(3f));
        g2.drawLine(x + radius, y, x + w - radius, y);
    }

    public static void paintBadge(Graphics2D g2, String text, Color bg, int cx, int cy) {
        g2.setFont(body(11, Font.BOLD));
        FontMetrics fm = g2.getFontMetrics();
        int tw = fm.stringWidth(text);
        int pw = tw + 16, ph = 20;
        int rx = cx - pw / 2, ry = cy - ph / 2;
        g2.setColor(bg);
        g2.fill(new RoundRectangle2D.Float(rx, ry, pw, ph, RADIUS_PILL, RADIUS_PILL));
        g2.setColor(TEXT_ON_PRIMARY);
        g2.drawString(text, cx - tw / 2, cy + fm.getAscent() / 2 - 1);
    }

    public static Border cardBorder() {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1, true),
            BorderFactory.createEmptyBorder(12, 16, 12, 16)
        );
    }

    public static void paintDivider(Graphics2D g2, int x, int y, int w) {
        GradientPaint gp = new GradientPaint(x, y, new Color(0,0,0,0), x + w/2, y, BORDER_LIGHT,
                                              false);
        float[] fractions = {0f, 0.3f, 0.7f, 1f};
        Color[] colors = {new Color(0,0,0,0), BORDER_LIGHT, BORDER_LIGHT, new Color(0,0,0,0)};
        g2.setPaint(new LinearGradientPaint(x, y, x+w, y, fractions, colors));
        g2.setStroke(new BasicStroke(1f));
        g2.drawLine(x, y, x + w, y);
    }
}