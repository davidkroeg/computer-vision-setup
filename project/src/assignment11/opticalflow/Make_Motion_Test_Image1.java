package assignment11.opticalflow;

import java.awt.Color;

import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.PlugIn;
import ij.plugin.filter.GaussianBlur;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

/**
 * This ImageJ plugin creates a synthetic motion sequence 
 * showing a set of radially expanding, blurred disks.
 * The result is displayed as an image stack.
 * 
 * @author WB
 *
 */
public class Make_Motion_Test_Image1 implements PlugIn {
	
	static int K = 16;
	static double initDist = 120.5;
	static double deltaDist = 2.33333;
	static double radius = 30;
	static int width = 600;
	static int height = 400;
	
	static Color circleColor = Color.gray.darker();
	static Color backgroundColor = Color.white;
	static double sigmaGauss = 3.0;

	@Override
	public void run(String arg0) {
		ImageStack stack = new ImageStack(width, height);
		double xc = 0.5 * width;
		double yc = 0.5 * height;
		GaussianBlur gb = new GaussianBlur();
		
		for (int k = 0; k < K; k++) {
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
					ip.drawDot(u, v);
				}
				
			}
		}
	}
	
	private double sqr(double x) {
		return x * x;
	}

}
