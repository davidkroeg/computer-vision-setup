package assignment07;

import static ij.process.Blitter.ADD;
import static ij.process.Blitter.SUBTRACT;

import java.util.HashMap;

import ij.plugin.filter.Convolver;
import ij.plugin.filter.GaussianBlur;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import imagingbook.lib.math.Matrix;

/**
 * This class defines static methods for calculating Laws' texture
 * maps for a given image.
 * 
 * @author WB
 * @version 2020/04/22
 */
public abstract class LawsTextures {
	
	public static double PREPROCESSING_BLUR_SIGMA = 4.0;
	public static double ENERGY_MAP_SMOOTH_SIGMA = 3.0;
	public static boolean NORMALIZE_ENERGY_MAPS = true;
	
	// individual Law's texture filters (1D)
	private static final float[] HL = { 1,  4, 6,  4,  1};
	private static final float[] HE = {-1, -2, 0,  2,  1};
	private static final float[] HS = {-1,  0, 2,  0, -1};
	private static final float[] HR = { 1, -4, 6, -4,  1};


	/**
	 * Calculates Laws' texture energy maps for the given input image.
	 * Each energy map is contained in a {@link FloatProcessor} instance.
	 * Returns a hash map which associates names and energy maps.
	 * @param ip the input image (of any type)
	 * @return the energy maps contained in a hash map
	 */
	public static HashMap<String, FloatProcessor> makeTextureEnergyMaps(ImageProcessor ip) {
		FloatProcessor ipPrep = ip.convertToFloatProcessor(); 
		FloatProcessor ipAvg =  ip.convertToFloatProcessor();

		// calculate the local average (ipAvg) using a Gaussian blur:   	
		GaussianBlur gb = new GaussianBlur();
		gb.blurGaussian(ipAvg, PREPROCESSING_BLUR_SIGMA);

		// subtract average from original image (ipPrep <- ipPrep - ipAvg)
		// this is effectively a high-pass filter!
		ipPrep.copyBits(ipAvg, 0, 0, SUBTRACT);

		// create all 16 energy maps
		//    	FloatProcessor I_LL = makeTextureEnergy (ipPrep, HL, HL);	// not used
		FloatProcessor I_LE = makeTextureEnergy (ipPrep, HL, HE);
		FloatProcessor I_LS = makeTextureEnergy (ipPrep, HL, HS);
		FloatProcessor I_LR = makeTextureEnergy (ipPrep, HL, HR);

		FloatProcessor I_EL = makeTextureEnergy (ipPrep, HE, HL);
		FloatProcessor I_EE = makeTextureEnergy (ipPrep, HE, HE);
		FloatProcessor I_ES = makeTextureEnergy (ipPrep, HE, HS);
		FloatProcessor I_ER = makeTextureEnergy (ipPrep, HE, HR);

		FloatProcessor I_SL = makeTextureEnergy (ipPrep, HS, HL);
		FloatProcessor I_SE = makeTextureEnergy (ipPrep, HS, HE);
		FloatProcessor I_SS = makeTextureEnergy (ipPrep, HS, HS);
		FloatProcessor I_SR = makeTextureEnergy (ipPrep, HS, HR);

		FloatProcessor I_RL = makeTextureEnergy (ipPrep, HE, HL);
		FloatProcessor I_RE = makeTextureEnergy (ipPrep, HE, HE);
		FloatProcessor I_RS = makeTextureEnergy (ipPrep, HE, HS);
		FloatProcessor I_RR = makeTextureEnergy (ipPrep, HE, HR);

		I_EL.copyBits(I_LE, 0, 0, ADD); // I_EL <- I_EL + I_LE
		I_SL.copyBits(I_LS, 0, 0, ADD);
		I_RL.copyBits(I_LR, 0, 0, ADD);
		I_SE.copyBits(I_ES, 0, 0, ADD);
		I_RE.copyBits(I_ER, 0, 0, ADD);
		I_RS.copyBits(I_SR, 0, 0, ADD);

		// Collect energy maps and associate names in a hashmap for convenience:
		HashMap<String, FloatProcessor> eMaps = new HashMap<>();
		eMaps.put("EE", I_EE);
		eMaps.put("SS", I_SS);
		eMaps.put("RR", I_RR);
		eMaps.put("EL", I_EL);
		eMaps.put("SL", I_SL);
		eMaps.put("RL", I_RL);
		eMaps.put("SE", I_SE);
		eMaps.put("RE", I_RE);
		eMaps.put("RS", I_RS);

		if (NORMALIZE_ENERGY_MAPS) {
			for (FloatProcessor p : eMaps.values()) {
				normalize (p);
			}
		}

		return eMaps;
	}

	private static FloatProcessor makeTextureEnergy(FloatProcessor orig, float[] h_hor, float[] h_ver) {
		FloatProcessor p = (FloatProcessor) orig.duplicate();
		convolve1h(p, h_hor);
		convolve1v(p, h_ver);
		// take absolute values
		abs(p);
		GaussianBlur gb = new GaussianBlur();
		gb.blurGaussian(p, ENERGY_MAP_SMOOTH_SIGMA);
		return p;
	}

	private static FloatProcessor convolve1h(FloatProcessor p, float[] h) {
		Convolver conv = new Convolver();
		conv.setNormalize(false);
		conv.convolve(p, h, 1, h.length);
		return p;
	}

	private static FloatProcessor convolve1v(FloatProcessor p, float[] h) {
		Convolver conv = new Convolver();
		conv.setNormalize(false);
		conv.convolve(p, h, h.length, 1);
		return p;
	}

	private static void abs(FloatProcessor p) {
		float[] pixels = (float[]) p.getPixels();
		for (int i = 0; i < pixels.length; i++) {
			float pix = pixels[i];
			if (pix < 0f)
				pixels[i] = -pix;
		}
	}

	private static void normalize(FloatProcessor fp) {
		float[] pixels = (float[]) fp.getPixels();
		float max = Matrix.max(pixels);
		fp.multiply(1.0 / max);
	}
}
