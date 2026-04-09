package utils;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon; // Thêm thư viện này
import java.awt.Image; // Thêm thư viện này
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ImageLoader {
    private static Map<String, BufferedImage> images = new HashMap<>();
    private static Map<String, BufferedImage[]> animations = new HashMap<>();
    
    // 1. Thêm một Map riêng chuyên chứa ảnh động (dùng java.awt.Image)
    private static Map<String, Image> gifs = new HashMap<>();

    public static void loadImages() {
        try {
            images.put("bg", loadImage("assets/background.jpg")); 

            BufferedImage waterSheet = loadImage("assets/water.png");
            if (waterSheet != null) {
                int h = waterSheet.getHeight();
                int w = h;
                int cols = waterSheet.getWidth() / w;
                
                BufferedImage[] frames = new BufferedImage[cols];
                for (int i = 0; i < cols; i++) {
                    frames[i] = waterSheet.getSubimage(i * w, 0, w, h);
                }
                animations.put("water", frames);
            }
            
            // 2. Load file GIF bằng hàm chuyên dụng mới
            gifs.put("bggif", loadGif("assets/bggif.gif"));
            
            images.put("hit", loadImage("assets/hit.png"));
            images.put("miss", loadImage("assets/miss.png"));
            images.put("ship_2", loadImage("assets/ship_2.png"));
            images.put("ship_3", loadImage("assets/ship_3.png"));
            images.put("ship_4", loadImage("assets/ship_4.png"));
            images.put("ship_5", loadImage("assets/ship_5.png"));
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static BufferedImage loadImage(String path) {
        try {
            return ImageIO.read(new File(path));
        } catch (IOException e) {
            return null;
        }
    }

    // 3. Hàm mới để load GIF bằng ImageIcon (giữ được animation)
    private static Image loadGif(String path) {
        File file = new File(path);
        if (file.exists()) {
            return new ImageIcon(path).getImage();
        } else {
            System.err.println("Không tìm thấy file GIF tại: " + path);
            return null;
        }
    }

    public static BufferedImage get(String key) { return images.get(key); }
    public static BufferedImage[] getAnimation(String key) { return animations.get(key); }
    
    // 4. Hàm getter lấy GIF
    public static Image getGif(String key) { return gifs.get(key); }
}