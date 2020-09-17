package assignment09;

import java.awt.Color;
import java.awt.Font;
import java.awt.Shape;
import java.awt.geom.Path2D;
import java.awt.geom.QuadCurve2D;
import java.util.ArrayList;
import java.util.List;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.ShapeRoi;
import ij.gui.TextRoi;
import ij.plugin.filter.PlugInFilter;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import imagingbook.lib.image.ImageMapper;
import imagingbook.lib.interpolation.InterpolationMethod;
import imagingbook.pub.geometry.basic.Point;
import imagingbook.pub.geometry.mappings.Mapping2D;
import imagingbook.pub.geometry.mappings.linear.ProjectiveMapping2D;
import imagingbook.pub.sift.SiftDescriptor;
import imagingbook.pub.sift.SiftDetector;
import imagingbook.pub.sift.SiftMatch;
import imagingbook.pub.sift.SiftMatcher;

public class Image_Stitching implements PlugInFilter {


	static Color SeparatorColor = 	Color.black;
	static Color DescriptorColor1 = Color.green;
	static Color DescriptorColor2 = Color.green;
	static Color MatchLineColor = 	Color.magenta;
	static Color LabelColor = 		Color.yellow;
	static Font LabelFont = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
	
	static int NumberOfMatchesToShow = 25;
	static double MatchLineCurvature = 0.25;
	static double FeatureScale = 1.0;
	static double FeatureStrokewidth = 1.0;

	static boolean ShowFeatureLabels = true;
	
	ImagePlus imp = null;
	
	@Override
	public int setup(String arg, ImagePlus imp) {
		this.imp = imp;
		return DOES_8G + DOES_RGB + STACK_REQUIRED + NO_CHANGES;
	}

	@Override
	public void run(ImageProcessor ip) {
		if(imp.getStackSize() < 2) {
			IJ.error("Stack with at least 2 images required!");
			return;
		}
		
		ImageStack stack = imp.getImageStack();
		final int w = stack.getWidth();
		final int h = stack.getHeight();
		
		FloatProcessor imageA = stack.getProcessor(1).convertToFloatProcessor();
		FloatProcessor imageB = stack.getProcessor(2).convertToFloatProcessor();
		
		SiftDetector.Parameters params = new SiftDetector.Parameters();
		
		SiftDetector sdA = new SiftDetector(imageA, params);
		SiftDetector sdB = new SiftDetector(imageB, params);
		
		List<SiftDescriptor> featuresA = sdA.getSiftFeatures();
		List<SiftDescriptor> featuresB = sdB.getSiftFeatures();
		
		IJ.log("SIFT features image A: " + featuresA.size());
		IJ.log("SIFT features image B: " + featuresB.size());
		
		IJ.log("matching...");
		
		SiftMatcher siftMatcher = new SiftMatcher(featuresA);
		List<SiftMatch> matches = siftMatcher.matchDescriptors(featuresB);
		
		IJ.log("Matches count: " + matches.size());
		
		List<imagingbook.pub.geometry.basic.Point> m1 = new ArrayList<imagingbook.pub.geometry.basic.Point>();
		List<imagingbook.pub.geometry.basic.Point> m2 = new ArrayList<imagingbook.pub.geometry.basic.Point>();
		
		//split matches into two lists
		int count = 1;
		for(SiftMatch m : matches) {
			SiftDescriptor tempSdA = m.getDescriptor1();
			SiftDescriptor tempSdB = m.getDescriptor2();
			
			IJ.log("distance: " + m.getDistance());
			
			m1.add(imagingbook.pub.geometry.basic.Point.create(tempSdA.getX(), tempSdA.getY()));
			m2.add(imagingbook.pub.geometry.basic.Point.create(tempSdB.getX(), tempSdB.getY()));
			
			count++;
			if( count > NumberOfMatchesToShow) break;
		}
		
		imagingbook.pub.geometry.basic.Point[] m1a = new imagingbook.pub.geometry.basic.Point[m1.size()];
		imagingbook.pub.geometry.basic.Point[] m2a = new imagingbook.pub.geometry.basic.Point[m2.size()];
		
		m1a = m1.toArray(m1a);
		m2a = m2.toArray(m2a);
		
		Mapping2D inversMapping = ProjectiveMapping2D.fromPoints(m1a, m2a).getInverse();
		Mapping2D mapping = ProjectiveMapping2D.fromPoints(m1a, m2a);
		
		ImageMapper mapper = new ImageMapper(inversMapping, InterpolationMethod.Bicubic);
		
		//calculate new image dimensions
		Point[] extremaOld = new Point[4];
		Point[] extremaNew = new Point[4];
		
		extremaOld[0] = Point.create(0,   0);
		extremaOld[1] = Point.create(w, 0);
		extremaOld[2] = Point.create(0,   h);
		extremaOld[3] = Point.create(w, h);
		
		extremaNew = mapping.applyTo(extremaOld);
		
		double xMin = Double.MAX_VALUE;
		double xMax = Double.MIN_VALUE;
		double yMin = Double.MAX_VALUE;
		double yMax = Double.MIN_VALUE;
		for(int i = 0; i < 4; i++) {
			double tempX = extremaNew[i].getX();
			double tempY = extremaNew[i].getY();
			
			if(tempX < xMin) xMin = tempX;
			if(tempX > xMax) xMax = tempX;
			if(tempY < yMin) yMin = tempY;
			if(tempY > yMax) yMax = tempY;
		}

		double xMinAbs = Math.abs(xMin);
		double yMinAbs = Math.abs(yMin);
	
		double widthNewImage = w;
		double heightNewImage = h;
		
		int xOffset = 0;
		int yOffset = 0;
		
		if(xMin < 0) {
			widthNewImage += xMinAbs;
			xOffset = (int)Math.ceil(xMinAbs);
		}
		else 
		if(xMax > w) {
			widthNewImage += xMax-w;
		}
		
		if(yMin < 0) {
			heightNewImage += yMinAbs;
			yOffset = (int)Math.ceil(yMinAbs);
		}
		if(yMax > h) {
			heightNewImage += yMax-h;
		}
		
		IJ.log("New image dimensions: " + widthNewImage + " " + heightNewImage);
		
		FloatProcessor I_new = new FloatProcessor((int)Math.ceil(widthNewImage), (int)Math.ceil(heightNewImage));
		I_new.insert(imageA, xOffset, yOffset);
		
		mapper.map(I_new);
		
		for(int y = 0; y < h; y++) {
			for(int x = 0; x < w; x++) {
				float pixelOld = I_new.getPixelValue(x+xOffset, y+yOffset);
				float pixelNew = (imageB.getf(x, y) + pixelOld)/2.0f;
				
				I_new.setf(x+xOffset, y+yOffset, pixelNew);
			}
		}
		
		new ImagePlus("stitched image", I_new).show();
	}

	private ShapeRoi makeStraightLine(double x1, double y1, double x2, double y2, Color col) {
		Path2D poly = new Path2D.Double();
		poly.moveTo(x1, y1);
		poly.lineTo(x2, y2);
		ShapeRoi roi = new ShapeRoi(poly);
		roi.setStrokeWidth((float)FeatureStrokewidth);
		roi.setStrokeColor(col);
		return roi;
	}

	private ShapeRoi makeSiftMarker(SiftDescriptor d, double xo, double yo, Color col) {
		double x = d.getX() + xo; 
		double y = d.getY() + yo; 
		double scale = FeatureScale * d.getScale();
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
		roi.setStrokeWidth((float)FeatureStrokewidth);
		roi.setStrokeColor(col);
		return roi;
	}

	private ShapeRoi makeConnectingLine(SiftDescriptor f1, SiftDescriptor f2, double xo, double yo, Color col) {
		double x1 = f1.getX(); 
		double y1 = f1.getY();
		double x2 = f2.getX() + xo; 
		double y2 = f2.getY() + yo;
		double dx = x2 - x1;
		double dy = y2 - y1;
		double ctrlx = (x1 + x2) / 2 - MatchLineCurvature * dy;
		double ctrly = (y1 + y2) / 2 + MatchLineCurvature * dx;
		Shape curve = new QuadCurve2D.Double(x1, y1, ctrlx, ctrly, x2, y2);
		ShapeRoi roi = new ShapeRoi(curve);
		roi.setStrokeWidth((float)FeatureStrokewidth);
		roi.setStrokeColor(col);
		return roi;
	}

	private TextRoi makeSiftLabel(SiftDescriptor d, double xo, double yo, String text) {
		double x = d.getX() + xo; 
		double y = d.getY() + yo; 
		TextRoi roi = new TextRoi((int)Math.rint(x), (int)Math.rint(y), text, LabelFont);
		roi.setStrokeColor(LabelColor);
		return roi;
	}
	
}
