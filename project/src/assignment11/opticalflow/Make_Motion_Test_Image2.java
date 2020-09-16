package assignment11.opticalflow;

import java.awt.Color;

import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.PlugIn;
import ij.plugin.filter.GaussianBlur;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

/**
 * Creates a radially expanding dot pattern, where dots are filled with
 * a cosine texture. The result is displayed as an image stack.
 * 
 * @author WB
 *
 */
public class Make_Motion_Test_Image2 implements PlugIn {
	
	static int numberOfFrames = 16;
	static double initDist = 120.5;
	static double deltaDist = 2.33333;
	static double radius = 30;
	static int numberOfCosines = 2;
	static double sigmaGauss = 2.0;
	
	static int width = 600;
	static int height = 400;
	
	static Color circleColor = Color.blue;
	static Color backgroundColor = Color.white;


	@Override
	public void run(String arg0) {
		ImageStack stack = new ImageStack(width, height);
		double xc = 0.5 * width;
		double yc = 0.5 * height;
		GaussianBlur gb = new GaussianBlur();
		
		for (int k = 0; k < numberOfFrames; k++) {
			ImageProcessor ip = new ByteProcessor(width, height);
			ip.setColor(backgroundColor);
			ip.fill();
			
			ip.setColor(circleColor);
			double d = initDist + deltaDist * k;
			for (int i = 0; i < 8; i++) {
				double a = 2 * Math.PI * i / 8;
				double x = xc + d * Math.cos(a);
				double y = yc + d * Math.sin(a);
				drawCircle(ip, x, y, radius);
			}
			gb.blurGaussian(ip, sigmaGauss);
			stack.addSlice(ip);
		}
		new ImagePlus("Stack", stack).show();
	}
	
	// brute-force circle drawing (with floating-point parameters)
	private void drawCircle(ImageProcessor ip, double x, double y, double r) {
		double r2 = sqr(r);
		for (int v = 0; v < ip.getHeight(); v++) {
			for (int u = 0; u < ip.getWidth(); u++) {
				double d2 = sqr(u - x) + sqr(v - y);
				if (d2 < r2) {
					double d = Math.sqrt(d2);
					double val = 0.5 * (1 - Math.cos(2 * Math.PI * d * numberOfCosines / r));
					ip.putPixel(u, v, (int) Math.round((255 * val)));
				}
				
			}
		}
	}
	
	private double sqr(double x) {
		return x * x;
	}

}
