package org.treetank.filelistener.ui.composites;

import java.util.HashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.wb.swt.SWTResourceManager;
import org.treetank.access.conf.StorageConfiguration;
import org.treetank.filelistener.file.Filelistener;

public class MainComposite extends Composite {
	private Label lblFoldersYouAre;
	private List list;
	private Composite composite;
	
	private java.util.Map<String, Filelistener> filelistenerList;
	private java.util.Map<String, StorageConfiguration> storageConfigurationList;
	
	public MainComposite(Composite parent, int style) {
		super(parent, style);
		setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));

		setLocation(0, 0);
		setSize(parent.getSize().x, parent.getSize().y);
		
		filelistenerList = new HashMap();
		
		lblFoldersYouAre = new Label(this, SWT.NONE);
		lblFoldersYouAre.setLocation(10, 10);
		lblFoldersYouAre.setSize(lblFoldersYouAre.computeSize(200, SWT.DEFAULT));
		lblFoldersYouAre.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblFoldersYouAre.setText("Folders you are listening to:");
		
		list = new List(this, SWT.BORDER);
		list.setBounds(10, 33, 200, this.getClientArea().height - lblFoldersYouAre.computeSize(200, SWT.DEFAULT).y - 100);
		
		composite = new Composite(this, SWT.BORDER);
		composite.setBounds(225, 10, this.getClientArea().width - 235, this.getClientArea().height - 50);
		
		this.addListener(SWT.Resize, new Listener(){

			@Override
			public void handleEvent(Event event) {
				resize();
			}
			
		});
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
	
	public void resize(){
		int width = this.getClientArea().width;
		int height = this.getClientArea().height;
		
		list.setBounds(10, 33, 200, height - lblFoldersYouAre.computeSize(200, SWT.DEFAULT).y - 100);
		composite.setBounds(225, 10, width - 235, height - 20);
	}
	
	public void configurationListChanged(){
		
	}
}
