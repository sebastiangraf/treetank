package org.treetank.filelistener.ui.dialogs;

import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.treetank.exception.TTException;
import org.treetank.filelistener.exceptions.StorageAlreadyExistsException;
import org.treetank.filelistener.file.StorageManager;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.wb.swt.SWTResourceManager;

public class CreateStorageConfigurationDialog extends Dialog {

	protected Object result;
	protected Shell shell;
	private Label lblNameOfThe;
	private Text text;
	private Combo combo;
	private Label lblChooseABackend;
	private Button btnSubmit;
	
	private int backend;
	private String name;
	private CLabel lblTheStorageAlready;

	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public CreateStorageConfigurationDialog(Shell parent, int style) {
		super(parent, style);
		setText("Create a new storage configuration");
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
		shell.setSize(438, 291);
		shell.setText(getText());
		
		lblNameOfThe = new Label(shell, SWT.NONE);
		lblNameOfThe.setBounds(10, 10, 266, 17);
		lblNameOfThe.setText("Name of the configuration");
		
		text = new Text(shell, SWT.BORDER);
		text.addModifyListener(new ModifyListener() {
			public void modifyText(final ModifyEvent e) {
				do_text_modifyText(e);
			}
		});
		text.setBounds(10, 33, 416, 27);
		
		lblChooseABackend = new Label(shell, SWT.NONE);
		lblChooseABackend.setBounds(10, 84, 266, 17);
		lblChooseABackend.setText("Choose a backend to use");
		
		combo = new Combo(shell, SWT.NONE);
		combo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				do_combo_widgetSelected(e);
			}
		});
		combo.setBounds(10, 107, 420, 29);
		combo.add("JClouds", StorageManager.BACKEND_INDEX_JCLOUDS);
		
		btnSubmit = new Button(shell, SWT.NONE);
		btnSubmit.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				do_btnSubmit_widgetSelected(e);
			}
		});
		btnSubmit.setBounds(83, 159, 266, 27);
		btnSubmit.setText("Submit");
		
		lblTheStorageAlready = new CLabel(shell, SWT.NONE);
		lblTheStorageAlready.setImage(SWTResourceManager.getImage(CreateStorageConfigurationDialog.class, "/com/sun/java/swing/plaf/gtk/icons/image-failed.png"));
		lblTheStorageAlready.setBounds(10, 202, 420, 53);
		lblTheStorageAlready.setText("The storage already exists try another name");
		lblTheStorageAlready.setVisible(false);

	}
	protected void do_btnSubmit_widgetSelected(final SelectionEvent e) {
		try {
			StorageManager.createStorage(this.name, this.backend);
		} catch (StorageAlreadyExistsException e1) {
			lblTheStorageAlready.setVisible(true);
			return;
		} catch (TTException e1) {
			lblTheStorageAlready.setText("Something went wrong. Please try another backend and/or another name.");
			lblTheStorageAlready.setVisible(true);
			return;
		}
		
		this.getParent().close();
	}
	
	public String getName(){
		return name;
	}
	
	protected void do_text_modifyText(final ModifyEvent e) {
		this.name = text.getText();
	}
	protected void do_combo_widgetSelected(final SelectionEvent e) {
		this.backend = combo.getSelectionIndex();
	}
}
