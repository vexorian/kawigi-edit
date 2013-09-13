package kawigi.widget;

/**
 *	This class is used to persist data about timing on a specific problem that
 *	you've opened.  This persistence allows KawigiEdit to show correct timing
 *	info for a problem that you closed and reopened, as long as you didn't exit
 *	the contest applet in between.
 **/
public class ProblemTimingInfo
{
	/**
	 *	The point value of the problem.
	 **/
	private double points;
	/**
	 *	The time that the problem was opened.
	 **/
	private long startTime;
	/**
	 *	TopCoder's unique ID of the current problem.
	 **/
	private int ID;
	/**
	 *	The length of the current contest.
	 **/
	private int contestLength;
	
	/**
	 *	Constructs a new ProblemTimingInfo object and initializes variables.
	 *	
	 *	Note that the contest length is assumed to be 75 minutes here.
	 **/
	public ProblemTimingInfo(double points, long startTime, int problemID)
	{
		this.points = points;
		this.startTime = startTime;
		ID = problemID;
		contestLength = 75*60*1000;
	}
	
	/**
	 *	Returns the point value for this problem.
	 **/
	public double getPoints()
	{
		return points;
	}
	
	/**
	 *	Returns the unique ProblemID of this problem from TopCoder.
	 **/
	public int getProblemID()
	{
		return ID;
	}
	
	/**
	 *	Returns the time that this problem was opened.
	 **/
	public long getStartTime()
	{
		return startTime;
	}
	
	/**
	 *	Sets the length of the contest.
	 **/
	public void setContestLength(int minutes)
	{
		contestLength = minutes*60*1000;
	}
	
	/**
	 *	Returns the score on the current problem if it were submitted right now.
	 **/
	public double currentScore()
	{
		long soFar = System.currentTimeMillis()-startTime;
		double score = points*(.3 + .7*contestLength*contestLength/(10.0*soFar*soFar + (double)contestLength*contestLength));
		return score;
	}
}
