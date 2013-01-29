package org.treetank.filelistener.ui.dialogs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;
import org.treetank.api.IFilelistenerReadTrx;
import org.treetank.exception.TTIOException;
import org.treetank.filelistener.file.Filelistener;

import com.google.common.io.Files;

public class RestoreDialog extends Dialog {

    protected Object mResult;
    protected Shell mShell;
    private Combo mCombo;
    private Label mLblChooseConfigurationFrom;
    private Label mLblFolder;
    private Text mTextFolder;
    private Button mBtnFolder;
    private Button mBtnSubmit;
    private Button mBtnCancel;

    private Filelistener mListener;

    private String mListenFolder;

    /**
     * Create the dialog.
     * 
     * @param parent
     * @param style
     * @param mListener
     */
    public RestoreDialog(Shell parent, int style, Filelistener pListener) {
        super(parent, style);
        mListener = pListener;
        setText("Restore into new folder");
    }

    /**
     * Open the dialog.
     * 
     * @return the result
     */
    public Object open() {
        createContents();
        mShell.open();
        mShell.layout();
        Display display = getParent().getDisplay();
        while (!mShell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        return mResult;
    }

    /**
     * Create contents of the dialog.
     */
    private void createContents() {
        mShell = new Shell(getParent(), getStyle());
        mShell.setSize(448, 273);
        mShell.setText(getText());

        mCombo = new Combo(mShell, SWT.NONE);
        mCombo.setBounds(10, 133, 430, 29);

        try {
            for (String s : mListener.getFilelisteners().keySet()) {
                mCombo.add(s);
            }
        } catch (FileNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (ClassNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        mLblChooseConfigurationFrom = new Label(mShell, SWT.NONE);
        mLblChooseConfigurationFrom.setBounds(10, 104, 430, 17);
        mLblChooseConfigurationFrom.setText("Choose configuration from below");

        mLblFolder = new Label(mShell, SWT.NONE);
        mLblFolder.setText("Choose an empty folder:");
        mLblFolder.setBounds(10, 20, 430, 17);

        mTextFolder = new Text(mShell, SWT.BORDER);
        mTextFolder.setBounds(10, 54, 372, 27);

        mBtnFolder = new Button(mShell, SWT.NONE);
        mBtnFolder.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                do_mBtnFolder_widgetSelected(e);
            }
        });
        mBtnFolder.setImage(SWTResourceManager.getImage(RestoreDialog.class,
            "/com/sun/java/swing/plaf/gtk/icons/Directory.gif"));
        mBtnFolder.setBounds(388, 54, 52, 29);

        mBtnSubmit = new Button(mShell, SWT.NONE);
        mBtnSubmit.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                do_mBtnSubmit_widgetSelected(e);
            }
        });
        mBtnSubmit.setBounds(248, 185, 91, 29);
        mBtnSubmit.setText("Submit");

        mBtnCancel = new Button(mShell, SWT.NONE);
        mBtnCancel.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                do_btnCancel_widgetSelected(e);
            }
        });
        mBtnCancel.setBounds(345, 185, 91, 29);
        mBtnCancel.setText("Cancel");

    }

    protected void do_btnCancel_widgetSelected(final SelectionEvent e) {
        getParent().dispose();
    }

    protected void do_mBtnFolder_widgetSelected(final SelectionEvent e) {
        DirectoryDialog dialog = new DirectoryDialog(mShell);
        dialog.setText("Choose a folder");
        String homefolder = System.getProperty("user.home");
        dialog.setFilterPath(homefolder);

        dialog.open();

        mTextFolder.setText(dialog.getFilterPath());
        this.mListenFolder = mTextFolder.getText();
    }

    protected void do_mBtnSubmit_widgetSelected(final SelectionEvent e) {
        File f = new File(this.mListenFolder);

        IFilelistenerReadTrx trx = mListener.getTrx(mCombo.getText());

        if (!f.exists()) {
            // TODO: Errordialog no folder..
        }

        if (f.isDirectory() && f.listFiles().length == 0) {
            if (trx != null) {
                String[] filePaths = trx.getFilePaths();

                for (String s : filePaths) {
                    System.out.println("Restoring file: " + s);
                    try {
                        File file = trx.getFullFile(s);

                        Files.copy(file, new File(new StringBuilder().append(this.mListenFolder).append(s)
                            .toString()));

                    } catch (TTIOException | IOException e1) {
                        // TODO: ErrorDialog for this file..
                        e1.printStackTrace();
                    }
                }
                
                mShell.dispose();
                
            }
        } else {
            // TODO: Not directory OR not empty
        }

    }
}
