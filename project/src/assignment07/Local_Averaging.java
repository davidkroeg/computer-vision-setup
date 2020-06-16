package assignment07;

import static ij.process.Blitter.SUBTRACT;

import ij.IJ;
import ij.ImagePlus;
import ij.io.LogStream;
import ij.plugin.filter.PlugInFilter;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

public class Local_Averaging implements PlugInFilter {

	static float window = 15;	// window size of local averaging

	static {
		LogStream.redirectSystem();		// to enable System.out.println() etc.
	}
	
	public int setup(String arg0, ImagePlus arg1) {
		return DOES_8G;
	}

	public void run(ImageProcessor ip) {
		final int w = ip.getWidth();
		final int h = ip.getHeight();
		
		// create 2 FloatProcessors (duplicates of ip):
		FloatProcessor fpOrig = (FloatProcessor) ip.convertToFloat();
		FloatProcessor fpBlur = (FloatProcessor) fpOrig.duplicate();

		
		
		//do local averaging 
		for (int u = 0; u < w; u++) {
			for (int v = 0; v < h; v++) {
				simpleSmoothing(fpBlur, u, v);
			}
		}

		// subtract the blurred image from the original:
		fpOrig.copyBits(fpBlur, 0, 0, SUBTRACT);

		// recalculate the limiting values (for display only):
		fpOrig.resetMinAndMax();
		fpBlur.resetMinAndMax();

		// display the 2 images:
		(new ImagePlus("Blurred image", fpBlur)).show();
		(new ImagePlus("High-Pass image", fpOrig)).show();
	}
	
	private void simpleSmoothing(FloatProcessor fp, int u, int v) {
		float newValue = 0;
		for(int i = -7; i <= 7; i++) {
			for(int j = -7; j <= 7; j++) {
				newValue += Float.intBitsToFloat(fp.getPixel(u + i, v + j));
			}
		}
		
		newValue = newValue/(window*window);
		
		fp.putPixel(u, v, Float.floatToIntBits(newValue));
	}
}
