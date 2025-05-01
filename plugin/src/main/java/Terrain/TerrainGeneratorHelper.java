package Terrain;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.Levelled;
import org.bukkit.scheduler.BukkitRunnable;

import Main.KinectSandbox;

public class TerrainGeneratorHelper {
	static Random random = new Random();
	public static void addWater(String biome)
	{
		int waterLevel = -1;
		
		if (biome.equals("grass"))
			waterLevel = 6;
		else if (biome.equals("nether"))
			waterLevel = 14;
		else if (biome.equals("snow"))
			waterLevel = 10;
		else
			return; // a biome where we dont place water
			
		for (int x = 0; x <= findXEnd(); x++) {
            for (int y = 0; y < waterLevel; y++) {
                for (int z = 0; z <= findZEnd(); z++) {
                	if (KinectSandbox.getInstance().world.getBlockAt(x, y, z).getType().equals(Material.AIR))
                		KinectSandbox.getInstance().world.getBlockAt(x, y, z).setType(Material.WATER);
                }
            }
        }
	}
	
	public static void removeWater(String biome)
	{
		int waterLevel = -1;
		
		if (biome.equals("grass"))
			waterLevel = 6;
		else if (biome.equals("nether"))
			waterLevel = 14;
		else if (biome.equals("snow"))
			waterLevel = 10;
		else
			return; // a biome where we dont place water
		
		// remove water and gold block for aquaduct gamemod
        for (int x = -2; x <= findXEnd(); x++) {
            for (int y = 0; y < waterLevel; y++) {
                for (int z = -2; z <= findZEnd(); z++) {
                	if (KinectSandbox.getInstance().world.getBlockAt(x, y, z).getType().equals(Material.WATER))
                		KinectSandbox.getInstance().world.getBlockAt(x, y, z).setType(Material.AIR);
                }
            }
        }
	}
	
	public static void removeAllBlocks(int numXLayers, int numZLayers, Runnable onComplete) {
	    if (numXLayers <= 0 || numZLayers <= 0) {
	        Bukkit.getLogger().warning("Invalid layer size for water removal!");
	        onComplete.run();
	        return;
	    }

	    int xEnd = findXEnd();
	    int zEnd = findZEnd();
	    int yMax = TerrainGenerator.yCoordThreshold;

	    final int startX = -2;
	    final int startZ = -2;
	    final int startY = 0;

	    final int blocksPerTick = 15000; // Adjust for performance balance

	    BukkitRunnable task = new BukkitRunnable() {
	        int currentX = startX;
	        int currentZ = startZ;
	        int currentY = startY;

	        int innerX = 0;
	        int innerZ = 0;

	        @Override
	        public void run() {
	            int blocksProcessed = 0;
	            World world = KinectSandbox.getInstance().world;

	            if (world == null) {
	                Bukkit.getLogger().warning("World is null, canceling task.");
	                this.cancel();
	                onComplete.run();
	                return;
	            }

	            while (blocksProcessed < blocksPerTick) {
	                int targetX = currentX + innerX;
	                int targetZ = currentZ + innerZ;

	                if (currentY > yMax) {
	                    this.cancel();
	                    onComplete.run();
	                    return;
	                }

	                if (targetX <= xEnd && targetZ <= zEnd) {
	                    Block block = world.getBlockAt(targetX, currentY, targetZ);
	                    //if (block.getType() == Material.WATER || block.getType() == Material.GOLD_BLOCK) {
	                        block.setType(Material.AIR);
	                    //}
	                    blocksProcessed++;
	                }

	                innerZ++;

	                if (innerZ >= numZLayers || targetZ >= zEnd) {
	                    innerZ = 0;
	                    innerX++;

	                    if (innerX >= numXLayers || targetX >= xEnd) {
	                        innerX = 0;
	                        currentX += numXLayers;

	                        if (currentX > xEnd) {
	                            currentX = startX;
	                            currentZ += numZLayers;

	                            if (currentZ > zEnd) {
	                                currentZ = startZ;
	                                currentY++;
	                            }
	                        }
	                    }
	                }
	            }
	        }
	    };

	    // Runs every 2 ticks; tweak as needed
	    task.runTaskTimer(KinectSandbox.getInstance(), 0L, 1L);
	}

	
	
	
	public static void updateBiome(String biome, int numXLayers, int numZLayers, Runnable onComplete) {
	    if (numXLayers <= 0) {
	        Bukkit.getLogger().warning("numXLayers must be positive!");
	        onComplete.run(); // Call complete immediately if invalid input
	        return;
	    }
	     if (numZLayers <= 0) {
	        Bukkit.getLogger().warning("numZLayers must be positive!");
	        onComplete.run(); // Call complete immediately if invalid input
	        return;
	    }
	     
	    //Bukkit.broadcastMessage("updateBiome called");

	    int xEnd = findXEnd();
	    int zEnd = findZEnd();
	    int yMax = TerrainGenerator.yCoordThreshold; // Maximum Y coordinate to process up to (inclusive)

	    final int startX = 0;
	    final int startZ = 0;
	    final int startY = 0;

	    // Adjust blocksPerTick based on server performance.
	    // This is the maximum number of blocks to attempt to process in a single tick.
	    final int blocksPerTick = 20000; // Example value

	    BukkitRunnable task = new BukkitRunnable() {
	        // Current position in the overall iteration (start of the current numXLayers * numZLayers block)
	        int currentX = startX;
	        int currentZ = startZ;
	        int currentY = startY;

	        // Offsets within the current numXLayers * numZLayers block being processed
	        int innerX = 0;
	        int innerZ = 0;


	        @Override
	        public void run() {
	            int blocksProcessedThisTick = 0;
	            World world = KinectSandbox.getInstance().world; // Access world from plugin instance

	            if (world == null) {
	                 Bukkit.getLogger().warning("World not available from plugin instance!");
	                 this.cancel();
	                 onComplete.run();
	                 return;
	            }

	            // Main loop continues as long as we haven't processed all blocks for this tick
	            while (blocksProcessedThisTick < blocksPerTick) {

	                // Calculate target coordinates based on current block start and inner offsets
	                int targetX = currentX + innerX;
	                int targetZ = currentZ + innerZ;

	                // --- Check for completion of the entire area ---
	                // If currentY has exceeded the max, we are done.
	                if (currentY > yMax) {
	                    this.cancel(); // Cancel the task
	                    onComplete.run(); // Execute the completion callback
	                    return; // Exit the run method
	                }

	                // --- Check if we need to move to the next block within the current Y layer ---
	                // Check if we have finished the current row of Z blocks within the current X column
	                // or if the next innerZ would go beyond the overall Z boundary for the current currentZ block.
	                if (currentZ + innerZ > zEnd || innerZ >= numZLayers) {
	                    innerZ = 0; // Reset inner Z offset
	                    innerX++; // Move to the next X column within the current block

	                    // Check if we have finished the current block of X columns within the current Z block
	                    // or if the next innerX would go beyond the overall X boundary for the current currentX block.
	                    if (currentX + innerX > xEnd || innerX >= numXLayers) {
	                        innerX = 0; // Reset inner X offset
	                        // Move to the start of the next numXLayers * numZLayers block in X
	                        currentX += numXLayers;

	                        // Check if we have finished the current row of numXLayers blocks in X
	                        // or if the next currentX is beyond the overall X boundary.
	                        if (currentX > xEnd) {
	                            currentX = startX; // Reset current X position to the start of the area
	                            // Move to the start of the next numZLayers block in Z
	                            currentZ += numZLayers;

	                            // Check if we have finished the current Z slice
	                            // or if the next currentZ is beyond the overall Z boundary.
	                            if (currentZ > zEnd) {
	                                currentZ = startZ; // Reset current Z position to the start of the area
	                                currentY++; // Move to the next Y layer

	                                // Check if we have finished the entire area after incrementing Y.
	                                // This will be caught by the completion check at the start of the while loop in the next tick.
	                                // No need for an explicit return here, let the loop continue or finish.
	                            }
	                        }
	                    }
	                    // Continue to the next iteration of the while loop to process the block at the new position
	                    continue; // Skip block processing in this iteration as we just moved to a new position
	                }

	                // --- Process the block at the current (targetX, currentY, targetZ) ---
	                // Ensure target coordinates are within the overall bounds before processing.
	                if (targetX <= xEnd && targetZ <= zEnd) {
	                    Block block = world.getBlockAt(targetX, currentY, targetZ);

	                    // ONLY call placeAsBiome if the block is NOT air
	                    if (block.getType() != Material.AIR) {
	                        // Call placeAsBiome to update the block to the specified biome type.
	                        // We pass 'true' for 'adding' because we are setting the block to the new biome type.
	                        placeAsBiome(targetX, currentY, targetZ, biome, true, false);
	                    }

	                    blocksProcessedThisTick++; // Increment count for each block coordinate considered
	                } else {
	                     // If target coordinates are out of bounds, it means we finished the current block
	                     // or are at the edge of the overall area. The increment logic above
	                     // should have already handled moving to the next valid position.
	                     // If we reach here and targetX/targetZ are out of bounds, it indicates an issue
	                     // with the increment logic or boundaries. For safety, we can break the while loop
	                     // to avoid potential infinite loops, and the task will resume in the next tick.
	                     Bukkit.getLogger().warning("Block coordinates out of bounds during processing: (" + targetX + ", " + currentY + ", " + targetZ + ")");
	                     break; // Exit the while loop for this tick
	                }


	                // --- Move to the next block in the iteration sequence within the current numXLayers * numZLayers block ---
	                // This is done after processing a block, so the next iteration of the while loop
	                // will process the next block in the Z direction within the current X column.
	                // The logic at the top of the loop handles wrapping around X and Z blocks and Y layers.
	                innerZ++; // Move to the next Z position within the current X column


	                // If the while loop finishes because blocksPerTick was reached, the runnable
	                // will be scheduled again, continuing from the last saved position (currentX, currentZ, currentY, innerX, innerZ).
	            }
	        }
	    };

	    // Schedule the task to run immediately and repeat every 1 tick.
	    // Running every tick (1L) provides the smoothest clearing animation but might
	    // cause more lag if blocksPerTick is too high. Adjust the period as needed.
	    task.runTaskTimer(KinectSandbox.getInstance(), 0L, 1L);
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
            for (int y = -70; y < 350; y++) {
                for (int z = -2; z < 525; z++) {
                	KinectSandbox.getInstance().world.getBlockAt(x, y, z).setType(Material.AIR);
                }
            }
        }
    }
	
	public static void removeWall()
	{
		int mid = TerrainGeneratorHelper.findZEnd()/2;
		for (int i = 0; i <= TerrainGeneratorHelper.findXEnd(); i++)
		{
			for (int j = 0; j <= 64; j++)
			{
				KinectSandbox.getInstance().world.getBlockAt(i, j, mid).setType(Material.AIR);
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
	
	public static int[][] modePool(int[][] input, int size) {
	    int height = input.length;
	    int width = input[0].length;

	    int newHeight = (height + size - 1) / size;  // Round up division
	    int newWidth = (width + size - 1) / size;    // Round up division
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

	
	public static int findHighestBlock()
	{
		int max = -1;
		
		for (int x = 0; x < TerrainGeneratorHelper.findXEnd(); x++)
		{
			for (int z = 0; z < TerrainGeneratorHelper.findZEnd(); z++)
			{
				if (KinectSandbox.getInstance().world.getHighestBlockYAt(x, z) > max)
				{
					// find highest block
					int count = 0;
					
					for (int i = 0; i < 320; i++)
						if (!KinectSandbox.getInstance().world.getBlockAt(x, i, z).getType().equals(Material.AIR))
							count++;
					
					max = Math.max(max, count);
				}
			}
		}
		
		return max;
	}
	
	public static int[][] gaussianFilter(int[][] arr, int size) {
        if (arr == null || arr.length == 0 || arr[0].length == 0) {
            System.err.println("Input array is null or empty.");
            // Return a new empty array or null, depending on desired behavior for empty input
            return new int[0][0];
        }

        // Ensure size is a positive odd number
        if (size <= 0) {
            size = 1;
        }
        if (size % 2 == 0) {
            size++; // Adjust to the next odd number
        }

        int height = arr.length;
        int width = arr[0].length;
        int[][] result = new int[height][width];

        // Calculate the Gaussian kernel
        double[][] kernel = new double[size][size];
        double sigma = (size - 1) / 6.0; // Common rule of thumb for sigma
        double sum = 0;
        int kernelRadius = size / 2; // Integer division

        // Calculate kernel values
        for (int i = -kernelRadius; i <= kernelRadius; i++) {
            for (int j = -kernelRadius; j <= kernelRadius; j++) {
                double exponent = -(i * i + j * j) / (2 * sigma * sigma);
                kernel[i + kernelRadius][j + kernelRadius] = (1.0 / (2 * Math.PI * sigma * sigma)) * Math.exp(exponent);
                sum += kernel[i + kernelRadius][j + kernelRadius];
            }
        }

        // Normalize the kernel
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                kernel[i][j] /= sum;
            }
        }

        // Apply the convolution
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                double weightedSum = 0;

                for (int ki = 0; ki < size; ki++) {
                    for (int kj = 0; kj < size; kj++) {
                        int arrRow = i - kernelRadius + ki;
                        int arrCol = j - kernelRadius + kj;

                        // Handle boundary conditions by replicating edge pixels
                        if (arrRow < 0) arrRow = 0;
                        if (arrRow >= height) arrRow = height - 1;
                        if (arrCol < 0) arrCol = 0;
                        if (arrCol >= width) arrCol = width - 1;

                        weightedSum += arr[arrRow][arrCol] * kernel[ki][kj];
                    }
                }
                result[i][j] = (int) Math.round(weightedSum);
            }
        }

        // Return the new smoothed array
        return result;
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
				depth[i][j] = offset - depth[i][j];
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


	
	public static void placeAsBiome(int i, int k, int j, String biome, boolean adding, boolean bypass)
	{
		Block block = KinectSandbox.getInstance().world.getBlockAt(i, k, j);
		Material type = block.getType();
		// Ignore ores from OreHunt and Dig Roulette gamemodes
		if (!bypass && (type.equals(Material.IRON_BLOCK) || type.equals(Material.GOLD_BLOCK) || type.equals(Material.TNT) || type.equals(Material.DIAMOND_BLOCK) || type.equals(Material.EMERALD_BLOCK) || type.equals(Material.COAL_BLOCK) || type.equals(Material.BEDROCK)))
			return;
		
		// dont overwrite water source blocks if allowWaterFlow is enabled (from aquaduct gamemode)
		if ((KinectSandbox.allowWaterFlow  && type == Material.WATER  && block.getBlockData() instanceof Levelled  && ((Levelled) block.getBlockData()).getLevel() == 0))
			return;
		
		if (biome.equals("grass"))
		{	
			if (adding)
			{
				if (k > TerrainGenerator.yCoordThreshold && TerrainGenerator.initialized != 0)
					block.setType(Material.AIR);
				else if (k > 28)
					block.setType(Material.SNOW_BLOCK);
				else if (k > 20)
					block.setType(Material.STONE);
				else if (k > 10)
					block.setType(Material.GRASS_BLOCK);
				else	
					block.setType(Material.STONE);
			}
			else
				block.setType(Material.AIR);
			
			int waterLevel = 6;
			if (KinectSandbox.getInstance().waterEnabled)
				placeLiquid(i, waterLevel, j, "water");
		}
		if (biome.equals("snow"))
		{	
			if (adding)
			{
				if (k > TerrainGenerator.yCoordThreshold && TerrainGenerator.initialized != 0)
					block.setType(Material.AIR);
				else if (k > 20)
					block.setType(Material.SNOW_BLOCK);
				else if (k > 10)
					block.setType(Material.ICE);
				else
					block.setType(Material.BLUE_ICE);
			}
			else
				block.setType(Material.AIR);
			
			int waterLevel = 10;
			if (KinectSandbox.getInstance().waterEnabled)
				placeLiquid(i, waterLevel, j, "water");
		}
		if (biome.equals("sand"))
		{	
			if (adding)
			{
				if (k > TerrainGenerator.yCoordThreshold && TerrainGenerator.initialized != 0)
					block.setType(Material.AIR);
				else if (k > 20)
					block.setType(Material.SAND);
				else if (k > 10)
					block.setType(Material.SANDSTONE);
				else
					block.setType(Material.STONE);
			}
			else
				block.setType(Material.AIR);
		}
		if (biome.equals("mesa"))
		{	
			if (adding)
			{
				if (k > TerrainGenerator.yCoordThreshold && TerrainGenerator.initialized != 0)
					block.setType(Material.AIR);
				else if (k > 20)
					block.setType(Material.RED_TERRACOTTA);
				else if (k > 19)
					block.setType(Material.WHITE_TERRACOTTA);
				else if (k > 15)
					block.setType(Material.RED_TERRACOTTA);
				else if (k > 14)
					block.setType(Material.GRAY_TERRACOTTA);
				else if (k > 10)
					block.setType(Material.ORANGE_TERRACOTTA);
				else
					block.setType(Material.RED_SAND);
			}
			else
				block.setType(Material.AIR);
		}
		if (biome.equals("stone"))
		{	
			if (adding)
			{
				if (k > TerrainGenerator.yCoordThreshold && TerrainGenerator.initialized != 0)
					block.setType(Material.AIR);
				else if (k > 20)
					block.setType(Material.STONE);
				else if (k > 10)
					block.setType(Material.STONE);
				else
					block.setType(Material.GRASS_BLOCK);
			}
			else
				block.setType(Material.AIR);
		}
		if (biome.equals("nether"))
		{	
			if (adding)
			{
				if (k > TerrainGenerator.yCoordThreshold && TerrainGenerator.initialized != 0)
					block.setType(Material.AIR);
				else if (k > 20)
					block.setType(Material.NETHERRACK);
				else if (k > 10)
					block.setType(Material.BASALT);
				else
					block.setType(Material.SOUL_SAND);
			}
			else
				block.setType(Material.AIR);
			
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
		        block.setType(randomWool);	
		    else
		        block.setType(Material.AIR);
		}
		block.getState().update(true, true);
	}
	
	// add random veins, blocks, or etc to make biomes look more natural
	// not the most optimal way to do this, as im re-scanning through all blocks
	// only expensive part is replacing blocks so it should be fine
	public static void touchUpBiome(String biome)
	{
		if (biome.equals("grass"))
		{
//			for (int i = 0; i < 200; i++)
//			{
//				for (int j = 0; j < 200; j++)
//				{
//					for (int y = 20; y < 28; y++)
//					{
//							if (KinectSandbox.getInstance().world.getBlockAt(i, y, j).getType().equals(Material.STONE) && KinectSandbox.getInstance().world.getBlockAt(i, y+1, j).getType().equals(Material.AIR))
//								KinectSandbox.getInstance().world.getBlockAt(i, y, j).setType(Material.SNOW);
//						
//					}
//				}
//			}
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
