package kawigi.editor;
import javax.swing.text.*;

/**
 *	Obedient because it returns what I tell it to.
 *	
 *	Also sounds better than "HackedViewFactory".
 **/
public class ObedientViewFactory implements ViewFactory
{
	/**
	 *	Class from which Views are made in this ViewFactory.
	 **/
	protected Class<? extends View> viewClass;
	
	/**
	 *	Creates an ObedientViewFactory with no default View implementation.
	 **/
	public ObedientViewFactory()
	{
	}
	
	/**
	 *	Creates an ObedientViewFactory using v as the View implementation class.
	 *	
	 *	v should be compatible with javax.swing.text.View.
	 **/
	public ObedientViewFactory(Class<? extends View> v)
	{
		viewClass = v;
	}
	
	/**
	 *	Sets the class of the View implementation to be used.
	 *	
	 *	v should be compatible with javax.swing.text.View.
	 **/
	public void setViewClass(Class<? extends View> v)
	{
		viewClass = v;
	}
	
	/**
	 *	Creates a View instance of the class designated to this ViewFactory.
	 *	
	 *	If it fails for some reason (i.e. - no View class specified or the given
	 *	view class doesn't except a single Element parameter for its
	 *	constructor), a GenericView is returned.
	 **/
	public View create(Element elem)
	{
		try
		{
			return viewClass.getDeclaredConstructor(new Class[]{Element.class}).newInstance(new Object[]{elem});
		}
		catch (Exception ex)
		{
			return new GenericView(elem);
		}
	}
}
