package assignment01;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;


/**
 * This ImageJ plugin collects the foreground points in
 * a binary image, converts to a new color image and
 * draws half of the dots in color.
 * 
 * @author WB
 * @version 2020-02-26
 */
public class Collect_Points_Demo implements PlugInFilter {
	
	private static int threshold = 1;
	private static int repeatCount = 50;
	
	private Circle detectedCircle = new Circle();
	
	public int setup(String arg, ImagePlus im) {
		return PlugInFilter.DOES_8G;
	}

	public void run(ImageProcessor ip) {
		if (!getUserInput()) {
			return;
		}
		
		final int w = ip.getWidth();
		final int h = ip.getHeight();
		
		// collect all image points with pixel values greater than zero:
		List<Point> pntlist = new ArrayList<Point>();
		for (int v = 0; v < h; v++) {
			for (int u = 0; u < w; u++) {
				int p = ip.getPixel(u, v);
				if (p > 0) {
					pntlist.add(new Point(u, v));
				}
			}
		}
		
		IJ.log("Found " + pntlist.size() + " foreground points.");
		
		// copy 'ip' to a new color image
		ImageProcessor cp = ip.convertToColorProcessor();
		
		for (int i = 0; i < repeatCount; i++) {
			// choose 3 random points
			Point[] circlePoints = chooseThreeRandomPoints(pntlist);
			
			// create circle
			Circle circle = new Circle(circlePoints[0], circlePoints[1], circlePoints[2]);
			circle.checkPointsOnCircle(pntlist, threshold);
			
			// check if it has more points on circle
			if (circle.pointCount > detectedCircle.pointCount) {
				detectedCircle = circle;
			}
		}
		
		// draw the circle
		cp.setColor(Color.blue);
		cp.setLineWidth(1);
		int radius = (int)detectedCircle.getRadius();
		cp.drawOval(detectedCircle.getCenter().x - radius, detectedCircle.getCenter().y - radius, radius * 2, radius * 2);
		IJ.log("Points on Circle: " + Integer.toString(detectedCircle.pointCount));
		IJ.log("Circle Center: " + Integer.toString(detectedCircle.getCenter().x) + "/" +  Integer.toString(detectedCircle.getCenter().y));
		
		// draw dots on circle
		cp.setColor(Color.red);
		for (Point p : detectedCircle.getPointsOnCircle()) {
			cp.drawDot(p.x, p.y);
		}
		
		// display the newly created image:
		showImage(cp, "colored dots");
	}
	
	void showImage(ImageProcessor ip, String title) {
		(new ImagePlus(title, ip)).show();
	}
	
	private boolean getUserInput() {
		GenericDialog gd = new GenericDialog("Create Circle Test Image");
		gd.addNumericField("Threshold", threshold, 0);
		gd.addNumericField("Repeat Count", repeatCount, 0);
		gd.showDialog();
		if (gd.wasCanceled()) {
			return false;
		}
		threshold = (int) gd.getNextNumber();
		repeatCount = (int) gd.getNextNumber();
		return true;
	}
	
	private Point[] chooseThreeRandomPoints(List<Point> pntlist) {
		Point[] circlePoints = new Point[3];
		List<Integer> alreadyRolledIndices = new ArrayList<Integer>();
		
		for (int i = 0; i < circlePoints.length; i++) {
			int index = getRandomIndex(pntlist.size() - 1);
			if (!alreadyRolledIndices.contains(index)) {
				circlePoints[i] = pntlist.get(index);
			} else {
				i--;
			}
		}
		return circlePoints;
	}
	
	private int getRandomIndex(int max) {
		double randomDouble = Math.random();
		randomDouble = randomDouble * max;
		int randomIndex = (int) randomDouble;
		return randomIndex;
	}

}
