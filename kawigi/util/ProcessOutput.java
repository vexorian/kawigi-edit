package kawigi.util;
import java.io.*;

/**
 *	A threaded output listener for a process output stream (either stdout or
 *	stderr).
 **/
public class ProcessOutput implements Runnable
{
	/**
	 *	The thread that listens for output on this stream.
	 **/
	private Thread t;
	/**
	 *	The InputStream representing this output stream.
	 *	
	 *	It may seem strange at first that we get output from an InputStream, but
	 *	consider this to be a pipe from the output of another process.
	 **/
	private InputStream out;
	/**
	 *	The output display component that we pipe into.
	 **/
	private ConsoleDisplay output;
	/**
	 *	Returns true if our thread has exited.
	 **/
	private boolean done;
	
	/**
	 *	Creates a new <code>ProcessOutput</code> that reads <code>out</code>
	 *	into <code>output</code>.
	 **/
	public ProcessOutput(InputStream out, ConsoleDisplay output)
	{
		this.out = out;
		this.output = output;
		output.clear();
	}
	
	/**
	 *	Starts the thread.
	 **/
	public synchronized void start()
	{
		t = new Thread(this);
		t.start();
	}
	
	/**
	 *	Reads characters from the stream until it runs out of them.
	 *	
	 *	Flushes every line so there's a sort of compromise between speed and
	 *	being able to see what's happened so far.
	 **/
	public synchronized void run()
	{
		if (Thread.currentThread() == t)
		{
			try
			{
				int c;
				String s = "";
				while ((c = out.read()) != -1)
				{
					s += (char)c;
					if (c == '\n')
					{
						output.print(s);
						s = "";
					}
				}
				output.print(s);
			}
			catch (IOException ex)
			{
				ex.printStackTrace();
				output.println("***IOException***\n");
				done = true;
			}
			done = true;
		}
	}
	
	/**
	 *	Returns true if this output stream has stopped producing output.
	 **/
	public boolean isDone()
	{
		return done;
	}
	
	/**
	 *	Returns the display component being used by this thread.
	 **/
	public ConsoleDisplay getOutputDisplay()
	{
		return output;
	}
}
