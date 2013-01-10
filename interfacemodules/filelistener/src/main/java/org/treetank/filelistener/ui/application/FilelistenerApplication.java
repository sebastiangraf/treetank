package org.treetank.filelistener.ui.application;

import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.SWT;
import org.treetank.filelistener.ui.composites.MainComposite;
import org.treetank.filelistener.ui.dialogs.ListenToFolderDialog;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

public class FilelistenerApplication {

	protected Shell shell;
	private MainComposite composite;
	private Menu menu;
	private Menu menu_1;
	private MenuItem mntmFile;
	private Menu menu_2;
	private MenuItem mntmEdit;
	private Menu menu_3;
	private MenuItem mntmSettings;
	private Menu menu_4;
	private MenuItem mntmListenToA;
	private MenuItem mntmExit;
	private MenuItem mntmSettings_1;
	private MenuItem mntmAbout;

	/**
	 * Launch the application.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			FilelistenerApplication window = new FilelistenerApplication();
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	/**
	 * Open the window.
	 */
	public void open() {
		Display display = Display.getDefault();
		createContents();
		
		shell.addListener(SWT.Resize, new Listener(){

			@Override
			public void handleEvent(Event event) {
				composite.setSize(shell.getClientArea().width, shell.getClientArea().height);
			}
			
		});
		
		shell.open();
		shell.layout();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}

	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shell = new Shell();
		shell.setBounds(
				(int) (shell.getDisplay().getClientArea().width * 0.125),
				(int) (shell.getDisplay().getClientArea().height * 0.125),
				(int) (shell.getDisplay().getClientArea().width * 0.75),
				(int) (shell.getDisplay().getClientArea().height * 0.75));
		shell.setText("Treetank Fileservice");
		
		composite = new MainComposite(shell, SWT.NONE);
		
		menu_1 = new Menu(shell, SWT.BAR);
		
		mntmFile = new MenuItem(menu_1, SWT.CASCADE);
		mntmFile.setText("File");
		
		menu_2 = new Menu(mntmFile);
		mntmFile.setMenu(menu_2);
		
		mntmListenToA = new MenuItem(menu_2, SWT.NONE);
		mntmListenToA.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				do_mntmListenToA_widgetSelected(e);
			}
		});
		mntmListenToA.setText("Listen to a folder");
		
		mntmExit = new MenuItem(menu_2, SWT.NONE);
		mntmExit.setText("Exit");
		
		mntmEdit = new MenuItem(menu_1, SWT.CASCADE);
		mntmEdit.setText("Edit");
		
		menu_3 = new Menu(mntmEdit);
		mntmEdit.setMenu(menu_3);
		
		mntmSettings_1 = new MenuItem(menu_3, SWT.NONE);
		mntmSettings_1.setText("Settings");
		
		mntmSettings = new MenuItem(menu_1, SWT.CASCADE);
		mntmSettings.setText("Help");
		
		menu_4 = new Menu(mntmSettings);
		mntmSettings.setMenu(menu_4);
		
		mntmAbout = new MenuItem(menu_4, SWT.NONE);
		mntmAbout.setText("About");
		
		shell.setMenuBar(menu_1);
		
	}
	
	protected void do_mntmListenToA_widgetSelected(final SelectionEvent e) {
		ListenToFolderDialog dialog = new ListenToFolderDialog(new Shell(), SWT.DIALOG_TRIM);
		dialog.open();
		
		composite.configurationListChanged();
	}
}
