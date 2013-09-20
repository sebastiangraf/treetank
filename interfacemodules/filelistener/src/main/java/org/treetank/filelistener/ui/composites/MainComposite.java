package org.treetank.filelistener.ui.composites;

import java.io.File;
import java.io.IOException;
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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wb.swt.SWTResourceManager;
import org.treetank.exception.TTException;
import org.treetank.filelistener.exceptions.ResourceNotExistingException;
import org.treetank.filelistener.file.Filelistener;
import org.treetank.filelistener.ui.dialogs.RestoreDialog;

public class MainComposite extends Composite {
    private Label mLblFoldersYouAre;
    private List mList;
    private Composite mComposite;

    private Filelistener mListener;
    private StyledText mStyledText;

    public MainComposite(Composite parent, int style) {
        super(parent, style);

        parent.addDisposeListener(new DisposeListener() {

            @Override
            public void widgetDisposed(DisposeEvent e) {
                try {
                    mListener.shutDownListener();
                } catch (TTException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }
        });

        setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));

        setLocation(0, 0);
        setSize(parent.getSize().x, parent.getSize().y);

        mLblFoldersYouAre = new Label(this, SWT.NONE);
        mLblFoldersYouAre.setLocation(10, 10);
        mLblFoldersYouAre.setSize(mLblFoldersYouAre.computeSize(200, SWT.DEFAULT));
        mLblFoldersYouAre.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
        mLblFoldersYouAre.setText("Folders you are listening to:");

        mList = new List(this, SWT.BORDER);
        mList.setBounds(10, 33, 200, this.getClientArea().height
            - mLblFoldersYouAre.computeSize(200, SWT.DEFAULT).y - 100);

        mComposite = new Composite(this, SWT.BORDER);
        mComposite.setBounds(225, 10, this.getClientArea().width - 235, this.getClientArea().height - 50);

        mStyledText = new StyledText(mComposite, SWT.BORDER);
        mStyledText.setBounds(0, 0, 965, 642);

        try {
            mListener = new Filelistener();

            try {
                for (Entry<String, String> e : Filelistener.getFilelisteners().entrySet()) {
                    mList.add(e.getValue() + " : " + e.getKey());

                    mListener.watchDir(new File(e.getValue()));

                    mStyledText.setText(mStyledText.getText() + "\n" + "\tWatching dir: " + e.getValue());
                }

                try {
                    mListener.startListening();
                } catch (ResourceNotExistingException | TTException e1) {
                    e1.printStackTrace();
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
    protected void checkSubclass() {
    }

    public void resize() {
        int width = this.getClientArea().width;
        int height = this.getClientArea().height;

        mList.setBounds(10, 33, 200, height - mLblFoldersYouAre.computeSize(200, SWT.DEFAULT).y - 100);
        mComposite.setBounds(225, 10, width - 235, height - 20);
        mStyledText.setBounds(15, 15, width - 270, height - 55);
    }

    public void configurationListChanged() throws IOException {

        mList.removeAll();
        try {
            mListener.shutDownListener();
        } catch (TTException e2) {
            // TODO Auto-generated catch block
            e2.printStackTrace();
        }

        try {
            mListener = new Filelistener();

            try {
                mStyledText.setText(mStyledText.getText() + "\n" + "Restarting listeners...");

                for (Entry<String, String> e : Filelistener.getFilelisteners().entrySet()) {
                    mList.add(e.getValue() + " : " + e.getKey());

                    mListener.watchDir(new File(e.getValue()));

                    mStyledText.setText(mStyledText.getText() + "\n" + "\tWatching dir: " + e.getValue());
                }

                try {
                    mListener.startListening();
                } catch (ResourceNotExistingException e1) {
                    e1.printStackTrace();
                } catch (TTException e1) {
                    e1.printStackTrace();
                }
            } catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    public void shutdown() {
        if (mListener == null)
            return;
        try {
            mListener.shutDownListener();
        } catch (TTException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public void restore() {
        RestoreDialog d = new RestoreDialog(new Shell(), SWT.DIALOG_TRIM, mListener);
        d.open();
    }
}
