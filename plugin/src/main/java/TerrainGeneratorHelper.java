import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

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
		for (Player player : Bukkit.getOnlinePlayers()) {
			player.sendMessage("loading terrain...");
		}
        for (int x = 0; x < 400; x++) {
            for (int y = -150; y < 200; y++) {
                for (int z = 0; z < 400; z++) {
                	plugin.world.getBlockAt(x, y, z).setType(Material.AIR);
                }
            }
        }
    }
	
	// given input, crop array from user-defined values in the launcher
	public int[][] cropArray(int[][] input, int x1, int x2, int y1, int y2) {
	    // Ensure the input array is not null and the dimensions are valid
	    if (input == null || x1 < 0 || y1 < 0 || x2 >= input[0].length || y2 >= input.length) {
	        throw new IllegalArgumentException("Invalid cropping indices.");
	    }

	    // Calculate the number of rows and columns for the cropped array
	    int croppedHeight = y2 - y1 + 1;
	    int croppedWidth = x2 - x1 + 1;

	    // Create the output cropped array with the calculated dimensions
	    int[][] croppedArray = new int[croppedHeight][croppedWidth];

	    // Copy the relevant portion of the input array into the cropped array
	    for (int i = 0; i < croppedHeight; i++) {
	        for (int j = 0; j < croppedWidth; j++) {
	            croppedArray[i][j] = input[y1 + i][x1 + j];
	        }
	    }

	    return croppedArray;
	}

	
	// Given int[][], it will return an int[][] downsampled
	public int[][] meanPool(int[][] input, int size) {
	    int height = input.length;
	    int width = input[0].length;

	    int newHeight = (height + size - 1) / size;  // Round up division
	    int newWidth = (width + size - 1) / size;    // Round up division
	    int[][] output = new int[newHeight][newWidth];

	    for (int i = 0; i < newHeight; i++) {
	        for (int j = 0; j < newWidth; j++) {
	            int sum = 0;
	            int count = 0;

	            // Iterate over the size x size block
	            for (int di = 0; di < size; di++) {
	                for (int dj = 0; dj < size; dj++) {
	                    int row = i * size + di;
	                    int col = j * size + dj;

	                    // Only include values within bounds of the input array
	                    if (row < height && col < width) {
	                        sum += input[row][col];
	                        count++;
	                    }
	                }
	            }

	            // Compute the mean, avoid division by zero (though count should never be zero)
	            output[i][j] = sum / count;
	        }
	    }

	    return output;
	}

	
	// Similar to meanPool but doesnt not change dimension of input
	public int[][] meanFilter(int[][] input, int size) {
	    int height = input.length;
	    int width = input[0].length;
	    int[][] output = new int[height][width];
	    
	    int halfSize = size / 2;
	    
	    for (int i = 0; i < height; i++) {
	        for (int j = 0; j < width; j++) {
	            int sum = 0;
	            int count = 0;
	            
	            // Loop over the window centered at (i, j)
	            for (int di = -halfSize; di <= halfSize; di++) {
	                for (int dj = -halfSize; dj <= halfSize; dj++) {
	                    int ni = i + di;
	                    int nj = j + dj;
	                    
	                    // Check boundaries: if within bounds, include the element in the mean calculation
	                    if (ni >= 0 && ni < height && nj >= 0 && nj < width) {
	                        sum += input[ni][nj];
	                        count++;
	                    }
	                }
	            }
	            
	            // Set the output pixel to the average of the valid pixels in the window
	            output[i][j] = sum / count;
	        }
	    }
	    
	    return output;
	}

	// val is an arbitrary scaling factor to concert kinect depth values to minecraft y values
	// offset represents the distance from kinect to the sandbox (inimplemented)
	public int[][] convertToCoordinates(int [][] depth, double val, int offset)
	{
		for (int i = 0; i < depth.length; i++)
			for (int j = 0; j < depth[0].length; j++)
				depth[i][j] = offset - (int)Math.floor(depth[i][j] * val);
		
		return depth;
	}
	
	public int[][] mirrorXYAxis(int[][] array) {
	    int rows = array.length;
	    int cols = array[0].length;
	    int[][] mirroredArray = new int[rows][cols];

	    for (int i = 0; i < rows; i++) {
	        for (int j = 0; j < cols; j++) {
	            mirroredArray[i][j] = array[rows - 1 - i][cols - 1 - j]; // Swap rows & columns
	        }
	    }
	    return mirroredArray;
	}
}
