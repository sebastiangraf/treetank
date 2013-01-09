package org.treetank.filelistener.ui.dialogs;

import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Button;
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

public class ListenToFolderDialog extends Dialog {

	protected Object result;
	protected Shell shell;
	private Label lblFolder;
	private Text text;
	private Button btnUseAnExisting;
	private Button btnChooseStorageConfiguration;
	private Button button;
	private Button btnCreateANew;
	private Button btnSubmit;
	private Button btnCancel;

	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public ListenToFolderDialog(Shell parent, int style) {
		super(parent, style);
		setText("Please configure which folder you want to listen to");
	}

	/**
	 * Open the dialog.
	 * @return the result
	 */
	public Object open() {
		createContents();
		shell.open();
		shell.layout();
		Display display = getParent().getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return result;
	}

	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {
		shell = new Shell(getParent(), getStyle());
		shell.setSize(450, 218);
		shell.setText(getText());
		
		lblFolder = new Label(shell, SWT.NONE);
		lblFolder.setBounds(10, 10, 70, 17);
		lblFolder.setText("Folder:");
		
		text = new Text(shell, SWT.BORDER);
		text.setBounds(86, 10, 298, 27);
		
		button = new Button(shell, SWT.NONE);
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				btnFolderDialog();
			}
		});
		button.setImage(SWTResourceManager.getImage(ListenToFolderDialog.class, "/com/sun/java/swing/plaf/gtk/icons/Directory.gif"));
		button.setBounds(390, 10, 52, 29);
		
		btnUseAnExisting = new Button(shell, SWT.CHECK);
		btnUseAnExisting.setBounds(10, 43, 430, 24);
		btnUseAnExisting.setText("Use an existing storage configuration?");
		
		btnChooseStorageConfiguration = new Button(shell, SWT.NONE);
		btnChooseStorageConfiguration.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				btnChooseStorageConfiguration();
			}
		});
		btnChooseStorageConfiguration.setBounds(10, 71, 428, 29);
		btnChooseStorageConfiguration.setText("Choose storage configuration");
		btnChooseStorageConfiguration.setEnabled(false);
		
		btnCreateANew = new Button(shell, SWT.NONE);
		btnCreateANew.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				btnCreateStorageconfiguration();
			}
		});
		btnCreateANew.setBounds(10, 106, 428, 29);
		btnCreateANew.setText("Create a new storage configuration");
		
		btnSubmit = new Button(shell, SWT.NONE);
		btnSubmit.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				btnSubmit();
			}
		});
		btnSubmit.setBounds(10, 141, 91, 29);
		btnSubmit.setText("Submit");
		
		btnCancel = new Button(shell, SWT.NONE);
		btnCancel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				btnCancel();
			}
		});
		btnCancel.setBounds(107, 141, 91, 29);
		btnCancel.setText("Cancel");

	}
	
	private void btnFolderDialog(){
		
	}
	
	private void btnChooseStorageConfiguration(){
		
	}
	
	private void btnCreateStorageconfiguration(){
		
	}
	
	private void btnSubmit(){
		
	}
	
	private void btnCancel(){
		
	}
}
