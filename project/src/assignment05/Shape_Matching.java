package assignment05;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealVector;

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

public class Shape_Matching implements PlugInFilter {

	private final int histogram_circles = 3;
	private final int histogram_sections = 12;
	private final int region_min_points = 10;
	
	@Override
	public int setup(String arg, ImagePlus imp) {
		return PlugInFilter.DOES_8G;
	}

	@Override
	public void run(ImageProcessor ip) {
		int w = ip.getWidth();
		int h = ip.getHeight();
		
		ip.setRoi(new Rectangle(0,0, w, 130));
		ImageProcessor ip_ref= ip.crop();

		ip.setRoi(new Rectangle(0, 130, w, h-131));
		ImageProcessor ip_test = ip.crop();
		
		ColorProcessor cp_ref = ip_ref.convertToColorProcessor();
		ColorProcessor cp_test = ip_test.convertToColorProcessor();
		
		ByteProcessor bp_ref = ip_ref.convertToByteProcessor();
		ByteProcessor bp_test = ip_test.convertToByteProcessor();
		
		RegionContourLabeling segmenter_ref = new RegionContourLabeling(bp_ref);
		RegionContourLabeling segmenter_test = new RegionContourLabeling(bp_test);
		
		List<BinaryRegion> regions_ref = segmenter_ref.getRegions(true);
		List<BinaryRegion> regions_test = segmenter_test.getRegions(true);
		
		List<List<ShapeHistogram>> histograms_reference = new ArrayList<List<ShapeHistogram>>();
		
		RandomColorGenerator rcg = new RandomColorGenerator();
		
		for(BinaryRegion br : regions_ref) {
			if(br.getSize() < region_min_points) {
				continue;
			}
			
			ShapeHistogram hist1 = new ShapeHistogram(histogram_circles, histogram_sections);
			ShapeHistogram hist2 = new ShapeHistogram(histogram_circles, histogram_sections);
			
			//iterable to List<Point> to use existing util functions
			List<Point> pts = new ArrayList<Point>();
			br.forEach(pts::add);
			
			Point.Double centroid = ImageUtils.calculateCentroid(pts);
			Point furthest = ImageUtils.getFurthestPoint(centroid, pts);
			double radius = centroid.distance(furthest);
			double angular_offset_rad = ImageUtils.getOrientationRadian(pts);
			double angular_offset_rad2 = angular_offset_rad + Math.PI;
			
			Contour c = br.getOuterContour();
			
			RealVector centroid_vec = MatrixUtils.createRealVector(new double[] {0, radius});
			RealVector centroid_vec_rotated = MatrixUtils.createRealVector(new double[]
												{Math.cos(angular_offset_rad)*centroid_vec.getEntry(0) - Math.sin(angular_offset_rad)*centroid_vec.getEntry(1),
												 Math.sin(angular_offset_rad)*centroid_vec.getEntry(0) + Math.cos(angular_offset_rad)*centroid_vec.getEntry(1)
												});
			
			RealVector centroid_vec_rotated2 = MatrixUtils.createRealVector(new double[]
					{Math.cos(angular_offset_rad2)*centroid_vec.getEntry(0) - Math.sin(angular_offset_rad2)*centroid_vec.getEntry(1),
					 Math.sin(angular_offset_rad2)*centroid_vec.getEntry(0) + Math.cos(angular_offset_rad2)*centroid_vec.getEntry(1)
					});
			
			double circle_step_size = radius/histogram_circles;
			double section_step_size = 360/histogram_sections;
			
			
			//draw orientation line
			Path2D orientation_line = new Path2D.Double();
			orientation_line.moveTo(centroid.getX(), centroid.getY());
			double rotation_orientation_line = Math.toRadians(270) + angular_offset_rad;
			double x_orientation_line = Math.cos(rotation_orientation_line) * (centroid.getX() - centroid.getX())
					- Math.sin(rotation_orientation_line) * (centroid.getY() - radius - centroid.getY()) + centroid.getX();
			double y_orientation_line = Math.sin(rotation_orientation_line) * (centroid.getX() - centroid.getX())
					+ Math.cos(rotation_orientation_line) * (centroid.getY()- radius - centroid.getY()) + centroid.getY();
			
			cp_ref.setColor(Color.red);
			cp_ref.drawLine((int)centroid.getX(), (int)centroid.getY(), (int)x_orientation_line, (int)y_orientation_line);
			
			//draw orientation line2
			Path2D orientation_line2 = new Path2D.Double();
			orientation_line2.moveTo(centroid.getX(), centroid.getY());
			double rotation_orientation_line2 = Math.toRadians(270) + angular_offset_rad2;
			double x_orientation_line2 = Math.cos(rotation_orientation_line2) * (centroid.getX() - centroid.getX())
					- Math.sin(rotation_orientation_line2) * (centroid.getY() - radius - centroid.getY()) + centroid.getX();
			double y_orientation_line2 = Math.sin(rotation_orientation_line2) * (centroid.getX() - centroid.getX())
					+ Math.cos(rotation_orientation_line2) * (centroid.getY()- radius - centroid.getY()) + centroid.getY();
			
			cp_ref.setColor(Color.green);
			cp_ref.drawLine((int)centroid.getX(), (int)centroid.getY(), (int)x_orientation_line2, (int)y_orientation_line2);
			
			//assign contour points to corresponding sections
			for(Point p : c) {
				//mapping the distance to the circle
				double distance_tmp = centroid.distance(p);
				int idx_circle = Math.min((int) (distance_tmp / circle_step_size), histogram_circles - 1);
				
				RealVector p_vec = MatrixUtils.createRealVector(new double[] { p.getX() - centroid.getX(), p.getY() - centroid.getY() });
				
				//get section idx1
				double angle = Math.atan2(p_vec.getEntry(1), p_vec.getEntry(0))
							 - Math.atan2(centroid_vec_rotated.getEntry(1), centroid_vec_rotated.getEntry(0));
				if(angle < 0) {
					angle += 2*Math.PI;
				}
				
				angle = Math.toDegrees(angle);
				int idx_section = (int)(angle/section_step_size);
				
				hist1.increaseCount(idx_circle, idx_section);
				
				
				//get section idx2
				double angle2 = Math.atan2(p_vec.getEntry(1), p_vec.getEntry(0))
							 - Math.atan2(centroid_vec_rotated2.getEntry(1), centroid_vec_rotated2.getEntry(0));
				if(angle2 < 0) {
					angle2 += 2*Math.PI;
				}
				
				angle2 = Math.toDegrees(angle2);
				int idx_section2 = (int)(angle2/section_step_size);
				
				hist2.increaseCount(idx_circle, idx_section2);
				
			}
			
			hist1.normalize();
			hist2.normalize();

			Color col = rcg.nextColor();
			hist1.setColor(col);
			hist2.setColor(col);
			
			hist1.setBoundingBox(br.getBoundingBox());
			hist2.setBoundingBox(br.getBoundingBox());
			
			List<ShapeHistogram> tmp_hist = new ArrayList<ShapeHistogram>();
			tmp_hist.add(hist1);
			tmp_hist.add(hist2);
			histograms_reference.add(tmp_hist);
			
			//cp_ref.setColor(Color.red);
			//cp_ref.drawDot(centroid.x, centroid.y);
			
			cp_ref.setColor(col);
			Rectangle bounding_box = br.getBoundingBox();
			cp_ref.drawRect(bounding_box.x, bounding_box.y, bounding_box.width, bounding_box.height);
		
		}
		
		
		List<List<ShapeHistogram>> histograms_test= new ArrayList<List<ShapeHistogram>>();
		
		for(BinaryRegion br : regions_test) {
			if(br.getSize() < region_min_points) {
				continue;
			}
			
			ShapeHistogram hist1 = new ShapeHistogram(histogram_circles, histogram_sections);
			ShapeHistogram hist2 = new ShapeHistogram(histogram_circles, histogram_sections);
			
			//iterable to List<Point> to use existing util functions
			List<Point> pts = new ArrayList<Point>();
			br.forEach(pts::add);
			
			Point.Double centroid = ImageUtils.calculateCentroid(pts);
			Point furthest = ImageUtils.getFurthestPoint(centroid, pts);
			double radius = centroid.distance(furthest);
			double angular_offset_rad = ImageUtils.getOrientationRadian(pts);
			double angular_offset_rad2 = angular_offset_rad + Math.PI;
			
			Contour c = br.getOuterContour();
			
			RealVector centroid_vec = MatrixUtils.createRealVector(new double[] {0, radius});
			RealVector centroid_vec_rotated = MatrixUtils.createRealVector(new double[]
												{Math.cos(angular_offset_rad)*centroid_vec.getEntry(0) - Math.sin(angular_offset_rad)*centroid_vec.getEntry(1),
												 Math.sin(angular_offset_rad)*centroid_vec.getEntry(0) + Math.cos(angular_offset_rad)*centroid_vec.getEntry(1)
												});
			
			RealVector centroid_vec_rotated2 = MatrixUtils.createRealVector(new double[]
					{Math.cos(angular_offset_rad2)*centroid_vec.getEntry(0) - Math.sin(angular_offset_rad2)*centroid_vec.getEntry(1),
					 Math.sin(angular_offset_rad2)*centroid_vec.getEntry(0) + Math.cos(angular_offset_rad2)*centroid_vec.getEntry(1)
					});
			
			double circle_step_size = radius/histogram_circles;
			double section_step_size = 360/histogram_sections;
			
			
			//draw orientation line
			Path2D orientation_line = new Path2D.Double();
			orientation_line.moveTo(centroid.getX(), centroid.getY());
			double rotation_orientation_line = Math.toRadians(270) + angular_offset_rad;
			double x_orientation_line = Math.cos(rotation_orientation_line) * (centroid.getX() - centroid.getX())
					- Math.sin(rotation_orientation_line) * (centroid.getY() - radius - centroid.getY()) + centroid.getX();
			double y_orientation_line = Math.sin(rotation_orientation_line) * (centroid.getX() - centroid.getX())
					+ Math.cos(rotation_orientation_line) * (centroid.getY() - radius - centroid.getY()) + centroid.getY();
			
			cp_test.setColor(Color.red);
			cp_test.drawLine((int)centroid.x, (int)centroid.y, (int)x_orientation_line, (int)y_orientation_line);
			
			//draw orientation line2
			Path2D orientation_line2 = new Path2D.Double();
			orientation_line2.moveTo(centroid.getX(), centroid.getY());
			double rotation_orientation_line2 = Math.toRadians(270) + angular_offset_rad2;
			double x_orientation_line2 = Math.cos(rotation_orientation_line2) * (centroid.getX() - centroid.getX())
					- Math.sin(rotation_orientation_line2) * (centroid.getY() - radius - centroid.getY()) + centroid.getX();
			double y_orientation_line2 = Math.sin(rotation_orientation_line2) * (centroid.getX() - centroid.getX())
					+ Math.cos(rotation_orientation_line2) * (centroid.getY()- radius - centroid.getY()) + centroid.getY();
			
			cp_test.setColor(Color.green);
			cp_test.drawLine((int)centroid.getX(), (int)centroid.getY(), (int)x_orientation_line2, (int)y_orientation_line2);
			
			//assign contour points to corresponding sections
			for(Point p : c) {
				//mapping the distance to the circle
				double distance_tmp = centroid.distance(p);
				int idx_circle = Math.min((int) (distance_tmp / circle_step_size), histogram_circles - 1);
				
				RealVector p_vec = MatrixUtils.createRealVector(new double[] { p.getX() - centroid.getX(), p.getY() - centroid.getY() });
				
				//get section idx1
				double angle = Math.atan2(p_vec.getEntry(1), p_vec.getEntry(0))
							 - Math.atan2(centroid_vec_rotated.getEntry(1), centroid_vec_rotated.getEntry(0));
				if(angle < 0) {
					angle += 2*Math.PI;
				}
				
				angle = Math.toDegrees(angle);
				int idx_section = (int)(angle/section_step_size);
				
				hist1.increaseCount(idx_circle, idx_section);
				
				
				//get section idx2
				double angle2 = Math.atan2(p_vec.getEntry(1), p_vec.getEntry(0))
							 - Math.atan2(centroid_vec_rotated2.getEntry(1), centroid_vec_rotated2.getEntry(0));
				if(angle2 < 0) {
					angle2 += 2*Math.PI;
				}
				
				angle2 = Math.toDegrees(angle2);
				int idx_section2 = (int)(angle2/section_step_size);
				
				hist2.increaseCount(idx_circle, idx_section2);
			}
			
			hist1.normalize();
			hist2.normalize();

			Color col = rcg.nextColor();
			hist1.setColor(col);
			hist2.setColor(col);
			
			hist1.setBoundingBox(br.getBoundingBox());
			hist2.setBoundingBox(br.getBoundingBox());
			
			List tmp_hist = new ArrayList<ShapeHistogram>();
			tmp_hist.add(hist1);
			tmp_hist.add(hist2);
			histograms_test.add(tmp_hist);
			
			//cp_ref.setColor(Color.red);
			//cp_ref.drawDot(centroid.x, centroid.y);
		}
		
		
		
		for(List<ShapeHistogram> sh_test_pair : histograms_test) {
			for(List<ShapeHistogram> sh_ref_pair : histograms_reference) {
				if(sh_test_pair.get(0).equals(sh_ref_pair.get(0), 0.05)) {
					sh_test_pair.get(0).setColor(sh_ref_pair.get(0).getColor());
					Rectangle bb = sh_test_pair.get(0).getBoundingBox();
					cp_test.setColor(sh_ref_pair.get(0).getColor());
					cp_test.drawRect(bb.x, bb.y, bb.width, bb.height);
					IJ.log("matched");
				} else if(sh_test_pair.get(0).equals(sh_ref_pair.get(1), 0.05)) {
					sh_test_pair.get(0).setColor(sh_ref_pair.get(1).getColor());
					Rectangle bb = sh_test_pair.get(0).getBoundingBox();
					cp_test.setColor(sh_ref_pair.get(1).getColor());
					cp_test.drawRect(bb.x, bb.y, bb.width, bb.height);
					IJ.log("matched");
				} else if(sh_test_pair.get(1).equals(sh_ref_pair.get(0), 0.05)) {
					sh_test_pair.get(1).setColor(sh_ref_pair.get(0).getColor());
					Rectangle bb = sh_test_pair.get(1).getBoundingBox();
					cp_test.setColor(sh_ref_pair.get(0).getColor());
					cp_test.drawRect(bb.x, bb.y, bb.width, bb.height);
					IJ.log("matched");
				} else if(sh_test_pair.get(1).equals(sh_ref_pair.get(1), 0.05)) {
					sh_test_pair.get(1).setColor(sh_ref_pair.get(1).getColor());
					Rectangle bb = sh_test_pair.get(1).getBoundingBox();
					cp_test.setColor(sh_ref_pair.get(1).getColor());
					cp_test.drawRect(bb.x, bb.y, bb.width, bb.height);
					IJ.log("matched");
				} else {
					IJ.log("not matched");
				}
			}
		}
		
		/*
		int i = 0;
		//match the test histograms to the reference histograms
		for(ShapeHistogram sh_test : histograms_test.get(0)) {
			for(List<ShapeHistogram> shs_ref : histograms_reference) {
				if(sh_test.equals(shs_ref.get(0), 0.99)) {
					sh_test.setColor(shs_ref.get(0).getColor());
					Rectangle bb = sh_test.getBoundingBox();
					cp_test.setColor(shs_ref.get(0).getColor());
					cp_test.drawRect(bb.x, bb.y, bb.width, bb.height);
					IJ.log("matched");
				} else if (sh_test.equals(shs_ref.get(1), 0.06)) {
					sh_test.setColor(shs_ref.get(1).getColor());
					Rectangle bb = sh_test.getBoundingBox();
					cp_test.setColor(shs_ref.get(1).getColor());
					cp_test.drawRect(bb.x, bb.y, bb.width, bb.height);					
					IJ.log("matched");
				}
				else {
					IJ.log("not matched");
				}
			}
		}
		
		for(ShapeHistogram sh_test : histograms_test.get(1)) {
			for(List<ShapeHistogram> shs_ref : histograms_reference) {
				if(sh_test.equals(shs_ref.get(0), 0.99)) {
					sh_test.setColor(shs_ref.get(0).getColor());
					Rectangle bb = sh_test.getBoundingBox();
					cp_test.setColor(shs_ref.get(0).getColor());
					cp_test.drawRect(bb.x, bb.y, bb.width, bb.height);
					IJ.log("matched");
				} else if (sh_test.equals(shs_ref.get(1), 0.06)) {
					sh_test.setColor(shs_ref.get(1).getColor());
					Rectangle bb = sh_test.getBoundingBox();
					cp_test.setColor(shs_ref.get(1).getColor());
					cp_test.drawRect(bb.x, bb.y, bb.width, bb.height);					
					IJ.log("matched");
				}
				else {
					IJ.log("not matched");
				}
			}
		}*/
		
		
		(new ImagePlus("reference", cp_ref)).show();
		(new ImagePlus("test", cp_test)).show();
	}
}
