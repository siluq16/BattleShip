package utils;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.File;

public class SoundManager {
    public static boolean isMuted = false;

    public static void playSound(String fileName) {
        if (isMuted) return;

        new Thread(() -> {
            try {
                File soundFile = new File("assets/sounds/" + fileName + ".wav");
                if (!soundFile.exists()) {
                    System.out.println("Không tìm thấy file âm thanh: " + soundFile.getAbsolutePath());
                    return;
                }

                AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);
                Clip clip = AudioSystem.getClip();
                clip.open(audioIn);
                clip.start();
                
                clip.addLineListener(event -> {
                    if (event.getType() == javax.sound.sampled.LineEvent.Type.STOP) {
                        clip.close();
                    }
                });
            } catch (Exception e) {
                System.out.println("Lỗi phát âm thanh: " + e.getMessage());
            }
        }).start(); 
    }
}