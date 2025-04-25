package Terrain;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Levelled;

import Main.KinectSandbox;

public class TerrainGeneratorHelper {
	public static int terrainWidth = 0;
	public static int terrainHeight = 0;
	public static boolean terrainPaused = false;
	
	public static void pauseTerrain()
	{
		terrainPaused = true;
	}
	
	public static void unpauseTerrain()
	{
		terrainPaused = false;
	}
	
	public static int[][][] findDifference(int[][] prevDepth, int [][] newDepth)
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
	public static void resetBlocks() {
        for (int x = -2; x < 525; x++) {
            for (int y = -70; y < 255; y++) {
                for (int z = -2; z < 525; z++) {
                	KinectSandbox.getInstance().world.getBlockAt(x, y, z).setType(Material.AIR);
                }
            }
        }
    }
	
	// given input, crop array from user-defined values in the launcher
	public static int[][] cropArray(int[][] input, int x1, int x2, int y1, int y2) {
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

    private final static int MAX_HISTORY = 2;
    private final static LinkedList<int[][]> historyFrames = new LinkedList<>();

    public static int[][] movingMode(int[][] currentFrame) {
        int height = currentFrame.length;
        int width = currentFrame[0].length;

        // Add current frame to history
        historyFrames.addLast(currentFrame);
        if (historyFrames.size() > MAX_HISTORY) {
            historyFrames.removeFirst(); // Keep only the last 10
        }

        int[][] output = new int[height][width];

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                Map<Integer, Integer> freq = new HashMap<>();
                int maxCount = 0;
                int mode = 0;

                for (int[][] frame : historyFrames) {
                    int val = frame[i][j];
                    int count = freq.getOrDefault(val, 0) + 1;
                    freq.put(val, count);

                    if (count > maxCount || (count == maxCount && val < mode)) {
                        maxCount = count;
                        mode = val;
                    }
                }

                output[i][j] = mode;
            }
        }

        return output;
    }
	
	public static int[][] modePool(int[][] input, int size) {
	    int height = input.length;
	    int width = input[0].length;

	    int newHeight = (height + size - 1) / size;  // Round up division
	    int newWidth = (width + size - 1) / size;    // Round up division
	    terrainWidth = newWidth;
	    terrainHeight = newHeight;
	    int[][] output = new int[newHeight][newWidth];

	    for (int i = 0; i < newHeight; i++) {
	        for (int j = 0; j < newWidth; j++) {
	            Map<Integer, Integer> freq = new HashMap<>();
	            int maxCount = 0;
	            int mode = 0;

	            for (int di = 0; di < size; di++) {
	                for (int dj = 0; dj < size; dj++) {
	                    int row = i * size + di;
	                    int col = j * size + dj;

	                    if (row < height && col < width) {
	                        int val = input[row][col];
	                        int count = freq.getOrDefault(val, 0) + 1;
	                        freq.put(val, count);

	                        if (count > maxCount || (count == maxCount && val < mode)) {
	                            maxCount = count;
	                            mode = val;
	                        }
	                    }
	                }
	            }

	            output[i][j] = mode;
	        }
	    }

	    return output;
	}

	// val is an arbitrary scaling factor to concert kinect depth values to minecraft y values
	// offset represents the distance from kinect to the sandbox (nimplemented)
	public static int[][] convertToCoordinates(int [][] depth, int offset)
	{
		for (int i = 0; i < depth.length; i++)
		{
			for (int j = 0; j < depth[0].length; j++)
			{
				if (depth[i][j] == 0)
					continue;
				depth[i][j] = offset - (int)Math.floor(depth[i][j]);
			}
		}
			
		return depth;
	}
	
	public static int[][] mirrorXYAxis(int[][] array) {
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
	
	public static int findZEnd() {
	    int lastNonZeroZ = -1;

	    for (int z = 0; z < 525; z++) {
	        Block block = KinectSandbox.getInstance().world.getHighestBlockAt(0, z); // ✅ Correct order: x, z
	        Material type = block.getType();
	        if (type.isSolid()) {
	            lastNonZeroZ = z;
	        }
	    }

	    return lastNonZeroZ;
	}

	public static int findXEnd() {
	    int lastNonZeroX = -1;

	    for (int x = 0; x < 525; x++) {
	        Block block = KinectSandbox.getInstance().world.getHighestBlockAt(x, 0);
	        Material type = block.getType();
	        if (type.isSolid()) {
	            lastNonZeroX = x;
	        }
	    }

	    return lastNonZeroX;
	}


	
	public static void placeAsBiome(int i, int k, int j, String biome, boolean adding)
	{
		// Ignore ores from OreHunt and Dig Roulette gamemodes
		if (KinectSandbox.getInstance().world.getBlockAt(i, k, j).getType().equals(Material.IRON_BLOCK) ||KinectSandbox.getInstance().world.getBlockAt(i, k, j).getType().equals(Material.GOLD_BLOCK) || KinectSandbox.getInstance().world.getBlockAt(i, k, j).getType().equals(Material.TNT) || KinectSandbox.getInstance().world.getBlockAt(i, k, j).getType().equals(Material.DIAMOND_BLOCK) || KinectSandbox.getInstance().world.getBlockAt(i, k, j).getType().equals(Material.EMERALD_BLOCK) || KinectSandbox.getInstance().world.getBlockAt(i, k, j).getType().equals(Material.COAL_BLOCK) || KinectSandbox.getInstance().world.getBlockAt(i, k, j).getType().equals(Material.BEDROCK))
			return;
		
		// dont overwrite water source blocks if allowWaterFlow is enabled (from aquaduct gamemode)
		if ((KinectSandbox.allowWaterFlow  && KinectSandbox.getInstance().world.getBlockAt(i, k, j).getType() == Material.WATER  && KinectSandbox.getInstance().world.getBlockAt(i, k, j).getBlockData() instanceof Levelled  && ((Levelled) KinectSandbox.getInstance().world.getBlockAt(i, k, j).getBlockData()).getLevel() == 0))
			return;
		
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
		    Material[] woolColors = {
		        Material.WHITE_WOOL, Material.ORANGE_WOOL, Material.MAGENTA_WOOL, Material.LIGHT_BLUE_WOOL,
		        Material.YELLOW_WOOL, Material.LIME_WOOL, Material.PINK_WOOL, Material.GRAY_WOOL,
		        Material.LIGHT_GRAY_WOOL, Material.CYAN_WOOL, Material.PURPLE_WOOL, Material.BLUE_WOOL,
		        Material.BROWN_WOOL, Material.GREEN_WOOL, Material.RED_WOOL, Material.BLACK_WOOL
		    };
		    
		    Random random = new Random();
		    Material randomWool = woolColors[random.nextInt(woolColors.length)];

		    if (adding)
		        KinectSandbox.getInstance().world.getBlockAt(i, k, j).setType(randomWool);	
		    else
		        KinectSandbox.getInstance().world.getBlockAt(i, k, j).setType(Material.AIR);
		}

	}
	
	// add random veins, blocks, or etc to make biomes look more natural
	// not the most optimal way to do this, as im re-scanning through all blocks
	// only expensive part is replacing blocks so it should be fine
	public static void touchUpBiome(String biome)
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
	public static void placeLiquid(int i, int level, int j, String type)
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
