package kawigi.cmd;

import kawigi.editor.*;
import kawigi.widget.*;
import java.awt.*;
import java.awt.event.FocusListener;
import javax.swing.*;
import javax.xml.parsers.*;
import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

/**
 *	This class loads UI constructs from XML.
 *
 *	This has a bit of magic that needs to be cleaned up a bit, and the answer
 *	may be to just set properties' values in such a way that is strongly typed.
 **/
public class UIHandler extends DefaultHandler
{
	/**
	 *	Returns the result of loading of the given MenuID, and actions will be
	 *	created using the given Dispatcher.
	 **/
	public static Container loadMenu(MenuID menu, Dispatcher disp)
	{
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser parser = factory.newSAXParser();
			UIHandler xmlHandler = new UIHandler(disp);
			InputStream xmlin = menu.getXMLStream();
			parser.parse(xmlin, xmlHandler);
			xmlin.close();
			return xmlHandler.getCurrentComponent();
		} catch (Exception ex) {
			reportError(ex, false);
			return null;
		}
	}

	// Instance variables and XML handling variables:
	/**
	 *	Component that we are in the middle of creating.
	 **/
	private Container currentComponent;
	/**
	 *	True if we have finished creating the GUI.
	 **/
	private boolean finished;
	/**
	 *	Stack of containers that we are currently inside.  Normally, we could
	 *	move up this hierarchy by using getParent() on the controls, but in some
	 *	cases, the literal parent container isn't the one we really want.  For
	 *	example, if we put things on a menu that's on a menu bar, getParent will
	 *	return a JPopupMenu that comes up as part of the implementation of JMenu
	 *	rather than returning the JMenu itself.
	 **/
	private Stack<Container> hierarchy;
	/**
	 *	This is true if we encountered an error creating the UI.
	 **/
	private static boolean error;
	/**
	 *	The dispatcher from which commands should be created.
	 **/
	private Dispatcher dispatcher;

	/**
	 *	Constructs a new UIHandler to handle an XML GUI specification and create
	 *	actions using the given Dispatcher.
	 **/
	public UIHandler(Dispatcher disp)
	{
		dispatcher = disp;
		finished = false;
		hierarchy = new Stack<Container>();
	}

	/**
	 *	Returns the control that we created, or null if we aren't finished.
	 **/
	public Container getCurrentComponent()
	{
		if (error || !finished)
			return null;
		else
			return currentComponent;
	}

	/**
	 *	Called when we begin parsing the GUI XML.
	 **/
	public void startDocument()
	{
		error = false;
		currentComponent = null;
	}

	/**
	 *	Called when a start XML tag is encountered.
	 *
	 *	Here, we create the control as specified by the opening tag, but its
	 *	creation isn't really complete until we get the end tag.  This control
	 *	will be set as the current one, and will be added to the old current
	 *	control.
	 *
	 *	There are parts of this method that I'm not proud of - in theory, you
	 *	should be able to put attributes on these tags in the form X="val" and
	 *	this code will call setX("val") on that control for you, but it's a bit
	 *	quirky.  It doesn't seem to be able to call methods inherited by the
	 *	controls from their parent classes.  It also should work for "setX"s
	 *	that take strings, booleans and ints as parameters, but the way I do
	 *	that is really hacky and should be smitten.  One solution might be to
	 *	require the attributes' values to be strongly typed in some way (which
	 *	makes it easy to be perfectly flexible, but harder for people to learn).
	 *	Another thing I could do is to absolutely limit these calls to single-
	 *	parameter setX methods that take primitive types or strings as
	 *	parameters, and just hope that they aren't overloaded with more than one
	 *	supported type, and try to coerce the value into one of those types.
	 *	Either way, this method isn't perfect, it just "works" for the UI we
	 *	have right now because I made it so :-)
	 *
	 *	There are three attributes that are treated specially (no setX method is
	 *	called for them).  The first is Action, which specifies the ActID for
	 *	the control, which bounds it to data and an action.  The second is
	 *	MenuID, which if specified, will cause another .ui file to be inserted
	 *	(this is how the snippet menu is inserted into the right-click menu).
	 *	The third special attribute is DispatcherName, which tells us that a
	 *	certain control is some important UI element that actions are going to
	 *	want global access to, like the main editor.
	 **/
	public void startElement(String uri, String localName, String qName, Attributes attributes)
	{
		ActID act = null;
		if (attributes.getValue("Action") != null) {
			try {
				act = Enum.valueOf(ActID.class, attributes.getValue("Action"));
			} catch (IllegalArgumentException ex) {
				reportError(ex, false);
			}
		}
		Action action = null;
		if (act != null) {
			action = dispatcher.getAction(act);
		}
		String classname = qName;
		try {
			Class<? extends Container> controlClass = getClass(classname).asSubclass(Container.class);
			Container control = null;
			if (action != null) {
				try {
					Constructor<? extends Container> c = controlClass.getConstructor(javax.swing.Action.class);
					control = c.newInstance(action);
				} catch (InvocationTargetException ex) {
					reportError(ex.getCause(), true);
				} catch (Exception ex) {
					Constructor<?>[] cs = controlClass.getConstructors();
					for (int i=0; i<cs.length; i++) {
						try {
							control = (Container)cs[i].newInstance(action);
						} catch (InvocationTargetException ex2) {
							reportError(ex2.getCause(), true);
						} catch (Exception ex2) {
						}
					}
				}
				if (control == null) {
					reportError(new Exception("No Action constructor available for " + controlClass), false);
				} else if (action instanceof FocusListener) {
					control.addFocusListener((FocusListener)action);
				}
			} else if (attributes.getValue("MenuID") != null) {
				try {
					MenuID menuid = Enum.valueOf(MenuID.class, attributes.getValue("MenuID"));
					control = loadMenu(menuid, dispatcher);
				} catch (IllegalArgumentException ex) {
					reportError(ex, false);
				}
			}
			if (control == null) {
				try {
					control = controlClass.newInstance();
				} catch (Throwable t) {
					reportError(t, false);
					control = new JPanel();	//last resort - getting here is a bug, not a feature.
				}
			}
			for (int i=0; i<attributes.getLength(); i++) {
				if (!attributes.getQName(i).equals("Action") && !attributes.getQName(i).equals("DispatcherName") && !attributes.getQName(i).equals("MenuID")) {
					String method = "set" + attributes.getQName(i);
					String value = attributes.getValue(i);
					try {
						Method m = controlClass.getMethod(method, String.class);
						m.invoke(control, value);
					} catch (InvocationTargetException ex) {
						reportError(ex.getCause(), true);
					} catch (Exception ex) {
						try {
							int ival = Integer.parseInt(value);
							Method m = controlClass.getMethod(method, Integer.class);
							m.invoke(control, ival);
						} catch (InvocationTargetException ex2) {
							reportError(ex, true);
							reportError(ex2.getCause(), true);
						} catch (Exception ex2) {
							try {
								// One last try.
								Method m = controlClass.getMethod(method, Boolean.class);
								m.invoke(control, Boolean.valueOf(value));
							} catch (InvocationTargetException ex3) {
								reportError(ex, true);
								reportError(ex2, true);
								reportError(ex3.getCause(), true);
							} catch (Exception ex3) {
								reportError(ex, true);
								reportError(ex2, true);
								reportError(ex3, true);
							}
						}
					}
				}
			}
			if (currentComponent != null) {
				hierarchy.push(currentComponent);
				currentComponent.add(control);
			}
			currentComponent = control;
			if (attributes.getValue("DispatcherName") != null) {
				String name = attributes.getValue("DispatcherName");
				if (name.equalsIgnoreCase("Window")) {
					Dispatcher.setWindow((JFrame)currentComponent);
				} else if (name.equalsIgnoreCase("EditorPanel")) {
					Dispatcher.setEditorPanel((EditorPanel)currentComponent);
				} else if (name.equalsIgnoreCase("LocalCode")) {
					Dispatcher.setLocalCodeEditorPanel((EditorPanel)currentComponent);
				} else if (name.equalsIgnoreCase("TestCode")) {
					Dispatcher.setTestEditorPanel((EditorPanel)currentComponent);
				} else if (name.equalsIgnoreCase("Compile")) {
					Dispatcher.setCompileComponent((SimpleOutputComponent)currentComponent);
				} else if (name.equalsIgnoreCase("Output")) {
					Dispatcher.setOutputComponent((SimpleOutputComponent)currentComponent);
				} else if (name.equalsIgnoreCase("Log")) {
					Dispatcher.setLogComponent((SimpleOutputComponent)currentComponent);
				} else if (name.equalsIgnoreCase("TabbedPane")) {
					Dispatcher.setTabbedPane((JTabbedPane)currentComponent);
				} else if (name.equalsIgnoreCase("Timer")) {
					Dispatcher.setProblemTimer((ProblemTimer)currentComponent);
				} else if (name.equalsIgnoreCase("TemplateEditor")) {
					Dispatcher.setTemplateEditor((EditorPanel)currentComponent);
				} else {
					reportError(new Exception("Unknown DispatcherName: " + name), false);
				}
			}
		} catch (Throwable ex) {
			reportError(ex, false);
		}
	}

	/**
	 *	Notifies us of non-tag characters between tags.
	 *
	 *	Normally we don't care about what's here, except in the case of Snippet
	 *	controls.  If the current component is a snippet, these characters are
	 *	part of the "code" in the snippet.  Unfortunately, the SAX parser
	 *	doesn't give us all the contiguous non-tag characters at once, they give
	 *	us stuff line by line, so when creating the snippets, we have to append
	 *	the input of each of these calls into the snippet's code.
	 **/
	public void characters(char[] ch, int start, int length)
	{
		if (currentComponent instanceof Snippet) {
			((Snippet)currentComponent).changeCode(((Snippet)currentComponent).getCode() + new String(ch, start, length));
		}
	}

	/**
	 *	Notifies us of an end tag being reached.
	 *
	 *	Once this is called, we are done working with the current component,
	 *	so we pop its parent off the hierarchy stack and the parent becomes
	 *	the current component again, unless there is nothing in the stack, which
	 *	generally means that this is the last end tag in the document.
	 **/
	public void endElement(String uri, String localName, String qName)
	{
		if (!hierarchy.empty()) {
			currentComponent = hierarchy.pop();
		}
	}

	/**
	 *	Notifies us that the parser has reached the end of the UI document.
	 **/
	public void endDocument()
	{
		finished = true;
	}

	/**
	 *	Notifies us of any error encountered in parsing.
	 **/
	public void fatalError(SAXParseException e)
	{
		reportError(e, false);
	}

	/**
	 *	Notifies us of any recoverable warning encountered in parsing.
	 **/
	public void warning(SAXParseException e)
	{
		reportError(e, true);
	}

	/**
	 *	Given a class name, tries to find the fully qualified class.  It will
	 *	first try to find the class with exactly this fully qualified class
	 *	name, which means you can have a tag in the form package.ClassName no
	 *	matter what package it's in.  Next, it attempts to load the class as
	 *	javax.swing.[ClassName].  If that doesn't work, it trys to load the
	 *	class similarly from the kawigi.widget package and the kawigi.editor
	 *	package.
	 **/
	private static Class<?> getClass(String className) throws ClassNotFoundException
	{
		ClassNotFoundException e = null;
		try {
			return Class.forName(className);
		} catch (ClassNotFoundException ex) {
			e = ex;
		}
		try {
			return Class.forName("javax.swing." + className);
		} catch (ClassNotFoundException ex) {
		}
		try {
			return Class.forName("kawigi.widget." + className);
		} catch (ClassNotFoundException ex) {
		}
		try {
			return Class.forName("kawigi.editor." + className);
		} catch (ClassNotFoundException ex) {
		}
		throw e;
	}

	/**
	 *	Prints the stack trace of an error or warning and notifies the user with
	 *	a dialog.  Under normal conditions, users shouldn't get any of these
	 *	dialogs, they are to notify people modifying the UI that something
	 *	didn't work quite right.
	 **/
	public static void reportError(Throwable e, boolean warning)
	{
		error = true;
		System.err.println("----------------------");
		e.printStackTrace();
		System.err.println("----------------------");
		try {
			JOptionPane.showMessageDialog((Component)null, e, "Error loading UI", warning ? JOptionPane.WARNING_MESSAGE : JOptionPane.ERROR_MESSAGE);
		} catch (HeadlessException ex) {
		}
	}
}
