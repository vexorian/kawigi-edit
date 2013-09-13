package kawigi.widget;
import kawigi.properties.*;
import javax.swing.*;
import java.util.*;
import java.awt.*;

/**
 *	I think seven-segment LED-looking displays are cool.
 *	
 *  This one is rotated 90 degrees and counts down how many points you can still
 *	get for submitting the current problem (you can watch your points slip
 *	through your fingers!)  The graphical code comes from some old game
 *	programming I did.
 **/
@SuppressWarnings("serial")
public class ProblemTimer extends JComponent implements Runnable
{
	/**
	 *	This thread repaints and waits until doomsday.
	 **/
	private Thread anim;
	/**
	 *	This maps TopCoder problem IDs to ProblemTimingInfo objects, which hold
	 *	point values and start times for the problems.
	 **/
	private HashMap<Integer,ProblemTimingInfo> problemTimes;
	/**
	 *	This is the ID of the current problem.
	 **/
	private Integer currentProblemID;
	/**
	 *	This is the delay in milliseconds between updates.
	 **/
	private static int delay;
	/**
	 *	This is the color on the LED display for unlit segments.
	 **/
	private static Color unlit;
	/**
	 *	This is the color on the LED display for lit segments.
	 **/
	private static Color foreground;
	/**
	 *	This is the color on the LED display for the background.
	 **/
	private static Color background;
	
	/**
	 *	Causes the animation delay and colors in the widget to be reset from the
	 *	preferences.
	 **/
	public static void resetPrefs()
	{
		PrefProxy prefs = PrefFactory.getPrefs();
		delay = prefs.getInt("kawigi.timer.delay", 1000);
		foreground = prefs.getColor("kawigi.timer.foreground", Color.green);
		background = prefs.getColor("kawigi.timer.background", Color.black);
		unlit = prefs.getColor("kawigi.timer.unlit", new Color(0, 63, 0));
	}
	
	/**
	 *	Calls resetDelay() when this class is loaded.
	 **/
	static
	{
		resetPrefs();
	}
	
	/**
	 *	Where each number's segments are (the first index is the digit, the
	 *	second is the segment number, as indicated in the comments in the
	 *	constructor.
	 **/
	private Polygon[][] segments;
	
	/**
	 *	These magic numbers tell me which segments to light up for each digits.
	 *	They're each 10-bit bitmasks.
	 **/
	private final int[] segmentkarnaughmaps = {1005, 927, 1019, 877, 325, 881, 892};
	
	/**
	 *	Creates the new widget and initializes the polygons (aren't you glad I
	 *	only generate those once?)
	 **/
	public ProblemTimer()
	{
		problemTimes = new HashMap<Integer,ProblemTimingInfo>();
		//this is for testing offline:
		select(1, 1000);
		setBackground(Color.black);
		/*
			info on segments array:
			3 digits of 7 segments - indexed [digit index][segment index].
			basically, I'll make it go like this:
				top segment - 0
				top-right segment - 1
				bottom-right segment - 2
				bottom segment - 3
				bottom-left segment - 4
				top-left segment - 5
				middle segment - 6
				
				 <0>
				A   A
				5   1
				V   V
				 <6>
				A   A
				4   2
				V   V
				 <3>
			
			Each segment has an associated "karnaugh map" in that array of
			constants above.  The way those work is that the nth least
			significant digit of each binary number	determines whether the
			segment should be lit up for n.  For instance, the magic number for
			segment 4 is 325, which is 0101000101 in binary.  Starting at the
			end of the number, this means that segment 4 should light up for 0,
			2, 6 and 8.  Segment 1's number is 1005, which is 1111101101 in
			binary.  This means that segment 1 is lit up for 0, 2, 3, 5, 6, 7, 8
			and 9, because the 0th, 2nd, 3rd, 5th, 6th, 7th and 8th least
			signficant digits are ones.
		*/
		// prototype is a seven-segment digit if it were at 0,0 and right-side
		// up.  I'm going to copy it over and offset it to various locations,
		// and also turn it on its side.  Part of the reason I make them first
		// right-side up is that I'm reusing code from another project ;-)
		Polygon[] prototype = new Polygon[7];
		for (int i=0; i<7; i++)
			prototype[i] = new Polygon();
		// top segment
		prototype[0].addPoint(2,1);
		prototype[0].addPoint(3,0);
		prototype[0].addPoint(8,0);
		prototype[0].addPoint(9,1);
		prototype[0].addPoint(8,2);
		prototype[0].addPoint(3,2);
		// top-right segment
		prototype[1].addPoint(10,2);
		prototype[1].addPoint(11,3);
		prototype[1].addPoint(11,8);
		prototype[1].addPoint(10,9);
		prototype[1].addPoint(9,8);
		prototype[1].addPoint(9,3);
		// bottom-right segment
		prototype[2].addPoint(10,11);
		prototype[2].addPoint(11,12);
		prototype[2].addPoint(11,17);
		prototype[2].addPoint(10,18);
		prototype[2].addPoint(9,17);
		prototype[2].addPoint(9,12);
		// bottom segment
		prototype[3].addPoint(2,19);
		prototype[3].addPoint(3,20);
		prototype[3].addPoint(8,20);
		prototype[3].addPoint(9,19);
		prototype[3].addPoint(8,18);
		prototype[3].addPoint(3,18);
		// bottom-left segment
		prototype[4].addPoint(1,11);
		prototype[4].addPoint(0,12);
		prototype[4].addPoint(0,17);
		prototype[4].addPoint(1,18);
		prototype[4].addPoint(2,17);
		prototype[4].addPoint(2,12);
		// top-left segment
		prototype[5].addPoint(1,2);
		prototype[5].addPoint(2,3);
		prototype[5].addPoint(2,8);
		prototype[5].addPoint(1,9);
		prototype[5].addPoint(0,8);
		prototype[5].addPoint(0,3);
		// middle segment
		prototype[6].addPoint(2,10);
		prototype[6].addPoint(3,9);
		prototype[6].addPoint(8,9);
		prototype[6].addPoint(9,10);
		prototype[6].addPoint(8,11);
		prototype[6].addPoint(3,11);
		segments = new Polygon[6][7];
		for (int i=0; i<6; i++)
		{
			for (int j=0; j<7; j++)
				segments[i][j] = new Polygon();		//initialize empty polygons
		}
		for (int k=0; k<6; k++)
			for (int i=0; i<7; i++)
				for (int j=0; j<prototype[i].npoints; j++)
					segments[k][i].addPoint(prototype[i].ypoints[j]+3, 100-(prototype[i].xpoints[j]+k*15));
		PrefProxy prefs = PrefFactory.getPrefs();
		if (prefs.getBoolean("kawigi.timer.show", true))
			start();
		else
			stop();
	}
	
	/**
	 *	Starts the update thread and figures out the size of the timer widget.
	 *	
	 *	Should be called once this component is actually on some container and
	 *	possibly visible.
	 **/
	public void start()
	{
		setPreferredSize(new Dimension(25, 100));
		Dimension maxSize = getMaximumSize();
		maxSize.width = 25;
		setMaximumSize(maxSize);
		setMinimumSize(getPreferredSize());
		if (anim == null)
		{
			anim = new Thread(this);
			anim.start();
		}
	}
	/**
	 *	The method run by the update thread.
	 *	
	 *	Repeatedly repaints and waits until it's interrupted.
	 **/
	public synchronized void run()
	{
		while (Thread.currentThread() == anim)
		{
			repaint();
			try
			{
				wait(delay);
			}
			catch (InterruptedException ex)
			{
				break;
			}
		}
	}
	
	/**
	 *	Draws the seven-segment display with the current point value.
	 *	
	 *	In KawigiEdit 1.1, this used Java's DecimalFormat to format the score
	 *	as a string and then aligned the display to the decimal point.
	 *	Unfortunately this was a stupid idea (even though it was simple to code)
	 *	because it needed to be able to run on platforms where the decimal point
	 *	was not '.'.  Of course, the code could have been fixed to use the
	 *	decimal point symbol of the current locale, or to force the formatter to
	 *	use the US locale, but I decided rather to repent and figure out the
	 *	digits to display arithmetically.
	 **/
	public void paint(Graphics g)
	{
		if (g == null)
			return;
		g.setColor(background);
		g.fillRect(0, 0, getSize().width, getSize().height);
		double points = 0;
		if (currentProblemID != null && problemTimes.containsKey(currentProblemID))
			points = ((ProblemTimingInfo)problemTimes.get(currentProblemID)).currentScore();
		points /= 1000;
		boolean nonzero = false;
		g.setColor(foreground);
		g.drawRect(21, 41, 1, 1);
		for (int i=0; i<6; i++, points *= 10)
		{
			int digit = ((int)points)%10;
			if (digit != 0)
				nonzero = true;
			if (nonzero)
			{
				for (int j=0; j<7; j++)
				{
					if (((segmentkarnaughmaps[j]>>digit)&1) > 0) //if there is a 1 in the karnaugh map for this digit at this location
						g.setColor(foreground);
					else
						g.setColor(unlit);
					g.fillPolygon(segments[i][j]);
				}
			}
		}
	}
	
	/**
	 *	Changes which problem is being looked at.
	 *	
	 *	If problemID is a new problem, it starts the timer for that problem.
	 *	Otherwise, it reloads the data it has for when the problem was started
	 *	(note that <tt>points</tt> is ignored in this case).
	 **/
	public void select(int problemID, double points)
	{
		if (!problemTimes.containsKey(currentProblemID = new Integer(problemID)))
		{
			problemTimes.put(currentProblemID, new ProblemTimingInfo(points, System.currentTimeMillis(), problemID));
		}
	}
	
	/**
	 *	Kills the timer thread.
	 **/
	public synchronized void stop()
	{
		if (anim != null)
		{
			anim = null;
			notify();
		}
		
		setPreferredSize(new Dimension(0, 0));
		Dimension maxSize = getMaximumSize();
		maxSize.width = 0;
		setMaximumSize(maxSize);
		setMinimumSize(getPreferredSize());
	}
}
