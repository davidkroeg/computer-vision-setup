package assignment08;

import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

/**
 * Demo plugin which extracts and displays a subimage using the selected ROI.
 * Also demonstrates the conversion of images to 2D float-arrays and back.
 * @author WB
 *
 */
public class Crop_Demo implements PlugInFilter {

	@Override
	public int setup(String arg, ImagePlus imp) {
		return DOES_8G + ROI_REQUIRED + NO_CHANGES;
	}

	@Override
	public void run(ImageProcessor I) {
		
		// extract and show the reference image
		ImageProcessor R = I.crop();
		new ImagePlus("R", R).show();
		
		// extract pixel data as 2D float arrays
		float[][] Ia = I.getFloatArray();
		float[][] Ra = R.getFloatArray();
		
		// create a FloatProcessor from a 2D float array
		FloatProcessor I2 = new FloatProcessor(Ia);
		new ImagePlus("I2", I2).show();
	}

}
