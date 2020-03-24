package assignment02;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

//import org.apache.commons.math3.linear.DecompositionSolver;
//import org.apache.commons.math3.linear.MatrixUtils;
//import org.apache.commons.math3.linear.RealMatrix;
//import org.apache.commons.math3.linear.RealVector;
//import org.apache.commons.math3.linear.SingularValueDecomposition;

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
		
//		RealMatrix A = MatrixUtils.createRealMatrix(new double[][] 
//				{{ 0.013,  1.088, 18.688}, 
//				 {-1.000,  -0.05,  127.5}, 
//				 { 0, 0, 1}});
		
		
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
		//FIXME: Error is still wrong
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
	
	//probably relevant for next exercise
//	private Point matrixMultiplication(RealMatrix A, RealVector b) {
//		DecompositionSolver s = new SingularValueDecomposition(A).getSolver();
//		IJ.log("Start: b = " + b.toString());
//		// Solve the system of equations:
//		RealVector x = s.solve(b);
//		
//		IJ.log("Solution: x = " + x.toString()); // = {-0.3698630137; 0.1780821918; -0.602739726}
//		
//		// Verify that A.x = b:
//		RealVector bb = A.operate(x);
//		IJ.log("Check: A.x = " + bb.toString());
//		
//		Point resultPoint = new Point();
//		resultPoint.setLocation(x.getEntry(0), x.getEntry(1));
//		
//		return resultPoint;
//	}
	
	private double calculateError(Point p, Point x) {
		//only fits if Point p is the not transformed point
//		double error = Math.pow((0.013*p.getX() + 1.088*p.getY() + 18.688 - x.getX()), 2) +
//				Math.pow((-1*p.getX() - 0.05*p.getY() + 127.5 - x.getY()), 2);
		double error = Math.abs(p.getX() - x.getX()) + Math.abs(p.getY() - x.getY());
		
		return error;
	}
	
	private Point multiplyPointWithMatrix(Point p) {
		double x = p.getX() * 0.013 + p.getY() * 1.088 + 18.688;
		double y = p.getX() * -1 - p.getY() * 0.05 + 127.5;
		
		Point resultPoint = new Point();
		resultPoint.setLocation(x, y);
		
		return resultPoint;
	}
	
	/**
	 * Run through resulPoints and find each corresponding point in the realPoints which is closest. Calculate the least squares error.
	 * @param resultPoints calculated points after the transformation
	 * @param realPoints the real points of the right image
	 * @return least squares error as a double
	 */
	private double calculateError(List<Point> resultPoints, List<Point> realPoints) {
		//TODO: run through result Points and find the point in the right set which is closest -> calculate Error and add to sum
		//for loop result points
			//for loop real points
			//calc distance - save points with closest distance
		//calc error to closest point and add to error sum
		
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
	
	void showImage(ImageProcessor ip, String title) {
		(new ImagePlus(title, ip)).show();
	}

}
