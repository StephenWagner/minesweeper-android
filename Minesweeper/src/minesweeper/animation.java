/*Stephen Wagner
 * 14 May 2014
 * D4
 * Coach
 * Final Project
 */

package minesweeper;


import java.applet.Applet;
import java.applet.AudioClip;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.Object;
import java.net.URL;
import java.util.TimerTask;
import java.util.Timer;

import javax.imageio.ImageIO;

//**************************SETTINGS FRAME****************************
class toolbar extends Frame 
{
	Button apply;
	animation parent;
	Label startLabel;

	toolbar(String title, animation p) 
	{
		super(title);
		parent = p;

		setLayout(new FlowLayout(FlowLayout.LEFT));

		// add a label
		startLabel = new Label("Press the button to see what I think about my my frustrating project");
		add(startLabel);

		// add button to apply settings changes
		apply = new Button("Press");
		add(apply);

	}

	// hide window when terminated by user
	public boolean handleEvent(Event evtObj) 
	{
		if (evtObj.id == Event.WINDOW_DESTROY) 
		{
			setVisible(false);
			return true;
		}
		return super.handleEvent(evtObj);
	}

	public boolean action(Event evtObj, Object arg) 
	{

		if (evtObj.target instanceof Button) 
		{
			parent.startAnim();
			return true;
		}

		return false;
	}

}



// Main applet code*********************************
public class animation extends Applet 
{
	
	boolean anim = false;
	Button btn;
	toolbar fr;
	private AudioClip boom;
	private Image explosion[], tile;
	private int totalImages = 25, // total number of images
			currentImage = 0, // current image subscript
			sleepTime = 100; // milliseconds to sleep
	private BufferedImage explosion1;
	MediaTracker imageTracker;
	Timer myTimer;
	
	
	
	// load the images when the applet begins executing
	public void init() 
	{
		this.setSize(420, 420);
		fr = new toolbar("Toolbar", this);
		fr.setVisible(true);
		fr.setSize(400, 100);
		
		//load sound file
		boom = getAudioClip(getCodeBase(),"pics/boom.au");
		
		//load tile image
		tile = getImage(getCodeBase(),"pics/10.png");
		
		//timer
		myTimer = new Timer(true);
		myTimer.schedule(new TimerTask() 
		{
			public void run() 
			{
				repaint();
			}
		}, 0, sleepTime);

		explosion = new Image[totalImages];

		try 
		{
			explosion1 = ImageIO.read(new URL(getCodeBase() + "pics/explosion25.png"));//new File("pics/explosion25.png"));
		}

		catch (IOException e) 
		{
			e.printStackTrace();
		}

		imageTracker = new MediaTracker(this);

		for (int i = 0, count = 0; i < 5; i++) 
		{
			for (int j=0; j<5; j++) 
			{
				explosion[count] = explosion1.getSubimage(j*64, i*64, 64,64);
				imageTracker.addImage(explosion[count], count);
				count++;
			}
		}

		try 
		{
			imageTracker.waitForID(0);
		}
		catch (InterruptedException e) 
		{}
	}

	
	
	
	public void start(Graphics g) 
	{
		g.drawImage(explosion[0], 0, 0, 300, 300, this);
		currentImage = 1;
	}

	
	
	
	public void startAnim() 
	{
		anim = true;
		currentImage = 0;
	}

	
	
	
	public void paint(Graphics g) 
	{
		//sets up the tiles when the applet is opened
		if (!anim)
		{
			for (int i=0; i<16; i++)
				for (int j=0; j<16; j++)
				{
					g.drawImage(tile,j*25+10,i*25+10,25,25,this);
				}
		}

		//plays the animation of explosion
		if (imageTracker.checkID(currentImage, true)) 
		{
			if (anim) 
			{
				g.drawImage(explosion[currentImage], 50, 50, 300, 300, this);
				if (currentImage == 0)
					explosion[totalImages - 1].flush();
				else
					explosion[currentImage - 1].flush();

				currentImage = ++currentImage % totalImages;
				
				if(currentImage==0)
				{
					anim=false;
					super.update(g);
				}
				
				//play sound
				boom.play();
				
				//display a large "Game Over" on the screen
				g.setFont(new Font("TimesRoman", Font.BOLD, 40));
				g.setColor(Color.red);
				g.drawString("Game Over", 100, 200);
			}
		} 
		else
			postEvent(new Event(this, Event.MOUSE_ENTER, ""));
	}
	
	
	
	// override update to eliminate flicker
	public void update(Graphics g) 
	{
		paint(g);
	}

}
