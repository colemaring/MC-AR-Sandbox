package Terrain;
import java.util.Random;

import org.bukkit.Material;

import Main.KinectSandbox;

public class TerrainGeneratorHelper {
	public static int terrainWidth = 0;
	public static int terrainHeight = 0;
	public static boolean terrainPaused = false;
	
	public static void pauseTerrain()
	{
//		Bukkit.broadcastMessage(ChatColor.GOLD + "Terrain Paused.");
		terrainPaused = true;
	}
	
	public static void unpauseTerrain()
	{
//		Bukkit.broadcastMessage(ChatColor.GOLD + "Terrain unpaused.");
		terrainPaused = false;
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
		
//		for (Player player : Bukkit.getOnlinePlayers()) {
//			player.sendMessage(ChatColor.GREEN + "Resetting terrain..");
//		}
        for (int x = -2; x < 200; x++) {
            for (int y = -100; y < 100; y++) {
                for (int z = -2; z < 200; z++) {
                	KinectSandbox.getInstance().world.getBlockAt(x, y, z).setType(Material.AIR);
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
	    terrainWidth = newWidth;
	    terrainHeight = newHeight;
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
	
	public void placeAsBiome(int i, int k, int j, String biome, boolean adding)
	{
		if (biome.equals("grass"))
		{	
			if (adding)
			{
				if (k > 20)
					KinectSandbox.getInstance().world.getBlockAt(i, k, j).setType(Material.SNOW_BLOCK);
				else if (k > 10)
					KinectSandbox.getInstance().world.getBlockAt(i, k, j).setType(Material.STONE);
				else
					KinectSandbox.getInstance().world.getBlockAt(i, k, j).setType(Material.GRASS_BLOCK);
			}
			else
				KinectSandbox.getInstance().world.getBlockAt(i, k, j).setType(Material.AIR);
			
			int waterLevel = 6;
			if (KinectSandbox.getInstance().waterEnabled)
				placeLiquid(i, waterLevel, j, "water");
		}
		if (biome.equals("snow"))
		{	
			if (adding)
			{
				if (k > 20)
					KinectSandbox.getInstance().world.getBlockAt(i, k, j).setType(Material.SNOW_BLOCK);
				else if (k > 10)
					KinectSandbox.getInstance().world.getBlockAt(i, k, j).setType(Material.ICE);
				else
					KinectSandbox.getInstance().world.getBlockAt(i, k, j).setType(Material.BLUE_ICE);
			}
			else
				KinectSandbox.getInstance().world.getBlockAt(i, k, j).setType(Material.AIR);
			
			int waterLevel = 10;
			if (KinectSandbox.getInstance().waterEnabled)
				placeLiquid(i, waterLevel, j, "water");
		}
		if (biome.equals("sand"))
		{	
			if (adding)
			{
				if (k > 20)
					KinectSandbox.getInstance().world.getBlockAt(i, k, j).setType(Material.SAND);
				else if (k > 10)
					KinectSandbox.getInstance().world.getBlockAt(i, k, j).setType(Material.SANDSTONE);
				else
					KinectSandbox.getInstance().world.getBlockAt(i, k, j).setType(Material.STONE);
			}
			else
				KinectSandbox.getInstance().world.getBlockAt(i, k, j).setType(Material.AIR);
		}
		if (biome.equals("mesa"))
		{	
			if (adding)
			{
				if (k > 20)
					KinectSandbox.getInstance().world.getBlockAt(i, k, j).setType(Material.RED_TERRACOTTA);
				else if (k > 19)
					KinectSandbox.getInstance().world.getBlockAt(i, k, j).setType(Material.WHITE_TERRACOTTA);
				else if (k > 15)
					KinectSandbox.getInstance().world.getBlockAt(i, k, j).setType(Material.RED_TERRACOTTA);
				else if (k > 14)
					KinectSandbox.getInstance().world.getBlockAt(i, k, j).setType(Material.GRAY_TERRACOTTA);
				else if (k > 10)
					KinectSandbox.getInstance().world.getBlockAt(i, k, j).setType(Material.ORANGE_TERRACOTTA);
				else
					KinectSandbox.getInstance().world.getBlockAt(i, k, j).setType(Material.RED_SAND);
			}
			else
				KinectSandbox.getInstance().world.getBlockAt(i, k, j).setType(Material.AIR);
		}
		if (biome.equals("stone"))
		{	
			if (adding)
			{
				if (k > 20)
					KinectSandbox.getInstance().world.getBlockAt(i, k, j).setType(Material.STONE);
				else if (k > 10)
					KinectSandbox.getInstance().world.getBlockAt(i, k, j).setType(Material.STONE);
				else
					KinectSandbox.getInstance().world.getBlockAt(i, k, j).setType(Material.GRASS_BLOCK);
			}
			else
				KinectSandbox.getInstance().world.getBlockAt(i, k, j).setType(Material.AIR);
		}
		if (biome.equals("nether"))
		{	
			if (adding)
			{
				if (k > 20)
					KinectSandbox.getInstance().world.getBlockAt(i, k, j).setType(Material.NETHERRACK);
				else if (k > 10)
					KinectSandbox.getInstance().world.getBlockAt(i, k, j).setType(Material.BASALT);
				else
					KinectSandbox.getInstance().world.getBlockAt(i, k, j).setType(Material.SOUL_SAND);
			}
			else
				KinectSandbox.getInstance().world.getBlockAt(i, k, j).setType(Material.AIR);
			
			int lavaLevel = 14;
			if (KinectSandbox.getInstance().waterEnabled)
				placeLiquid(i, lavaLevel, j, "lava");
		}
		if (biome.equals("rainbow"))
		{	
			Material[] oreBlocks = {Material.NETHERITE_BLOCK, Material.DIAMOND_BLOCK, Material.REDSTONE_BLOCK, Material.GOLD_BLOCK, Material.IRON_BLOCK, Material.EMERALD_BLOCK, Material.LAPIS_BLOCK};
			Random random = new Random();
		    Material randomOreBlock = oreBlocks[random.nextInt(oreBlocks.length)];
			if (adding)
				KinectSandbox.getInstance().world.getBlockAt(i, k, j).setType(randomOreBlock);	
			else
				KinectSandbox.getInstance().world.getBlockAt(i, k, j).setType(Material.AIR);
		}
	}
	
	// add random veins, blocks, or etc to make biomes look more natural
	// not the most optimal way to do this, as im re-scanning through all blocks
	// only expensive part is replacing blocks so it should be fine
	public void touchUpBiome(String biome)
	{
		if (biome.equals("grass"))
		{
			
		}
		else if (biome.equals("sand"))
		{
			
		}
		else if (biome.equals("snow"))
		{
			
		}
		else if (biome.equals("mesa"))
		{
			
		}
		else if (biome.equals("stone"))
		{
			
		}
		else if (biome.equals("nether"))
		{
			
		}
		else if (biome.equals("rainbow"))
		{
			
		}
	}
	
	// scan y coordinate and below for air, replace with water
	// not efficient, use the difference array somehow
	public void placeLiquid(int i, int level, int j, String type)
	{
		if (type.equals("water"))
		{
			for (int x = 0; x < level; x++)
				if (KinectSandbox.getInstance().world.getBlockAt(i, x, j).getType().equals(Material.AIR))
					KinectSandbox.getInstance().world.getBlockAt(i, x, j).setType(Material.WATER);
		}
		else
		{
			for (int x = 0; x < level; x++)
				if (KinectSandbox.getInstance().world.getBlockAt(i, x, j).getType().equals(Material.AIR))
					KinectSandbox.getInstance().world.getBlockAt(i, x, j).setType(Material.LAVA);
		}
			
		
	}
}
