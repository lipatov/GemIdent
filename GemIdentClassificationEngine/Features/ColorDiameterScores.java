package GemIdentClassificationEngine.Features;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import GemIdentImageSets.ImageAndScoresBank;
import GemIdentImageSets.ImageSetInterface;
import GemIdentOperations.Run;
import GemIdentTools.Geometry.Rings;
import GemIdentTools.Geometry.Diameters;
import GemIdentTools.Matrices.IntMatrix;

public class ColorDiameterScores extends DatumFeatureSet {
	
	/** A map from color name --> intensity values for each pixel, for this image */
	private HashMap<String, IntMatrix> color_scores;
	/** radius of the circle within which to compute diameter scores */
	private int R;
	/** endpoints of the diameters */
	private ArrayList<Point> diameter_endpoints;
	private Set<String> color_names;
	
	@Override
	public void InitializeForRun(ImageSetInterface imageSetInterface) {
//		System.out.println("InitializeDataForRun()  " + imageSetInterface);	
		color_names = imageSetInterface.getFilterNames();
		R = Run.it.getMaxPhenotypeRadiusPlusMore(null);
		if (diameter_endpoints == null) {
			// generate diameter endpoints in the first two quadrants
			diameter_endpoints = new ArrayList<Point>();
			for (Point t1 : Rings.getRing(R)) {
				if (((t1.y>=0)&&(t1.x>0)) || ((t1.y>0)&&(t1.x<=0))) {
					diameter_endpoints.add(t1);
				}
			}
		}
		num_features = diameter_endpoints.size() * color_names.size();
	}	

	@Override
	public void InitializeDataForImage(String filename) {
		color_scores = ImageAndScoresBank.getOrAddScores(filename);		
	}

	@Override
	public void BuildFeaturesIntoRecord(Point t, double[] record, int p_0) {
		int Lo=0;
		for (String color : color_scores.keySet()){			
			for (Point t1 : diameter_endpoints){
				record[Lo] = ComputeDiameterScore(color_scores.get(color), t, t1);
				Lo++;
			}
		}		
	}

	@Override
	public void UpdateFeatureTypes(ArrayList<FeatureType> feature_types, int p_0) {
		for (int p = p_0; p < num_features + p_0; p++){
			feature_types.add(p, FeatureType.NUMBER);
		}
	}

	@Override
	public void UpdateFeatureNames(ArrayList<String> feature_names, int p_0) {
		for (String color : color_names){
			for (int i = 0 ; i < diameter_endpoints.size(); i++){
				feature_names.add(p_0, color + "_diameter_" + i);
				p_0++;
			}
		}
	}

	/**
	 * Generates a "diameter score" -  a scalar score for 
	 * a given diameter of a circle and a given score matrix
	 * by adding up the scores in the score matrix at each coordinate
	 * of a given diameter of the circle. 
	 * The diameter is represented by the circle-centered coordinates of one of the diameter's endpoints.
	 * The method is static because we will want to share it with other classes.
	 *  
	 * @param scoreMatrix		the matrix of scores for a given color
	 * @param to				global, image-wide coordinates of the center of the circle
	 * @param t1				local, circle-centered coordinates of one of the diameter's endpoints
	 * @return					the summed up total score
	 * 
	 */
	public static int ComputeDiameterScore(IntMatrix scoreMatrix, Point to, Point t1){		
		int score = 0;
		for (Point t : Diameters.getDiameter(t1)){
			score += scoreMatrix.get(t.x + to.x, t.y + to.y);
		}
		return score;
	}
	
	@Override
	public void UpdateFeatureColors(ArrayList<Color> feature_colors, int p_0) {
		for (String color : color_names){
			for (int i = 0 ; i < diameter_endpoints.size(); i++) {
				feature_colors.add(p_0, Run.it.imageset.getWaveColor(color));
				p_0++;
			}
		}
	}	

}
