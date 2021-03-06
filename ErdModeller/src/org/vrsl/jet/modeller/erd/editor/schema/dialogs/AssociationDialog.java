/* 
 * The MIT License
 *
 * Copyright 2016 Viktor Radzivilo.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.vrsl.jet.modeller.erd.editor.schema.dialogs;

import java.util.Enumeration;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.openide.windows.WindowManager;
import org.vrsl.jet.modeller.erd.editor.schema.view.associations.ElementsAssociation;
import org.vrsl.jet.models.erd.variants.associations.ErdModelReferenceMultiplicity;

public class AssociationDialog extends javax.swing.JDialog {

    private ElementsAssociation association;
    private boolean updated = false;

    /**
     * Creates new form AssociationDialog
     */
    public AssociationDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        setCenter();

        fromAssociationType.add(jFromRadioButton1);
        fromAssociationType.add(jFromRadioButton2);
        jFromRadioButton2.setSelected(true);
        fromAssociationType.add(jFromRadioButton3);
        fromAssociationType.add(jFromRadioButton4);

        toAssociationType.add(jToRadioButton1);
        toAssociationType.add(jToRadioButton2);
        jToRadioButton2.setSelected(true);
        toAssociationType.add(jToRadioButton3);
        toAssociationType.add(jToRadioButton4);

        validateInput();

        jTextFieldFrom.getDocument().addDocumentListener(
                new DocumentListener() {
                    @Override
                    public void insertUpdate(DocumentEvent e) {
                        validateInput();
                    }

                    @Override
                    public void removeUpdate(DocumentEvent e) {
                        validateInput();
                    }

                    @Override
                    public void changedUpdate(DocumentEvent e) {
                        validateInput();
                    }
                });

        jTextFieldTo.getDocument().addDocumentListener(
                new DocumentListener() {
                    @Override
                    public void insertUpdate(DocumentEvent e) {
                        validateInput();
                    }

                    @Override
                    public void removeUpdate(DocumentEvent e) {
                        validateInput();
                    }

                    @Override
                    public void changedUpdate(DocumentEvent e) {
                        validateInput();
                    }
                });
        
        String title = WindowManager.getDefault().getMainWindow().getTitle();
        setTitle(title);
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of
     * this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        fromAssociationType = new javax.swing.ButtonGroup();
        toAssociationType = new javax.swing.ButtonGroup();
        jBannerPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jButtonsPanel = new javax.swing.JPanel();
        jAcceptButton = new javax.swing.JButton();
        jCancelButton = new javax.swing.JButton();
        jHelpButton = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jFromRadioButton1 = new javax.swing.JRadioButton();
        jFromRadioButton2 = new javax.swing.JRadioButton();
        jFromRadioButton3 = new javax.swing.JRadioButton();
        jToRadioButton1 = new javax.swing.JRadioButton();
        jToRadioButton2 = new javax.swing.JRadioButton();
        jToRadioButton3 = new javax.swing.JRadioButton();
        jFromRadioButton4 = new javax.swing.JRadioButton();
        jToRadioButton4 = new javax.swing.JRadioButton();
        jTextFieldFrom = new javax.swing.JTextField();
        jTextFieldTo = new javax.swing.JTextField();
        jLabelFrom = new javax.swing.JLabel();
        jLabelTo = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(org.openide.util.NbBundle.getMessage(AssociationDialog.class, "AssociationDialog.title")); // NOI18N
        setMaximumSize(new java.awt.Dimension(2147483647, 345));
        setMinimumSize(new java.awt.Dimension(595, 345));
        setName("ElementsAssociation"); // NOI18N
        setPreferredSize(new java.awt.Dimension(595, 345));
        setResizable(false);

        jBannerPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/vrsl/jet/modeller/erd/images/DialogTitleAssociation.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(AssociationDialog.class, "AssociationDialog.jLabel1.text")); // NOI18N

        javax.swing.GroupLayout jBannerPanelLayout = new javax.swing.GroupLayout(jBannerPanel);
        jBannerPanel.setLayout(jBannerPanelLayout);
        jBannerPanelLayout.setHorizontalGroup(
            jBannerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 595, Short.MAX_VALUE)
        );
        jBannerPanelLayout.setVerticalGroup(
            jBannerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)
        );

        getContentPane().add(jBannerPanel, java.awt.BorderLayout.PAGE_START);

        org.openide.awt.Mnemonics.setLocalizedText(jAcceptButton, org.openide.util.NbBundle.getMessage(AssociationDialog.class, "AssociationDialog.jAcceptButton.text")); // NOI18N
        jAcceptButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jAcceptButtonActionPerformed(evt);
            }
        });
        jButtonsPanel.add(jAcceptButton);

        org.openide.awt.Mnemonics.setLocalizedText(jCancelButton, org.openide.util.NbBundle.getMessage(AssociationDialog.class, "AssociationDialog.jCancelButton.text")); // NOI18N
        jCancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCancelButtonActionPerformed(evt);
            }
        });
        jButtonsPanel.add(jCancelButton);

        org.openide.awt.Mnemonics.setLocalizedText(jHelpButton, org.openide.util.NbBundle.getMessage(AssociationDialog.class, "AssociationDialog.jHelpButton.text")); // NOI18N
        jButtonsPanel.add(jHelpButton);

        getContentPane().add(jButtonsPanel, java.awt.BorderLayout.PAGE_END);

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel1.setMaximumSize(new java.awt.Dimension(230, 180));
        jPanel1.setMinimumSize(new java.awt.Dimension(230, 180));
        jPanel1.setPreferredSize(new java.awt.Dimension(230, 180));
        java.awt.GridBagLayout jPanel1Layout = new java.awt.GridBagLayout();
        jPanel1Layout.columnWidths = new int[] {0, 20, 0};
        jPanel1Layout.rowHeights = new int[] {0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0};
        jPanel1.setLayout(jPanel1Layout);

        org.openide.awt.Mnemonics.setLocalizedText(jFromRadioButton1, org.openide.util.NbBundle.getMessage(AssociationDialog.class, "AssociationDialog.jFromRadioButton1.text")); // NOI18N
        jFromRadioButton1.setMaximumSize(new java.awt.Dimension(100, 23));
        jFromRadioButton1.setMinimumSize(new java.awt.Dimension(100, 23));
        jFromRadioButton1.setPreferredSize(new java.awt.Dimension(100, 23));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        jPanel1.add(jFromRadioButton1, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jFromRadioButton2, org.openide.util.NbBundle.getMessage(AssociationDialog.class, "AssociationDialog.jFromRadioButton2.text")); // NOI18N
        jFromRadioButton2.setMaximumSize(new java.awt.Dimension(100, 23));
        jFromRadioButton2.setMinimumSize(new java.awt.Dimension(100, 23));
        jFromRadioButton2.setPreferredSize(new java.awt.Dimension(100, 23));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel1.add(jFromRadioButton2, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jFromRadioButton3, org.openide.util.NbBundle.getMessage(AssociationDialog.class, "AssociationDialog.jFromRadioButton3.text")); // NOI18N
        jFromRadioButton3.setMaximumSize(new java.awt.Dimension(100, 23));
        jFromRadioButton3.setMinimumSize(new java.awt.Dimension(100, 23));
        jFromRadioButton3.setPreferredSize(new java.awt.Dimension(100, 23));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel1.add(jFromRadioButton3, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jToRadioButton1, org.openide.util.NbBundle.getMessage(AssociationDialog.class, "AssociationDialog.jToRadioButton1.text")); // NOI18N
        jToRadioButton1.setMaximumSize(new java.awt.Dimension(100, 23));
        jToRadioButton1.setMinimumSize(new java.awt.Dimension(100, 23));
        jToRadioButton1.setPreferredSize(new java.awt.Dimension(100, 23));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel1.add(jToRadioButton1, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jToRadioButton2, org.openide.util.NbBundle.getMessage(AssociationDialog.class, "AssociationDialog.jToRadioButton2.text")); // NOI18N
        jToRadioButton2.setMaximumSize(new java.awt.Dimension(100, 23));
        jToRadioButton2.setMinimumSize(new java.awt.Dimension(100, 23));
        jToRadioButton2.setPreferredSize(new java.awt.Dimension(100, 23));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel1.add(jToRadioButton2, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jToRadioButton3, org.openide.util.NbBundle.getMessage(AssociationDialog.class, "AssociationDialog.jToRadioButton3.text")); // NOI18N
        jToRadioButton3.setMaximumSize(new java.awt.Dimension(87, 23));
        jToRadioButton3.setPreferredSize(new java.awt.Dimension(100, 23));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel1.add(jToRadioButton3, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jFromRadioButton4, org.openide.util.NbBundle.getMessage(AssociationDialog.class, "AssociationDialog.jFromRadioButton4.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel1.add(jFromRadioButton4, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jToRadioButton4, org.openide.util.NbBundle.getMessage(AssociationDialog.class, "AssociationDialog.jToRadioButton4.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel1.add(jToRadioButton4, gridBagConstraints);

        jTextFieldFrom.setMaximumSize(new java.awt.Dimension(160, 22));
        jTextFieldFrom.setMinimumSize(new java.awt.Dimension(160, 22));
        jTextFieldFrom.setPreferredSize(new java.awt.Dimension(160, 22));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        jPanel1.add(jTextFieldFrom, gridBagConstraints);

        jTextFieldTo.setMaximumSize(new java.awt.Dimension(160, 22));
        jTextFieldTo.setMinimumSize(new java.awt.Dimension(160, 22));
        jTextFieldTo.setPreferredSize(new java.awt.Dimension(160, 22));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 6);
        jPanel1.add(jTextFieldTo, gridBagConstraints);

        jLabelFrom.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabelFrom, org.openide.util.NbBundle.getMessage(AssociationDialog.class, "AssociationDialog.jLabelFrom.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        jPanel1.add(jLabelFrom, gridBagConstraints);

        jLabelTo.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabelTo, org.openide.util.NbBundle.getMessage(AssociationDialog.class, "AssociationDialog.jLabelTo.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        jPanel1.add(jLabelTo, gridBagConstraints);

        getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(AssociationDialog.class, "AssociationDialog.AccessibleContext.accessibleName")); // NOI18N

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jAcceptButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jAcceptButtonActionPerformed
        applayDataInput();
        dispose();
    }//GEN-LAST:event_jAcceptButtonActionPerformed

    private void jCancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCancelButtonActionPerformed
        dispose();
    }//GEN-LAST:event_jCancelButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup fromAssociationType;
    private javax.swing.JButton jAcceptButton;
    private javax.swing.JPanel jBannerPanel;
    private javax.swing.JPanel jButtonsPanel;
    private javax.swing.JButton jCancelButton;
    private javax.swing.JRadioButton jFromRadioButton1;
    private javax.swing.JRadioButton jFromRadioButton2;
    private javax.swing.JRadioButton jFromRadioButton3;
    private javax.swing.JRadioButton jFromRadioButton4;
    private javax.swing.JButton jHelpButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabelFrom;
    private javax.swing.JLabel jLabelTo;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JTextField jTextFieldFrom;
    private javax.swing.JTextField jTextFieldTo;
    private javax.swing.JRadioButton jToRadioButton1;
    private javax.swing.JRadioButton jToRadioButton2;
    private javax.swing.JRadioButton jToRadioButton3;
    private javax.swing.JRadioButton jToRadioButton4;
    private javax.swing.ButtonGroup toAssociationType;
    // End of variables declaration//GEN-END:variables

    /**
     * @return the association
     */
    public ElementsAssociation getAssociation() {
        return association;
    }

    /**
     * @param association the association to set
     */
    public void setAssociation(ElementsAssociation association) {
        this.association = association;
        applayUIData();
    }

    public boolean isUpdated() {
        return updated;
    }

    private void setCenter() {
        java.awt.Rectangle screenRect = this.getGraphicsConfiguration().getBounds();
        this.setLocation(
                screenRect.x + screenRect.width / 2 - this.getSize().width / 2,
                screenRect.y + screenRect.height / 2 - this.getSize().height / 2);
    }

    private void validateInput() {
        if (association == null) {
            return;
        }
        if (!association.isAssociationToItself()) {
            if (!jTextFieldFrom.getText().isEmpty() || !jTextFieldTo.getText().isEmpty()) {
                if (!jTextFieldFrom.getText().isEmpty() && !jTextFieldTo.getText().isEmpty()) {
                    jAcceptButton.setEnabled(true);
                } else {
                    jAcceptButton.setEnabled(false);
                }
            } else {
                jAcceptButton.setEnabled(true);
            }
        } else {
            jAcceptButton.setEnabled(jTextFieldFrom.getText().isEmpty() && !jTextFieldTo.getText().isEmpty());
        }
    }

    private void applayUIData() {

        jLabelFrom.setText(association.getFromObject() != null ? association.getFromObject().getName() : "N/A");
        jLabelTo.setText(association.getToObject() != null ? association.getToObject().getName() : "N/A");

        jTextFieldFrom.setText(association.getNameFrom());
        jTextFieldTo.setText(association.getNameTo());

        switch (association.getFromMode()) {
            case NONE_OR_ONE:
                jFromRadioButton1.setSelected(true);
                break;
            case ONE:
                jFromRadioButton2.setSelected(true);
                break;
            case NONE_OR_MANY:
                jFromRadioButton3.setSelected(true);
                break;
            case MANY:
                jFromRadioButton4.setSelected(true);
                break;
            default:
                break;
        }

        switch (association.getToMode()) {
            case NONE_OR_ONE:
                jToRadioButton1.setSelected(true);
                break;
            case ONE:
                jToRadioButton2.setSelected(true);
                break;
            case NONE_OR_MANY:
                jToRadioButton3.setSelected(true);
                break;
            case MANY:
                jToRadioButton4.setSelected(true);
                break;
            default:
                break;
        }
        
        if(association.isAssociationToItself()) {
            jFromRadioButton3.setEnabled(false);
            jFromRadioButton4.setEnabled(false);
            jToRadioButton2.setEnabled(false);
            jToRadioButton4.setEnabled(false);
        }
    }

    private void applayDataInput() {
        association.setFrom(association.getFromObject(), jTextFieldFrom.getText(), getCategory(fromAssociationType));
        association.setTo(association.getToObject(), jTextFieldTo.getText(), getCategory(toAssociationType));
        association.setDirty(true);
        updated = true;
    }

    private ErdModelReferenceMultiplicity.Category getCategory(ButtonGroup gr) {
        ErdModelReferenceMultiplicity.Category[] categories = new ErdModelReferenceMultiplicity.Category[]{
            ErdModelReferenceMultiplicity.Category.NONE_OR_ONE,
            ErdModelReferenceMultiplicity.Category.ONE,
            ErdModelReferenceMultiplicity.Category.NONE_OR_MANY,
            ErdModelReferenceMultiplicity.Category.MANY,};
        int num = 0;
        for (Enumeration<AbstractButton> buttons = gr.getElements(); buttons.hasMoreElements(); num++) {
            AbstractButton button = buttons.nextElement();
            if (button.isSelected()) {
                break;
            }
        }
        return categories[num];
    }
}
