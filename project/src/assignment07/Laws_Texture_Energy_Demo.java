package assignment07;

import java.awt.Point;
import java.util.HashMap;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealVector;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Roi;
import ij.io.LogStream;
import ij.plugin.filter.PlugInFilter;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import imagingbook.pub.threshold.global.GlobalThresholder;
import imagingbook.pub.threshold.global.OtsuThresholder;

/**
 * This ImageJ plugin calculates Law's texture energy maps for the current
 * image and shows the combined results as an image stack.
 * 
 * @author W. Burger
 * @version 2020/04/21
 */
public class Laws_Texture_Energy_Demo implements PlugInFilter {
	
	static {
		LogStream.redirectSystem();		// to enable System.out.println() etc.
	}

	private ImagePlus imp;
	private int W, H;

	public int setup(String arg, ImagePlus imp) {
		this.imp = imp;
		return DOES_ALL + NO_CHANGES + ROI_REQUIRED;	// + ROI_REQUIRED  (to enforce an ROI)
	}

	//--------------------------------------------------------------------

	public void run(ImageProcessor ip) {
		W = ip.getWidth();
		H = ip.getHeight();

		HashMap<String, FloatProcessor> eMaps = LawsTextures.makeTextureEnergyMaps(ip);
		showAsStack(eMaps);
		
		// Hint: how to obtain an array of FloatProcessors
		FloatProcessor[] emA = eMaps.values().toArray(new FloatProcessor[0]);
		
		// Hint: how to obtain the ROI and iterate over all contained points
		Roi roi = imp.getRoi();
		
		float[] reference = new float[9];
		
		//get mean of all feature vectors in the region for reference feature vector xR
		int count = 0;
		for (Point p : roi) {
			int u = p.x;
			int v = p.y;
			int index = 0;
			for(FloatProcessor fp : emA) {
				reference[index] += Float.intBitsToFloat(fp.getPixel(u, v));
				index++;
			}
			count++;
		}
		
		for(int i = 0; i < reference.length; i++) {
			reference[i] = reference[i] / count;
		}
		//reference vector is done here
		
		// create distance map
		FloatProcessor fpDistance = (FloatProcessor) ip.convertToFloat();
		
		for (int u = 0; u < W; u++) {
			for (int v = 0; v < H; v++) {
				//get the feature vector of the pixel
				float[] localFeatureVector = getLocalFeatureVector(emA, u, v);
				//calculate the distance to the reference vector
				float distance = getL2Distance(reference, localFeatureVector);
				//put the value of the distance as pixel at that position
				fpDistance.putPixel(u, v, Float.floatToIntBits(distance));
			}
		}

		(new ImagePlus("Distance map", fpDistance)).show();

		//do thresholding
		ByteProcessor bp = (ByteProcessor) fpDistance.duplicate().convertToByteProcessor();

		GlobalThresholder thr = new OtsuThresholder();
		int q = thr.getThreshold((ByteProcessor) bp);
		if (q < 0) {
			IJ.error("no threshold found");
			return;
		}
		bp.threshold(q);
		
		(new ImagePlus("With Thresholding", bp)).show();
	}

	// -----------------------------------------------------

	private void showAsStack(HashMap<String, FloatProcessor> eMaps) {
		ImageStack stack = new ImageStack(W, H);
		
		for (String name : eMaps.keySet()) {
			stack.addSlice(name, eMaps.get(name));
		}

		ImagePlus stackIm = new ImagePlus("TextureEnergy of " + imp.getTitle(), stack);
		stackIm.setDisplayRange(0.0, 1.0);
		stackIm.show();
	}
	
	private float[] getLocalFeatureVector(FloatProcessor[] emA, int u, int v) {
		float[] localFeatureVector = new float[9];
		
		int index = 0;
		for(FloatProcessor fp : emA) {
			localFeatureVector[index] = Float.intBitsToFloat(fp.getPixel(u, v));
			index++;
		}
		
		return localFeatureVector;
	}
	
	private float getL2Distance(float[] referenceVector, float[] localVector) {
		double squaredDistance = 0;
		
		for(int i = 0; i < referenceVector.length; i++) {
			squaredDistance += Math.pow((double)localVector[i] - (double)referenceVector[i], 2);
		}
		
		double l2 = Math.sqrt(squaredDistance);
		return (float) l2;
	}
	
	private float getL1Distance(float[] referenceVector, float[] localVector) {
		double distance = 0;
		
		for(int i = 0; i < referenceVector.length; i++) {
			distance += (double)localVector[i] - (double)referenceVector[i];
		}
		
		double l1 = Math.abs(distance);
		return (float) l1;
	}

}
