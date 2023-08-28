package main;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class WFCBitmap extends Bitmap
{
	private boolean[][] collapsed;
	
	public WFCBitmap(int width, int height)
	{
		super(width, height);
		collapsed = new boolean[width][height];
	}
	
	public void reset()
	{
		for(int i = 0; i < width; i++)
		for(int j = 0; j < height; j++)
		{
			collapsed[i][j] = false;
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
		int[] imgCollapse = pickImageCollapsePoint(img, bmCollapse, cellWidth, cellHeight);
		
		for(int i = 0; i < cellWidth; i++)
		for(int j = 0; j < cellHeight; j++)
		{
			int ix = bmCollapse[0] + i;
			int iy = bmCollapse[1] + j;
			
			if(loop)
			{
				if(ix >= width) ix -= width;
				if(iy >= height) iy -= height;
				
				if(ix < 0) ix += width;
				if(iy < 0) iy += height;
			}
			else
			{
				if(ix >= width || iy >= height || ix < 0 || iy < 0) continue;
			}
			
			if(collapsed[ix][iy]) continue;
			
			collapsed[ix][iy] = true;
			
			BufferedImage im = img[imgCollapse[0]];
			
			int imgX = (imgCollapse[1] + i) % im.getWidth();
			int imgY = (imgCollapse[2] + j) % im.getHeight();
			
			image.setRGB(ix, iy, im.getRGB(imgX, imgY));
		}
		
		return true;
	}
	
	public int[] pickImageCollapsePoint(BufferedImage[] img, int[] point, int cellWidth, int cellHeight)
	{
		int[] result = new int[3];
		
		int sx = point[0];
		int sy = point[1];
		
		int maximum = -1;
		int cutoff = cellWidth * cellHeight;
		
		ArrayList<Integer> candidates = new ArrayList<Integer>();
		
		for(int i = 0; i < img.length; i++)
		{
			int width = img[i].getWidth();
			int height = img[i].getHeight();
			
			for(int x = 0; x < width; x++)
			for(int y = 0; y < height; y++)
			{
				int count = 0;
				
				for(int a = 0; a < cellWidth; a++)
				for(int b = 0; b < cellHeight; b++)
				{
					int ix = x + a;
					int iy = y + b;
					
					int bx = sx + a;
					int by = sy + b;
					
					if(bx < 0) bx += this.width;
					if(by < 0) by += this.height;
					
					if(bx >= this.width) bx -= this.width;
					if(by >= this.height) by -= this.height;
					
					if(ix >= width) ix -= width;
					if(iy >= height) iy -= height;
						
					if(ix < 0) ix += width;
					if(iy < 0) iy += height;
					
					count += (img[i].getRGB(ix, iy) == image.getRGB(bx, by) && collapsed[bx][by]) ? 1 : 0;
				}
				
				if(count >= cutoff) continue;
				
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

	public int[] pickCollapsePoint(int cellWidth, int cellHeight, boolean loop)
	{
		int[] result = new int[2];
		
		int maximum = -1;
		int cutoff = (cellWidth * cellHeight);
		
		ArrayList<Integer> candidates = new ArrayList<Integer>();
		
		for(int i = 0; i < width; i++)
		for(int j = 0; j < height; j++)
		{
			int count = 0;
			
			for(int x = 0; x < cellWidth; x++)
			for(int y = 0; y < cellHeight; y++)
			{
				int ix = i + x;
				int iy = j + y;
				
				if(loop)
				{
					if(ix >= width) ix -= width;
					if(iy >= height) iy -= height;
					
					if(ix < 0) ix += width;
					if(iy < 0) iy += height;
				}
				else
				{
					if(ix >= width || iy >= height || ix < 0 || iy < 0) continue;
				}
				
				count += collapsed[ix][iy] ? 1 : 0;
			}
			
			if(count >= cutoff) continue;
			
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
