package assignment11.opticalflow;

import java.awt.Color;

import ij.process.ColorProcessor;
import ij.process.FloatProcessor;


public class Util {
	
	/**
	 * Creates a hue-encoded color image from the 2D vector field (vx, vy).
	 * @param vx
	 * @param vy
	 * @return
	 */
	public static ColorProcessor makeHueImage(float[][] vx, float[][] vy) {
		final double PI2 = 2 * Math.PI;
		int width = vx.length;
		int height = vx[0].length;
		ColorProcessor hi = new ColorProcessor(width, height);
		
		for (int v = 0; v < height; v++) {
			for (int u = 0; u < width; u++) {
				float x = vx[u][v];
				float y = vy[u][v];				
				float h = (float) (Math.atan2(y, x) / PI2); // 		h is in [-0.5, +0.5]
				float s = 1;
				float b = (float) Math.min(1, Math.sqrt(x * x + y * y));
				Color c = Color.getHSBColor(h, s, b);
				hi.putPixel(u, v, c.getRGB());
			}
		}
		return hi;
	}
	
	
	public static FloatProcessor makeGrayImage(float[][] vx, float[][] vy) {
		int width = vx.length;
		int height = vx[0].length;
		FloatProcessor hi = new FloatProcessor(width, height);
		
		for (int v = 0; v < height; v++) {
			for (int u = 0; u < width; u++) {
				float x = vx[u][v];
				float y = vy[u][v];				
				float b = (float) Math.sqrt(x * x + y * y);
				hi.setf(u, v, b);
			}
		}
		return hi;
	}

}
