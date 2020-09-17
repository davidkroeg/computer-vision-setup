package assignment08;

public interface ScoreFunction {
	public float[][] calculateScoreMap(float[][] _I, float[][] _R);
	public String getName();
	public boolean minimize();
}
