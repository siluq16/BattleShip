package view;

import model.Board;
import model.CellState;
import model.Ship;
import utils.ImageLoader;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.awt.event.*;

public class BoardPanel extends JPanel {

    private Board board;
    private boolean isOwner;
    private boolean isSetupMode = false;
    private boolean useRealShipImages;
    private int cellSize = 45;
    private Color gridColor = UITheme.BORDER;

    private ClickListener clickListener;

    private Ship draggingShip = null;
    private int dragOffsetX, dragOffsetY;
    private int mouseX, mouseY;
    private boolean dragHorizontal;

    private int hoverX = -1, hoverY = -1;

    public interface ClickListener { void onCellClicked(int x, int y); }

    public BoardPanel(Board initialBoard, boolean isOwner, boolean useRealShipImages, ClickListener listener) {
        this.board = initialBoard;
        this.isOwner = isOwner;
        this.useRealShipImages = useRealShipImages;
        this.clickListener = listener;
        setOpaque(false);
        setupMouseListeners();
    }


    private int getPadLeft()   { return useRealShipImages ? 32 : 0; }
    private int getPadTop()    { return useRealShipImages ? 32 : 0; }
    private int getPadRight()  { return useRealShipImages ? 10 : 0; }
    private int getPadBottom() { return useRealShipImages ? 10 : 0; }


    public void setCellSize(int newSize) {
        this.cellSize = newSize;
        setPreferredSize(new Dimension(
            Board.SIZE * cellSize + getPadLeft() + getPadRight(),
            Board.SIZE * cellSize + getPadTop() + getPadBottom()
        ));
        revalidate();
        repaint();
    }

    public void setBoard(Board b)              { this.board = b;       repaint(); }
    public void setSetupMode(boolean v)        { this.isSetupMode = v;            }
    public void setOwnerMode(boolean v)        { this.isOwner = v;     repaint(); }
    public void setGridColor(Color c)          { this.gridColor = c;   repaint(); }


    private void setupMouseListeners() {
        MouseAdapter ma = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int gx = (e.getX() - getPadLeft()) / cellSize;
                int gy = (e.getY() - getPadTop()) / cellSize;

                if (!isSetupMode) {
                    if (SwingUtilities.isLeftMouseButton(e) && clickListener != null)
                        if (gx >= 0 && gx < Board.SIZE && gy >= 0 && gy < Board.SIZE)
                            clickListener.onCellClicked(gx, gy);
                    return;
                }

                Ship s = board.getShipAt(gx, gy);
                if (SwingUtilities.isRightMouseButton(e) && s != null) {
                    board.removeShip(s);
                    if (!board.placeShip(s.getX(), s.getY(), s.getLength(), !s.isHorizontal()))
                        board.placeShip(s.getX(), s.getY(), s.getLength(), s.isHorizontal());
                    repaint();
                    return;
                }
                if (SwingUtilities.isLeftMouseButton(e) && s != null) {
                    draggingShip  = s;
                    dragHorizontal = s.isHorizontal();
                    dragOffsetX   = e.getX() - (getPadLeft() + s.getX() * cellSize);
                    dragOffsetY   = e.getY() - (getPadTop()  + s.getY() * cellSize);
                    mouseX = e.getX(); mouseY = e.getY();
                    board.removeShip(s);
                    repaint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (isSetupMode && draggingShip != null && SwingUtilities.isLeftMouseButton(e)) {
                    int gx = Math.round((float)(e.getX() - getPadLeft() - dragOffsetX) / cellSize);
                    int gy = Math.round((float)(e.getY() - getPadTop()  - dragOffsetY) / cellSize);
                    if (!board.placeShip(gx, gy, draggingShip.getLength(), dragHorizontal))
                        board.placeShip(draggingShip.getX(), draggingShip.getY(), draggingShip.getLength(), dragHorizontal);
                    draggingShip = null;
                    repaint();
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                int gx = (e.getX() - getPadLeft()) / cellSize;
                int gy = (e.getY() - getPadTop())  / cellSize;
                if (gx >= 0 && gx < Board.SIZE && gy >= 0 && gy < Board.SIZE) {
                    if (hoverX != gx || hoverY != gy) { hoverX = gx; hoverY = gy; repaint(); }
                } else {
                    if (hoverX != -1) { hoverX = -1; hoverY = -1; repaint(); }
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hoverX = -1; hoverY = -1; repaint();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (isSetupMode && draggingShip != null) { mouseX = e.getX(); mouseY = e.getY(); repaint(); }
                int gx = (e.getX() - getPadLeft()) / cellSize;
                int gy = (e.getY() - getPadTop())  / cellSize;
                if (gx >= 0 && gx < Board.SIZE && gy >= 0 && gy < Board.SIZE) { hoverX = gx; hoverY = gy; }
            }
        };
        addMouseListener(ma);
        addMouseMotionListener(ma);
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int pl = getPadLeft(), pt = getPadTop();
        int boardPx = Board.SIZE * cellSize;

        if (useRealShipImages) {
            g2.setColor(new Color(13, 27, 62, 180));
            g2.fill(new RoundRectangle2D.Float(pl, pt, boardPx, boardPx, UITheme.RADIUS_SM, UITheme.RADIUS_SM));
        } else {
            g2.setColor(new Color(22, 42, 88, 200));
            g2.fillRect(0, 0, getWidth(), getHeight());
        }

        if (!isOwner && !isSetupMode && hoverX >= 0) {
            CellState st = board.getCellState(hoverX, hoverY);
            if (st == CellState.WATER) {
                g2.setColor(new Color(6, 182, 212, 50)); 
                g2.fillRect(pl + hoverX * cellSize, pt + hoverY * cellSize, cellSize, cellSize);
            }
        }

        for (Ship s : board.getShips()) {
            boolean visible = isOwner || isSetupMode || s.isSunk();
            if (!visible) continue;

            if (useRealShipImages) {
                BufferedImage img = ImageLoader.get("ship_" + s.getLength());
                if (img != null) drawRotatedImage(g2, img, s.getX(), s.getY(), s.getLength(), s.isHorizontal(), pl, pt);
                else             drawShipBlock(g2, s, pl, pt);
            } else {
                drawShipBlock(g2, s, pl, pt);
            }
        }

        g2.setColor(new Color(gridColor.getRed(), gridColor.getGreen(), gridColor.getBlue(), 60));
        g2.setStroke(new BasicStroke(0.8f));
        for (int i = 0; i <= Board.SIZE; i++) {
            int pos = i * cellSize;
            g2.drawLine(pl,          pt + pos, pl + boardPx, pt + pos);
            g2.drawLine(pl + pos,    pt,        pl + pos,    pt + boardPx);
        }

        g2.setColor(gridColor);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRect(pl, pt, boardPx, boardPx);

        if (useRealShipImages) {
            g2.setFont(UITheme.body(12, Font.BOLD));
            g2.setColor(UITheme.TEXT_SECONDARY);
            FontMetrics fm = g2.getFontMetrics();
            for (int i = 0; i < Board.SIZE; i++) {
                String num    = String.valueOf(i + 1);
                String letter = String.valueOf((char)('A' + i));
                int nw = fm.stringWidth(num);
                int lw = fm.stringWidth(letter);
                g2.drawString(num,    pl + i * cellSize + (cellSize - nw) / 2, pt - 10);
                g2.drawString(letter, pl - lw - 10, pt + i * cellSize + (cellSize + fm.getAscent()) / 2 - 2);
            }
        }

        for (int y = 0; y < Board.SIZE; y++) {
            for (int x = 0; x < Board.SIZE; x++) {
                CellState st = board.getCellState(x, y);
                if (st == CellState.WATER) continue;
                drawMarker(g2, st, pl + x * cellSize, pt + y * cellSize);
            }
        }

        if (isSetupMode && draggingShip != null) {
            BufferedImage img = ImageLoader.get("ship_" + draggingShip.getLength());
            int dx = mouseX - dragOffsetX, dy = mouseY - dragOffsetY;
            if (img != null) {
                Composite old = g2.getComposite();
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.65f));
                AffineTransform at = new AffineTransform();
                at.translate(dx, dy);
                if (!dragHorizontal) { at.translate(cellSize, 0); at.rotate(Math.PI / 2); }
                at.scale((double)(draggingShip.getLength() * cellSize) / img.getWidth(),
                         (double)(cellSize) / img.getHeight());
                g2.drawImage(img, at, null);
                g2.setComposite(old);
            } else {
                int w = dragHorizontal ? draggingShip.getLength() * cellSize : cellSize;
                int h = dragHorizontal ? cellSize : draggingShip.getLength() * cellSize;
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
                g2.setColor(UITheme.ACCENT);
                g2.fill(new RoundRectangle2D.Float(dx, dy, w, h, UITheme.RADIUS_SM, UITheme.RADIUS_SM));
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
            }
        }
    }

    private void drawShipBlock(Graphics2D g2, Ship s, int pl, int pt) {
        int x = pl + s.getX() * cellSize;
        int y = pt + s.getY() * cellSize;
        int w = s.isHorizontal() ? s.getLength() * cellSize : cellSize;
        int h = s.isHorizontal() ? cellSize : s.getLength() * cellSize;
        int r = UITheme.RADIUS_SM;

        Color fill = s.isSunk()
            ? new Color(UITheme.DANGER.getRed(), UITheme.DANGER.getGreen(), UITheme.DANGER.getBlue(), 140)
            : new Color(UITheme.PRIMARY.getRed(), UITheme.PRIMARY.getGreen(), UITheme.PRIMARY.getBlue(), 160);
        Color border = s.isSunk() ? UITheme.DANGER : UITheme.PRIMARY_LIGHT;

        g2.setColor(fill);
        g2.fill(new RoundRectangle2D.Float(x + 1, y + 1, w - 2, h - 2, r, r));
        g2.setColor(border);
        g2.setStroke(new BasicStroke(1.5f));
        g2.draw(new RoundRectangle2D.Float(x + 1, y + 1, w - 2, h - 2, r, r));
    }

    private void drawMarker(Graphics2D g2, CellState st, int px, int py) {
        int size = (int)(cellSize * 0.55);
        int off  = (cellSize - size) / 2;

        if (st == CellState.HIT || st == CellState.SUNK) {
            g2.setColor(new Color(239, 68, 68, 60));
            g2.fillOval(px + off - 3, py + off - 3, size + 6, size + 6);
            g2.setColor(UITheme.DANGER);
            g2.fillOval(px + off, py + off, size, size);
            g2.setColor(new Color(255, 150, 150, 160));
            g2.fillOval(px + off + 3, py + off + 3, size / 3, size / 3);

        } else if (st == CellState.MISSED) {
            g2.setColor(new Color(255, 255, 255, 30));
            g2.fillOval(px + off - 2, py + off - 2, size + 4, size + 4);
            g2.setColor(new Color(255, 255, 255, 200));
            g2.fillOval(px + off, py + off, size, size);
            g2.setColor(new Color(255, 255, 255, 120));
            g2.setStroke(new BasicStroke(1.2f));
            g2.drawOval(px + off, py + off, size, size);
            int sh = size / 4;
            g2.setColor(new Color(255, 255, 255, 180));
            g2.fillOval(px + off + 3, py + off + 3, sh, sh);
        }
    }

    private void drawRotatedImage(Graphics2D g2, BufferedImage img,
                                   int gx, int gy, int len, boolean horiz, int pl, int pt) {
        int x = pl + gx * cellSize, y = pt + gy * cellSize;
        AffineTransform at = new AffineTransform();
        at.translate(x, y);
        if (!horiz) { at.translate(cellSize, 0); at.rotate(Math.PI / 2); }
        at.scale((double)(len * cellSize) / img.getWidth(), (double)(cellSize) / img.getHeight());
        g2.drawImage(img, at, null);
    }
}