package main;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class WFCBitmap extends Bitmap
{
	private boolean[][] collapsed;
	private float[][] stability;
	
	public WFCBitmap(int width, int height)
	{
		super(width, height);
		collapsed = new boolean[width][height];
		stability = new float[width][height];
		
		reset();
	}
	
	public void reset()
	{
		for(int i = 0; i < width; i++)
		for(int j = 0; j < height; j++)
		{
			collapsed[i][j] = false;
			stability[i][j] = 0;
		}
	}
	
	public boolean collapsePoint(BufferedImage[] img, int cellWidth, int cellHeight, boolean loop)
	{
		int[] bmCollapse = pickCollapsePoint(cellWidth, cellHeight, loop);
		if(bmCollapse == null)
		{
			System.err.println("Nothing more to collapse.");
			return false;
		}
		
		//System.out.println("Chosen point on bitmap: " + bmCollapse[0] + ", " + bmCollapse[1]);
		
		int[] imgCollapse = pickImageCollapsePoint(img, bmCollapse, cellWidth, cellHeight);
		float max = (float) Math.sqrt(Math.pow(cellWidth, 2) + Math.pow(cellHeight, 2));
		
		//System.out.println("Chosen sample: " + imgCollapse[0]);
		//System.out.println("Chosen point on sample: " + imgCollapse[1] + ", " + imgCollapse[2]);
		
		BufferedImage sample = img[imgCollapse[0]];
		
		int  sampleWidth = sample.getWidth();
		int sampleHeight = sample.getHeight();
		
		for(int i =  -cellWidth; i <=  cellWidth; i++)
		for(int j = -cellHeight; j <= cellHeight; j++)
		{
			//System.out.println("------------------");
			//System.out.println(i + ", " + j);
			float distance = (float) Math.sqrt(Math.pow(i, 2) + Math.pow(j, 2));
			float normalized = 1 - (distance / max);
						
			int ix = bmCollapse[0] + i;
			int iy = bmCollapse[1] + j;
			
			if(ix >=  width) ix -=  width;
			if(iy >= height) iy -= height;
				
			if(ix < 0) ix +=  width;
			if(iy < 0) iy += height;
			
			if(i == 0 && j == 0)
			{
				int rgbC = sample.getRGB(imgCollapse[1], imgCollapse[2]);
				image.setRGB(ix, iy, rgbC);
				collapsed[ix][iy] = true;
				stability[ix][iy] = 1;
				continue;
			}
			
			float previous = stability[ix][iy];
			float next = (normalized * (1 - stability[ix][iy]));
			float sum = previous + next;
			
			//System.out.println(previous + ", " + next + " : " + sum);
			
			stability[ix][iy] += next;
			
			int rgb1 = image.getRGB(ix, iy);
						
			int a1 = (rgb1 >> 24) % 256;
			int r1 = (rgb1 >> 16) % 256;
			int g1 = (rgb1 >>  8) % 256;
			int b1 = (rgb1      ) % 256;
			
			if(a1 < 0) a1 += 256;
			if(r1 < 0) r1 += 256;
			if(g1 < 0) g1 += 256;
			if(b1 < 0) b1 += 256;
			
			//System.out.println((int) r1 + ", " + (int) g1 + ", " + (int) b1 + ", " + (int) a1);
			
			int dx = imgCollapse[1] + i;
			int dy = imgCollapse[2] + j;
			
			if(dx >=  sampleWidth) dx -=  sampleWidth;
			if(dy >= sampleHeight) dy -= sampleHeight;
				
			if(dx < 0) dx +=  sampleWidth;
			if(dy < 0) dy += sampleHeight;
						
			int rgb2 = sample.getRGB(dx, dy);
						
			int a2 = (rgb2 >> 24) % 256;
			int r2 = (rgb2 >> 16) % 256;
			int g2 = (rgb2 >>  8) % 256;
			int b2 = (rgb2      ) % 256;
			
			if(a2 < 0) a2 += 256;
			if(r2 < 0) r2 += 256;
			if(g2 < 0) g2 += 256;
			if(b2 < 0) b2 += 256;
			
			//System.out.println((int) r2 + ", " + (int) g2 + ", " + (int) b2 + ", " + (int) a2);
			
			float sP = previous / sum;
			float sN =     next / sum;
			
			float aP = (float) a1 * sP + (float) a2 * sN;
			float rP = (float) r1 * sP + (float) r2 * sN;
			float gP = (float) g1 * sP + (float) g2 * sN;
			float bP = (float) b1 * sP + (float) b2 * sN;
			
			//System.out.println(rP + ", " + gP + ", " + bP + ", " + aP);
			
			Color finalColor = new Color((int) rP, (int) gP, (int) bP, (int) aP);
			
			image.setRGB(ix, iy, finalColor.getRGB());
		}
		
		return true;
	}
	
	//Iterates through each possible cell of each sample, and chooses the cell (or cells) that matches the most with the
	//given cell in the bitmap. If multiple fit, it picks one randomly.
	public int[] pickImageCollapsePoint(BufferedImage[] img, int[] point, int cellWidth, int cellHeight)
	{
		int[] result = new int[3];
		
		int sx = point[0];
		int sy = point[1];
		
		int maximum = -1;
		
		ArrayList<Integer> candidates = new ArrayList<Integer>();
		
		for(int i = 0; i < img.length; i++)
		{
			int width = img[i].getWidth();
			int height = img[i].getHeight();
			
			for(int x = 0; x < width; x++)
			for(int y = 0; y < height; y++)
			{
				int count = 0;
				
				for(int a =  -cellWidth; a <  cellWidth; a++)
				for(int b = -cellHeight; b < cellHeight; b++)
				{
					int ix =  x + a;
					int iy =  y + b;
					
					int bx = sx + a;
					int by = sy + b;
					
					if(bx < 0) bx +=  this.width;
					if(by < 0) by += this.height;
					
					if(bx >=  this.width) bx -=  this.width;
					if(by >= this.height) by -= this.height;
					
					if(ix >=  width) ix -=  width;
					if(iy >= height) iy -= height;
						
					if(ix < 0) ix +=  width;
					if(iy < 0) iy += height;
					
					count += (img[i].getRGB(ix, iy) == image.getRGB(bx, by) && collapsed[bx][by]) ? 5 : stability[bx][by];
				}
								
				if(maximum < count)
				{
					maximum = count;
					candidates.clear();
					candidates.add(i);
					candidates.add(x);
					candidates.add(y);
				}
				else if(maximum == count)
				{
					candidates.add(i);
					candidates.add(x);
					candidates.add(y);
				}
			}
		}
				
		int length = candidates.size() / 3;
		int index = (int) (Math.random() * length) * 3;
		
		result[0] = candidates.get(index);
		result[1] = candidates.get(index + 1);
		result[2] = candidates.get(index + 2);
				
		return result;
	}

	//Iterates each possible cell in the bitmap, and determines the cell with the least entropy.
	//If there are multiple cells with the same least entropy, it picks one randomly.
	public int[] pickCollapsePoint(int cellWidth, int cellHeight, boolean loop)
	{
		int[] result = new int[2];
		
		float maximum = -1;
		
		ArrayList<Integer> candidates = new ArrayList<Integer>();
		
		for(int i = 0; i <  width; i++)
		for(int j = 0; j < height; j++)
		{
			if(collapsed[i][j]) continue;
			float count = stability[i][j];
						
			if(maximum < count)
			{
				maximum = count;
				candidates.clear();
				candidates.add(i);
				candidates.add(j);
			}
			else if(maximum == count)
			{
				candidates.add(i);
				candidates.add(j);
			}
		}
		
		if(candidates.size() == 0) return null;
		
		int length = candidates.size() / 2;
		int index = (int) (Math.random() * length) * 2;
		
		result[0] = candidates.get(index);
		result[1] = candidates.get(index + 1);
		
		return result;
	}
}
