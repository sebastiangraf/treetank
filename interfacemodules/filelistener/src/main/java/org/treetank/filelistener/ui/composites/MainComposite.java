package org.treetank.filelistener.ui.composites;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.wb.swt.SWTResourceManager;
import org.treetank.access.conf.StorageConfiguration;
import org.treetank.exception.TTException;
import org.treetank.filelistener.exceptions.StorageNotExistingException;
import org.treetank.filelistener.file.Filelistener;

public class MainComposite extends Composite{
    private Label lblFoldersYouAre;
    private List list;
    private Composite composite;

    private java.util.Map<String, Filelistener> filelistenerList;
    private java.util.Map<String, StorageConfiguration> storageConfigurationList;
    
    private Filelistener listener;
    private StyledText styledText;

    public MainComposite(Composite parent, int style) {
        super(parent, style);
        
        parent.addDisposeListener(new DisposeListener() {
            
            @Override
            public void widgetDisposed(DisposeEvent e) {
                try {
                    listener.shutDownListener();
                } catch (TTException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }
        });
        
        setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));

        setLocation(0, 0);
        setSize(parent.getSize().x, parent.getSize().y);

        filelistenerList = new HashMap<String, Filelistener>();

        lblFoldersYouAre = new Label(this, SWT.NONE);
        lblFoldersYouAre.setLocation(10, 10);
        lblFoldersYouAre.setSize(lblFoldersYouAre.computeSize(200, SWT.DEFAULT));
        lblFoldersYouAre.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
        lblFoldersYouAre.setText("Folders you are listening to:");

        list = new List(this, SWT.BORDER);
        list.setBounds(10, 33, 200, this.getClientArea().height
            - lblFoldersYouAre.computeSize(200, SWT.DEFAULT).y - 100);

        composite = new Composite(this, SWT.BORDER);
        composite.setBounds(225, 10, this.getClientArea().width - 235, this.getClientArea().height - 50);
        
        styledText = new StyledText(composite, SWT.BORDER);
        styledText.setBounds(0, 0, 965, 642);
        
        try {
            listener = new Filelistener();
            
            try {
                for(Entry<String, String> e : Filelistener.getFilelisteners().entrySet()){
                    list.add(e.getValue() + " : " + e.getKey());
                    
                    listener.watchDir(new File(e.getValue()));
                    
                    styledText.setText(styledText.getText() + "\n" + "\tWatching dir: " + e.getValue());
                    
                    try {
                        listener.startListening();
                    } catch (StorageNotExistingException | TTException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                }
            } catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        this.addListener(SWT.Resize, new Listener() {

            @Override
            public void handleEvent(Event event) {
                resize();
            }

        });
    }

    @Override
    protected void checkSubclass() {}

    public void resize() {
        int width = this.getClientArea().width;
        int height = this.getClientArea().height;

        list.setBounds(10, 33, 200, height - lblFoldersYouAre.computeSize(200, SWT.DEFAULT).y - 100);
        composite.setBounds(225, 10, width - 235, height - 20);
        styledText.setBounds(15, 15, width - 270, height - 55);
    }

    public void configurationListChanged() {
        list.removeAll();
        
        try {
            listener.shutDownListener();
        } catch (TTException e2) {
            // TODO Auto-generated catch block
            e2.printStackTrace();
        }
        
        try {
            listener = new Filelistener();
            
            try {
                styledText.setText(styledText.getText() + "\n" + "Restarting listeners...");
                
                for(Entry<String, String> e : Filelistener.getFilelisteners().entrySet()){
                    list.add(e.getValue() + " : " + e.getKey());
                    
                    listener.watchDir(new File(e.getValue()));
                    
                    styledText.setText(styledText.getText() + "\n" + "\tWatching dir: " + e.getValue());
                    
                    try {
                        listener.startListening();
                    } catch (StorageNotExistingException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    } catch (TTException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                }
            } catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    public void shutdown() {
        if(listener == null) return;
        try {
            listener.shutDownListener();
        } catch (TTException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }
}
