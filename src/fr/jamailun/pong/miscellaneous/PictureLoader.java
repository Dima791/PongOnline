package fr.jamailun.pong.miscellaneous;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public final class PictureLoader {

	public static BufferedImage getImage(String file) {
		try {
			return ImageIO.read(new File(file));
		} catch (IOException e) {
			System.err.println("[PCTR][ERROR] Picture \""+file+"\" seems impossible to load:");
			e.printStackTrace();
			return null;
		}
	}
	
}
