package kawigi.util;
import kawigi.properties.*;

/**
 *	Waits for the number of seconds specified in the configuration and then
 *	abruptly stops the process if it hasn't already ended.
 **/
public class KillThread extends Thread
{
	private ProcessContainer proc;
	
	/**
	 *	Creates a new KillThread to monitor proc.
	 **/
	public KillThread(ProcessContainer proc)
	{
		this.proc = proc;
	}
	
	/**
	 *	Waits for a certain number of seconds and then kills the process if it
	 *	is not yet finished.
	 **/
	public synchronized void run()
	{
		int timeout = PrefFactory.getPrefs().getInt("kawigi.timeout", 10);
		if (currentThread() == this)
			try
			{
				wait(timeout*1000);
			}
			catch (InterruptedException ex)
			{
			}
		if (!proc.isDone())
		{
			proc.kill();
			proc.getErrDisplay().println("Process Timed Out!!");
		}
	}
}
