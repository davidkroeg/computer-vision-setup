package assignment02;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

public class Test_Affine_Transformation implements PlugInFilter {
	
	ImagePlus im = null;

	@Override
	public int setup(String arg, ImagePlus imp) {
		this.im = imp;
		return PlugInFilter.DOES_8G;
	}

	@Override
	public void run(ImageProcessor ip) {
		ImageStack stack = im.getStack();
		ImageProcessor ipTestImage = stack.getProcessor(1);
		ImageProcessor ipResultImage = stack.getProcessor(2);
		
		final int w = ip.getWidth();
		final int h = ip.getHeight();
		
		// collect all image points with pixel values greater than zero:
		List<Point> pointList = getPoints(ipTestImage, w, h);
		List<Point> realPoints = getPoints(ipResultImage, w, h);
		
		IJ.log("Found " + pointList.size() + " foreground points in Test Image.");		
		
		//iterate through point List, apply transformation and add it to the new result point list
		List<Point> resultPoints = new ArrayList<Point>();
		for (Point p : pointList) {
			resultPoints.add(multiplyPointWithMatrix(p));
		}
		
		//draw result point list on second stack image
		ImageProcessor cp = ipResultImage.convertToColorProcessor();
		cp.setColor(Color.red);
		for (Point p : resultPoints) {
			cp.drawDot(p.x, p.y);
		}
		showImage(cp, "colored dots");
		
		//calculate residual error (caused by rounding to pixel positions)
		double residualError = calculateError(resultPoints, realPoints);
		IJ.log("Residual Error: " + residualError);
	}
	
	private List<Point> getPoints(ImageProcessor ip, int width, int height) {
		List<Point> pntlist = new ArrayList<Point>();
		for (int v = 0; v < height; v++) {
			for (int u = 0; u < width; u++) {
				int p = ip.getPixel(u, v);
				if (p > 0) {
					pntlist.add(new Point(u, v));
				}
			}
		}
		return pntlist;
	}
	
	private Point multiplyPointWithMatrix(Point p) {		
		RealMatrix M = MatrixUtils.createRealMatrix(new double[][] 
				{{ 0.013,  1.088, 18.688}, 
				 {-1,  -0.05,  127.5}, 
				 { 0, 0, 1}});
		
		RealVector a = MatrixUtils.createRealVector(new double[]{p.getX(), p.getY(), 1});
		
		RealVector b = M.operate(a);
		
		Point resultPoint = new Point();
		resultPoint.setLocation(b.getEntry(0), b.getEntry(1));
		
		return resultPoint;
	}
	
	/**
	 * Run through resulPoints and find each corresponding point in the realPoints which is closest. Calculate the least squares error.
	 * @param resultPoints calculated points after the transformation
	 * @param realPoints the real points of the right image
	 * @return least squares error as a double
	 */
	private double calculateError(List<Point> resultPoints, List<Point> realPoints) {		
		double error = 0.0;
		
		for (Point resultPoint : resultPoints) {
			Point correspondancePoint = new Point();
			for (Point realPoint : realPoints) {
				double errorDistance = resultPoint.distance(realPoint);
				if (errorDistance < resultPoint.distance(correspondancePoint)) {
					correspondancePoint = realPoint;
				}
			}
			error += calculateError(resultPoint, correspondancePoint);
		}
		return error;
	}
	
	private double calculateError(Point p, Point x) {
		//take this if p is not the transformed point
//		double error = Math.pow(Math.pow((0.013*p.getX() + 1.088*p.getY() + 18.688 - x.getX()), 2) +
//				Math.pow((-1*p.getX() - 0.05*p.getY() + 127.5 - x.getY()), 2), 0.5);
		double error = Math.pow(Math.pow(p.getX() - x.getX(), 2) +
				Math.pow(p.getY() - x.getY(), 2), 0.5);
		return error;
	}
	
	void showImage(ImageProcessor ip, String title) {
		(new ImagePlus(title, ip)).show();
	}

}
