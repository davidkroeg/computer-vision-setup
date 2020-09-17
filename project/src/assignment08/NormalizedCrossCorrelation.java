package assignment08;

import ij.IJ;

public class NormalizedCrossCorrelation implements ScoreFunction {
	
	@Override
	public float[][] calculateScoreMap(float[][] _I, float[][] _R) {
		int rMax = _I.length;
		int sMax = _I[0].length;
		
		int iMax = _R.length;
		int jMax = _R[0].length;
		
		float[][] scoreMap = new float[rMax-iMax][sMax-jMax];
		
		for(int s = 0; s < sMax-jMax; s++) {
			for(int r = 0; r < rMax-iMax; r++) {
				float linearCrossCorr = 0;
				float localAverage = 0;
				float templateAverage = 0;
				
				for(int j = 0; j < jMax; j++) {
					for(int i = 0; i < iMax; i++) {
						linearCrossCorr	+= scoreMap[r][s] += Math.pow(_I[r+i][s+j] - _R[i][j], 2);
						localAverage	+= Math.pow(_I[r+i][s+j], 2);
						templateAverage += Math.pow(_R[i][j], 2);
					}
				}
				
				localAverage = (float) Math.sqrt(localAverage);
				templateAverage = (float) Math.sqrt(templateAverage);
				scoreMap[r][s] = linearCrossCorr / (localAverage * templateAverage);
				IJ.showProgress(s*rMax + r/sMax*rMax);
			}
		}
		
		return scoreMap;
	}

	@Override
	public String getName() {
		return "Normalized Cross Correlation";
	}

	@Override
	public boolean minimize() {
		return true;
	}
	
}
