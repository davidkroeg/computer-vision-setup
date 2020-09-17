package util.io;

import java.awt.Color;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.IntSummaryStatistics;
import java.util.List;

import ij.ImagePlus;
import ij.process.ColorProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

public class ImageUtils {

	public static ColorProcessor setWhite(ColorProcessor ip) {
		int w = ip.getWidth();
		int h = ip.getHeight();
		
		for(int y = 0; y < h; y++) {
			for(int x = 0; x < w; x++) {
				ip.putPixel(x, y, new int[] {255,255,255});
			}
		}
		return ip;
	}
	
	public static List<java.awt.Point> collectPoints(ImageProcessor ip){
		List<java.awt.Point> pointList = new ArrayList<java.awt.Point>();
		
		int w = ip.getWidth();
		int h = ip.getHeight();
		
		for(int y = 0; y < h; y++) {
			for(int x = 0; x < w; x++) {
				if(ip.getPixel(x, y) > 0) {
					pointList.add(new java.awt.Point(x, y));
				}
			}
		}
		return pointList;
	}
	
	public static List<java.awt.Point> collectPoints(ImagePlus ip){
		List<java.awt.Point> pointList = new ArrayList<java.awt.Point>();

		int w = ip.getWidth();
		int h = ip.getHeight();

		for(int y = 0; y < h; y++) {
			for(int x = 0; x < w; x++) {
				if(ip.getProcessor().getPixel(x, y) > 0) {
					pointList.add(new java.awt.Point(x, y));
				}
			}
		}
		return pointList;
	}
	
	public static java.awt.Point.Double calculateCentroid(List<java.awt.Point> points){
		double sumX = 0;
		double sumY = 0;
		
		for(java.awt.Point p : points) {
			sumX += p.getX();
			sumY += p.getY();
		}
		
		return new java.awt.Point.Double(sumX/points.size(), sumY/points.size());
	}
	
	public static java.awt.Point getFurthestPoint(java.awt.Point.Double  origin, List<java.awt.Point> points){
		double maxDistance = 0;
		java.awt.Point furthestPoint = null;
		
		for(java.awt.Point p : points) {
			if(p.distance(origin) > maxDistance) {
				furthestPoint = p;
				maxDistance = p.distance(origin);
			}
		}
		return furthestPoint;
	}
	
	public static double getOrientationRadian(List<java.awt.Point> points) {
		java.awt.Point.Double centroid = calculateCentroid(points);
		java.awt.Point furthest = getFurthestPoint(centroid, points);
		double radius = centroid.distance(furthest);
		
		double mu_11 = 0;
		double mu_20 = 0;
		double mu_02 = 0;
		
		for(java.awt.Point p : points) {
			mu_11 += (p.x - centroid.x) * (p.y - centroid.y);
			mu_20 += (p.x - centroid.x) * (p.x - centroid.x);
			mu_02 += (p.y - centroid.y) * (p.y - centroid.y);
		}
		double orientation = 0.5 * Math.atan2((2*mu_11),(mu_20 - mu_02));
		
		if(orientation < 0) {
			orientation += Math.PI;
		}
		return orientation;
	}
	
	public static void displayHistogram(int[][] data) {
		int circles = data.length;
		int sections = data[0].length;
		
		ImageProcessor hist = new ColorProcessor(data[0].length*20, data.length*20);
		hist.invert();
		
		int max = 0;
		for(int i = 0; i < data.length; i++) {
			IntSummaryStatistics stat = Arrays.stream(data[i]).summaryStatistics();
			if(stat.getMax() > max) {
				max = stat.getMax();
			}
		}
		
		for(int idx_circles = 0; idx_circles < circles; idx_circles++) {
			for(int idx_sections = 0; idx_sections < sections; idx_sections++) {
				//draw square in histogram
				for(int y = 0; y < 20; y++) {
					for(int x = 0; x < 20; x++) {
						int rgb_val = (int)((data[idx_circles][idx_sections]*1.0)/(max*1.0) * 255);
						Color c = new Color(rgb_val, rgb_val, rgb_val);
						hist.setColor(c);
						hist.drawDot(idx_sections*20+x, idx_circles*20+y);
					}
				}
			}
		}
		
		new ImagePlus("Histogram", hist).show();
	}
	
	public static double[][] normalizeHistogram(int[][] histogram){
		int circles = histogram.length;
		int sections = histogram[0].length;
		double[][] hist_normalized = new double[circles][sections];
		
		//calculate normalization factor
		double sum = 0;
		for(int idx_circles = 0; idx_circles < circles; idx_circles++) {
			for(int idx_sections = 0; idx_sections < sections; idx_sections++) {
				sum += histogram[idx_circles][idx_sections];
			}
		}
		
		//apply normalization factor
		for(int idx_circles = 0; idx_circles < circles; idx_circles++) {
			for(int idx_sections = 0; idx_sections < sections; idx_sections++) {
				hist_normalized[idx_circles][idx_sections] = histogram[idx_circles][idx_sections]/sum;
			}
		}

		return hist_normalized;
	}
	
	public static FloatProcessor crop(FloatProcessor image, Rectangle roi) {
		FloatProcessor cropped = new FloatProcessor(roi.width, roi.height);
		
		for(int y = 0; y < roi.y; y++) {
			for(int x = 0; x < roi.x; x++) {
				cropped.putPixel(x, y, image.getPixel(roi.x+x, roi.y+y));
			}
		}
		return cropped;
	}
	
}
