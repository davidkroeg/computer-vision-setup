package assignment07;

import static ij.process.Blitter.SUBTRACT;

import ij.ImagePlus;
import ij.plugin.filter.GaussianBlur;
import ij.plugin.filter.PlugInFilter;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

public class Gaussian_Blur_Highpass_Demo implements PlugInFilter {

	static double sigma = 10;	// width of Gaussian blur

	public int setup(String arg0, ImagePlus arg1) {
		return DOES_8G;
	}

	public void run(ImageProcessor ip) {
		// create 2 FloatProcessors (duplicates of ip):
		FloatProcessor fpOrig = (FloatProcessor) ip.convertToFloat();
		FloatProcessor fpBlur = (FloatProcessor) fpOrig.duplicate();

		// apply a Gaussian blur with specified sigma:
		GaussianBlur gb = new GaussianBlur();
		gb.blurGaussian(fpBlur, sigma, sigma, 0.02);

		// subtract the blurred image from the original:
		fpOrig.copyBits(fpBlur, 0, 0, SUBTRACT);

		// recalculate the limiting values (for display only):
		fpOrig.resetMinAndMax();
		fpBlur.resetMinAndMax();

		// display the 2 images:
		(new ImagePlus("Blurred image", fpBlur)).show();
		(new ImagePlus("High-Pass image", fpOrig)).show();
	}
}
