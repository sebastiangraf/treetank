package org.treetank.filelistener.ui.dialogs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;
import org.treetank.access.conf.ModuleSetter;
import org.treetank.exception.TTException;
import org.treetank.filelistener.exceptions.StorageAlreadyExistsException;
import org.treetank.filelistener.file.StorageManager;
import org.treetank.filelistener.file.node.FileNodeFactory;
import org.treetank.filelistener.file.node.FilelistenerMetaPageFactory;
import org.treetank.io.IBackend;
import org.treetank.io.jclouds.JCloudsStorage;
import org.treetank.revisioning.IRevisioning;
import org.treetank.revisioning.SlidingSnapshot;

/**
 * @author Andreas Rain
 *
 */
public class CreateStorageConfigurationDialog extends Dialog {

    protected Object result;
    protected Shell shell;
    private Label lblNameOfThe;
    private Text text;
    private Button btnSubmit;

    private String name;
    private CLabel lblTheStorageAlready;
    private Label lblBackend;
    private Text txtBackend;
    private Label lblRevisioning;
    private Text txtRevisioning;
    private Label lblInfoIfYou;

    /**
     * Create the dialog.
     * 
     * @param parent
     * @param style
     */
    public CreateStorageConfigurationDialog(Shell parent, int style) {
        super(parent, style);
        setText("Create a new storage configuration");
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
        shell.setSize(438, 352);
        shell.setText(getText());

        lblNameOfThe = new Label(shell, SWT.NONE);
        lblNameOfThe.setBounds(10, 10, 266, 17);
        lblNameOfThe.setText("Name of the resource");

        text = new Text(shell, SWT.BORDER);
        text.addModifyListener(new ModifyListener() {
            public void modifyText(final ModifyEvent e) {
                do_text_modifyText(e);
            }
        });
        text.setBounds(10, 33, 416, 27);

        btnSubmit = new Button(shell, SWT.NONE);
        btnSubmit.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                do_btnSubmit_widgetSelected(e);
            }
        });
        btnSubmit.setBounds(70, 286, 266, 27);
        btnSubmit.setText("Submit");

        lblTheStorageAlready = new CLabel(shell, SWT.NONE);
        lblTheStorageAlready.setImage(SWTResourceManager.getImage(CreateStorageConfigurationDialog.class,
            "/com/sun/java/swing/plaf/gtk/icons/image-failed.png"));
        lblTheStorageAlready.setBounds(6, 227, 420, 53);
        lblTheStorageAlready.setText("The storage already exists try another name");
        lblTheStorageAlready.setVisible(false);
        
        lblBackend = new Label(shell, SWT.NONE);
        lblBackend.setBounds(10, 66, 416, 15);
        lblBackend.setText("Type in a backend");
        
        txtBackend = new Text(shell, SWT.BORDER);
        txtBackend.setBounds(10, 87, 416, 21);
        
        lblRevisioning = new Label(shell, SWT.NONE);
        lblRevisioning.setBounds(10, 114, 416, 15);
        lblRevisioning.setText("Type in a revisioning");
        
        txtRevisioning = new Text(shell, SWT.BORDER);
        txtRevisioning.setBounds(10, 135, 416, 21);
        
        lblInfoIfYou = new Label(shell, SWT.WRAP);
        lblInfoIfYou.setFont(SWTResourceManager.getFont("Segoe UI", 9, SWT.ITALIC));
        lblInfoIfYou.setBounds(10, 181, 416, 40);
        lblInfoIfYou.setText("Info: If you type in a wrong class the default backend JCloudsStorage and default revisioning SlidingSnapshot are going to be used.");

    }

    @SuppressWarnings("unchecked")
    protected void do_btnSubmit_widgetSelected(final SelectionEvent e) {
        try {
            this.name = text.getText();
            
            String backend = txtBackend.getText();
            String rev = txtRevisioning.getText();
            Class<? extends IBackend> backendClass;
            Class<? extends IRevisioning> revClass;
            
            try {
                backendClass = (Class<IBackend>) Class.forName(backend);
            } catch (ClassNotFoundException e1) {
                backendClass = JCloudsStorage.class;
            }
            
            try {
                revClass = (Class<IRevisioning>) Class.forName(rev);
            } catch (ClassNotFoundException e1) {
                revClass = SlidingSnapshot.class;
            }
            
            StorageManager.createResource(this.name, new ModuleSetter().setNodeFacClass(FileNodeFactory.class).setMetaFacClass(FilelistenerMetaPageFactory.class)
                .setRevisioningClass(revClass).setBackendClass(backendClass).createModule());
            
        } catch (StorageAlreadyExistsException e1) {
            lblTheStorageAlready.setVisible(true);
            return;
        } catch (TTException e1) {
            lblTheStorageAlready
                .setText("Something went wrong. Please try another backend and/or another name.");
            lblTheStorageAlready.setVisible(true);
            return;
        }

        this.getParent().close();
    }
    
    /**
     * @return String - name of the resource
     */
    public String getName(){
        return this.name;
    }

    protected void do_text_modifyText(final ModifyEvent e) {
        this.name = text.getText();
    }
}
