package kawigi.cmd;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import javax.swing.*;

import kawigi.editor.CodePane;
import kawigi.language.EditorLanguage;
import kawigi.language.LanguageFactory;
import kawigi.problem.*;
import kawigi.properties.PrefFactory;
import kawigi.properties.PrefProxy;
import kawigi.util.AppEnvironment;
import kawigi.util.ProcessContainer;
import kawigi.util.StringsUtil;

/**
 *	Action implementation for actions related to local testing.
 *
 *	This includes most of the buttons across the bottom of the KawigiEdit panel.
 **/
@SuppressWarnings("serial")
public class LocalTestAction extends DefaultAction
{
    /**
	 *	Global process - we only allow one process to be running from KawigiEdit
	 *	at a time, so we don't go crazy messing things up on people's machines.
     **/
    private static ProcessContainer proc;
    
    /**
     * Last time when file was saved
     */
    private static long lastSaveTime;
    private static boolean isLoadFileAsked;
	private static boolean isInLoadSaveAction = false;
    private static LocalTestAction saveInstance;
    private static LocalTestAction loadInstance;
    
    private static long syncErrorMessageSaveTime = -1;
    

    /**
	 *	Constructs a new LocalTestAction for the given ActID.
     **/
    public LocalTestAction(ActID cmdid)
    {
        super(cmdid);
        if (cmdid == ActID.actSaveLocal)
            saveInstance = this;
        else if (cmdid == ActID.actLoad)
            loadInstance = this;
    }

    /**
	 *	Returns true if this action can be executed.
     **/
    public boolean isEnabled()
    {
        if (ProblemContext.getCurrentClass() == null)
        {
            // In standalone, we can always generate code (because we ask for
            // the problem statement whenever the button is pushed).  In plugin
            // mode, we can only generate code if we already have a
            // ProblemComponent from the plugin interface.
            return (cmdid == ActID.actGenerateCode && AppEnvironment.getEnvironment() != AppEnvironment.PluginMode) || cmdid == ActID.actOpenLocal;
        }
        if (cmdid == ActID.actKillProcess) {
            return proc != null && !proc.isDone();
        }
        return true;
    }

    /**
	 *	Runs the action!
     **/
    public void actionPerformed(ActionEvent e)
    {
        ClassDecl cl = ProblemContext.getCurrentClass();
        switch (cmdid)
        {
            case actGenerateCode:
                if (AppEnvironment.getEnvironment() != AppEnvironment.PluginMode)
                {
                    ProblemContext.setCurrentClass(cl = ClassDeclFactory.getClassDecl());
                    if (null == cl)
                        break;
                }
				generateStubCode();
				Dispatcher.sourceCodeChanged();
                break;
            case actSaveLocal:
                saveLocal();
                break;
            case actLoad:
                loadFromLocal();
                break;
            case actRunTests:
                saveLocal();
				compileLocal();
                break;
            case actKillProcess:
                if (proc != null && !proc.isDone())
                    proc.kill();
                // On occasion, we don't actually successfully kill the process,
                // and trying again probably won't help.  This leaves the user
                // in a state where they can't do any local compilation and
                // testing until they close down the arena and open it and log
                // back in again, hardly a state you want to be in for a match,
                // regardless of how rare it might be.  In this case, we've done
                // due diligence to kill the process but the OS or the program
                // the user wrote won't let us, so we just nullify the proc so
                // that the user can continue to work anyways.
                proc = null;
                break;
            case actOpenLocal:
                if (Dispatcher.getFileChooser().showOpenDialog(Dispatcher.getTabbedPane()) == JFileChooser.APPROVE_OPTION)
                {
                    File f = Dispatcher.getFileChooser().getSelectedFile();
                    try
                    {
                        BufferedReader inFile = new BufferedReader(new FileReader(f));
                        String text = "";
                        String line;
                        while ((line = inFile.readLine()) != null)
                            text += line + "\n";
                        inFile.close();
                        String filename = f.getName();
                        EditorLanguage lang = LanguageFactory.getLanguage(filename.substring(filename.lastIndexOf('.')+1));
                        CodePane localCodePane = Dispatcher.getLocalCodePane();
                        localCodePane.setContentType("text/" + lang.toString().toLowerCase());
                        localCodePane.setText(text);
                        ((JViewport)localCodePane.getParent()).setViewPosition(new Point(0, 0));
                    }
                    catch (IOException ex)
                    {
                        Dispatcher.getLocalCodePane().setText("IOException thrown!");
                    }
                    Dispatcher.getTabbedPane().setSelectedComponent(Dispatcher.getLocalCodeEditorPanel());
                }
                break;
        }
        Dispatcher.getGlobalDispatcher().UIRefresh();
    }

	public static void generateStubCode()
	{
		EditorLanguage lang = ProblemContext.getLanguage();
		ClassDecl cl = ProblemContext.getCurrentClass();
		Skeleton code = lang.getSkeleton(cl);
		CodePane codePane = Dispatcher.getCodePane();
		codePane.setContentType("text/" + lang.toString());
		codePane.readdUndoListener();
		String s = code.getSource().toString();
		codePane.setText(s);
		codePane.setCaretPosition(code.getCaret());
		codePane.requestFocusInWindow();
		CodePane testCodePane = Dispatcher.getTestCodePane();
		testCodePane.setContentType("text/" + lang.toString());
		testCodePane.readdUndoListener();
		s = lang.getTestCode(cl);
		testCodePane.setText(s);
	}
    /**
	 *	Parses the text between "BEGIN KAWIGIEDIT TESTING" and "END KAWIGIEDIT TESTING"
     *  into test cases, inserts this code into TestPane and inserts into the code tag <%:testing-code%>.
     **/
	protected void restoreTesting(StringBuilder text, ClassDecl cl, EditorLanguage lang)
	{
        StringBuilder tests = new StringBuilder(1000);
        cl.removeAllTests();
        lang.extractTestCases(text, cl, tests);
        Dispatcher.getTestCodePane().setText(tests.toString());
    }

    /**
	 *	Removes from code all between "BEGIN CUT HERE" and "END CUT HERE".
     **/
    protected void removeCutting(StringBuilder text) {
        while (true) {
            int ind = text.indexOf("BEGIN CUT HERE");
            if (-1 == ind)
                return;
			int	ind_s = StringsUtil.lastIndexOf(text, '\n', ind);
			int	ind_e = text.indexOf("END CUT HERE", ind);
            if (-1 == ind_e)
                ind_e = text.length();
            else {
                ind_e = StringsUtil.indexOf(text, '\n', ind_e);
                if (-1 == ind_e)
                    ind_e = text.length();
                else
                    ++ind_e;
            }
            StringsUtil.replace(text, ind_s + 1, ind_e, "");
        }
    }

    private static File getSaveFileObj()
    {
        PrefProxy prefs = PrefFactory.getPrefs();
        ClassDecl cl = ProblemContext.getCurrentClass();
        if (cl == null)
            return null;
        
        String filename = ProblemContext.getLanguage().getFileName(cl.getName());
        return new File(prefs.getWorkingDirectory(), filename);
    }
    
    /**
	 *	Saves the current problem to the local test directory.
     **/
    public static synchronized void saveLocal()
    {
		isInLoadSaveAction = true;
		try {
            File f = getSaveFileObj();
            if (f == null) {
                return;
            }
            boolean existed = f.exists();
            long fileTime = f.lastModified();
            if (existed && fileTime > getLastSaveTime()) {
                int sel = JOptionPane.showConfirmDialog(Dispatcher.getWindow(),
                    "The file on disk was changed by external program.\nAre you sure you want to save it?",
                    "Saving solution code", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (sel == JOptionPane.NO_OPTION) {
                    return;
                }
            }
            String source = Dispatcher.getCodePane().getText();
            source = source.replace(EditorLanguage.sTestingCodeTag, Dispatcher.getTestCodePane().getText());
            try {
                PrintWriter out = new PrintWriter(new FileWriter(f));
                String[] sourceArray = source.split(StringsUtil.sCRLFregex);
                for (String aSourceArray : sourceArray) {
                    out.println(aSourceArray);
                }
                out.flush();
                out.close();
                // We use currentTimeMillis instead of f.lastModified because 
                // f.lastModified does not have a guaranteed precision. (Different
                // file systems / Operating systems ) 
                // synchronization needs to compare lastSaveTime against 
                // last edit time, which is a variable that uses currentTimeMillis();
                // For example, in Linux with ext4, lastModified time was rounded
                // to 1000 miliseconds. If the real  save time was 17499 and the
                // last edit time was 17200. KawigiEdit would think that edit
                // time is higher because modifiedTime would give 17000.
                lastSaveTime = System.currentTimeMillis();
                Dispatcher.logln("Saved "+f.getAbsolutePath());
                Dispatcher.getGlobalDispatcher().runCommand(ActID.actUpdateTestCases);
            } catch (IOException ex) {
                saveInstance.reportError(ex, false);
            }
        } finally {
			isInLoadSaveAction = false;
        }
    }

	public static synchronized void loadFromLocal()
	{
		isInLoadSaveAction = true;
		try {
			File f = getSaveFileObj();
			if (f == null || !f.exists())
				return;
			
			long editTime = Dispatcher.getLastEditTime();
			if (editTime > getLastSaveTime()) {
				int sel = JOptionPane.showConfirmDialog(Dispatcher.getWindow(),
						"There are unsaved changes in the editor window.\nAre you sure you want to load code from external file?",
						"Loading solution code", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
				if (sel == JOptionPane.NO_OPTION) {
					return;
				}
			}
			
			try {
				BufferedReader in = new BufferedReader(new FileReader(f));
				String line;
				StringBuilder text = new StringBuilder(in.readLine());
				
				while ((line = in.readLine()) != null) {
					text.append('\n').append(line);
				}
				in.close();
				loadInstance.removeCutting(text);
				loadInstance.restoreTesting(text, ProblemContext.getCurrentClass(), ProblemContext.getLanguage());
	
				CodePane pane = Dispatcher.getCodePane();
				int caret_pos = pane.getCaretPosition();
				pane.setText(text.toString());
				if (caret_pos < text.length()) {
					pane.setCaretPosition(caret_pos);
				}
				
				lastSaveTime = f.lastModified();
				Dispatcher.resetLastEditTime();
				Dispatcher.getGlobalDispatcher().runCommand(ActID.actUpdateTestCases);			
				Dispatcher.getCodePane().grabFocus();
				Dispatcher.logln("Loaded "+f.getAbsolutePath());
			} catch (IOException ex) {
				loadInstance.reportError(ex, false);
			}
		} finally {
			isInLoadSaveAction = false;
		}
	}

    
    /**
     * Get last time when external file was saved
     * 
	 * @return		Last saving time
     */
    public static long getLastSaveTime()
    {
        return lastSaveTime;
    }
    
    public static void resetLastSaveTime()
    {
        lastSaveTime = 0;
        isLoadFileAsked = false;
    }
    
    /**
	 *	Compiles the saved code for this problem.
     **/
	public void compileLocal()
	{
		try
		{
            JTabbedPane tabb = Dispatcher.getTabbedPane();
            kawigi.widget.SimpleOutputComponent comc, logc, toTab;
            comc = Dispatcher.getCompileComponent();
            logc = Dispatcher.getLogComponent();
            toTab = null;
		    //<vexorian> Just kill the other process
		    if ( ( proc!=null) && (! proc.isDone() ) )
		    {
		        proc.kill();
		        proc=null;
		    }
		    //</vexorian>
			if (proc == null || proc.isDone()) {
                String command = ProblemContext.getLanguage().getCompileCommand(ProblemContext.getCurrentClass().getName(), PrefFactory.getPrefs().getWorkingDirectory().getPath());
				if ( command.equals("")) {
				    // No compile command, skip to run command:
				    proc = null;
                    runLocal();
                } else {
                    Dispatcher.logln("Run: "+command);
                    Process p = Runtime.getRuntime().exec(command, null, PrefFactory.getPrefs().getWorkingDirectory());
                    proc = new ProcessContainer(p, comc, false,
                                    new Runnable() {
                                        public void run() {
                                            runLocal();
                                        }
                                    });
                    proc.start();
                    toTab = comc;
                }
			} else {
			    String err = "Error: Can't compile while another process is running";
			    if (comc != null) {
			        toTab = comc;
			        comc.println(err);
			    } else {
			        toTab = logc;
			        Dispatcher.logln("Error: Can't compile while another process is running");
			    }
			}
			if (tabb != null && toTab != null) {
			    tabb.setSelectedComponent(toTab);
			}
		}
		catch (Exception ex)
		{
			reportError(ex, false);
		}
	}

	/**
	 * Run test program if compilation was successful
	 */
	public void runLocal()
	{
		try {
		    int excode = 0;
            final JTabbedPane tabb = Dispatcher.getTabbedPane();
            final kawigi.widget.SimpleOutputComponent outc, comc, logc;
            kawigi.widget.SimpleOutputComponent toTab = null;
            outc = Dispatcher.getOutputComponent();
            comc = Dispatcher.getCompileComponent();
            logc = Dispatcher.getLogComponent();


            if (proc != null) {
			    excode = proc.endVal();
			    Dispatcher.logln("Compile command finished (exit code = "+proc.endVal()+").");
			    if (excode != 0) {
			        if (logc != null) {
			            toTab = logc;
			        }
			    }
            }

			if (excode == 0) {
                if (comc != null) {
				    comc.println("Compiling finished");
                }
				toTab = ( (outc == null)? comc : outc );
				if (proc == null || proc.isDone()) {
				    String command = ProblemContext.getLanguage().getRunCommand(ProblemContext.getCurrentClass().getName(), PrefFactory.getPrefs().getWorkingDirectory().getPath());
				    Dispatcher.logln("Run: "+command);
					Process p = Runtime.getRuntime().exec(command, null, PrefFactory.getPrefs().getWorkingDirectory());
					proc = new ProcessContainer(p, ((outc == null) ? comc : outc), false, new Runnable() {
                                        public void run() {
                                            if (proc != null) {
                                                int ex = proc.endVal();
                                                Dispatcher.logln("Run command finished (exit code = "+ex+").");
                                                if ( ex != 0 && tabb != null && comc == null && outc == null && logc != null) {
                                                    tabb.setSelectedComponent(logc);
                                                }
                                            }
                                        }
                                    });
					proc.start();
				} else {
				    String err = "Error: Can't start new process while another is running.";
				    toTab = ( (outc == null) ? comc : outc);
				    toTab = ( (toTab == null) ? logc : toTab);
				}
			} else if (comc != null) {
			    toTab = comc;
				comc.println("Compiling errors");
			}
			if ( (toTab != null) && (tabb != null) ) {
			    tabb.setSelectedComponent(toTab);
			}
		} catch (Exception ex) {
			reportError(ex, false);
		}
	}

	/**
	 * Do auto-synchronization of the program text with external file
	 */
    public static synchronized void requestFileSync()
    {
		if (isInLoadSaveAction) {
			return;
		}
        boolean needSync = PrefFactory.getPrefs().getBoolean(ActID.actAutoFileSync.preference, false);
        if (!needSync) {
            return;
        }
        

        File f = getSaveFileObj();
        if (f == null) {
            return;
        }
        
        boolean needSave = false;
        boolean needLoad = false;
        if (!f.exists()) {
            needSave = true;
        } else {
            long fileTime = f.lastModified();
            long saveTime = getLastSaveTime();
            long editTime = Dispatcher.getLastEditTime();

            if (saveTime == 0) {
                if (editTime == 0) {
                    needLoad = PrefFactory.getPrefs().getBoolean(ActID.actPreferFileOpen.preference, false);
                    if (!needLoad) {
                        int sel = JOptionPane.NO_OPTION;
                        if (!isLoadFileAsked) {
                            isLoadFileAsked = true;
                            sel = JOptionPane.showConfirmDialog(Dispatcher.getWindow(),
                                "There is source of problem solution on disk.\nDo you want to load it?",
                                "Source synchronization with file", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
                        }
                        if (sel == JOptionPane.NO_OPTION) {
                            return;
                        }
                        
                        needLoad = true;
                    }
                } else {
                    needSave = true;
                }
            } else if (fileTime <= saveTime) {
                if (editTime > saveTime) {
                    needSave = true;
                }
            } else if (saveTime >= editTime) {
                needLoad = true;
            } else {
                // If preference is set to give priority to external file, do it:
                needLoad = PrefFactory.getPrefs().getBoolean(ActID.actPreferFileOpen.preference, false);
                // Use the variable so the message box appears once per event:
                if ( !needLoad && Math.max(saveTime, fileTime) != syncErrorMessageSaveTime ) {
                    syncErrorMessageSaveTime = Math.max(saveTime, fileTime);
                    JOptionPane.showMessageDialog(Dispatcher.getWindow(),
                            "External file and KawigiEditor were both modified since last save.\nPress Load to load the external file and Save to override it with KawigiEdit's contents.",
                            "Synchronization conflict", JOptionPane.ERROR_MESSAGE);
                    Dispatcher.logln("File synchronization conflict: "+f.getAbsolutePath());
                }
                return;
            }
        }
        
        if (needSave) {
            saveInstance.saveLocal();
        } else if (needLoad) {
            loadInstance.loadFromLocal();
        }
    }
}
