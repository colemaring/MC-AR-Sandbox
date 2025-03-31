public class TerrainGeneratorHelper {
	private KinectSandbox plugin;
	
	public TerrainGeneratorHelper(KinectSandbox plugin)
	{
		this.plugin = plugin;
	}
	
	// returns true if currDepth is <threshold>% similar to prevDepth
	// similarity is calc'd by summing abs(currDepth[i][j]-prevDepth[i][j]) for all i, k 
	// and mapping and arbitrary percentage value to total sums
	// threshold is a percentage value eg. 0.7 == 70%
//	public boolean isDifferentEnough(int[][] currDepth, int [][] prevDepth, double threshold)
//	{
//		int totalDiff = 0;
//
//        // Ensure input is valid
//        if (currDepth == null || prevDepth == null || currDepth.length != prevDepth.length || currDepth[0].length != prevDepth[0].length)
//            throw new IllegalArgumentException("Invalid input parameters.");
//		
//        // Calculate the sum
//		for (int i = 0; i < currDepth.length; i++)
//			for (int j = 0; j < currDepth[0].length; j++)
//				totalDiff += Math.abs(currDepth[i][j] - prevDepth[i][j]);
//		
//		// The theoretical maximum possible total difference 
//		int maxPossibleDiff = plugin.rawKinectWidth * plugin.rawKinectHeight * plugin.rawKinectMaxDepth;
//		// A realistic maximum possible difference to base our similarity score from
//		int maxRealisticDiff = (plugin.rawKinectWidth * plugin.rawKinectHeight * plugin.rawKinectMaxDepth) / 4;
//		
//		double similarity = 1.0 - (totalDiff / maxRealisticDiff);
//
//		return similarity < threshold;
//	}
}
