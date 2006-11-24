// ============================================================================
//
// Talend Community Edition
//
// Copyright (C) 2006 Talend - www.talend.com
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
//
// ============================================================================
package org.talend.repository.ui.wizards.metadata.connection.files.regexp;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.talend.commons.ui.swt.dialogs.ErrorDialogWidthDetailArea;
import org.talend.commons.ui.swt.formtools.Form;
import org.talend.commons.ui.swt.formtools.LabelledCheckboxCombo;
import org.talend.commons.ui.swt.formtools.LabelledCombo;
import org.talend.commons.ui.swt.formtools.LabelledText;
import org.talend.commons.ui.swt.formtools.UtilsButton;
import org.talend.core.model.metadata.EMetadataEncoding;
import org.talend.core.model.metadata.builder.connection.FieldSeparator;
import org.talend.core.model.metadata.builder.connection.FileFormat;
import org.talend.core.model.metadata.builder.connection.RowSeparator;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.utils.XmlArray;
import org.talend.repository.i18n.Messages;
import org.talend.repository.preview.ProcessDescription;
import org.talend.repository.ui.swt.preview.ShadowProcessPreview;
import org.talend.repository.ui.swt.utils.AbstractRegexpFileStepForm;
import org.talend.repository.ui.utils.ShadowProcessHelper;

/**
 * @author ocarbone
 * 
 */
public class RegexpFileStep2Form extends AbstractRegexpFileStepForm {

    private static Logger log = Logger.getLogger(RegexpFileStep2Form.class);

    private static final String EMPTY_VALUE = Messages.getString("FileStep2.empty");

    private static final String[] STRING_NUMBERS_DATA = { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13",
            "14", "15", "16", "17", "18", "19", "20" };

    private static final String REGEXP_DEFAULT = "^(.*)$";

    /**
     * Main Fields.
     */
    private LabelledCombo encodingCombo;

    private Text fieldSeparatorType;

    private Text fieldSeparatorText;

    private LabelledCombo rowSeparatorCombo;

    private LabelledCheckboxCombo rowsToSkipHeaderCheckboxCombo;

    private LabelledCheckboxCombo rowsToSkipLimitCheckboxCombo;

    private LabelledCheckboxCombo rowsToSkipFooterCheckboxCombo;

    private LabelledText rowSeparatorText;

    private Button emptyRowsToSkipCheckbox;

    private Label escapeCharFlag;

    private Label textEnclosureFlag;

    /**
     * Fields use to preview.
     */

    private Group previewGroup;

    private Button firstRowIsCaptionCheckbox;

    private Button previewButton;

    private Label previewInformationLabel;

    private ShadowProcessPreview regexpFilePreview;

    /**
     * Another.
     */

    private UtilsButton cancelButton;

    private boolean readOnly;

    /**
     * Constructor to use by RCP Wizard.
     * 
     * @param Composite
     * @param Wizard
     * @param Style
     */
    public RegexpFileStep2Form(Composite parent, ConnectionItem connectionItem) {
        super(parent, connectionItem);
        setupForm();
    }

    /**
     * 
     * Initialize value, forceFocus first field.
     */
    protected void initialize() {

        // Fields to the Group Regex File Settings
        if (getConnection().getEncoding() != null && !getConnection().getEncoding().equals("")) {
            encodingCombo.setText(getConnection().getEncoding());
        } else {
            encodingCombo.select(0);
        }

        // fieldSeparatorType.setText(getConnection().getFieldSeparatorType().getName());
        if (getConnection().getFieldSeparatorValue() != null) {
            fieldSeparatorText.setText(getConnection().getFieldSeparatorValue()); // +"\n tototototot"+"\n
                                                                                    // ouuuuuuuuuuu"
        } else {
            fieldSeparatorText.setText(REGEXP_DEFAULT);
        }
        fieldSeparatorText.setEditable(true);

        rowSeparatorCombo.setText(getConnection().getRowSeparatorType().getLiteral());
        rowSeparatorText.setText(getConnection().getRowSeparatorValue());
        rowSeparatorText.setEditable(false);

        // adpat Separator Combo and Text
        rowSeparatorManager();

        // Fields to the Group Rows To Skip
        int i = getConnection().getHeaderValue();
        if (i > 0) {
            rowsToSkipHeaderCheckboxCombo.setText("" + getConnection().getHeaderValue());
        }
        i = getConnection().getFooterValue();
        if (i > 0) {
            rowsToSkipFooterCheckboxCombo.setText("" + getConnection().getFooterValue());
        }
        i = getConnection().getLimitValue();
        if (i > 0) {
            rowsToSkipLimitCheckboxCombo.setText("" + getConnection().getLimitValue());
        }

        emptyRowsToSkipCheckbox.setSelection(getConnection().isRemoveEmptyRow());
        firstRowIsCaptionCheckbox.setSelection(getConnection().isFirstLineCaption());

        // clearSelection of the selected combo
        encodingCombo.clearSelection();
        // fieldSeparatorType.clearSelection();
        rowSeparatorCombo.clearSelection();

        emptyRowsToSkipCheckbox.setSelection(getConnection().isRemoveEmptyRow());
        checkFieldsValue();
    }

    /**
     * DOC ocarbone Comment method "adaptFormToReadOnly".
     */
    protected void adaptFormToReadOnly() {
        readOnly = isReadOnly();
        encodingCombo.setReadOnly(isReadOnly());
        rowSeparatorCombo.setReadOnly(isReadOnly());
        rowSeparatorText.setReadOnly(isReadOnly());
        rowsToSkipHeaderCheckboxCombo.setReadOnly(isReadOnly());
        rowsToSkipFooterCheckboxCombo.setReadOnly(isReadOnly());
        rowsToSkipLimitCheckboxCombo.setReadOnly(isReadOnly());
        emptyRowsToSkipCheckbox.setEnabled(!isReadOnly());
        firstRowIsCaptionCheckbox.setEnabled(!isReadOnly());
    }

    /**
     * add Field to Group Regex File Settings.
     * 
     * @param mainComposite
     * @param form
     * @param width
     * @param height
     */
    private void addGroupRegexFileSettings(final Composite mainComposite, final int width, final int height) {
        Group group = Form.createGroup(mainComposite, 2, Messages.getString("FileStep2.groupDelimitedFileSettings"), height);
        Composite compositeFileDelimitor = Form.startNewDimensionnedGridLayout(group, 4, width, height);

        EMetadataEncoding[] values = EMetadataEncoding.values();
        String[] encodingData = new String[values.length];
        for (int j = 0; j < values.length; j++) {
            encodingData[j] = values[j].getName();
        }

        encodingCombo = new LabelledCombo(compositeFileDelimitor, Messages.getString("FileStep2.encoding"), Messages
                .getString("FileStep2.encodingTip"), encodingData, 3, true, SWT.NONE);

        // Row Separator Combo & Text
        String[] rowSeparatorData = { RowSeparator.STANDART_EOL_LITERAL.getLiteral(),
                RowSeparator.CUSTOM_STRING_LITERAL.getLiteral() };
        rowSeparatorCombo = new LabelledCombo(compositeFileDelimitor, Messages.getString("FileStep2.rowSeparator"), Messages
                .getString("FileStep2.rowSeparatorTip"), rowSeparatorData, 1, true, SWT.READ_ONLY);
        rowSeparatorText = new LabelledText(compositeFileDelimitor, "", 1, true, SWT.RIGHT);
    }

    private void addGroupRowsToSkip(final Composite mainComposite, final int width, final int height) {
        // compositerowsToSkip Main Fields
        Group group = Form.createGroup(mainComposite, 1, Messages.getString("FileStep2.groupRowsToSkip"), height);
        Composite compositeRowsToSkip = Form.startNewDimensionnedGridLayout(group, 3, width - 100, height);

        // Information rowsToSkip
        Label info = new Label(compositeRowsToSkip, SWT.NONE);
        GridData gridData = new GridData();
        gridData.horizontalSpan = 3;
        info.setLayoutData(gridData);
        info.setText(Messages.getString("FileStep2.rowsToSkipTip"));

        // Header
        rowsToSkipHeaderCheckboxCombo = new LabelledCheckboxCombo(compositeRowsToSkip, Messages.getString("FileStep2.header"),
                Messages.getString("FileStep2.headerTip"), STRING_NUMBERS_DATA, 1, true, SWT.NONE);

        // Footer
        rowsToSkipFooterCheckboxCombo = new LabelledCheckboxCombo(compositeRowsToSkip, Messages.getString("FileStep2.footer"),
                Messages.getString("FileStep2.footerTip"), STRING_NUMBERS_DATA, 1, true, SWT.NONE);

        // Empty row
        emptyRowsToSkipCheckbox = new Button(compositeRowsToSkip, SWT.CHECK);
        emptyRowsToSkipCheckbox.setText(Messages.getString("FileStep2.removeEmptyRow"));
        emptyRowsToSkipCheckbox.setAlignment(SWT.LEFT);
        gridData = new GridData(SWT.FILL, SWT.BOTTOM, true, false);
        gridData.horizontalSpan = 3;
        emptyRowsToSkipCheckbox.setLayoutData(gridData);

    }

    /**
     * add Field to Group Regex.
     * 
     * @param mainComposite
     * @param form
     * @param width
     * @param height
     */
    private void addGroupRegex(final Composite mainComposite, final int width, final int height) {

        // Composite Regex
        Group group = Form.createGroup(mainComposite, 2, Messages.getString("RegexpStep2.groupRegexSettings"), height);
        Composite compositeRegex = Form.startNewDimensionnedGridLayout(group, 1, width, height);

        fieldSeparatorText = new Text(compositeRegex, SWT.MULTI | SWT.LEFT | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL); // |
                                                                                                                        // SWT.WRAP

        // Dimension of columb of Separator Text
        GridData gridData = new GridData(GridData.FILL_BOTH);
        gridData.minimumWidth = 80;
        gridData.minimumHeight = 70;

        fieldSeparatorText.setLayoutData(gridData);

        // Field Separator Text & Text
        fieldSeparatorType = new Text(compositeRegex, SWT.NONE);
        fieldSeparatorType.setVisible(false);
        fieldSeparatorType.setText(FieldSeparator.CUSTOM_REG_EXP_LITERAL.getName());
        FieldSeparator seperator = FieldSeparator.getByName(fieldSeparatorType.getText());
        getConnection().setFieldSeparatorType(seperator);

    }

    /**
     * add field to Group Limit.
     * 
     * @param mainComposite
     * @param form
     * @param width
     * @param height
     */
    private void addGroupLimit(final Composite mainComposite, final int width, final int height) {
        // Composite Limited rows
        Group group = Form.createGroup(mainComposite, 2, Messages.getString("FileStep2.groupLimitOfRows"), height);
        Composite compositeLimit = Form.startNewDimensionnedGridLayout(group, 3, width, height);

        // Information Limit
        Label info = new Label(compositeLimit, SWT.NONE);
        GridData gridData = new GridData();
        gridData.horizontalSpan = 2;
        info.setLayoutData(gridData);
        info.setText(Messages.getString("FileStep2.groupLimitOfRowsTip"));

        // Limit
        rowsToSkipLimitCheckboxCombo = new LabelledCheckboxCombo(compositeLimit, Messages.getString("FileStep2.limit"), Messages
                .getString("FileStep2.limitTip"), STRING_NUMBERS_DATA, 1, true, SWT.NONE);
    }

    /**
     * add Field to Group File Viewer.
     * 
     * @param parent
     * @param form
     * @param width
     * @param height
     */
    private void addGroupFileViewer(final Composite parent, final int width, int height) {
        // composite Regex File Preview
        previewGroup = Form.createGroup(parent, 1, Messages.getString("FileStep2.groupPreview"), height);
        Composite compositeRegexFilePreviewButton = Form.startNewDimensionnedGridLayout(previewGroup, 4, width,
                HEIGHT_BUTTON_PIXEL);
        height = height - HEIGHT_BUTTON_PIXEL - 15;

        // Regex File Preview Info
        firstRowIsCaptionCheckbox = new Button(compositeRegexFilePreviewButton, SWT.CHECK);
        firstRowIsCaptionCheckbox.setText(Messages.getString("FileStep2.firstRowsIsCaption"));
        firstRowIsCaptionCheckbox.setAlignment(SWT.LEFT);

        previewButton = new Button(compositeRegexFilePreviewButton, SWT.NONE);
        previewButton.setText(Messages.getString("FileStep2.refreshPreview"));
        previewButton.setSize(WIDTH_BUTTON_PIXEL, HEIGHT_BUTTON_PIXEL);

        // simple space
        new Label(compositeRegexFilePreviewButton, SWT.NONE);
        // Information Label
        previewInformationLabel = new Label(compositeRegexFilePreviewButton, SWT.NONE);
        previewInformationLabel
                .setText("                                                                                                                        ");
        previewInformationLabel.setForeground(getDisplay().getSystemColor(SWT.COLOR_BLUE));

        Composite compositeRegexFilePreview = Form.startNewDimensionnedGridLayout(previewGroup, 1, width, height);

        // Regex File Preview
        regexpFilePreview = new ShadowProcessPreview(compositeRegexFilePreview, null, width, height - 10);
        regexpFilePreview.newTablePreview();
    }

    protected void addFields() {

        // compositeFileDelimitor Main Fields
        Composite mainComposite = Form.startNewGridLayout(this, 2);

        addGroupRegexFileSettings(mainComposite, 400, 110);
        addGroupRowsToSkip(mainComposite, 300, 110);
        addGroupRegex(mainComposite, 400, 85);
        addGroupLimit(mainComposite, 300, 85);
        addGroupFileViewer(this, 700, 210);

        if (!isInWizard()) {
            // Bottom Button
            Composite compositeBottomButton = Form.startNewGridLayout(this, 2, false, SWT.CENTER, SWT.CENTER);
            // Button Cancel
            cancelButton = new UtilsButton(compositeBottomButton, Messages.getString("CommonWizard.cancel"), WIDTH_BUTTON_PIXEL,
                    HEIGHT_BUTTON_PIXEL);
        }
        addUtilsButtonListeners();
    }

    /**
     * create ProcessDescription and set it.
     * 
     * WARNING ::field FieldSeparator, RowSeparator, EscapeChar and TextEnclosure are surround by double quote.
     * 
     * @param getConnection()
     * 
     * @return processDescription
     */
    private ProcessDescription getProcessDescription() {

        ProcessDescription processDescription = ShadowProcessHelper.getProcessDescription(getConnection());

        // Adapt Header width firstRowIsCaption to preview the first line on caption or not
        Integer i = 0;
        if (rowsToSkipHeaderCheckboxCombo.isInteger()) {
            i = new Integer(rowsToSkipHeaderCheckboxCombo.getText());
        }
        if (firstRowIsCaptionCheckbox.getSelection()) {
            i--;
        }
        processDescription.setHeaderRow(i);

        // adapt the limit to the preview
        processDescription.setLimitRows(MAXIMUM_ROWS_TO_PREVIEW);
        if (rowsToSkipLimitCheckboxCombo.isInteger()) {
            i = new Integer(rowsToSkipLimitCheckboxCombo.getText());
            if (firstRowIsCaptionCheckbox.getSelection()) {
                i++;
            }
            if (i < MAXIMUM_ROWS_TO_PREVIEW) {
                processDescription.setLimitRows(i);
            }
        }

        return processDescription;
    }

    /**
     * clear the table preview.
     */
    void clearPreview() {
        regexpFilePreview.clearTablePreview();
    }

    /**
     * refreshPreview use ShadowProcess to refresh the preview.
     */
    void refreshPreview() {
        clearPreview();

        // if no file, the process don't be executed
        if (getConnection().getFilePath() == null || getConnection().getFilePath().equals("")) {
            previewInformationLabel.setText("   " + Messages.getString("FileStep2.filePathIncomplete"));
            return;
        }

        // if incomplete settings, , the process don't be executed
        if (!checkFieldsValue()) {
            previewInformationLabel.setText("   " + Messages.getString("FileStep2.settingsIncomplete"));
            return;
        }

        previewInformationLabel.setText("   " + Messages.getString("FileStep2.previewProgress"));

        // get the XmlArray width an adapt ProcessDescription
        try {
            ProcessDescription processDescription = getProcessDescription();
            XmlArray xmlArray = ShadowProcessHelper.getXmlArray(processDescription, "FILE_REGEXP");
            if (xmlArray == null) {
                previewInformationLabel.setText("   " + Messages.getString("FileStep2.previewFailure"));
            } else {
                previewInformationLabel.setText("   " + Messages.getString("FileStep2.previewIsDone"));

                // refresh TablePreview on this step
                regexpFilePreview.refreshTablePreview(xmlArray, firstRowIsCaptionCheckbox.getSelection());
                previewInformationLabel.setText("");
            }
        } catch (CoreException e) {
            previewInformationLabel.setText("   " + Messages.getString("FileStep2.previewFailure"));
            new ErrorDialogWidthDetailArea(getShell(), PID, Messages.getString("FileStep2.previewFailure"), e.getMessage());
            log.error(Messages.getString("FileStep2.previewFailure") + " " + e.getMessage());
        }
    }

    /**
     * Main Fields addControls.
     */
    protected void addFieldsListeners() {
        addFieldsListenersGroupRegexFileSettings();
        addFieldsListenersGroupsRowToSkipAndLimit();
        addFieldsListenersGroupsRegex();
        addFieldsListenersGroupsFileViewer();
    }

    /**
     * add Controls to group File Viewer.
     */
    private void addFieldsListenersGroupsFileViewer() {
        // Manage rowsToSkipHeader when firstRowIsCaption is checked
        firstRowIsCaptionCheckbox.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(final SelectionEvent e) {
                getConnection().setFirstLineCaption(firstRowIsCaptionCheckbox.getSelection());

                if (firstRowIsCaptionCheckbox.getSelection()) {
                    // when firstRowIsCaption is checked
                    if (rowsToSkipHeaderCheckboxCombo.isEmpty()) {
                        // at least, rowsToSkipHeader = 1
                        rowsToSkipHeaderCheckboxCombo.setText("1");
                        getConnection().setHeaderValue(1);
                    } else {
                        // rowsToSkipHeader ++
                        int value = new Integer(rowsToSkipHeaderCheckboxCombo.getText());
                        value++;
                        String newValue = new String("" + value);
                        rowsToSkipHeaderCheckboxCombo.setText(newValue);
                        getConnection().setHeaderValue(new Integer(rowsToSkipHeaderCheckboxCombo.getText()));
                    }
                } else {
                    // when firstRowIsCaption isn't checked
                    if (rowsToSkipHeaderCheckboxCombo.getText().equals("1")) {
                        // rowsToSkipHeader is unusable
                        rowsToSkipHeaderCheckboxCombo.deselectAll();
                        getConnection().setHeaderValue(0);
                    } else {
                        // rowsToSkipHeader --
                        int value = new Integer(rowsToSkipHeaderCheckboxCombo.getText());
                        value--;
                        String newValue = new String("" + value);
                        rowsToSkipHeaderCheckboxCombo.setText(newValue);
                        getConnection().setHeaderValue(new Integer(rowsToSkipHeaderCheckboxCombo.getText()));
                    }
                }
                checkFieldsValue();
            }
        });
    }

    /**
     * add Controls to group Escape Char.
     */
    private void addFieldsListenersGroupsRegex() {
        // // Escape Char Combo
        // escapeCharCombo.addModifyListener(new ModifyListener() {
        //
        // public void modifyText(final ModifyEvent e) {
        // getConnection().setEscapeChar(escapeCharCombo.getText());
        // checkFieldsValue();
        // }
        // });
        // textEnclosureCombo.addModifyListener(new ModifyListener() {
        //
        // public void modifyText(final ModifyEvent e) {
        // getConnection().setTextEnclosure(textEnclosureCombo.getText());
        // checkFieldsValue();
        // }
        // });
    }

    /**
     * DOC ocarbone Comment method "setCharFlag".
     * 
     * @param escapeCharFlag2
     * @param text
     */
    protected void setCharFlag(Label label, String string) {
        // PTODO OCA : when is utils in the Flag ?
        label.setText(string);
    }

    /**
     * add Controls to group Row To Skip and Limit.
     */
    private void addFieldsListenersGroupsRowToSkipAndLimit() {
        // Event modify
        ArrayList<LabelledCheckboxCombo> labelledCheckboxCombo2Control = new ArrayList<LabelledCheckboxCombo>();
        labelledCheckboxCombo2Control.add(rowsToSkipHeaderCheckboxCombo);
        labelledCheckboxCombo2Control.add(rowsToSkipLimitCheckboxCombo);
        labelledCheckboxCombo2Control.add(rowsToSkipFooterCheckboxCombo);

        Iterator<LabelledCheckboxCombo> iCheckboxCombo;
        LabelledCheckboxCombo labelledCheckboxCombo;

        // Event : keyPressed
        for (iCheckboxCombo = labelledCheckboxCombo2Control.iterator(); iCheckboxCombo.hasNext();) {
            labelledCheckboxCombo = iCheckboxCombo.next();

            // Event Key (numeric value only)
            labelledCheckboxCombo.addKeyListener(new KeyAdapter() {

                public void keyPressed(KeyEvent e) {
                    if (Character.getNumericValue(e.character) >= 10) {
                        e.doit = false;
                    }
                }
            });
        }
        // Event : Modify (to control the use of Ctrl V)
        rowsToSkipHeaderCheckboxCombo.addModifyListener(new ModifyListener() {

            public void modifyText(final ModifyEvent e) {
                if (!rowsToSkipHeaderCheckboxCombo.isEmpty()) {
                    if (!rowsToSkipHeaderCheckboxCombo.isInteger() || rowsToSkipHeaderCheckboxCombo.getText().equals("0")) {
                        rowsToSkipHeaderCheckboxCombo.deselectAll();
                        getConnection().setUseHeader(rowsToSkipHeaderCheckboxCombo.isChecked());
                        getConnection().setHeaderValue(0);

                        // if rowsHeaderToSkip isn't integer or is equals to 0, the firstRowIsCaptionCheckbox is
                        // unusable.
                        firstRowIsCaptionCheckbox.setSelection(false);
                        getConnection().setFirstLineCaption(false);
                    } else {
                        getConnection().setHeaderValue(new Integer(rowsToSkipHeaderCheckboxCombo.getText()));
                        getConnection().setUseHeader(rowsToSkipHeaderCheckboxCombo.isChecked());
                    }
                } else {
                    getConnection().setUseHeader(rowsToSkipHeaderCheckboxCombo.isChecked());
                    getConnection().setHeaderValue(0);
                }
                checkFieldsValue();
            }
        });

        rowsToSkipFooterCheckboxCombo.addModifyListener(new ModifyListener() {

            public void modifyText(final ModifyEvent e) {
                if (!rowsToSkipFooterCheckboxCombo.isEmpty()) {
                    if (!rowsToSkipFooterCheckboxCombo.isInteger() || rowsToSkipFooterCheckboxCombo.getText().equals("0")) {
                        rowsToSkipFooterCheckboxCombo.deselectAll();
                        getConnection().setUseFooter(rowsToSkipFooterCheckboxCombo.isChecked());
                        getConnection().setFooterValue(0);
                    } else {
                        getConnection().setFooterValue(new Integer(rowsToSkipFooterCheckboxCombo.getText()));
                    }
                } else {
                    getConnection().setUseFooter(rowsToSkipFooterCheckboxCombo.isChecked());
                    getConnection().setFooterValue(0);
                }
                checkFieldsValue();
            }
        });

        rowsToSkipLimitCheckboxCombo.addModifyListener(new ModifyListener() {

            public void modifyText(final ModifyEvent e) {
                if (!rowsToSkipLimitCheckboxCombo.isEmpty()) {
                    if (!rowsToSkipLimitCheckboxCombo.isInteger() || rowsToSkipLimitCheckboxCombo.getText().equals("0")) {
                        rowsToSkipLimitCheckboxCombo.deselectAll();
                        getConnection().setUseLimit(rowsToSkipLimitCheckboxCombo.isChecked());
                        getConnection().setLimitValue(0);
                    } else {
                        getConnection().setLimitValue(new Integer(rowsToSkipLimitCheckboxCombo.getText()));
                    }
                } else {
                    getConnection().setUseLimit(rowsToSkipLimitCheckboxCombo.isChecked());
                    getConnection().setLimitValue(0);
                }
                checkFieldsValue();
            }
        });

        // If nothing in rowsToSkipHeader, the firstRowIsCaption mustn't be checked
        rowsToSkipHeaderCheckboxCombo.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(final SelectionEvent e) {
                if ((!rowsToSkipHeaderCheckboxCombo.isChecked()) || rowsToSkipHeaderCheckboxCombo.getText().equals("0")) {
                    firstRowIsCaptionCheckbox.setSelection(false);
                    getConnection().setFirstLineCaption(false);
                }
            }
        });

        // empty Rows To Skip
        emptyRowsToSkipCheckbox.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(final SelectionEvent e) {
                getConnection().setRemoveEmptyRow(emptyRowsToSkipCheckbox.getSelection());
            }
        });
    }

    /**
     * add Controls of Group Regex File Settings.
     */
    private void addFieldsListenersGroupRegexFileSettings() {
        // Event encodingCombo
        encodingCombo.addModifyListener(new ModifyListener() {

            public void modifyText(final ModifyEvent e) {
                getConnection().setEncoding(encodingCombo.getText());
                checkFieldsValue();
            }
        });

        rowSeparatorCombo.addModifyListener(new ModifyListener() {

            public void modifyText(final ModifyEvent e) {
                // Label Custom of rowSeparatorText
                rowSeparatorManager();
            }
        });

        // Separator Text (field and row)
        fieldSeparatorText.addModifyListener(new ModifyListener() {

            public void modifyText(final ModifyEvent e) {
                getConnection().setFieldSeparatorValue(fieldSeparatorText.getText());
                checkFieldsValue();
            }
        });
        fieldSeparatorText.addKeyListener(new KeyAdapter() {

            public void keyPressed(KeyEvent e) {
                Boolean quoteIsEscape = false;
                if ((e.character) == Character.valueOf('"')) {
                    Point selection = fieldSeparatorText.getSelection();
                    if (selection.x > 0) {
                        if (fieldSeparatorText.getText().substring(selection.x - 1, selection.x).equals("\\")) {
                            quoteIsEscape = true;
                        }
                    }
                    if (!quoteIsEscape) {
                        updateStatus(IStatus.ERROR, Messages.getString("FileStep2.quoteDelimitedTip"));
                    }
                    e.doit = quoteIsEscape;
                }
            }
        });

        rowSeparatorText.addModifyListener(new ModifyListener() {

            public void modifyText(final ModifyEvent e) {
                getConnection().setRowSeparatorValue(rowSeparatorText.getText());
                checkFieldsValue();
            }
        });
        rowSeparatorText.addKeyListener(new KeyAdapter() {

            public void keyPressed(KeyEvent e) {
                Boolean quoteIsEscape = false;
                if ((e.character) == Character.valueOf('"')) {
                    Point selection = rowSeparatorText.getSelection();
                    if (selection.x > 0) {
                        if (rowSeparatorText.getText().substring(selection.x - 1, selection.x).equals("\\")) {
                            quoteIsEscape = true;
                        }
                    }
                    if (!quoteIsEscape) {
                        updateStatus(IStatus.ERROR, Messages.getString("FileStep2.quoteDelimitedTip"));
                    }
                    e.doit = quoteIsEscape;
                }
            }
        });

    }

    /**
     * rowSeparator : Adapt Custom Label and set the field Text.
     */
    protected void rowSeparatorManager() {
        RowSeparator separator = RowSeparator.getByName(rowSeparatorCombo.getText());
        getConnection().setRowSeparatorType(separator);

        if (rowSeparatorCombo.getSelectionIndex() == 1) {
            // Adapt Custom Label
            rowSeparatorText.setLabelText(rowSeparatorCombo.getText());
            rowSeparatorText.setEditable(true);
            rowSeparatorText.forceFocus();
        } else {
            // set the Flag width the char value of the Combo
            // { "Standard EOL", "Custom String" };
            if (rowSeparatorCombo.getSelectionIndex() == 0) {
                if (getConnection().getFormat().toString().equals(FileFormat.MAC_LITERAL.getName())) {
                    rowSeparatorText.setText("\\r");
                } else {
                    rowSeparatorText.setText("\\n");
                }
            }
            // Init Custom Label
            rowSeparatorText.setLabelText(Messages.getString("FileStep2.correspondingCharacter"));
            getConnection().setRowSeparatorValue(rowSeparatorText.getText());
            rowSeparatorText.setEditable(false);
        }
    }

    /**
     * Ensures that fields are set. Update checkEnable / use to checkConnection().
     * 
     * @return
     */
    protected boolean checkFieldsValue() {
        previewInformationLabel.setText("   " + Messages.getString("FileStep2.settingsIncomplete"));
        updateStatus(IStatus.OK, null);
        previewButton.setEnabled(false);

        // Separator Combo (field and row)
        if ("".equals(fieldSeparatorText.getText())) {
            updateStatus(IStatus.ERROR, Messages.getString("FileStep2.fieldSeparatorAlert"));
            return false;
        }
        if (fieldSeparatorText.getText().equals("\\") || fieldSeparatorText.getText().endsWith("\\")) {
            updateStatus(IStatus.ERROR, Messages.getString("FileStep2.fieldSeparatorIncomplete"));
            return false;
        }

        if ("".equals(rowSeparatorText.getText())) {
            updateStatus(IStatus.ERROR, Messages.getString("FileStep2.rowSeparatorAlert"));
            return false;
        }
        if (rowSeparatorText.getText().equals("\\") || rowSeparatorText.getText().endsWith("\\")) {
            updateStatus(IStatus.ERROR, Messages.getString("FileStep2.rowSeparatorIncomplete"));
            return false;
        }

        // Labelled Checkbox Combo (Row to Skip and Limit)
        ArrayList<LabelledCheckboxCombo> labelledCheckboxCombo2Control = new ArrayList<LabelledCheckboxCombo>();
        labelledCheckboxCombo2Control.add(rowsToSkipHeaderCheckboxCombo);
        labelledCheckboxCombo2Control.add(rowsToSkipLimitCheckboxCombo);
        labelledCheckboxCombo2Control.add(rowsToSkipFooterCheckboxCombo);

        Iterator<LabelledCheckboxCombo> iCheckboxCombo;
        LabelledCheckboxCombo labelledCheckboxCombo;

        for (iCheckboxCombo = labelledCheckboxCombo2Control.iterator(); iCheckboxCombo.hasNext();) {
            labelledCheckboxCombo = iCheckboxCombo.next();
            // if the checkbox is checked, check Numeric value
            if (labelledCheckboxCombo.getCheckbox().getSelection()) {
                if (labelledCheckboxCombo.getText() == "") {
                    updateStatus(IStatus.ERROR, labelledCheckboxCombo.getLabelText()
                            + Messages.getString("FileStep2.mustBePrecised"));
                    return false;
                }
            }
        }

        previewInformationLabel.setText("");
        previewButton.setEnabled(true);
        updateStatus(IStatus.OK, null);
        return true;
    }

    /**
     * addButtonControls.
     * 
     * @param cancelButton
     */
    protected void addUtilsButtonListeners() {

        // Event PreviewButton
        previewButton.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(final SelectionEvent e) {
                if (!previewButton.getText().equals(Messages.getString("FileStep2.wait"))) {
                    previewButton.setText(Messages.getString("FileStep2.wait"));
                    refreshPreview();
                } else {
                    previewButton.setText(Messages.getString("FileStep2.refreshPreview"));
                }
            }
        });

        if (cancelButton != null) {
            // Event CancelButton
            cancelButton.addSelectionListener(new SelectionAdapter() {

                public void widgetSelected(final SelectionEvent e) {
                    getShell().close();
                }
            });
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.swt.widgets.Control#setVisible(boolean)
     * 
     */
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (super.isVisible()) {
            // Adapt the UI rowSeparator to the file format
            rowSeparatorManager();

            // Fields to the Group Regex File Settings
            if (getConnection().getEncoding() != null && !getConnection().getEncoding().equals("")) {
                encodingCombo.setText(getConnection().getEncoding());
            } else {
                encodingCombo.select(0);
            }

            // Refresh the preview width the adapted rowSeparator
            // If metadata exist, refreshMetadata
            if (getConnection().getFilePath() != null && !("").equals(getConnection().getFilePath())) {
                if (getConnection().getFieldSeparatorValue() == null) {
                    fieldSeparatorText.setText(REGEXP_DEFAULT);
                }
                refreshPreview();
            }
            if (isReadOnly() != readOnly) {
                adaptFormToReadOnly();
            }
        }
    }
}
