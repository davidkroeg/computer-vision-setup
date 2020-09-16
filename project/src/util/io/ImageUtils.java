package util.io;

import ij.process.ColorProcessor;

public class ImageUtils {

	public static ColorProcessor setWhite(ColorProcessor ip) {
		int w = ip.getWidth();
		int h = ip.getHeight();
		
		for(int i_h = 0; i_h < h; i_h++) {
			for(int i_w = 0; i_w < w; i_w++) {
				ip.putPixel(i_w, i_h, new int[] {255,255,255});
			}
		}
		
		return ip;
	}
	
}
