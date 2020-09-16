package assignment04;

import java.awt.Color;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import imagingbook.lib.color.RandomColorGenerator;
import imagingbook.pub.regions.Contour;
import imagingbook.pub.regions.RegionContourLabeling;
import imagingbook.pub.regions.RegionLabeling.BinaryRegion;
import util.io.ImageUtils;

public class Polygon_Segmentation implements PlugInFilter {

	@Override
	public int setup(String arg, ImagePlus imp) {
		return DOES_8G + NO_CHANGES;
	}

	@Override
	public void run(ImageProcessor ip) {
		RegionContourLabeling segmenter = new RegionContourLabeling((ByteProcessor) ip);
		
		//get all regions
		List<BinaryRegion> regions = segmenter.getRegions(true);
		IJ.log("Region count: " + regions.size());
		
		ColorProcessor cp = new ColorProcessor(ip.getWidth(), ip.getHeight());
		ImageUtils.setWhite(cp);
		
		cp.setColor(Color.yellow);
		BinaryRegion region = regions.get(0);
		Contour c = region.getOuterContour();
//		for(Point p : c) {
//			cp.drawDot(p.x, p.y);
//		}
		
		int preload_amount = 10;
		List<Point> pts = c.getPointList();
		AutoFitLine fittedLine = new AutoFitLine();
		
		//preload fittedLine with some points
		for(int i = 0; i < preload_amount; i++) {
			fittedLine.addBack(pts.get(i));
		}
		
		double maxError = 20;
		int i = preload_amount;
		List<AlgebraicLine> boundingLines = new ArrayList<AlgebraicLine>();
		while(i < pts.size()) {
			fittedLine.addBack(pts.get(i));
			//found end of line
			if(fittedLine.getError() > maxError) {
				while(fittedLine.getError() > maxError) {
					fittedLine.removeBack();
					i--;
				}
				boundingLines.add(fittedLine.getLine());
				fittedLine.clear();
				
				//preload fittedLine with some points
				int j = 0;
				while(i < pts.size() && j < preload_amount) {
					fittedLine.addBack(pts.get(i));
					j++;
					i++;
				}
			}
			i++;
		}
		boundingLines.add(fittedLine.getLine());
		
		List<Point> intersections = new ArrayList<Point>();
		for(int l = 0; l < boundingLines.size(); l++) {
			Point2D intersectionPoint = boundingLines.get(l).intersect(boundingLines.get((l+1)%boundingLines.size()));
			Point p = new Point();
			p.setLocation(intersectionPoint.getX(), intersectionPoint.getY());
			intersections.add(p);
		}
		
		
		RandomColorGenerator rcg = new RandomColorGenerator();
		for(int l = 0; l < intersections.size(); l++) {
			cp.setColor(rcg.nextColor());
			Point p1 = intersections.get(l);
			Point p2 = intersections.get((l+1)%intersections.size());
			cp.drawLine(p1.x, p1.y, p2.x, p2.y);
		}
		
		cp.setColor(Color.red);
		for(Point p : intersections) {
			cp.drawOval((int)p.getX() - 5, (int)p.getY() - 5, 10, 10);
		}
		
		new ImagePlus("Polygon Segmentation", cp).show();
	}
	
}
