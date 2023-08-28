package main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class Main
{
	private WFCBitmap bitmap;
	private JPanel pCanvas;
	
	private BufferedImage[] imgs;
	
	private int cameraX, cameraY;
	private float mouseX, mouseY, prevMouseX, prevMouseY, zoom;
	
	private boolean lmb, rmb;
	
	public Main()
	{
		cameraX = 0;
		cameraY = 0;
		
		mouseX = 0;
		mouseY = 0;
		prevMouseX = 0;
		prevMouseY = 0;
		
		zoom = 1;
		
		lmb = false;
		rmb = false;
		
		imgs = new BufferedImage[1];
		
		try
		{
			File file = new File("test.png");
			System.out.println("Reading: " + file.getAbsolutePath());
			imgs[0] = ImageIO.read(file);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		
		bitmap = new WFCBitmap(200, 200);
		cameraX = 250;
		cameraY = 250;
		initGUI();
	}
	
	public void runWFC()
	{
		bitmap.reset();
		
		new Thread()
		{
			@Override
			public void run()
			{
				while(bitmap.collapsePoint(imgs, 5, 5, true))
				{
					pCanvas.repaint();
				}
			}
		}.start();
	}
	
	@SuppressWarnings("serial")
	public void initGUI()
	{
		JFrame frame = new JFrame("WFC Art Utility");

		frame.setSize(1280, 720);
		frame.setLayout(new BorderLayout());
		frame.setResizable(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JPanel pMainEast, pMainWest, pMainBelowCanvas;
		
		pMainEast = new JPanel();
		pMainEast.setLayout(new BorderLayout());
		pMainEast.setPreferredSize(new Dimension(400, 0));
		pMainEast.setBackground(new Color(255, 255, 0));
		
		pMainWest = new JPanel();
		pMainWest.setLayout(new BorderLayout());
		pMainWest.setBackground(new Color(0, 255, 255));
		
		pMainBelowCanvas = new JPanel();
		pMainBelowCanvas.setLayout(new BorderLayout());
		pMainBelowCanvas.setBackground(new Color(255, 0, 255));
		pMainBelowCanvas.setPreferredSize(new Dimension(0, 300));
		
		pCanvas = new JPanel()
		{
			@Override
			public void paintComponent(Graphics g)
			{
				canvasPaint(g);
			}
		};
		
		pCanvas.setFocusable(true);
		
		pCanvas.addMouseListener(new MouseListener()
		{
			public void mouseClicked(MouseEvent e) {}
			public void mouseEntered(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}

			@Override
			public void mousePressed(MouseEvent e)
			{
				if(SwingUtilities.isLeftMouseButton(e))
				{
					lmb = true;
				}
				else if(SwingUtilities.isRightMouseButton(e))
				{
					rmb = true;
				}
			}

			@Override
			public void mouseReleased(MouseEvent e)
			{
				if(SwingUtilities.isLeftMouseButton(e))
				{
					lmb = false;
				}
				else if(SwingUtilities.isRightMouseButton(e))
				{
					rmb = false;
				}
			}			
		});
		
		pCanvas.addMouseMotionListener(new MouseMotionListener()
		{
			public void mouseDragged(MouseEvent e)
			{
				mouseX = e.getX() - prevMouseX;
				mouseY = e.getY() - prevMouseY;
				
				prevMouseX = e.getX();
				prevMouseY = e.getY();
				
				cameraX += mouseX;
				cameraY += mouseY;
				
				pCanvas.repaint();
			}

			public void mouseMoved(MouseEvent e)
			{
				mouseX = e.getX() - prevMouseX;
				mouseY = e.getY() - prevMouseY;
				
				prevMouseX = e.getX();
				prevMouseY = e.getY();
			}
		});
		
		pCanvas.addMouseWheelListener(new MouseWheelListener()
		{
			@Override
			public void mouseWheelMoved(MouseWheelEvent e)
			{
				zoom -= (float) e.getUnitsToScroll() / 30;
				if(zoom < 0.5f) zoom = 0.5f;
				pCanvas.repaint();
			}
		});
		
		pCanvas.addKeyListener(new KeyListener()
		{
			public void keyTyped(KeyEvent e) {}

			@Override
			public void keyPressed(KeyEvent e)
			{
				int k = e.getKeyCode();
				
				System.out.println("Is this even working?");
				
				if(k == e.VK_C)
				{
					System.out.println("Collapsing point...");
					bitmap.collapsePoint(imgs, 5, 5, true);
					pCanvas.repaint();
				}
				if(k == e.VK_SPACE)
				{
					runWFC();
				}
			}

			public void keyReleased(KeyEvent e) {}
		});
		
		pMainWest.add(pMainBelowCanvas, BorderLayout.SOUTH);
		pMainWest.add(pCanvas, BorderLayout.CENTER);
		
		frame.add(pMainEast, BorderLayout.EAST);
		frame.add(pMainWest, BorderLayout.CENTER);
		
		initGUIMenuBar(frame);
		
		frame.setVisible(true);
	}
	
	public void canvasPaint(Graphics g)
	{
		int width = pCanvas.getWidth();
		int height = pCanvas.getHeight();
				
		g.setColor(new Color(200, 200, 200));
		g.fillRect(0, 0, width, height);
		
		if(bitmap != null)
		{
			int x = (int) (cameraX - (bitmap.getWidth() / 2) * zoom);
			int y = (int) (cameraY - (bitmap.getHeight() / 2) * zoom);
			g.drawImage(bitmap.getImage(), x, y, (int) (bitmap.getWidth() * zoom), (int) (bitmap.getHeight() * zoom), null);
		}
	}
	
	public void initGUIMenuBar(JFrame frame)
	{
		JMenuBar menuBar = new JMenuBar();
		
		JMenu menuFile = new JMenu("File");
		
		JMenuItem miFileNew = new JMenuItem("New");
		menuFile.add(miFileNew);
	
		JMenuItem miFileOpen = new JMenuItem("Open");
		menuFile.add(miFileOpen);
		
		JMenuItem miFileSave = new JMenuItem("Save");
		menuFile.add(miFileSave);
		
		JMenuItem miFileSaveAs = new JMenuItem("Save as...");
		menuFile.add(miFileSaveAs);
		
		menuFile.addSeparator();
		
		JMenuItem miFileProperties = new JMenuItem("Properties");
		menuFile.add(miFileProperties);
		
		JMenuItem miFileExit = new JMenuItem("Exit");
		menuFile.add(miFileExit);
		
		menuBar.add(menuFile);
		
		frame.setJMenuBar(menuBar);
	}
	
	public static void main(String[] args)
	{
		SwingUtilities.invokeLater(new Thread()
		{
			@Override
			public void run()
			{
				try
				{
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				}
				catch(ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e)
				{
					e.printStackTrace();
					System.exit(1);
				}
				
				new Main();
			}
		});
	}
}
