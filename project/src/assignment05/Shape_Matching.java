package assignment05;

import java.awt.Color;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.util.List;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.OvalRoi;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.plugin.filter.PlugInFilter;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import imagingbook.pub.regions.Contour;
import imagingbook.pub.regions.RegionContourLabeling;
import imagingbook.pub.regions.RegionLabeling.BinaryRegion;

public class Shape_Matching implements PlugInFilter {

	private ImagePlus im;

	public int setup(String arg, ImagePlus im) {
		this.im = im;
		return DOES_8G + NO_CHANGES;
	}

	public void run(ImageProcessor ip) {

		RegionContourLabeling segmenter = new RegionContourLabeling((ByteProcessor) ip);
		
		// This returns a list of regions, sorted by size:
		List<BinaryRegion> regions = segmenter.getRegions(true);
		IJ.log("Number of regions found: " + regions.size());

		ColorProcessor cp = ip.convertToColorProcessor();
		
		// Visit and color the pixels inside of each region:
		cp.setColor(new Color(255, 185, 182));
		for (BinaryRegion R : regions) {
			IJ.log("Region " + R.getLabel() + " has size " + R.getSize());
			IJ.log("   Center : " + R.getCenterPoint().toString());
			for (Point p : R) {
				cp.drawDot(p.x, p.y);
			}
		}
		
		Overlay oly = new Overlay();
		
		// Color the outer contour pixels of each region:
		cp.setColor(Color.blue);
		for (BinaryRegion R : regions) {
			Contour c = R.getOuterContour();
			double r = 0;
			int xC = (int)R.getXc();
			int yC = (int)R.getYc();
			for (Point p : c) {
				cp.drawDot(p.x, p.y);
				double distance = R.getCenterPoint().distance(new Point2D.Double(p.getX(), p.getY()));
				if(distance > r) {
					r = distance;
				}
			}
			
			cp.drawDot(xC, yC);
			drawCircles(xC, yC, (int)r, oly);
			
			for(Point p : c) {
				p = transformToUnitCirclePoint(p.getX(), p.getY(), r);
				double l2 = calculateL2Distance(R.getCenterPoint(), p);				
				double radialIndex = calculateRadialIndex(3, l2);
				double angulaIndex = calculateAngularIndex(12, p);
				IJ.log(p.toString() + ": ("+ radialIndex +","+angulaIndex+")");
			}
			
		}
		
		
		//create cdhp
		//get point farthest away
		
		
		// create an empty overlay

		ImagePlus newIm = new ImagePlus(im.getShortTitle() + "-colored", cp);
		newIm.setOverlay(oly);
		newIm.show();
	}
	
	private void drawCircles(int xC, int yC, int r, Overlay oly) {
		Roi roi = new OvalRoi(xC - r, yC - r, r*2, r*2);
		roi.setStrokeColor(Color.green);
		roi.setStrokeWidth(0.5f);
		oly.add(roi);
		
		int r1 = r - r/3;
		
		Roi roi1 = new OvalRoi(xC - r1, yC - r1, r1*2, r1*2);
		roi1.setStrokeColor(Color.green);
		roi1.setStrokeWidth(0.5f);
		oly.add(roi1);
		
		int r2 = r1 - r/3;
		
		Roi roi2 = new OvalRoi(xC - r2, yC - r2, r2*2, r2*2);
		roi2.setStrokeColor(Color.green);
		roi2.setStrokeWidth(0.5f);
		oly.add(roi2);
	}
	
	private double calculateRadialIndex(int nR, double r) {
		return Math.min(Math.floor(nR * r), nR - 1);
	}
	
	private double calculateAngularIndex(int nA, Point p) {
		return Math.floorMod((int)Math.floor(nA * (Math.atan2(p.getY(), p.getX()) / 2 * Math.PI)), nA);
	}
	
	private Point transformToUnitCirclePoint(double xC, double yC, double rMax) {
		Point unitPoint = new Point();
		//u = S * T * p
		Point centerPoint = new Point();
		centerPoint.setLocation(xC, yC);
		RealMatrix S = getScalingMatrix(rMax);
		RealMatrix T = getTransformationMatrix(xC, yC);
		RealVector p = getPointVector(centerPoint);
		
		RealVector u = (S.multiply(T)).operate(p);
		unitPoint.setLocation(u.getEntry(0), u.getEntry(1));
		
		return unitPoint;
	}
	
	private double calculateL2Distance(Point2D center, Point p) {
		double l2 = Math.sqrt((Math.pow(center.getX() - p.getX(), 2) + Math.pow(center.getY() - p.getY(), 2)));
		return l2;
	}
	
	private RealMatrix getScalingMatrix(double rMax) {
		double [][] Sa = {{1/rMax, 0, 0}, {0, 1/rMax, 0}, {0,0,1}};
		return MatrixUtils.createRealMatrix(Sa);
	}
	
	private RealMatrix getTransformationMatrix(double xC, double yC) {
		double [][] Ta = {{1, 0,-xC},{0,1,-yC},{0,0,1}};
		return MatrixUtils.createRealMatrix(Ta);
	}
	
	private RealVector getPointVector(Point p) {
		double [] pa = {p.getX(), p.getY(), 1};
		return MatrixUtils.createRealVector(pa);
	}

}
