package kawigi.editor;

import javax.swing.text.*;

/**
 *	It's configurable because you can "Configure" the kind of View its
 *	ViewFactory returns.
 *	
 *	And it sounds better than "HackableEditorKit".
 **/
@SuppressWarnings("serial")
public class ConfigurableEditorKit extends StyledEditorKit
{
	/**
	 *	The Class of View components for this EditorKit.
	 **/
	protected Class<? extends View> viewClass;
	/**
	 *	My ViewFactory that I return:
	 **/
	protected ViewFactory factory;
	
	/**
	 *	Constructs a ConfigurableEditorKit that uses Views of class v.
	 *	
	 *	v should be compatible with javax.swing.text.View and should probably be
	 *	GenericView or a subclass of GenericView.
	 **/
	public ConfigurableEditorKit(Class<? extends View> v)
	{
		viewClass = v;
	}
	
	/**
	 *	Constructs a ConfigurableEditorKit with no default View implementation.
	 **/
	public ConfigurableEditorKit()
	{
	}
	
	/**
	 *	Sets the class used to create Views for this EditorKit.
	 *	
	 *	v should be compatible with javax.swing.text.View, and should probably
	 *	be GenericView or a subclass of GenericView.
	 **/
	public void setViewClass(Class<? extends View> v)
	{
		viewClass = v;
		if (factory != null && (factory instanceof ObedientViewFactory))
			((ObedientViewFactory)factory).setViewClass(v);
	}
	
	/**
	 *	Overridden from StyledEditorKit to return my ViewFactory.
	 **/
	public ViewFactory getViewFactory()
	{
		if (factory == null)
			factory = new ObedientViewFactory(viewClass);
		return factory;
	}
}
