package org.treetank.filelistener.ui.dialogs;

import java.util.List;

import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.treetank.filelistener.file.StorageManager;

public class ChooseExisitingStorageDialog extends Dialog {

    protected Object result;
    protected Shell shell;

    private String chosenStorage = "";
    private Combo combo;
    private Button btnSubmit;

    /**
     * Create the dialog.
     * 
     * @param parent
     * @param style
     */
    public ChooseExisitingStorageDialog(Shell parent, int style) {
        super(parent, style);
        setText("Choose a storage from the list");
    }

    /**
     * Open the dialog.
     * 
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
        shell.setSize(320, 149);
        shell.setText(getText());

        combo = new Combo(shell, SWT.NONE);
        combo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                do_combo_widgetSelected(e);
            }
        });
        combo.setBounds(10, 10, 272, 29);

        List<String> storages = StorageManager.getResources();
        
        if(storages != null){
            for (String s : StorageManager.getResources()) {
                combo.add(s);
            }
        }
        else{
            combo.add("There are no existing storages.");
        }
        

        btnSubmit = new Button(shell, SWT.NONE);
        btnSubmit.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                do_btnSubmit_widgetSelected(e);
            }
        });

        btnSubmit.setBounds(191, 64, 91, 29);
        btnSubmit.setText("Submit");
    }

    public String getStorageName() {
        return this.chosenStorage;
    }

    protected void do_btnSubmit_widgetSelected(final SelectionEvent e) {
        this.getParent().close();
    }

    protected void do_combo_widgetSelected(final SelectionEvent e) {
        this.chosenStorage = combo.getText();
    }
}
