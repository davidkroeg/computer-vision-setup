package tools;

import ij.IJ;
import ij.plugin.PlugIn;
import imagingbook.Info;
import imagingbook.lib.util.FileUtils;

/**
 * A simple ImageJ plugin for testing the 'imagingbook' installation.
 * @author W. Burger
 * @version 2016/03/23
 */
public class Test_Installation implements PlugIn {

	public void run(String arg0) {
		IJ.log("Executing plugin ...... " + this.getClass().getName());
		IJ.log("Operating system ...... " + System.getProperty("os.name") + " / " +
				System.getProperty("sun.arch.data.model") + " bits");
		IJ.log("Java version .......... " + System.getProperty("java.version"));
		IJ.log("Java runtime .......... " + System.getProperty("java.runtime.version"));
		IJ.log("Java VM ............... " + System.getProperty("java.vm.version"));
		IJ.log("ImageJ version ........ " + IJ.getFullVersion());
		
		try {
			IJ.log("imagingbook location .. " + FileUtils.getClassPath(Info.class));
			IJ.log("imagingbook version ... " + Info.getVersionInfo());
			IJ.log("imagingbook installation seems to be running OK.");
		} catch (Exception e) {
			IJ.log("imagingbook libary not found:");
			IJ.log("make sure 'imagingbook-common.jar' is placed in the ImageJ/jars/ folder!");
		}
	}

}
