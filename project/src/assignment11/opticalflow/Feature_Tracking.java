package assignment11.opticalflow;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.ShapeRoi;
import ij.plugin.filter.PlugInFilter;
import ij.process.ColorProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import imagingbook.pub.sift.SiftDescriptor;
import imagingbook.pub.sift.SiftDetector;
import imagingbook.pub.sift.SiftMatch;
import imagingbook.pub.sift.SiftMatcher;
import util.io.ImageUtils;

public class Feature_Tracking implements PlugInFilter {
	
	private final int circleRadius = 10;
	ImagePlus imp = null;
	
	
	@Override
	public int setup(String arg, ImagePlus imp) {
		this.imp = imp;
		return DOES_8G + ROI_REQUIRED + NO_CHANGES;
	}

	@Override
	public void run(ImageProcessor ip) {
		if(imp.getStackSize() < 2) {
			IJ.error("Stack with at least 2 images required!");
			return;
		}
		final int stackSize = imp.getStackSize();
		
		ImageStack stack = imp.getImageStack();
		ImageStack resultImageStack = new ImageStack();
		ImageProcessor R = stack.getProcessor(1);
		Rectangle roi = imp.getRoi().getBounds();
		
		FloatProcessor roi_fp = ImageUtils.crop(stack.getProcessor(1).convertToFloatProcessor(), roi);
		
		SiftDetector.Parameters params = new SiftDetector.Parameters();
		SiftDetector sd = new SiftDetector(roi_fp, params);
		List<SiftDescriptor> fs = sd.getSiftFeatures();
		
		if(fs.size() == 0) {
			IJ.error("No SIFT features found in selected region");
			return;
		}
		IJ.log("fs_roi size: " + fs.size());		
		
		List<SiftDescriptor> feature = new ArrayList<SiftDescriptor>();
		feature.add(fs.get(0));
		
		int featureX = (int)fs.get(0).getX()+roi.x;
		int featureY = (int)fs.get(0).getY()+roi.y;

		IJ.log("Descriptor: " + featureX + " " + featureY);
		ColorProcessor initialFp = stack.getProcessor(1).convertToColorProcessor();
		initialFp.setColor(Color.black);
		initialFp.drawOval(featureX-circleRadius, featureY-circleRadius, circleRadius*2, circleRadius*2);
		resultImageStack.addSlice(initialFp);
		
		for(int i = 1; i < stackSize; i++) {
			FloatProcessor i_current = stack.getProcessor(i).convertToFloatProcessor();
			SiftDetector sd_current = new SiftDetector(i_current, params);
			List<SiftDescriptor> fs_current = sd_current.getSiftFeatures();
			
			SiftMatcher currentSiftMatcher = new SiftMatcher(feature);
			List<SiftMatch> matches = currentSiftMatcher.matchDescriptors(fs_current);
			
			if(matches.size() == 0) {
				IJ.log("No match found on iteration: " + i);
			}
			else {
				SiftDescriptor d_current = matches.get(0).getDescriptor2();
				featureX = (int)d_current.getX();
				featureY = (int)d_current.getY();			
			}
			
			SiftDescriptor currentDescriptor = matches.get(0).getDescriptor2();
			featureX = (int)currentDescriptor.getX();
			featureY = (int)currentDescriptor.getY();
			
			ColorProcessor currentFp = stack.getProcessor(i).convertToColorProcessor();
			currentFp.setColor(Color.black);
			currentFp.drawOval(featureX-circleRadius, featureY-circleRadius, circleRadius*2, circleRadius*2);
			resultImageStack.addSlice(currentFp);
			
			feature.clear();
			feature.add(currentDescriptor);
			IJ.log("Finished image " + i + "/" + stackSize);
		}
		
		new ImagePlus("Feature Tracking", resultImageStack).show();

	}
	
	private ShapeRoi makeSiftMarker(SiftDescriptor d, double xo, double yo, Color col) {
		double x = d.getX() + xo; 
		double y = d.getY() + yo; 
		double scale = d.getScale();
		double orient = d.getOrientation();
		double sin = Math.sin(orient);
		double cos = Math.cos(orient);
		Path2D poly = new Path2D.Double();	
		poly.moveTo(x + (sin - cos) * scale, y - (sin + cos) * scale);
		//poly.lineTo(x, y);
		poly.lineTo(x + (sin + cos) * scale, y + (sin - cos) * scale);
		poly.lineTo(x, y);
		poly.lineTo(x - (sin - cos) * scale, y + (sin + cos) * scale);
		poly.lineTo(x - (sin + cos) * scale, y - (sin - cos) * scale);
		poly.closePath();
		ShapeRoi roi = new ShapeRoi(poly);
		roi.setStrokeWidth((float)3);
		roi.setStrokeColor(col);
		return roi;
	}
	
}
