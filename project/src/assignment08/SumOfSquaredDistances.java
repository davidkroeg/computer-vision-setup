package assignment08;

import ij.IJ;

public class SumOfSquaredDistances implements ScoreFunction {

	@Override
	public float[][] calculateScoreMap(float[][] _I, float[][] _R) {
		int rMax = _I.length;
		int sMax = _I[0].length;
		
		int iMax = _R.length;
		int jMax = _R[0].length;
		
		float[][] scoreMap = new float[rMax-iMax][sMax-jMax];
		
		for(int s = 0; s < sMax-jMax; s++) {
			for(int r = 0; r < rMax-iMax; r++) {
				for(int j = 0; j < jMax; j++) {
					for(int i = 0; i < iMax; i++) {
						scoreMap[r][s] += Math.pow(_I[r+i][s+j] - _R[i][j], 2);
					}
				}
				scoreMap[r][s] = (float) Math.sqrt(scoreMap[r][s]);
				IJ.showProgress(s*rMax + r/sMax*rMax);
			}
		}
		
		return scoreMap;
	}

	@Override
	public String getName() {
		return "Sum Of Squared Distances";
	}

	@Override
	public boolean minimize() {
		return true;
	}
	
}
