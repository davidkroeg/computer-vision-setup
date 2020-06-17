package assignment03;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Overlay;
import ij.plugin.filter.PlugInFilter;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import imagingbook.lib.math.Matrix;
import imagingbook.pub.corners.Corner;
import imagingbook.pub.geometry.basic.Point;
import imagingbook.pub.corners.HarrisCornerDetector;
import imagingbook.pub.geometry.fitting.AffineFit2D;
import imagingbook.pub.geometry.fitting.LinearFit2D;

public class Matching_Points implements PlugInFilter {

	private static int cornerSize = 2;					// size of cross-markers
	private static Color cornerColor = Color.green;		// color of cross markers
	
	private static double topSpacing = 45;
	private static double bottomSpacing = 70;
	private static double sideSpacing = 125;
	private static double midSpacing = 10;
	
	ImagePlus im;
	
	@Override
	public int setup(String arg, ImagePlus im) {
		this.im = im;
        return DOES_ALL + NO_CHANGES;
	}

	@Override
	public void run(ImageProcessor ip) {
		
		final int width = ip.getWidth();
		final int height = ip.getHeight();
		
		//run corner detector
		HarrisCornerDetector cd = new HarrisCornerDetector(ip);
		List<Corner> corners = cd.getCorners();
		
		//split into 2 point clouds P and Q
		List<Point> leftCorners = new ArrayList<Point>();
		List<Point> rightCorners = new ArrayList<Point>();
		List<Corner> cornersL =  new ArrayList<Corner>();
		List<Corner> cornersR =  new ArrayList<Corner>();
		
		for (Corner c : corners) {
			double x = c.getX();
			double y = c.getY();
			
			if(y > topSpacing & y < height - bottomSpacing) {
				if(x > sideSpacing & x  < width/2 - 10) {
					leftCorners.add(Point.create(x, y));
					cornersL.add(c);
				} else if (x > width/2 + 10 & x < width - sideSpacing) {
					rightCorners.add(Point.create(x, y));
					cornersR.add(c);
				}
			}
		}
		
		//get centroids
		Point leftCentroid = getCentroid(leftCorners);
		Point rightCentroid = getCentroid(rightCorners);
		
		IJ.log("Left Centroid: " + leftCentroid.toString());
		IJ.log("Right Centroid: " + rightCentroid.toString());
		
		
		//calculate initial transformation A 
		RealMatrix A = createInitialTransformationMatrix(leftCentroid, rightCentroid);
		IJ.log("Initial Trans Matrix: " + A.toString());
		
		//apply A on right point cloud
		 List<Point> leftTransformedCorners = applyAffineTransformation(leftCorners, A);
		
		//execute Affine Fitter
		
		LinearFit2D fitter = new AffineFit2D(Point.toArray(leftTransformedCorners), Point.toArray(rightCorners));
		double[][] Af = fitter.getTransformationMatrix().getData();
		IJ.log("\nA (estimated) = \n" + Matrix.toString(Af));
		IJ.log(String.format("\nRMS error: \u03B5 = %.6f", Math.sqrt(fitter.getError())));
		
		Overlay oly = new Overlay();
		ImagePlus newIm = new ImagePlus("Overlay Test", ip);
		newIm.setOverlay(oly);
		
		ColorProcessor R = ip.convertToColorProcessor();
		drawCorners(R, corners, cornerColor);
		drawCorners(R, cornersL, Color.red);
		drawCorners(R, cornersR, Color.blue);
		
		new ImagePlus("Corners from " + im.getShortTitle(), R).show();
	}
	
	private Point getCentroid(List<Point> pointCloud) {
		double dx = 0.0;
		double dy = 0.0;
		
		for(Point p : pointCloud) {
			dx += p.getX();
			dy += p.getY();
		}
		
		dx /= pointCloud.size();
		dy /= pointCloud.size();
		
		return Point.create(dx, dy);
	}
	
	private RealMatrix createInitialTransformationMatrix(Point centroidLeft, Point centroidRight) {
		RealMatrix A = MatrixUtils.createRealMatrix(new double[][] 
				{{ 1,  0, centroidRight.getX() - centroidLeft.getX()}, 
				 { 0,  1,  centroidRight.getY() - centroidLeft.getY()}, 
				 { 0,  0,  1}});
		return A;
	}
	
	private List<Point> applyAffineTransformation(List<Point> pointList, RealMatrix M) {
		List<Point> resultPoints = new ArrayList<Point>();
		for (Point p : pointList) {
			//b*M = a
			RealVector a = MatrixUtils.createRealVector(new double[]{p.getX(), p.getY(), 1});
			RealVector b = M.operate(a);
			
			Point resultPoint = Point.create(b.getEntry(0), b.getEntry(1));
			
			resultPoints.add(resultPoint);
		}
		
		return resultPoints;
	}
	
	private void drawCorners(ImageProcessor ip, List<Corner> corners, Color color) {
		ip.setColor(color);
		int n = 0;
		for (Corner c: corners) {
			c.draw(ip, cornerSize);
			n = n + 1;
		}
	}

}
