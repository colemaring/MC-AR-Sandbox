import java.util.ArrayList;

import org.bukkit.Material;

public class TerrainGeneratorHelper {
	private KinectSandbox plugin;
	
	public TerrainGeneratorHelper(KinectSandbox plugin)
	{
		this.plugin = plugin;
	}
	
	public int[][][] findDifference(int[][] prevDepth, int [][] newDepth)
	{
		int [][][] ret = new int[newDepth.length][newDepth[0].length][3];
		for (int i = 0; i < newDepth.length; i++)
		{
			for (int j = 0; j < newDepth[0].length; j++)
			{
				// adding blocks
				if (newDepth[i][j] > prevDepth[i][j])
				{
					// upper block range
					ret[i][j][0] = newDepth[i][j];
					// lower block range
					ret[i][j][1] = prevDepth[i][j];
					// add
					ret[i][j][2] = 0;
				}
				// removing blocks
				else if (newDepth[i][j] < prevDepth[i][j])
				{
					// upper block range
					ret[i][j][0] = prevDepth[i][j];
					// lower block range
					ret[i][j][1] = newDepth[i][j];
					// remove
					ret[i][j][2] = 1;
				}
				// do nothing
				else
					ret[i][j][2] = -1;
					
			}
		}
		
		return ret;
	}
	
	// set all blocks to air in the region that depth encompasses
	public void resetBlocks() {	
        for (int x = 0; x < 500; x++) {
            for (int y = -100; y < 0; y++) {
                for (int z = 0; z < 500; z++) {
                	plugin.world.getBlockAt(x, y, z).setType(Material.AIR);
                }
            }
        }
    }
	
	// Given int[][], it will return an int[][] downsampled
	// where size is a common factor of plugin.rawKinectHeight and plugin.rawKinectWidth
	public int[][] meanPool(int[][] input, int size) {
	    int height = input.length;
	    int width = input[0].length;
	    
	    if (height % size != 0 || width % size != 0)
	        throw new IllegalArgumentException("Size must be a common factor of the dimensions of the input.");

	    int newHeight = height / size;
	    int newWidth = width / size;
	    int[][] output = new int[newHeight][newWidth];

	    for (int i = 0; i < newHeight; i++)
	    {
	        for (int j = 0; j < newWidth; j++)
	        {
	            int sum = 0;
	            
	            // Sum all elements in the size x size block
	            for (int di = 0; di < size; di++)
	                for (int dj = 0; dj < size; dj++)
	                    sum += input[i * size + di][j * size + dj];
	            
	            // Compute the mean
	            output[i][j] = sum / (size * size);
	        }
	    }
	    
	    return output;
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
