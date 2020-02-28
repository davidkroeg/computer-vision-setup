package assignment01;

import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

import java.util.Random;


/**
 * This ImageJ plugin creates a binary image containing random points on a specified
 * circle plus additional random (noise) points uniformly distributed in the image.
 * Marked points have values I(u,v) = 255, background pixels have zero values.
 * The display is inverted, i.e., background pixels are shown white.
 * 
 * @author W. Burger
 * @version 2020/02/28
 */
public class Make_Circle_TestImage implements PlugIn {
	
	private static String imageTitle = "circle-test";
	
	private static int W = 400;					// width of the new image
	private static int H = 300;					// height of the new image
	
	private static double xc = 200;				// x-center of the circle
	private static double yc = 150;				// y-center of the circle
	private static double rad = 100;			// radius of the circle
	
	private static double xySigma = 0.5;		// standard deviation of Gaussian position noise
	private static int onCirclePts = 150;		// number of points ON the circle
	private static int offCirclePts = 100;		// number of additional random points 
	
	private static boolean showInverted = false;	// show inverted

	// --------------------------------------------------------------
	
	public void run(String arg0) {
		if (!getUserInput()) {
			return;
		}
		
		// create a new grayscale image:
		ImageProcessor ip = new ByteProcessor(W, H);
		
		if (showInverted)
			ip.invertLut();				// display black dots on white background
		
		Random rg = new Random();
		
		// create random points on/near the circle:
		for (int i = 0; i < onCirclePts; i++) {
			double phi = rg.nextDouble() * 2 * Math.PI;	// uniformly distributed angle 0,...,2pi
			double dx = xySigma * rg.nextGaussian();	// position disturbed by Gaussian noise
			double dy = xySigma * rg.nextGaussian();
			int u = (int) Math.round(xc + rad * Math.cos(phi) + dx);
			int v = (int) Math.round(yc + rad * Math.sin(phi) + dy);
			ip.putPixel(u, v, 255);
		}
		
		// create additional random points uniformly distributed in the image:
		for (int i = 0; i < offCirclePts; i++) {
			int u = rg.nextInt(W);
			int v = rg.nextInt(H);
			ip.putPixel(u, v, 255);
		}
		
		// display the new image:
		(new ImagePlus(imageTitle, ip)).show();
		
	}
	
	private boolean getUserInput() {
		GenericDialog gd = new GenericDialog("Create Circle Test Image");
		gd.addStringField("Title", imageTitle);
		gd.addNumericField("Image width", W, 0);
		gd.addNumericField("Image height", H, 0);
		gd.addNumericField("Center xc", xc, 2);
		gd.addNumericField("Center yc", yc, 2);
		gd.addNumericField("Radius r", rad, 2);
		gd.addNumericField("Position noise \u03C3", xySigma, 2);
		gd.addNumericField("No. of ON-circle points", onCirclePts, 0);
		gd.addNumericField("No. of OFF-circle points", offCirclePts, 0);
		gd.addCheckbox("Show inverted image", showInverted);
		gd.showDialog();
		if (gd.wasCanceled()) {
			return false;
		}
		imageTitle = gd.getNextString();
		W = (int) gd.getNextNumber();
		H = (int) gd.getNextNumber();
		xc = gd.getNextNumber();
		yc = gd.getNextNumber();
		rad = gd.getNextNumber();
		xySigma = gd.getNextNumber();
		onCirclePts = (int) gd.getNextNumber();
		offCirclePts = (int) gd.getNextNumber();
		showInverted = gd.getNextBoolean();
		return true;
	}

}
