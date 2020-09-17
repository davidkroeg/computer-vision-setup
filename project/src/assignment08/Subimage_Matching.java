package assignment08;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.plugin.filter.PlugInFilter;
import ij.process.ColorProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

public class Subimage_Matching implements PlugInFilter {

	private final int numberOfMatches = 2;
	private List<Rectangle> matches = new ArrayList<Rectangle>();
	
	@Override
	public int setup(String arg, ImagePlus imp) {
		return DOES_8G + ROI_REQUIRED + NO_CHANGES;
	}

	@Override
	public void run(ImageProcessor I) {
		
		//marked reference image
		ImageProcessor R = I.crop();
		new ImagePlus("R", R).show();
		
		
		float[][] Ia = I.getFloatArray();
		float[][] Ra = R.getFloatArray();
				
		
		ScoreFunction sosd = new SumOfSquaredDistances();
		ScoreFunction lcc = new LinearCrossCorrelation();
		ScoreFunction ncc = new NormalizedCrossCorrelation();
		
		
		matchSubImages(I, R, sosd, numberOfMatches);
		matchSubImages(I, R, lcc, numberOfMatches);
		matchSubImages(I, R, ncc, numberOfMatches);
		
		
		
		detectRepetitivePatterns(I, R, sosd, 0.2f);
		detectRepetitivePatterns(I, R, sosd, 0.4f);
		detectRepetitivePatterns(I, R, sosd, 0.6f);
		detectRepetitivePatterns(I, R, sosd, 0.8f);
		detectRepetitivePatterns(I, R, sosd, 1.0f);
		detectRepetitivePatterns(I, R, sosd, 2.0f);
		

		/*
		detectRepetitivePatterns(I, R, lcc, 0.2f);
		detectRepetitivePatterns(I, R, lcc, 0.4f);
		detectRepetitivePatterns(I, R, lcc, 0.6f);
		detectRepetitivePatterns(I, R, lcc, 0.8f);
		detectRepetitivePatterns(I, R, lcc, 1.0f);
		detectRepetitivePatterns(I, R, lcc, 2.0f);
		*/

		/*
		detectRepetitivePatterns(I, R, ncc, 0.2f);
		detectRepetitivePatterns(I, R, ncc, 0.4f);
		detectRepetitivePatterns(I, R, ncc, 0.6f);
		detectRepetitivePatterns(I, R, ncc, 0.8f);
		detectRepetitivePatterns(I, R, ncc, 1.0f);
		detectRepetitivePatterns(I, R, ncc, 2.0f);
		detectRepetitivePatterns(I, R, ncc, 3.0f);
		detectRepetitivePatterns(I, R, ncc, 4.0f);
		*/
		
		//Create distance map
		long startTime = 0;
		long elapsedTime = 0;
		float elapsedTimeSeconds = 0;
		
		
		startTime = System.nanoTime();
		
		float[][] Da1 = new SumOfSquaredDistances().calculateScoreMap(Ia, Ra);
		FloatProcessor Df1 = new FloatProcessor(Da1);
		
		for(int i = 0; i < numberOfMatches; i++) {
			Point pMin = getMinPoint(Da1);
			if(pMin == null)
			{
				IJ.log("error finding min point");
				return;
			}
			
			matches.add(new Rectangle(pMin.x, pMin.y, R.getWidth(), R.getHeight()));
			setROI(Da1, new Rectangle(pMin.x-R.getWidth()/2, pMin.y-R.getHeight()/2, R.getWidth(), R.getHeight()), (float)Df1.getMax());
		}
		
//		I_cp.setColor(Color.green);
//		for(Rectangle r : matches) {
//			I_cp.drawRoi(new Roi(r));
//		}
		
		FloatProcessor resultFp = new FloatProcessor(Da1);
		new ImagePlus("sum of squared distances2", resultFp).show();
//		new ImagePlus("found matches", I_cp).show();
		
		elapsedTime = System.nanoTime() - startTime;
		elapsedTimeSeconds = elapsedTime/1000000000;
		
		IJ.log("finished: sum of squared distances");
		IJ.log("execution time: " + elapsedTimeSeconds);
		

		/*
		startTime = System.nanoTime();
		
		float[][] Da2 = new LinearCrossCorrelation().calculateScoreMap(Ia, Ra);
		FloatProcessor Df2 = new FloatProcessor(Da2);
		new ImagePlus("linear crosscorrelation", Df2).show();
		
		elapsedTime = System.nanoTime() - startTime;
		elapsedTimeSeconds = elapsedTime/1000000000;
		
		IJ.log("finished: linear cosscorrelation");
		IJ.log("execution time: " + elapsedTimeSeconds);
		
		
		startTime = System.nanoTime();
		
		float[][] Da3 = new NormalizedCrossCorrelation().calculateScoreMap(Ia, Ra);		
		FloatProcessor Df3 = new FloatProcessor(Da3);
		new ImagePlus("normalized crosscorrelation", Df3).show();
		
		elapsedTime = System.nanoTime() - startTime;
		elapsedTimeSeconds = elapsedTime/1000000000;
		
		IJ.log("finished: normalized crosscorrelation");
		IJ.log("execution time: " + elapsedTimeSeconds);
		*/
		
		//MaximumFinder MF = new MaximumFinder();
		//int[] minima = MaximumFinder.findMinima((double[])Df1.array, 0.8, true);
		//new ImagePlus("maxima", minima).show();
	}
	
	public Point getMinPoint(FloatProcessor fp) {
		double min = fp.getMin();
		IJ.log("min: " + min);
		int w = fp.getWidth();
		int h = fp.getHeight();
		
		for(int y = 0; y < h; y++) {
			for(int x = 0; x < w; x++) {
				if(Double.compare(fp.get(x, y), min) == 0) {
					return new Point(x, y);
				}
			}
		}
		return null;
	}
	
	public Point getMinPoint(float[][] fa) {
		int w = fa.length;
		int h = fa[0].length;
		
		float min = Float.MAX_VALUE;
		int xMin = 0;
		int yMin = 0;
		
		for(int y = 0; y < h; y++){
			for(int x = 0; x < w; x++) {
				if(fa[x][y] < min) {
					min = fa[x][y];
					xMin = x;
					yMin = y;
				}
			}
		}
		
		if(Float.compare(min, Float.MAX_VALUE) == 0) {
			return null;
		}
		
		return new Point(xMin, yMin);
	}
	
	public Point getMaxPoint(float[][] fa) {
		int w = fa.length;
		int h = fa[0].length;
		
		float max = 0;
		int xMax = 0;
		int yMax = 0;
		
		for(int y = 0; y < h; y++){
			for(int x = 0; x < w; x++) {
				if(fa[x][y] > max) {
					max = fa[x][y];
					xMax = x;
					yMax = y;
				}
			}
		}
		
		if(Float.compare(max, Float.MAX_VALUE) == 0) {
			return null;
		}
		
		return new Point(xMax, yMax);
	}
	
	public void setROI(float[][] fa, Rectangle rec, float val) {
		for(int i_h = 0; i_h < rec.height; i_h++) {
			for(int i_w = 0; i_w < rec.width; i_w++) {
				if(rec.x+i_w > 0 && rec.x+i_w < fa.length && rec.y+i_h > 0 && rec.y+i_h < fa[0].length) {
					fa[rec.x+i_w][rec.y+i_h] = val;
				}
			}
		}
	}
	
	public FloatProcessor setROI(FloatProcessor fp, Rectangle rec, float val) {
		Roi roi = new Roi(rec);
		fp.setColor(val);
		fp.fill(roi);
		
		return fp;
	}

	public void matchSubImages(ImageProcessor _I, ImageProcessor _R, ScoreFunction _scoreFunction, int _numberOfMatches) {
		
		//convert input ImageProcessor to ColorProcessor to mark found matches
		ColorProcessor I_cp = _I.convertToColorProcessor();
		I_cp.setColor(Color.green);

		float[][] Ia = _I.getFloatArray();
		float[][] Ra = _R.getFloatArray();
		
		long startTime = 0;
		long elapsedTime = 0;
		float elapsedTimeSeconds = 0;
						
		startTime = System.nanoTime();
		
		//distance map
		float[][] im_internal = _scoreFunction.calculateScoreMap(Ia, Ra);
		float[][] im_external = deepCopy(im_internal);
		
		elapsedTime = System.nanoTime() - startTime;
		elapsedTimeSeconds = elapsedTime/1000000000;
		
		IJ.log("finished: " + _scoreFunction.getName());
		IJ.log("execution time: " + elapsedTimeSeconds);
		
		
		//value to replace the pixels inside the surrounding area of an already found match
		float replacement_value;
		if(_scoreFunction.minimize()) {
			replacement_value = (float)new FloatProcessor(im_internal).getMax();
		}
		else {
			replacement_value = (float)new FloatProcessor(im_internal).getMin();
		}
		
		//repeat depending on the number of required matches
		for(int i = 0; i < _numberOfMatches; i++) {
			Point pMatch;
			if(_scoreFunction.minimize()) {
				pMatch = getMinPoint(im_internal);
			}
			else {
				pMatch = getMaxPoint(im_internal);
			}
			
			if(pMatch == null)
			{
				IJ.log("error finding matching point");
				return;
			}
			
			IJ.log("min/max found at:");
			IJ.log("x: " + pMatch.x);
			IJ.log("y: " + pMatch.y);
			
			//mark match
			I_cp.drawRoi(new Roi(new Rectangle(pMatch.x, pMatch.y, _R.getWidth(), _R.getHeight())));
			
			//set pixels surrounding found match to remove them as possible matches in the future
			setROI(im_internal, new Rectangle(pMatch.x-_R.getWidth()/2, pMatch.y-_R.getHeight()/2, _R.getWidth(), _R.getHeight()), replacement_value);
		}
		
		new ImagePlus( _scoreFunction.getName() + ": intensity image", new FloatProcessor(im_external)).show();
		new ImagePlus( _scoreFunction.getName() + ": matched subimages " , I_cp).show();
	}
	
	public void detectRepetitivePatterns(ImageProcessor _I, ImageProcessor _R, ScoreFunction _scoreFunction, float _tolerance) {
		ColorProcessor I_cp = _I.convertToColorProcessor();
		I_cp.setColor(Color.green);
				
		float[][] Ia = _I.getFloatArray();
		float[][] Ra = _R.getFloatArray();
		
		float[][] imInternal = _scoreFunction.calculateScoreMap(Ia, Ra);
		float[][] imExternal = deepCopy(imInternal);
		
		float replacement_value;
		if(_scoreFunction.minimize()) {
			replacement_value = (float)new FloatProcessor(imInternal).getMax();
		}
		else {
			replacement_value = (float)new FloatProcessor(imInternal).getMin();
		}
		
		//get best match (selected roi)
		Point pointMatch;
		if(_scoreFunction.minimize()) {
			pointMatch = getMinPoint(imInternal);
		}
		else {
			pointMatch = getMaxPoint(imInternal);
		}
		
		if(pointMatch == null)
		{
			IJ.log("error finding matching point");
			return;
		}
		
		I_cp.drawRoi(new Roi(new Rectangle(pointMatch.x, pointMatch.y, _R.getWidth(), _R.getHeight())));
		setROI(imInternal, new Rectangle(pointMatch.x-_R.getWidth()/2, pointMatch.y-_R.getHeight()/2, _R.getWidth(), _R.getHeight()), replacement_value);
		
		
		//first occurance of pattern		
		if(_scoreFunction.minimize()) {
			pointMatch = getMinPoint(imInternal);
		}
		else {
			pointMatch = getMaxPoint(imInternal);
		}
		
		if(pointMatch == null)
		{
			IJ.log("error finding matching point");
			return;
		}
		
		float firstMatchValue = imInternal[pointMatch.x][pointMatch.y];
		
		I_cp.drawRoi(new Roi(new Rectangle(pointMatch.x, pointMatch.y, _R.getWidth(), _R.getHeight())));
		setROI(imInternal, new Rectangle(pointMatch.x-_R.getWidth()/2, pointMatch.y-_R.getHeight()/2, _R.getWidth(), _R.getHeight()), replacement_value);
	

		if(_scoreFunction.minimize()) {
			pointMatch = getMinPoint(imInternal);
		}
		else {
			pointMatch = getMaxPoint(imInternal);
		}
		
		if(pointMatch == null)
		{
			IJ.log("error finding matching point");
			return;
		}
		
		while(imInternal[pointMatch.x][pointMatch.y] < firstMatchValue*(1+_tolerance)) {
			I_cp.drawRoi(new Roi(new Rectangle(pointMatch.x, pointMatch.y, _R.getWidth(), _R.getHeight())));
			setROI(imInternal, new Rectangle(pointMatch.x-_R.getWidth()/2, pointMatch.y-_R.getHeight()/2, _R.getWidth(), _R.getHeight()), replacement_value);
		

			if(_scoreFunction.minimize()) {
				pointMatch = getMinPoint(imInternal);
			}
			else {
				pointMatch = getMaxPoint(imInternal);
			}
			
			if(pointMatch == null)
			{
				IJ.log("error finding matching point");
				return;
			}
		}
		
		new ImagePlus( _scoreFunction.getName() + ": intensity image", new FloatProcessor(imExternal)).show();
		new ImagePlus( _scoreFunction.getName() + ": repetitive patterns. tolerance: " + _tolerance, I_cp).show();
	}
	
	
	public float[][] deepCopy(float[][] matrix) {
	    return java.util.Arrays.stream(matrix).map(el -> el.clone()).toArray($ -> matrix.clone());
	}
	
}
