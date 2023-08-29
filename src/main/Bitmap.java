package main;

import java.awt.Color;
import java.awt.image.BufferedImage;

public class Bitmap
{
	protected int width, height;
	protected BufferedImage image;
	
	public Bitmap(int width, int height)
	{
		this.width = width;
		this.height = height;
		
		image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		for(int x = 0; x < width; x++)
		for(int y = 0; y < height; y++)
		{
			image.setRGB(x, y, new Color(0, 0, 0, 0).getRGB());
		}
	}
	
	public int getWidth()
	{
		return width;
	}
	
	public int getHeight()
	{
		return height;
	}
	
	public BufferedImage getImage()
	{
		return image;
	}
}
