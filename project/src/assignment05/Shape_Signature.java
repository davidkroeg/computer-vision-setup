package assignment05;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Point;
import java.awt.geom.Path2D;
import java.util.List;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.OvalRoi;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.gui.ShapeRoi;
import ij.plugin.filter.PlugInFilter;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import imagingbook.lib.color.RandomColorGenerator;
import imagingbook.pub.regions.Contour;
import imagingbook.pub.regions.RegionContourLabeling;
import imagingbook.pub.regions.RegionLabeling.BinaryRegion;
import util.io.ImageUtils;

public class Shape_Signature implements PlugInFilter {

	private final int numberOfCircles = 3;
	private final int numberOfSections = 12;

	@Override
	public int setup(String arg, ImagePlus imp) {
		return PlugInFilter.DOES_8G;
	}

	@Override
	public void run(ImageProcessor ip) {
		List<Point> originalPoints = ImageUtils.collectPoints(ip);

		Point.Double centroidOriginal = ImageUtils.calculateCentroid(originalPoints);
		Point furthest = ImageUtils.getFurthestPoint(centroidOriginal, originalPoints);
		double radius = centroidOriginal.distance(furthest);

		// create bigger image and move shape to it's center
		ColorProcessor cp = new ColorProcessor((int) (radius * 2.5), (int) (radius * 2.5));
		cp.setColor(Color.white);
		cp.fill();
		cp.setColor(Color.black);

		Point.Double centroid = new Point.Double(radius * 1.25, radius * 1.25);
		double offsetX = centroid.x - centroidOriginal.x;
		double offsetY = centroid.y - centroidOriginal.y;

		for (Point p : originalPoints) {
			cp.drawDot((int)(p.x + offsetX), (int)(p.y + offsetY));
		}

		// reference grid
		Overlay ol = getReferenceGrid(cp, numberOfCircles, numberOfSections, 0);

		// contour of shape
		ByteProcessor bp = cp.convertToByteProcessor();
		bp.invert();
		RegionContourLabeling segmenter = new RegionContourLabeling(bp);
		List<BinaryRegion> regions = segmenter.getRegions(true);
		Contour c = regions.get(0).getOuterContour();

		cp.setColor(Color.red);

		int[][] histogram = new int[numberOfCircles][numberOfSections];
		Color[][] contourColorLUT = getContourColorLUT(numberOfCircles, numberOfSections);

		for(Point p : c) {
			Point.Double p_unit = new Point.Double((p.getX()-centroid.getX())/radius, (p.getY()-centroid.getY())/radius);
			double r_unit = Math.sqrt(Math.pow(p_unit.getX(), 2) + Math.pow(p_unit.getY(), 2));

			//radial index
			int i_r = (int)Math.min(Math.floor(numberOfCircles * r_unit), numberOfCircles-1);

			//angular index
			int i_a = Math.floorMod((int)(numberOfSections * (Math.atan2(p_unit.getY(), p_unit.getX())/2*Math.PI)), numberOfSections);

			histogram[i_r][i_a]++;
			cp.setColor(contourColorLUT[i_r][i_a]);
		}

		Point.Double p_test = new Point.Double((110-centroid.getX())/radius, (40-centroid.getY())/radius);
		double r_unit = Math.sqrt(Math.pow(p_test.getX(), 2) + Math.pow(p_test.getY(), 2));
		//radial index
		int i_r = (int)Math.min(Math.floor(numberOfCircles * r_unit), numberOfCircles-1);

		//angular index
		int i_a = Math.floorMod((int)(numberOfSections * (Math.atan2(p_test.getY(), p_test.getX())/2*Math.PI)), numberOfSections);

		IJ.log("test point: circle: " + i_r + ", section: " + i_a);
		cp.drawDot(110, 40);

		ImageUtils.displayHistogram(histogram);
		ImagePlus finalImage = new ImagePlus("Shape Signature", cp);
		finalImage.setOverlay(ol);
		finalImage.show();

	}




	public Overlay getReferenceGrid(ColorProcessor cp, int numCircles, int numSections, double rotationRadian) {
		List<Point> pts = ImageUtils.collectPoints(cp);
		Point.Double centroid = ImageUtils.calculateCentroid(pts);
		Point furthest = ImageUtils.getFurthestPoint(centroid, pts);
		double radius = centroid.distance(furthest);

		// draw circles
		Overlay ol = new Overlay();

		for (int i = 0; i < numCircles; i++) {
			Roi gridCircleTmp = new OvalRoi(centroid.x - (radius / numCircles) * (numCircles - i),
					centroid.y - (radius / numCircles) * (numCircles - i),
					(radius / numCircles) * (numCircles - i) * 2,
					(radius / numCircles) * (numCircles - i) * 2);
			gridCircleTmp.setStrokeColor(Color.blue);
			gridCircleTmp.setStroke(new BasicStroke(0.75f));
			ol.add(gridCircleTmp);
		}

		Path2D gridLines = new Path2D.Double();
		gridLines.moveTo(centroid.getX(), centroid.getY());

		double initial_x = centroid.getX();
		double initial_y = centroid.getY() - radius;

		// draw lines
		for (int i = 0; i < numSections; i++) {
			double rotation_rad = Math.toRadians((360 / numSections) * i) + rotationRadian;
			double x_tmp = Math.cos(rotation_rad) * (initial_x - centroid.getX())
					- Math.sin(rotation_rad) * (initial_y - centroid.getY()) + centroid.getX();
			double y_tmp = Math.sin(rotation_rad) * (initial_x - centroid.getX())
					+ Math.cos(rotation_rad) * (initial_y - centroid.getY()) + centroid.getY();

			gridLines.lineTo(x_tmp, y_tmp);
			gridLines.moveTo(centroid.getX(), centroid.getX());
		}

		Roi gridLinesRoi = new ShapeRoi(gridLines);
		gridLinesRoi.setStrokeColor(Color.blue);
		gridLinesRoi.setStroke(new BasicStroke(0.75f));
		ol.add(gridLinesRoi);

		// draw orientation indication line
		Path2D orientationLine = new Path2D.Double();
		orientationLine.moveTo(centroid.getX(), centroid.getY());
		double rotation_orientation_line = Math.toRadians(270) + rotationRadian;
		double x_orientation_line = Math.cos(rotation_orientation_line) * (initial_x - centroid.getX())
				- Math.sin(rotation_orientation_line) * (initial_y - centroid.getY()) + centroid.getX();
		double y_orientation_line = Math.sin(rotation_orientation_line) * (initial_x - centroid.getX())
				+ Math.cos(rotation_orientation_line) * (initial_y - centroid.getY()) + centroid.getY();

		orientationLine.lineTo(x_orientation_line, y_orientation_line);
		Roi orientationLineRoi = new ShapeRoi(orientationLine);
		orientationLineRoi.setStrokeColor(Color.magenta);
		orientationLineRoi.setStroke(new BasicStroke(2f));
		ol.add(orientationLineRoi);

		return ol;
	}

	public Color[][] getContourColorLUT(int numCircles, int numSections) {
		Color[][] contourColorLUT = new Color[numCircles][numSections];
		RandomColorGenerator rcg = new RandomColorGenerator();


		for (int idx_circles = 0; idx_circles < numCircles; idx_circles++) {
			for (int idx_sections = 0; idx_sections < numSections; idx_sections++) {
				contourColorLUT[idx_circles][idx_sections] = rcg.nextColor();
			}
		}
		return contourColorLUT;
	}

}
