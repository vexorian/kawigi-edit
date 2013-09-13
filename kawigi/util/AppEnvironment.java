package kawigi.util;

/**
 *	This enum class tells us whether we're in plugin mode or standalone mode.
 **/
public enum AppEnvironment
{
	/**
	 *	As its name implies, this is when there is no mode set.
	 **/
	UndefinedMode,
	/**
	 *	The mode we're in if we're being loaded as a plugin.
	 *	
	 *	TopCoder classes are expected to be used.
	 **/
	PluginMode,
	/**
	 *	The mode we're in if KawigiEdit is being run as a standalone
	 *	application.
	 *	
	 *	TopCoder classes will not be used and do not need to be in the classpath
	 *	in this mode.
	 **/
	ApplicationMode;
	
	/**
	 *	The current KawigiEdit version.
	 **/
	public static final String VERSION = "2.0";
	
	/**
	 *	The mode that KawigiEdit is currently in.
	 **/
	private static AppEnvironment currentMode = UndefinedMode;
	
	/**
	 *	Sets the current application mode.
	 **/
	public static void setEnvironment(AppEnvironment env)
	{
		currentMode = env;
	}
	
	/**
	 *	Returns the current application mode.
	 **/
	public static AppEnvironment getEnvironment()
	{
		return currentMode;
	}
}
