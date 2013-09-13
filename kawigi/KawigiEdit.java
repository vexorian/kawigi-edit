package kawigi;
import java.awt.Container;
import kawigi.util.*;
import kawigi.widget.PluginOutsideFrame;
import kawigi.widget.VerticalPanel;
import kawigi.editor.*;
import kawigi.ahmed_aly.CPPProcessor;
import kawigi.cmd.*;
import kawigi.problem.*;
import javax.swing.*;

import com.topcoder.shared.language.*;
import com.topcoder.client.contestant.*;

/**
 *  This class is the actual TopCoder arena plugin.
 *
 *  When KawigiEdit is run as a plugin, this is where everything basically
 *  starts.
 *
 *  <h2>Kawigi's Discourse on KawigiEdit's Source Code</h2>
 *
 *  You'll notice that most of KawigiEdit's source code files don't know
 *  anything about TopCoder classes (this one is necessarily an exception, since
 *  the plugin interface needs to pass me TopCoder-defined objects).  This is to
 *  make it so that KawigiEdit can be run as a standalone application without
 *  ContestApplet.jar needing to be in the classpath.  The classes that do still
 *  reference TopCoder classes directly are currently:
 *  <ul>
 *      <li>kawigi.KawigiEdit (for reasons mentioned above)</li>
 *      <li>kawigi.properties.TCPrefs (in general, this will be referenced by
 *      the kawigi.properties.PrefProxy interface, which is TopCoder-agnostic
 *      and has another implementation for Standalone mode)</li>
 *      <li>kawigi.problem.TCProblemConverter, which converts TopCoder's
 *      ProblemComponent and Language objects into KawigiEdit objects to be used
 *      by the rest of the application.  This is also usually called through an
 *      interface, but it has different calling semantics than the standalone
 *      equivalent, which is the ProblemParser (and it works better).</li>
 *  </ul>
 *  Aside from classes that have to decide whether to use those classes or
 *  standalone classes, the only thing that currently requires the code to check
 *  which mode we're in is the action for the Generate Code button, which does
 *  something completely different in standalone mode than in plugin mode.  In
 *  general, where possible, any additions made to KawigiEdit should try to
 *  conform to that convention where a class is either strictly specific to
 *  plugin mode or doesn't reference TC classes directly.  An interface/factory
 *  pattern is an easy way to do this in most cases.
 *
 *  I'm assuming that people wanting to modify KawigiEdit will start by looking
 *  at this file.  There isn't much here in the way of juicy code, but that does
 *  make it an opportune place to describe the general organization of the code
 *  and make suggestions on how to modify it if you so desire.  Note that the
 *  HTML will be more readable if you look at the javadocs that should have been
 *  in the jar with any official release of KawigiEdit.
 *
 *  First, here's a rundown of what's in each package.  For those of you who are
 *  less initiated in Java, a package is basically a folder full of classes.  In
 *  KawigiEdit, the source files are found in those folders along with the class
 *  files.  The <code>kawigi</code> package is found in the <code>kawigi</code>
 *  folder, and the <code>kawigi.cmd</code> package is found in the
 *  <code>kawigi/cmd</code> folder.  Also, the source code for each public class
 *  is always in a source code file of the same name, so the source code for
 *  <code>kawigi.cmd.ActID</code> is in <code>kawigi\cmd\ActID.java</code>.
 *  <ul>
 *      <li><b>kawigi.*</b> - Just has <code>KawigiEdit</code> (the plugin) and
 *      <code>KawigiEditStandalone</code> (the standalone executable).</li>
 *      <li><b>kawigi.cmd.*</b> - This contains KawigiEdit 2.0's new command and
 *      UI infrastructures.  A pretty large percentage of what actually
 *      <i>happens</i> in KawigiEdit, when you click on buttons, use context
 *      menus, add snippets, hit keystrokes in the editor, or even just open up
 *      KawigiEdit, stuff is happening in this package.  The classes in this
 *      package fall into a few categories:
 *      <ol>
 *          <li>Most of the classes here are Action classes.  Each Action class
 *          manages properties and execution of some set of related commands.
 *          </li>
 *          <li>A few of the classes are Context classes - these hold data that
 *          instances of some Action might need to share.</li>
 *          <li>There are two public enums in this package - ActID and MenuID.
 *          Both of these may be important to you if you want to add commands
 *          to KawigiEdit.  Also, reading through ActID.java will give you a
 *          good idea of what KawigiEdit knows how to do.</li>
 *          <li>There are two very important classes that are the core
 *          implementation of command/action processing and GUIs in KawigiEdit.
 *          the Dispatcher class <i>is</i> KawigiEdit 2.0's command
 *          infrastructure, and one could theoretically use it and some ActIDs
 *          to automate certain processes in KawigiEdit beyond what is already
 *          done.  The UIHandler class is KawigiEdit 2.0's UI infrastructure.
 *          Its main purpose is to read .ui files (either loaded from resources
 *          or from customizations saved locally by the user).  The .ui files
 *          are XML files that specify the hierarchy and organization of some
 *          GUI container, and the UIHandler basically reads these and converts
 *          them into GUI components.  It also binds these GUI components to
 *          the appropriate actions from the Dispatcher and saves off important
 *          controls to the Dispatcher for use by the Actions.</li>
 *          <li>The WindowListener implementation for KawigiEditStandalone is
 *          also in this package, mainly for lack of a better place to put it.
 *          </li>
 *      </ol></li>
 *      <li><b>kawigi.editor.*</b> - This package has the implementation behind
 *      the editor part of KawigiEdit.  This includes:
 *      <ol>
 *          <li>Classes that customize the editor window itself -
 *          <code>CodePane</code>, <code>ConfigurableEditorKit</code> and
 *          <code>ObedientViewFactory</code>.</li>
 *          <li>"View" classes, which manage rendering the text.  That includes
 *          syntax highlighting and any other rendering actions.</li>
 *          <li>There are a few "accessory" classes that do things that go
 *          around the editor itself - <code>Interval</code>,
 *          <code>LineNumbers</code> and <code>EditorPanel</code>.</li>
 *          <li>Finally, there's the <code>KawigiEditKeyMap</code>, which sort
 *          of connects the <code>kawigi.editor</code> package to the
 *          <code>kawigi.cmd</code> package by mapping keystrokes to actions.
 *          </li>
 *      </ol></li>
 *      <li><b>kawigi.language.*</b> - This package is responsible for
 *      supporting language-specific features besides syntax highlighting.  This
 *      includes:
 *      <ol>
 *          <li>Two fairly important enums, <code>EditorLanguage</code> and
 *          <code>EditorDataType</code>, which are basically KawigiEdit's
 *          versions of classes defined by TopCoder.  Aside from the obvious
 *          stuff, the EditorLanguage enum is the way to get language-specfic
 *          settings (like how to save, compile and run code in that language),
 *          and it also has a method called prepareValForCode, which is the result of
 *          lots of learning about where it's harder to generate valid testing
 *          code in various languages.  Fixing these problems in the past has
 *          sometimes destablized other code generation scenarios, but in the
 *          end, KawigiEdit should generate valid and correct testing code for
 *          all languages all the time.  The <code>LanguageFactory</code> class
 *          can be used to get <code>EditorLanguage</code>s by name.</li>
 *          <li>The rest of the classes in this package are test code generators
 *          for each language.</li>
 *      </ol></li>
 *      <li><b>kawigi.problem.*</b> - This package has problem description and
 *      code-generation stuff that isn't language-specific:
 *      <ol>
 *          <li>Some of these are representations of parts of the problem -
 *          <code>ClassDecl</code>, <code>MethodDecl</code> and
 *          <code>Test</code>.</li>
 *          <li>Some are used to create <code>ClassDecl</code>s -
 *          <code>ClassDeclGenerator</code>, <code>ProblemParser</code>,
 *          <code>TCProblemConverter</code> and <code>ClassDeclFactory</code>.
 *          </li><code>Skeleton</code> is related to skeleton code generation.</li>
 *      </ol></li>
 *      <li><b>kawigi.properties.*</b> - This package is how KawigiEdit manages
 *      preferences and settings.  Outside packages will basically always access
 *      this functionality through the <code>PrefFactory</code> class and the
 *      <code>PrefProxy</code> interface.  The only <code>PrefProxy</code>
 *      implementation that should ever really be referenced directly outside of
 *      this package is the <code>ChainedPrefs</code> class, which is used
 *      directly by the config dialog.  Perhaps the other implementations
 *      shouldn't even be public, but I don't have a good enough reason to
 *      "lock" them up.</li>
 *      <li><b>kawigi.util.*</b> - Usually, "util" packages in Java are a wide
 *      variety of helpful classes that don't belong anywhere else, but this
 *      package is mostly pretty unified.  The only odd-ball class in here is
 *      the AppEnvironment enum, which keeps track of what mode KawigiEdit is
 *      running in (standalone or plugin mode).  All the other classes in here
 *      are used to enable KawigiEdit to start and manage native processes,
 *      which enables language-independent compiling and running of test code.
 *      The <code>ProcessContainer</code> class executes a process, capturing
 *      the output from the process using to asynchronous
 *      <code>ProcessOutput</code>s which display output on an implementation of
 *      <code>ConsoleDisplay</code>.  If the process doesn't terminate in a
 *      reasonable amount of time (configured by the user), the
 *      <code>KillThread</code> will attempt to end the process forcibly.</li>
 *      <li><b>kawigi.widget.*</b> - I'm a user interface programmer at heart,
 *      so in that sense, it shouldn't be surprising that the package with all
 *      my custom controls is one of the biggest packages in KawigiEdit.  Well,
 *      don't be alarmed about it.  A class ended up here in one of a few ways:
 *      <ol>
 *          <li>Java's implementation of some control didn't adequately support
 *          some property I needed to expose through Actions in order to fit
 *          things in my command infrastructure.  These won't appear to be
 *          real custom widgets to the user, they look just like Java's default
 *          stuff.  This is what <code>ActionLabel</code>,
 *          <code>ActionSpinner</code>, <code>ActionStateCheckBox</code>,
 *          <code>ActionStateRadioButton</code>, <code>ActionTextField</code>
 *          and <code>HideableButton</code> are.</li>
 *          <li>It was more convenient either for .ui XML or for mapping Actions
 *          to groups of UI elements to make a special or aggregated control.
 *          Again, these won't look like particularly exciting custom widgets.
 *          This includes <code>FilePanel</code>, <code>FontPanel</code>,
 *          <code>HorizontalPanel</code> and <code>VerticalPanel</code>.</li>
 *          <li>I required a control to do something somewhat abnormal, and it
 *          easiest to subclass an existing control and add the functionality
 *          this way.  This includes <code>Snippet</code> and
 *          <code>Category</code>, as well as
 *          <code>SimpleOutputComponent</code>.</li>
 *          <li>I really just wanted a control type that just plain didn't exist
 *          in Java.  This explains the <code>ColorSwatch</code>,
 *          <code>ColorSwatchDropdown</code> and <code>ProblemTimer</code>.</li>
 *          <li>The last few classes are just things used by the custom control
 *          classes (<code>ProblemTimingInfo</code> is needed by
 *          <code>ProblemTimer</code>, <code>ChipIcon</code> is used by
 *          <code>ColorSwatchDropdown</code>).</li>
 *      </ol></li>
 *  </ul>
 *
 *  Next, it might be useful to understand the resources included with and used
 *  by KawigiEdit.
 *
 *  Aside from a bunch of icons and images (that are mostly referenced in
 *  ActID.java, although some aren't used), there are four .words files directly
 *  in the rc folder, one for each language.  These are text files that contain
 *  keywords and tokens and how the Views should highlight them.
 *
 *  Then there's a folder called rc/templates which contains the default
 *  template for each language.  There's no reason to change these directly,
 *  since you can set it to use a modified version of the template using the
 *  KawigiEdit settings dialog.  If you think one of the templates should be
 *  modified for everyone, let me know.
 *
 *  Finally, there's a folder called rc/ui which has a bunch of .ui files in it.
 *  The .ui files are XML representations of GUI hierarchies, and each one is
 *  loaded for different reasons (kawigi/cmd/MenuID.java will give you some idea
 *  of what each one is for).  If you want to change the organization of the UI
 *  in KawigiEdit, you can edit these directly, or alternatively, you can write
 *  your own version and save it somewhere, then change your config file
 *  (contestapplet.conf for KawigiEdit as a plugin, or KawigiEdit.properties in
 *  standalone mode) to set a property called
 *  <code>kawigi.ui.[a name from MenuID.java]</code> to be the path to your
 *  version of the .ui file.
 *
 *  Perhaps I'll someday make a way to easily add commands to KawigiEdit without
 *  modifying KawigiEdit at all.  While I don't mind people hacking up
 *  KawigiEdit to their liking, it makes it hard for people to upgrade to a new
 *  version, so consider that if you want to make private modifications to the
 *  source code and try to make it as easy as possible to re-modify the
 *  KawigiEdit sources you touched.  For now, the best way to add commands to
 *  KawigiEdit is basically like this:
 *  <ol>
 *      <li>Create a class (perhaps in your own package) that extends
 *      <code>kawigi.cmd.DefaultAction</code>.  The constructor of your action
 *      should take an ActID as a parameter if it's a global action or an ActID
 *      and CodePane as parameters if it's an editing action.</li>
 *      <li>Edit ActID.java and add elements to that enum for any commands you
 *      want to make.  You can use the existing commands as a pattern, and you
 *      want to use your own action class as the class for your commands.  You
 *      may need to use a fully qualified classname for it to be found (i.e.
 *      package.classname.class).</li>
 *      <li>Implement what should happen when your command is executed in the
 *      <code>actionPerformed</code> method of your action class.  You may also
 *      want to customize the behavior of <code>isEnabled</code> or
 *      <code>isVisible</code> (note that <code>isVisible</code> won't work on
 *      many control types) and the <code>getProperty</code> and
 *      <code>putProperty</code> methods for overriding other properties.</li>
 *      <li>If you want to access your commands by keystrokes, you'll have to
 *      modify <code>kawigi.editor.KawigiEditKeyMap</code> to recognize those
 *      keystrokes.</li>
 *      <li>If you want your commands to be accessed by buttons or context menu
 *      items, follow the instructions above to customize .ui files.  You may
 *      want to make a copy of the existing .ui file to use as a starting point.
 *      Feel free to use other .ui files as examples as well.  If you want to
 *      make a whole new dialog or window or menu or something, you may have to
 *      add an entry to MenuID.java (although you can decide whether to add a
 *      .ui file to rc/ui or not - I think if you have the ui customized in the
 *      config files, it doesn't really matter if the resource file exists.
 *      Then you'll have to write the code to make that UI show up at the right
 *      time.</li>
 *  </ol>
 *
 *  Consider that you may not actually need to write code to customize
 *  KawigiEdit to your liking - for instance, if all you want to do is add a
 *  button to the main KawigiEdit UI that inserts your tokenizer, you could
 *  just customize Plugin.ui to include a Snippet item there and hardcode your
 *  tokenizer code (with appropriate XML escape sequences) into your version of
 *  Plugin.ui.
 *
 *  If you want to add some kind of post-processing to your code before saving
 *  it locally, you probably need to modify
 *  <code>kawigi.cmd.LocalTestAction.saveLocal()</code>.  I'm fairly certain
 *  that someday there will be something which allows you to do this without
 *  modifying KawigiEdit, I'm just not exactly sure what it will look like.
 *
 *  On the other hand, if you want to add some kind of post-processing to your
 *  code before submitting it to TopCoder, you should modify
 *  <code>kawigi.KawigiEdit.getSource()</code>.  Again, it's pretty likely that
 *  this will be specifically addressed in some way in a future version of
 *  KawigiEdit.
 **/
public class KawigiEdit
{
    /**
     * Contains string representing current version of plugin.
     * Created to exclude repeating of this string in several places.
     
        p stands for pivanof  : It has pivanof's updates (most useful)
        f stands for ffao     : Python support.
        a stands for ahmed_aly: Unused code cleaner for c++.
        x stands for vexorian : vexorian's modifications.
     */
    public final static String versionString = "KawigiEdit-pfax 2.4.0";
    /**
     * Credits line. Can be inserted in code using <%:kawigi-credits-line%>.
     */
    public final static String editorCreditsString = "Created by Kawigi. Updated by pivanof & ffao. c++ code cleaner by ahmed_aly. Tweaked by vexorian";

    /**
     * This is the panel returned to TopCoder that is displayed in the applet.
     **/
    private JPanel mainPanel;
    /**
     * Panel containing all plugin controls
     */
    private JPanel pluginPanel;
    /**
     * Frame for hosting plugin when working in out-of-the-arena mode
     */
    private JFrame outsideFrame;
    /**
     * Flag showing if out-of-the-arena mode is turned on or off
     */
    private boolean outsideMode = false;
    /**
     * Instance of plugin created by Arena
     */
    private static KawigiEdit instance;
    /**
     * This is the name given by the user to this instance of KawigiEdit.
     **/
    //private String name;

    /**
     * Code that was first generated on entering to the problem.
     */
    private String autoGeneratedCode;
    
    /**
     *  KawigiEdit plugin constructor - sets the AppEnvironment to PluginMode.
     **/
    public KawigiEdit()
    {
        AppEnvironment.setEnvironment(AppEnvironment.PluginMode);
        instance = this;
    }

    /**
     *  Returns the magic KawigiEdit panel.
     *
     *  From the TopCoder plugin interface.
     **/
    public JPanel getEditorPanel()
    {
        if (mainPanel == null)
        {
            pluginPanel = (JPanel)UIHandler.loadMenu(MenuID.PluginPanel, Dispatcher.getGlobalDispatcher());
            mainPanel = new VerticalPanel();
            outsideFrame = new PluginOutsideFrame();
            if (outsideMode) {
                outsideFrame.add(pluginPanel);
            }
            else {
                mainPanel.add(pluginPanel);
            }
        }
        if (Dispatcher.getTabbedPane() != null) {
            if (kawigi.properties.PrefFactory.getPrefs().getBoolean(ActID.actLogByDefault.preference, false) ) {
                Dispatcher.getTabbedPane().setSelectedComponent(Dispatcher.getLogComponent());
            } else {
                Dispatcher.getTabbedPane().setSelectedComponent(Dispatcher.getEditorPanel());
            }
            
        }
        if (Dispatcher.getOutputComponent() != null) {
            Dispatcher.getOutputComponent().clear();
        }
        if (Dispatcher.getCompileComponent() != null) {
            Dispatcher.getCompileComponent().clear();
        }
        return mainPanel;
    }

    /**
     *  Returns the text in the editor.
     *
     *  This is the text that TopCoder thinks is in the editor - what it saves
     *  remotely, compiles, tests and submits.  This is the code that can't
     *  break the UCR ;-)
     *
     *  From the TopCoder plugin interface.
     **/
    public String getSource()
    {
        Dispatcher.requestFileSync();

        String s = Dispatcher.getCodePane().getText();
        // If nothing was changed since auto-generation then return nothing.
        // This will help to generate code again if user simply changes languages.
        if (s.equals(autoGeneratedCode)) {
            s = "";
        }

        // Remove KawigiEdit tags.  Eventually we will probably have reason to
        // replace certain KawigiEdit tags with something interesting on
        // submission.
        s = s.replaceAll("<%:[a-zA-Z0-9_-]+%>", "");
        if (kawigi.properties.PrefFactory.getPrefs().getBoolean(ActID.actAhmedAlyCleaner.preference, false) ) {
            String fil = ProblemContext.getLanguage().getFileName("temp"); 
            if (fil.toLowerCase().trim().endsWith("cpp")) {
                CPPProcessor cleaner = new CPPProcessor(s);
                cleaner.cleanCode();
                String o = s;
                s = cleaner.getCode();
                if (s.equals(o)) {
                    Dispatcher.logln("Ahmed Aly's unused code cleaner didn't modify the code.");
                } else {
                    int d = s.length() - o.length();
                    String sd = ""+d;
                    if (d >= 0) {
                        sd = "+"+d;
                    }
                    Dispatcher.logln("Ahmed Aly's unused code cleaner modified the code ("+sd+" bytes).");
                }
            }
        }
        if (s.length() > 0) {
            Dispatcher.logln("Sent a "+s.length()+" bytes solution to TopCoder.");
        }
        return s;
    }

    /**
     *  Sets the text in the editor.
     *
     *  This implementation will ignore the request if the source provided is empty.
     *  This is to maintain auto-generated code.  Note that if you get code from
     *  TC and you want to keep it but use it for testing, you'll have to stick
     *  a <%:testing-code%> tag in there wherever the main method/testing code
     *  should be.
     *
     *  From the TopCoder plugin interface.
     **/
    public void setSource(String source)
    {
        Dispatcher.setFileSyncEnabled(false);
        Dispatcher.setAutoCodeEditing(true);
        Dispatcher.getGlobalDispatcher().runCommand(ActID.actGenerateCode);
        Dispatcher.setAutoCodeEditing(false);
        autoGeneratedCode = Dispatcher.getCodePane().getText();

        if (kawigi.properties.PrefFactory.getPrefs().getBoolean(ActID.actIgnoreTopCoderCode.preference, false) ) {
            if (source.length() > 0) {
                Dispatcher.logln("Ignored a "+source.length()+" bytes solution sent by TopCoder.");
            }
        } else if (source.length() > 0) {
            CodePane textPane = Dispatcher.getCodePane();
            source = ProblemContext.getLanguage().addAutoTestTag(source);
            textPane.setText(source);
            textPane.readdUndoListener();

            LocalTestAction.resetLastSaveTime();
            Dispatcher.resetLastEditTime();
            Dispatcher.logln("Retrieved a "+source.length()+" bytes solution from TopCoder.");
        }
        
        Dispatcher.setFileSyncEnabled(true);
        Dispatcher.requestFileSync();
        Dispatcher.hookMainWindow();
    }

    /**
     *  Empties the text pane.
     *
     *  From the TopCoder plugin interface.
     **/
    public void clear()
    {
        Dispatcher.getCodePane().setText("");
    }

    /**
     *  Enables/disables the text pane.
     *
     *  I've considered ignoring this request, I think TC just started actually
     *  calling it, but it doesn't get called consistently (like it might not
     *  be called if you close and reopen the problem).
     *
     *  From the TopCoder plugin interface.
     **/
    public void setTextEnabled(Boolean b)
    {
        Dispatcher.getCodePane().setEnabled(b.booleanValue());
    }

    /**
     *  Notifies the editor of a new problem being opened, or the language
     *  being changed, or whatever.
     *
     *  If the editor is empty, we will generate skeleton code.
     *
     *  From the TopCoder plugin interface.
     **/
    public void setProblemComponent(ProblemComponentModel component, Language lang, com.topcoder.shared.problem.Renderer renderer)
    {
        if (Dispatcher.getProblemTimer() != null)
            Dispatcher.getProblemTimer().select(component.getComponent().getProblemId(), component.getPoints().doubleValue());
        ProblemContext.setCurrentClass(ClassDeclFactory.getClassDecl(component.getComponent(), lang));
        try {
            ProblemContext.setStatement(renderer.toPlainText(lang));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *  Clears the text pane for a new problem.
     *
     *  From the TopCoder plugin interface.
     **/
    public void startUsing()
    {
        getEditorPanel();
        LocalTestAction.resetLastSaveTime();
        Dispatcher.resetLastEditTime();
    }

    /**
     *  Doesn't do anything.
     *
     *  From the TopCoder plugin interface.
     **/
    public void stopUsing()
    {
    }

    /**
     *  Brings up a configure dialog to set options in the editor plugin.
     *
     *  From the TopCoder plugin interface.
     **/
    public void configure()
    {
        Dispatcher.getGlobalDispatcher().runCommand(ActID.actLaunchConfig);
    }

    /**
     *  Verifies or sets several properties used by parts of the editor to set
     *  up.
     *
     *  For awhile, I was using this, but all the code pretty much checks to
     *  make sure I have all my configurations intact.  I may start using this
     *  again very soon to offer an optional wizard-like initial configuration.
     *
     *  From the TopCoder plugin interface.
     **/
    public void install()
    {
    }

    /**
     * Sets the name given to this plugin.
     *
     * From the TopCoder plugin interface.
     **/
    public void setName(String n)
    {
        //name = n;
    }

    /**
     * Get flag if plugin is working in out-of-the-arena mode
     */
    public static boolean getOutsideMode() {
        return instance.outsideMode;
    }

    /**
     * Turn out-of-the-arena mode of plugin working on or off
     */
    public static void setOutsideMode(boolean value) {
        if (instance.outsideMode == value)
            return;

        if (value) {
            instance.mainPanel.remove(instance.pluginPanel);
            instance.mainPanel.validate();
            instance.mainPanel.repaint();
            instance.outsideFrame.add(instance.pluginPanel);
            instance.outsideFrame.setVisible(true);
        }
        else {
            instance.outsideFrame.setVisible(false);
            instance.outsideFrame.remove(instance.pluginPanel);
            instance.mainPanel.add(instance.pluginPanel);
            instance.mainPanel.validate();
        }

        instance.outsideMode = value;
        Dispatcher.hookMainWindow();
    }

    /**
     * Get main plugin panel placed always inside the Arena
     */
    public static JPanel getMainPanel() {
        return instance.mainPanel;
    }

    /**
     * Get Arena window where plugin is placed
     */
    public static JFrame getArenaWindow() {
        if (instance.mainPanel != null) {
            Container cont = instance.mainPanel.getTopLevelAncestor();
            if (cont instanceof JFrame)
                return (JFrame)cont;
        }

        return null;
    }
}
