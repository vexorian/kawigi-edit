package kawigi.cmd;

import java.util.ArrayList;
import java.util.regex.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;

import kawigi.editor.CodePane;
import kawigi.language.*;
import kawigi.problem.*;
import kawigi.util.*;
import kawigi.widget.*;


/**
 * Implementation of all actions for test cases editing.
 **/
@SuppressWarnings("serial")
public final class TestCasesAction extends DefaultAction
{
	/**
	 * Class declaration for which dialogs already created
	 */
	private static ClassDecl cl;
    /**
     * Main dialog for test cases editing.
     **/
    private static JDialog mainDlg;
    /**
     * Panel to the left of main dialog for placing test cases names / check boxes.
     **/
    private static GridPanel checksPan;
	/**
	 * Panel in the middle of main dialog for placing "Edit" buttons.
	 */
    private static GridPanel editsPan;
	/**
	 * Panel to the right of main dialog for placing "Delete" buttons.
	 */
    private static GridPanel delsPan;
	/**
	 * All checkboxes with test cases names in main dialog.
	 */
    private static final ArrayList<JCheckBox> enabledCheckBoxes = new ArrayList<JCheckBox>();
	/**
	 * All "Edit" buttons in main dialog.
	 */
    private static final ArrayList<JButton> editButs = new ArrayList<JButton>();
	/**
	 * All "Delete" buttons in main dialog.
	 */
    private static final ArrayList<JButton> delButs = new ArrayList<JButton>();

	/**
	 * Dialog for single test case editing.
	 **/
	private static JDialog caseDlg;
    /**
     * Sequential number of test case which is edited at now time (if test case
     * will be added this variable is set to -1).
     **/
    private static int caseNum;
    /**
     * Test case which is edited at now time.
     **/
    private static Test testCase;
    /**
     * All edit fields (or buttons for arrays) for parameters and answer
     * in test case dialog.
     **/
    private static final ArrayList<JComponent> editFields = new ArrayList<JComponent>();
	/**
	 * All actions binded to edit fields in test case dialog.
	 */
	private static final ArrayList<TestCasesAction> editActions = new ArrayList<TestCasesAction>();
	/**
	 * Checkbox for stating that test case is with valid answer.
	 */
    private static JCheckBox ansBox;
	/**
	 * All dispatchers for edit fields and buttons for parameters and answer.
	 */
    private static final ArrayList<Dispatcher> caseDisps = new ArrayList<Dispatcher>();
    /**
     * TextBox or Button (in dialog for single test case)
     * for which this action was created.
     **/
    private JComponent thisField = null;

	/**
	 * Dialog for editing of array parameter for test case.
	 **/
	private static JDialog arrayDlg;
    /**
     * Sequential number of an array field in test case which is edited at nowtime.
     **/
    private static int fieldNum;
    /**
     * Label with array name in dialog for array parameter
     **/
    private static JLabel arrayLabel;
	/**
	 * Text area for multiline editing in dialog for array parameter
	 */
    private static JTextArea arrayText;
	/**
	 * Text field for single line editing in dialog for array parameter
	 */
    private static JTextField arrayField;
	/**
	 * Name of the type of editing array parameter
	 */
	private static String arrayTypeName;
	/**
	 * Base type for array which is edited at now time
	 */
    private static EditorDataType arrayBaseType;
    /**
     * Flag needed in synchronizing TextArea and TextBox when editing array parameter.
     **/
    private static boolean isChangingTexts = false;
    /**
     * Flag to temporarily disable the update test cases action
     * @see actionPerformed(ActionEvent)
     **/
    private static boolean cannotRunUpdate = false;
    /**
     * Flag that is enabled when doing bulk changes to test enabled checkBoxes:
     */
    private static boolean ignoreTestCheckboxEvent = false;

    /**
     * Constructs a new TestCasesAction for the given ActID.
     *
     * @param cmdid     Action to which this class belongs
     **/
    public TestCasesAction(ActID cmdid)
    {
        super(cmdid);
    }

    /**
     * Constructs a new TestCasesAction for the given ActID and given TextField.
     *
     * @param cmdid     Action to which this class belongs
     * @param field     TextField to which this class belongs
     **/
    public TestCasesAction(ActID cmdid, ActionTextField field)
    {
        super(cmdid);
        thisField = field;
    }

    /**
     * Constructs a new TestCasesAction for the given ActID and given Button.
     *
     * @param cmdid     Action to which this class belongs
     * @param field     Button to which this class belongs
     **/
    public TestCasesAction(ActID cmdid, JButton field)
    {
        super(cmdid);
        thisField = field;
    }

    /**
     * Main function for user interaction processing.
     *
     * @param e     Action made by user
     **/
    public void actionPerformed(ActionEvent e)
    {
	    // Simply redirecting all calls to appropriate static methods
        switch (cmdid)
        {
        case actTestCases:
            showMainDialog();
            break;
        case actCloseTestCases:
            mainDlg.setVisible(false);
            break;
        case actUpdateTestCases:
            if (! cannotRunUpdate) {
                if (mainDlg != null) {
                    mainDlg.setVisible(false);
                }
                updateContents();
            }
            break;
		case actAddExTestCases:
			addExampleTestCases();
			break;
		case actEnableAllTestCases:
			enableAllTestCases();
			break;
		case actDisableAllTestCases:
			disableAllTestCases();
			break;
        case actAddTestCase:
	        addTestCase();
	        break;
		case actEditTestCase:
			editTestCase(findObjIndex(editButs, e.getSource()));
			break;
		case actDeleteTestCase:
			deleteTestCase(findObjIndex(delButs, e.getSource()));
			break;
        case actCancelCaseParams:
	        hideCaseDialog();
	        break;
        case actSaveCaseParams:
	        saveCaseParams();
	        break;
		case actEditArrayParam:
			editArrayParam(findObjIndex(editFields, thisField));
			break;
        case actCancelArrayParam:
	        hideArrayDialog();
	        break;
		case actSaveArrayParam:
			saveArrayParam();
			break;
        }
    }

	/**
	 * Returns true if this action is available.
	 **/
	public boolean isEnabled()
	{
		// Here we need to compare with testCase.getParameters().length because
		// when this method called it can be that not all editFields are
		// created and added yet
	    if (null != thisField && null != testCase &&
	            findObjIndex(editFields, thisField) == testCase.getParameters().length)
	    {
	        return testCase.isWithAnswer();
	    } else {
	        return null != ProblemContext.getCurrentClass();
	    }
	}

	/**
	 * Getting text for parameter value TextBox in test case dialog.
	 *
	 * @param key           The key (only TEXT is implemented) for which value is needed
	 **/
	public Object getValue(String key)
	{
		checkClassChange();
		
		Object res = super.getValue(key);
	    if (null != testCase && TEXT.equals(key) && null != caseDlg &&
	    	null != thisField && thisField instanceof ActionTextField)
	    {
		    // The only distinction is answer/parameter
		    int ind = findObjIndex(editFields, thisField);
	        if (editFields.size() - 1 == ind)
	            res = testCase.getAnswer();
	        else if (-1 != ind)
                res = testCase.getParameters()[ind];
	    }
	    return res;
	}

	/**
	 * Saving text for parameter value TextBox after changing.
	 *
	 * @param key           The key (only TEXT is implemented) for which value is set
	 * @param value         The value to set for this key
	 **/
	public void putValue(String key, Object value)
	{
	    if (TEXT.equals(key) && null != thisField && thisField instanceof ActionTextField)
	    {
		    int ind = findObjIndex(editFields, thisField);
		    if (-1 != ind)
		        setTestCaseParameter(ind, (CharSequence)value);
	    }
	    super.putValue(key, value);
	}

	/**
	 * Changes text on the button for array parameter
	 * ("create" array if it's empty or "modify" if not).
	 *
	 * @param val           Value of an array (empty or some values coma-delimited)
	 **/
	private void updateArrayButState(CharSequence val)
	{
	    String state = 0 == val.length()? "create": "modify";
	    if (!state.equals(getValue(NAME)))
	        putValue(NAME, state);
	}

	/**
	 * Finds reference of some object in the given array (particularly ArrayList).
	 * To be honest this method should be placed in some utility class, but there
	 * is no such now and there are no so many methods to create new one.
	 *
	 * @param arr       Array to search in
	 * @param obj       Object to look for
	 * @return          Index in array where this object is, or -1 if object not found
	 */
	private static int findObjIndex(ArrayList<? extends Object> arr, Object obj)
	{
		int res = -1;
		for (int i = 0; arr.size() > i; ++i) {
			if (arr.get(i) == obj)
			{
				res = i;
				break;
			}
		}
		return res;
	}
	
	/**
	 *  Makes the contents of the dialog update the next time it is shown.
	 */
    public static void updateContents()
    {
        cl = null;
    }
	 
	/**
	 * Changes the whole code in TestPanel after some changes in test cases.
	 **/
	private static void updateTestPanel()
	{
	    EditorLanguage lang = ProblemContext.getLanguage();
	    CodePane testCodePane = Dispatcher.getTestCodePane();
	    testCodePane.setText(lang.getTestCode(cl));
	    Dispatcher.sourceCodeChanged();
	    cannotRunUpdate = true;
	    Dispatcher.requestFileSync();
	    cannotRunUpdate = false;
	}

	/**
	 * Changes default label colors if editor is in plugin mode.
	 *
	 * @param lab       Label to be tuned
	 **/
	private static void adjustLabelProps(JLabel lab)
	{
	    if (AppEnvironment.PluginMode == AppEnvironment.getEnvironment())
	        lab.setForeground(Color.WHITE);
	}

	/**
	 * Changes default checkbox colors when editor is in plugin mode.
	 **/
	private static void adjustCBoxProps(JCheckBox cbox)
	{
	    if (AppEnvironment.PluginMode == AppEnvironment.getEnvironment()) {
	        cbox.setForeground(Color.WHITE);
	        cbox.setBackground(Color.DARK_GRAY);
	    }
	}

	/**
	 * Shows dialog centered accordingly to parent window.
	 *
	 * @param dlg       Dialog to show
	 * @param frm       Parent window according to which we need to center dialog
	 */
	private static void showCentered(JDialog dlg, Window frm)
	{
		dlg.setLocation(frm.getX() + (frm.getWidth() - dlg.getWidth()) / 2,
		                frm.getY() + (frm.getHeight() - dlg.getHeight()) / 2);
		dlg.setVisible(true);
	}

	/**
	 * Checks if class declaration have been changed while test cases window was
	 * inactive. If so destroys our class-dependent windows to create them once more
	 * later and reinitializes reference to class declaration.
	 */
	private static void checkClassChange()
	{
	    Dispatcher.requestFileSync();
	    
		// If nothing changed, then we are done
		if (ProblemContext.getCurrentClass() == cl) {
			return;
		}

		// Clean main dialog
		if (null != mainDlg)
		{
			mainDlg.dispose();
			mainDlg = null;
		}
		// Clean test case info dialog and all structures related to it
		if (null != caseDlg)
		{
			caseDlg.dispose();
			caseDlg = null;
			editFields.clear();
			editActions.clear();
			Dispatcher disp = Dispatcher.getGlobalDispatcher();
			for (int i = 0; caseDisps.size() > i; ++i) {
			    disp.eraseSubDispatcher(caseDisps.get(i));
			}
			caseDisps.clear();
		}

		// Reinitialize our class declaration
		cl = ProblemContext.getCurrentClass();
	}

	/**
	 * Adds check box with test case number at the bottom of dialog for single
	 * test case editing
	 *
	 * @see     #createMainDialog()
	 */
	private static void addTestCaseCheckBox()
	{
	    boolean en = true;
	    int ts = enabledCheckBoxes.size();
	    if (ts < cl.countTests()) {
	        if ( cl.getTest(ts).isDisabled() ) {
	            en = false;
	        }
	    }
	    
		JCheckBox chk = new JCheckBox("Test " + ts, en);
		adjustCBoxProps(chk);
		checksPan.add(chk);
		chk.addItemListener(
				new ItemListener() {
					public void itemStateChanged(ItemEvent e) {
						saveTestCasesEnabled();
					}
				}
		);

		enabledCheckBoxes.add(chk);
	}

	/**
	 * Adds "Edit" button at the bottom of dialog for single
	 * test case editing
	 *
	 * @see     #createMainDialog()
	 */
	private static void addEditTestCaseButton()
	{
		Action act = Dispatcher.getGlobalDispatcher().getAction(ActID.actEditTestCase);
		JButton but = new JButton(act);
		editsPan.add(but);
		editButs.add(but);
	}

	/**
	 * Adds "Delete" button at the bottom of dialog for single
	 * test case editing
	 *
	 * @see     #createMainDialog()
	 */
	private static void addDelTestCaseButton()
	{
		Action act = Dispatcher.getGlobalDispatcher().getAction(ActID.actDeleteTestCase);
		JButton but = new JButton(act);
		delsPan.add(but);
		delButs.add(but);
	}

	/**
	 * Adjusts number of labels and buttons in main dialog to number of test cases
	 * in class declaration.
	 *
	 * @param doRepack          Flags if repacking of main dialog is necessary
	 */
	private static void updateMainDlgControls(boolean doRepack)
	{
		// Check Boxes:
		checksPan.setGridDimensions(cl.countTests() + ",1");
		while (cl.countTests() > enabledCheckBoxes.size()) {
			addTestCaseCheckBox();
		}
		for (int i = enabledCheckBoxes.size() - 1; cl.countTests() <= i; --i) {
			checksPan.remove(enabledCheckBoxes.get(i));
			enabledCheckBoxes.remove(i);
		}
		ignoreTestCheckboxEvent = true;
		for (int i = 0; i < cl.countTests(); i++) {
		    enabledCheckBoxes.get(i).setSelected( ! cl.getTest(i).isDisabled() );
		}
		ignoreTestCheckboxEvent = false;

		// Edit buttons
		editsPan.setGridDimensions(cl.countTests() + ",1");
		while (cl.countTests() > editButs.size()) {
			addEditTestCaseButton();
		}
		for (int i = editButs.size() - 1; cl.countTests() <= i; --i) {
			editsPan.remove(editButs.get(i));
			editButs.remove(i);
		}

		// Delete buttons
		delsPan.setGridDimensions(cl.countTests() + ",1");
		while (cl.countTests() > delButs.size())
			addDelTestCaseButton();
		for (int i = delButs.size() - 1; cl.countTests() <= i; --i)
		{
			delsPan.remove(delButs.get(i));
			delButs.remove(i);
		}

		if (doRepack) {
			mainDlg.pack();
		}
	}

	/**
	 * Creates main dialog for control over test cases.
	 * Dialog is made non-modal to be able to copy test cases
	 * info from other Arena windows.
	 */
	private static void createMainDialog()
	{
		Dispatcher disp = Dispatcher.getGlobalDispatcher();
		mainDlg = new JDialog((Frame)null, "Test cases editor", false);
		//mainDlg = new JDialog(Dispatcher.getWindow(), "Test cases editor", false);
		mainDlg.addWindowListener(Dispatcher.getGlobalDispatcher());

		// Main window elements
		VerticalPanel mainPan = new VerticalPanel();
		GridPanel casesPan = new GridPanel();
		casesPan.setGridDimensions("1,3");

		// All panels for labels and buttons
		checksPan = new GridPanel();
		// some beautifullness :)
		checksPan.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 10));
		enabledCheckBoxes.clear();
		editsPan = new GridPanel();
		editButs.clear();
		delsPan = new GridPanel();
		delButs.clear();

		// update labels and buttons
		updateMainDlgControls(false);

		// and add them to overall panel
		casesPan.add(checksPan);
		casesPan.add(editsPan);
		casesPan.add(delsPan);

		// We finished with main buttons
		mainPan.add(casesPan);

		// Now bottom buttons "Add" and "Add examples"
		GridPanel downPan = new GridPanel();
		downPan.setGridDimensions("2,1");
		GridPanel downUpPan = new GridPanel();
		downUpPan.setGridDimensions("2,2");
		Action act = disp.getAction(ActID.actAddTestCase);
		JButton addBut = new JButton(act);
		downUpPan.add(addBut);
		act = disp.getAction(ActID.actAddExTestCases);
		JButton addExBut = new JButton(act);
		downUpPan.add(addExBut);

		act = disp.getAction(ActID.actDisableAllTestCases);
		JButton disAllBut = new JButton(act);
		downUpPan.add(disAllBut);

		act = disp.getAction(ActID.actEnableAllTestCases);
		JButton enAllBut = new JButton(act);
		downUpPan.add(enAllBut);

		downPan.add(downUpPan);
		JPanel downDownPan = new JPanel();
		act = disp.getAction(ActID.actCloseTestCases);
		JButton closeBut = new JButton(act);
		downDownPan.add(closeBut);
		downPan.add(downDownPan);
		mainPan.add(downPan);

		// We can pack dialog
		mainDlg.getContentPane().add(mainPan);
		mainDlg.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		mainDlg.pack();

		// Additional feature: if we'll move to other window and then return back
		// here when problem statement in editor have been already changed, then
		// we will automatically kill this dialog.
		mainDlg.addWindowFocusListener(
				new WindowAdapter()
				{
					public void windowGainedFocus(WindowEvent e)
					{
						checkClassChange();
					}
				}
		);
	}

	/**
	 * Shows main dialog for editing of test cases. Automatically renews reference
	 * to class declaration if necessary.
	 **/
	private static void showMainDialog()
	{
		checkClassChange();
		if (null == mainDlg) {
		    createMainDialog();
		}
		showCentered(mainDlg, Dispatcher.getWindow());
	}

	/**
	 * Creates edit field or button to edit test case parameter value depending
	 * on type of this parameter.
	 *
	 * @param type      Type of the parameter
	 * @return          Component created for editing
	 */
	private static JComponent createCaseParamField(EditorDataType type)
	{
		JComponent res;
		ActID actid;
		boolean isArray = type.isArrayType();

		if (isArray)
		{
		    res = new JButton();
			actid = ActID.actEditArrayParam;
		}
		else
		{
		    res = new ActionTextField();
		    actid = ActID.actTestCaseParamsTexts;
		}
		// All fields are collected to understand what number of field user
		// wants to edit
		editFields.add(res);

		Dispatcher localDisp = Dispatcher.getGlobalDispatcher().createSubDispatcher(res);
		// Dispatchers are also collected to be able to remove them later
		caseDisps.add(localDisp);

		// And all components are assigned to actions (this class in particular)
		Action act = localDisp.getAction(actid, false);
		editActions.add((TestCasesAction)act);
		if (isArray)
			((JButton)res).setAction(act);
		else
			((ActionTextField)res).setAction(act);

		return res;
	}

	/**
	 * Creates dialog for editing of single test case parameters.
	 */
	private static void createCaseDialog()
	{
		Dispatcher disp = Dispatcher.getGlobalDispatcher();
		caseDlg = new JDialog(mainDlg, "Test case", true);

		// Main window elements
		VerticalPanel mainPan = new VerticalPanel();
		GridPanel paramsPan = new GridPanel();
		paramsPan.setGridDimensions("1,2");

		// All class parameters used in creation
		EditorLanguage lang = ProblemContext.getLanguage();
		MethodDecl mt = cl.getMethod();
		StringBuilder[] names = mt.getParamNames();
		EditorDataType[] types = mt.getParamTypes();
		EditorDataType retType = mt.getReturnType();
		String gridParamsDims = (names.length + 1) + ",1";

		// Labels naming all parameters and their types
		GridPanel labsPan = new GridPanel();
		labsPan.setGridDimensions(gridParamsDims);
		labsPan.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 10));
		StringBuilder labText = new StringBuilder(20);
		for (int i = 0; names.length > i; ++i) {
			labText.setLength(0);
			labText.append('(').append(i + 1).append(')')
					.append(lang.getTypeName(types[i])).append(' ').append(names[i]);
		    JLabel lab = new JLabel(labText.toString());
		    adjustLabelProps(lab);
		    labsPan.add(lab);
		}

		// Checkbox pointing to existence of answer
		ansBox = new JCheckBox(lang.getTypeName(retType) + " return");
		adjustCBoxProps(ansBox);
		ansBox.addItemListener(
				new ItemListener() {
					public void itemStateChanged(ItemEvent e) {
						ansBoxChanged();
					}
				}
		);
		labsPan.add(ansBox);

		// OK. Left part is finished
		paramsPan.add(labsPan);

		// All edit fields or buttons for parameters editing
		GridPanel editsPan = new GridPanel();
		editsPan.setGridDimensions(gridParamsDims);
		editFields.clear();
		caseDisps.clear();
		for (int i = 0; names.length > i; ++i) {
			editsPan.add(createCaseParamField(types[i]));
		}
		editsPan.add(createCaseParamField(retType));

		// OK. We did all top panel
		paramsPan.add(editsPan);
		mainPan.add(paramsPan);

		// Adding 2 buttons: save and cancel
		JPanel downPan = new JPanel();
		Action act = disp.getAction(ActID.actSaveCaseParams);
		JButton but = new JButton(act);
		downPan.add(but);
		act = disp.getAction(ActID.actCancelCaseParams);
		but = new JButton(act);
		downPan.add(but);

		// We did bottom panel
		mainPan.add(downPan);

		// Now we are ready to pack dialog
		caseDlg.getContentPane().add(mainPan);
		caseDlg.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		caseDlg.pack();
	}

	/**
	 * Changes UI after clicking on checkbox near answer
	 * showing if it is necessary to check the answer in test case.
	 **/
	private static void ansBoxChanged()
	{
	    testCase.setWithAnswer(ansBox.isSelected());
	    editFields.get(editFields.size() - 1).setEnabled(ansBox.isSelected());
	}

	/**
	 * Initializes edit field or button for some parameter with its value
	 * depending on the type of parameter.
	 *
	 * @param paramNum      Number of the parameter
	 * @param type          Type of the parameter
	 * @param value         Value of the parameter
	 */
	private static void initCaseParamValue(int paramNum, EditorDataType type,
	                                       StringBuilder value)
	{
		TestCasesAction act = editActions.get(paramNum);
		if (type.isArrayType()) {
		    act.updateArrayButState(value);
		} else {
		    act.putValue(TEXT, value);
		}
	}

	/**
	 * Initializes all fields in single test case edit dialog with the values
	 * set in current test case.
	 *
	 * @see     #testCase
	 */
	private static void initCaseDlgValues()
	{
		if (caseNum == -1) {
			caseDlg.setTitle("New test case");
		} else {
			caseDlg.setTitle("Test case " + caseNum);
		}
		
		// All parameters of class and test case
		MethodDecl mt = cl.getMethod();
		StringBuilder[] names = mt.getParamNames();
		EditorDataType[] types = mt.getParamTypes();
		EditorDataType retType = mt.getReturnType();
		StringBuilder[] values = testCase.getParameters();
		boolean hasAnswer = testCase.isWithAnswer();
		StringBuilder answer = testCase.getAnswer();

		// Let's set this parameters to fields
		ansBox.setSelected(hasAnswer);
		for (int i = 0; names.length > i; ++i)
			initCaseParamValue(i, types[i], values[i]);
		initCaseParamValue(names.length, retType, answer);

		// Some additional actions related to "hasAnswer" checkbox
		ansBoxChanged();
	}

	/**
	 * Shows dialog for editing single test case set in testCase.
	 *
	 * @see     #testCase
	 **/
	private static void showCaseDialog()
	{
		checkClassChange();
		if (null != mainDlg)
		{
			if (null == caseDlg)
				createCaseDialog();
			initCaseDlgValues();
			editFields.get(0).requestFocus();
			showCentered(caseDlg, mainDlg);
		}
	}

	/**
	 * Hides dialog for single test case editing and resets some fields to show
	 * that at this moment we don't edit any test case
	 */
	private static void hideCaseDialog()
	{
		testCase = null;
		caseNum = -1;
		caseDlg.setVisible(false);
	}

	/**
	 * Saves all test case parameters edited in test case dialog to class declaration.
	 * If dialog was called via Add, then does real adding of this test.
	 */
	private static void saveCaseParams()
	{
		if (-1 == caseNum) {
		    // here we add test case
		    cl.addTest(testCase);
			// update and repack dialog
			updateMainDlgControls(true);
		} else {
		    // here we only change Test and thats all
		    cl.setTest(caseNum, testCase);
		}

		// do not forget to change code in TestPanel
		updateTestPanel();
		hideCaseDialog();
	}
	
	/**
	 * Saves the contents of test case checkboxes so that test cases are enabled
	 * or disabled depending on what they say.
	 */
	private static void saveTestCasesEnabled()
	{
	    if (!ignoreTestCheckboxEvent) {
            for (int i=0; i<enabledCheckBoxes.size(); i++) {
                cl.getTest(i).setDisabled( ! enabledCheckBoxes.get(i).isSelected() );
            }
            // change code in TestPanel:
            updateTestPanel();
        }
	}

	/**
	 * Opens dialog for adding new test case.
	 */
	private static void addTestCase()
	{
		// Only show dialog for fake test case. Real adding will be in save action.
		StringBuilder[] params = new StringBuilder[cl.getMethod().getParamNames().length];
		for (int j = 0; params.length > j; ++j) {
			params[j] = new StringBuilder(100);
		}
		testCase = new Test(params);
		caseNum = -1;
		showCaseDialog();
	}

	/**
	 * Shows dialog for editing particular test case.
	 *
	 * @param caseToEdit    Number of test case to edit
	 */
	private static void editTestCase(int caseToEdit)
	{
		// some overcheck but still
		if (0 > caseToEdit || cl.countTests() <= caseToEdit) {
		    return;
		}

		caseNum = caseToEdit;
		try {
			// clone to be able to cancel changes later
		    testCase = (Test)cl.getTest(caseNum).clone();
			showCaseDialog();
		}
		catch (CloneNotSupportedException cnse) {
			// in fact this shouldn't ever happen
		}
	}

	/**
	 * Deletes particular test case.
	 *
	 * @param caseToDel     Number of test case to delete
	 */
	private static void deleteTestCase(int caseToDel)
	{
		// some overcheck, but still
		if (0 > caseToDel || cl.countTests() <= caseToDel)
			return;

		cl.removeTest(caseToDel);
		// after changing testcases list we need to change TestPanel
		// and change the appearance of the main dialog
		updateTestPanel();
		updateMainDlgControls(true);
	}

	/**
	 * Adds all example test cases to class declaration if they doesn't exist there.
	 */
	private static void addExampleTestCases()
	{
		// Parse the problem statement once again to find the example tests.
		ClassDecl newClass = ClassDeclFactory.reparseClassDecl();

		// For each test already in problem class, check if it came from examples.
		for (int i = 0; cl.countTests() > i; ++i) {
		    boolean found = false;
		    for (int j = 0; newClass.countTests() > j; ++j) {
		        if (newClass.getTest(j).equals(cl.getTest(i))) {
		            found = true;
		            break;
		        }
		    }
			// if not exists then add it to new class
		    if (!found) {
		        newClass.addTest(cl.getTest(i));
		    }
		}

		// Now move all tests from new class to now set
		cl.removeAllTests();
		for (int i = 0; newClass.countTests() > i; ++i)
		    cl.addTest(newClass.getTest(i));

		// refilling TestPanel and recreating main dialog to show all test cases
		updateTestPanel();
		updateMainDlgControls(true);
	}

	/**
	 * Single action to enable all the test cases.
	 */
    private static void enableAllTestCases()
    {
        ignoreTestCheckboxEvent = true;
	    for (int i=0; i<enabledCheckBoxes.size(); i++) {
	        enabledCheckBoxes.get(i).setSelected(true);
	        cl.getTest(i).setDisabled(false);
	    }
	    ignoreTestCheckboxEvent = false;
	    // change code in TestPanel:
	    updateTestPanel();
    }

	/**
	 * Single action to disable all the test cases.
	 */
    private static void disableAllTestCases()
    {
        ignoreTestCheckboxEvent = true;
	    for (int i=0; i<enabledCheckBoxes.size(); i++) {
	        enabledCheckBoxes.get(i).setSelected(false);
	        cl.getTest(i).setDisabled(true);
	    }
	    ignoreTestCheckboxEvent = false;
	    // change code in TestPanel:
	    updateTestPanel();        
    }
	
	/**
	 * Sets value of parameter in test case. Takes care about parameter numbers -
	 * parameter with last number is answer.
	 *
	 * @param paramNum          Number of the parameter
	 * @param value             Value of the parameter
	 */
	private static void setTestCaseParameter(int paramNum, CharSequence value)
	{
		// A bit of hack: we will change values inplace to avoid creation
		// of many new classes.
		StringBuilder holder;
	    if (editFields.size() - 1 == paramNum) {
	        holder = testCase.getAnswer();
	    } else {
			holder = testCase.getParameters()[paramNum];
		}

		// If we will not chek this then StringBuilders can interfere and
		// value can be erased by itself
		if (holder != value) {
			StringsUtil.reset(holder, value);
		}
	}

    /**
     * Creates the dialog for editing array parameter or array answer.
     **/
    private static void createArrayDialog()
    {
        Dispatcher disp = Dispatcher.getGlobalDispatcher();
        arrayDlg = new JDialog((Frame)null, "", true);

	    // Main element
        VerticalPanel mainPan = new VerticalPanel();
        mainPan.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));

	    // "Title" in the dialog
        JPanel topPan = new JPanel();
        topPan.setLayout(new FlowLayout(FlowLayout.LEFT));
        topPan.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
        arrayLabel = new JLabel("");
        adjustLabelProps(arrayLabel);
        topPan.add(arrayLabel);
        mainPan.add(topPan);

	    // Area with array elements in multiline
        arrayText = new JTextArea(5, 30);
        arrayText.getDocument().addDocumentListener(new ArrayListener(arrayText));
        JScrollPane scrollPan = new JScrollPane(arrayText);
        mainPan.add(scrollPan);

	    // Field with array elements comma-separated
        arrayField = new JTextField(35);
        arrayField.getDocument().addDocumentListener(new ArrayListener(arrayField));
        JPanel itemPan = new JPanel();
        itemPan.setBorder(BorderFactory.createEmptyBorder(10, 0, 5, 0));
        itemPan.add(arrayField);
        mainPan.add(itemPan);

	    // Standard dialog buttons
        JPanel downPan = new JPanel();
        Action act = disp.getAction(ActID.actSaveArrayParam);
        JButton but = new JButton(act);
        downPan.add(but);
        act = disp.getAction(ActID.actCancelArrayParam);
        but = new JButton(act);
        downPan.add(but);
        mainPan.add(downPan);

	    // Now we are ready to pack dialog
        arrayDlg.getContentPane().add(mainPan);
        arrayDlg.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        arrayDlg.pack();
    }

	/**
	 * Shows the dialog for editing of array parameter.
	 *
	 * @param fieldToEdit       Number of array parameter to edit
	 */
	private static void editArrayParam(int fieldToEdit)
	{
		// some overcheck, but still
		if (0 > fieldToEdit || editFields.size() <= fieldToEdit)
		    return;

		if (null == arrayDlg)
			createArrayDialog();

		fieldNum = fieldToEdit;

		// Initialize dialog label and title with this particular parameter
		EditorLanguage lang = ProblemContext.getLanguage();
		StringBuilder txt;
		EditorDataType arrayType;
		if (editFields.size() - 1 == fieldNum)
		{
			arrayTypeName = lang.getTypeName(cl.getMethod().getReturnType());
			txt = testCase.getAnswer();
			arrayType = cl.getMethod().getReturnType();
		}
		else
		{
			arrayTypeName = lang.getTypeName(cl.getMethod().getParamTypes()[fieldNum]);
			txt = testCase.getParameters()[fieldNum];
			arrayType = cl.getMethod().getParamTypes()[fieldNum];
		}

		arrayBaseType = arrayType.getPrimitiveType();
		arrayDlg.setTitle(arrayTypeName + " parameter");
		arrayField.setText(txt.toString());
		fieldChanged();

		// Now show the dialog
		showCentered(arrayDlg, caseDlg);
	}

	/**
	 * Hides the dialog for editing of array parameter.
	 */
	private static void hideArrayDialog()
	{
		arrayDlg.setVisible(false);
		fieldNum = -1;
	}

	/**
	 * Saves the value of array parameter and hides the editing dialog.
	 */
	private static void saveArrayParam()
	{
		// By this call we will standardize the look of array parameter value
		textChanged();

		// Now we can save it
		String txt = arrayField.getText();
		setTestCaseParameter(fieldNum, txt);
		editActions.get(fieldNum).updateArrayButState(txt);

		hideArrayDialog();
	}

    /**
     * Changes the label text when number of elements in the array is changed.
     **/
    private static void updateArrayLabel(int itemsCnt)
    {
        arrayLabel.setText(arrayTypeName + " -- [" + itemsCnt + ']');
    }

	/**
	 * Appends to array written in one comma-separated line next parameter with
	 * comma after it. Also does escaping of backslash and quote if they exist
	 * in string array values.
	 *
	 * @param val           Array value which will be edited
	 * @param txt           Text from which new value will be taken
	 * @param start         Start index of value in the text
	 * @param end           End index (one character adter end) of value in the text
	 */
	private static void appendLineArrayParam(StringBuilder val, CharSequence txt,
	                                         int start, int end)
	{
		// If this is string array then we need starting element separator
		if (EditorDataType.String == arrayBaseType)
			val.append('"');

		// Append value and then look for escapings in it
		int txtStart = val.length();
		val.append(txt, start, end);
		for (int i = txtStart; val.length() > i; ++i)
		{
			char c = val.charAt(i);
			if ('\\' == c || '"' == c)
			{
				val.insert(i, '\\');
				++i;
			}
		}

		// If this is string array then we need ending element separator
		if (EditorDataType.String == arrayBaseType)
			val.append('"');

		// And comma
		val.append(',');
	}

    /**
     * Changes text in TextBox (array comma-separated)
     * after editing TextArea (array one-element-in-line).
     **/
    private static void textChanged()
    {
        if (isChangingTexts)
            return;
        isChangingTexts = true;

	    String txt = arrayText.getText();
	    StringBuilder lineText = new StringBuilder(200);
	    Matcher matLines = Pattern.compile(StringsUtil.sCRLFregex).matcher(txt);
	    int linesCnt = 0;
	    int curInd = 0;

	    // Let's find every line ending and extract value in each line
	    while (matLines.find(curInd))
	    {
		    appendLineArrayParam(lineText, txt, curInd, matLines.start());
		    ++linesCnt;
		    curInd = matLines.end();
	    }
	    // If there is last line not ending with CRLF, then we do the same for it
	    if (curInd < txt.length())
	    {
		    appendLineArrayParam(lineText, txt, curInd, txt.length());
	        ++linesCnt;
	    }
	    if (0 < lineText.length())
	        lineText.setLength(lineText.length() - 1);

	    // Show number of parameters in the label and update field
	    updateArrayLabel(linesCnt);
	    arrayField.setText(lineText.toString());

        isChangingTexts = false;
    }

    /**
     * Changes text in TextArea (array one-element-in-line)
     * after editing TextBox (array comma-separated).
     **/
    private static void fieldChanged()
    {
        if (isChangingTexts)
            return;
        isChangingTexts = true;

        String txt = arrayField.getText();
	    StringBuilder multiText = new StringBuilder(200);
	    // All leading whitespace will be skipped
	    int curInd = StringsUtil.getFirstNonSpaceInd(txt);
	    int linesCnt = 0;
	    boolean inQuote = false;

	    // Will look at text character-by-character
	    while (txt.length() > curInd)
	    {
		    char c = txt.charAt(curInd);

		    // Quotes will be only in string arrays
		    if ('"' == c)
		    {
		        inQuote = !inQuote;
			    ++curInd;
			    // If we outside element then we will skip all whitespace
			    if (!inQuote)
			        curInd = StringsUtil.getFirstNonSpaceInd(txt, curInd);
			    continue;
		    }

		    // If we encountered escaped backslash or quote then simply move on to it
		    if ('\\' == c && inQuote && txt.length() > curInd + 1)
		    {
			    char c2 = txt.charAt(curInd + 1);
			    if ('\\' == c2 || '"' == c2)
			    {
				    ++curInd;
				    c = c2;
			    }
		    }

		    // Comma is delimiter for array elements and only outside quotes
		    if (',' == c && !inQuote)
		    {
			    multiText.append('\n');
			    // Again outside quotes and elements we doesn't need any whitespace
			    curInd = StringsUtil.getFirstNonSpaceInd(txt, curInd + 1);
			    ++linesCnt;
			    continue;
		    }

		    // All other characters are not special, so we simply
		    multiText.append(c);
		    ++curInd;
	    }

	    // If there were any elements then we will always count one less line
	    // than in reality
	    if (0 < multiText.length())
	    {
		    ++linesCnt;
		    // And it is better when we have CRLF after last element
	        multiText.append('\n');
	    }

	    // Now update label with number of elements and the multi-line text
	    updateArrayLabel(linesCnt);
	    arrayText.setText(multiText.toString());

        isChangingTexts = false;
    }


	private static class ArrayListener implements DocumentListener
	{
		private JTextComponent target;

		ArrayListener(JTextComponent target)
		{
			this.target = target;
		}

		private void changed()
		{
			if (target == arrayField)
				fieldChanged();
			else if (target == arrayText)
				textChanged();
		}

		public void changedUpdate(DocumentEvent e)
		{
			changed();
		}

		public void insertUpdate(DocumentEvent e)
		{
			changed();
		}

		public void removeUpdate(DocumentEvent e)
		{
			changed();
		}
	}
}
