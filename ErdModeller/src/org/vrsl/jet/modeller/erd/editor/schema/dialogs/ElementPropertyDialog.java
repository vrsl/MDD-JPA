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

import org.openide.windows.WindowManager;
import org.vrsl.jet.modeller.erd.editor.schema.view.elements.EntityProperty;

@SuppressWarnings({"unchecked", "rawtypes", "deprecation"})
public class ElementPropertyDialog extends javax.swing.JDialog {

    private final String[] propertyTypes = new String[]{
        "boolean",
        "char",
        "byte",
        "short",
        "integer",
        "long",
        "String",
        "float",
        "double",
        "Date"
    };
    private boolean updated = false;

    /**
     * Creates new form ElementPropertyDialog
     */
    public ElementPropertyDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();

        for (String propertyType : propertyTypes) {
            jTypeComboBox.addItem(propertyType);
        }

        String title = WindowManager.getDefault().getMainWindow().getTitle();
        setTitle(title);

        setCenter();
    }

    private void setCenter() {
        java.awt.Rectangle screenRect = this.getGraphicsConfiguration().getBounds();
        this.setLocation(
                screenRect.x + screenRect.width / 2 - this.getSize().width / 2,
                screenRect.y + screenRect.height / 2 - this.getSize().height / 2);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel1 = new javax.swing.JPanel();
        jAcceptButton = new javax.swing.JButton();
        jCancelButton = new javax.swing.JButton();
        jHelpButton = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jNameTextField = new javax.swing.JTextField();
        jTypeComboBox = new javax.swing.JComboBox();
        jIsKeyCheckBox = new javax.swing.JCheckBox();
        jIsAutogenerateCheckBox = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setPreferredSize(new java.awt.Dimension(595, 345));
        setResizable(false);

        org.openide.awt.Mnemonics.setLocalizedText(jAcceptButton, org.openide.util.NbBundle.getMessage(ElementPropertyDialog.class, "ElementPropertyDialog.jAcceptButton.text")); // NOI18N
        jAcceptButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jAcceptButtonActionPerformed(evt);
            }
        });
        jPanel1.add(jAcceptButton);

        org.openide.awt.Mnemonics.setLocalizedText(jCancelButton, org.openide.util.NbBundle.getMessage(ElementPropertyDialog.class, "ElementPropertyDialog.jCancelButton.text")); // NOI18N
        jCancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCancelButtonActionPerformed(evt);
            }
        });
        jPanel1.add(jCancelButton);

        org.openide.awt.Mnemonics.setLocalizedText(jHelpButton, org.openide.util.NbBundle.getMessage(ElementPropertyDialog.class, "ElementPropertyDialog.jHelpButton.text")); // NOI18N
        jPanel1.add(jHelpButton);

        getContentPane().add(jPanel1, java.awt.BorderLayout.PAGE_END);

        jPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel2.setPreferredSize(new java.awt.Dimension(626, 100));

        jLabel3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/vrsl/jet/modeller/erd/images/DialogTitleEntityProperty.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(ElementPropertyDialog.class, "ElementPropertyDialog.jLabel3.text")); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        getContentPane().add(jPanel2, java.awt.BorderLayout.PAGE_START);

        jPanel3.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel3.setMaximumSize(new java.awt.Dimension(230, 210));
        jPanel3.setMinimumSize(new java.awt.Dimension(230, 210));
        jPanel3.setPreferredSize(new java.awt.Dimension(230, 210));
        java.awt.GridBagLayout jPanel3Layout = new java.awt.GridBagLayout();
        jPanel3Layout.columnWidths = new int[] {0, 20, 0};
        jPanel3Layout.rowHeights = new int[] {0, 12, 0, 12, 0, 12, 0};
        jPanel3.setLayout(jPanel3Layout);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(ElementPropertyDialog.class, "ElementPropertyDialog.jLabel1.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        jPanel3.add(jLabel1, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(ElementPropertyDialog.class, "ElementPropertyDialog.jLabel2.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        jPanel3.add(jLabel2, gridBagConstraints);

        jNameTextField.setText(org.openide.util.NbBundle.getMessage(ElementPropertyDialog.class, "ElementPropertyDialog.jNameTextField.text")); // NOI18N
        jNameTextField.setPreferredSize(new java.awt.Dimension(200, 27));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        jPanel3.add(jNameTextField, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        jPanel3.add(jTypeComboBox, gridBagConstraints);

        jIsKeyCheckBox.setLabel(org.openide.util.NbBundle.getMessage(ElementPropertyDialog.class, "ElementPropertyDialog.jIsKeyCheckBox.label")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        jPanel3.add(jIsKeyCheckBox, gridBagConstraints);

        jIsAutogenerateCheckBox.setLabel(org.openide.util.NbBundle.getMessage(ElementPropertyDialog.class, "ElementPropertyDialog.jIsAutogenerateCheckBox.label")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        jPanel3.add(jIsAutogenerateCheckBox, gridBagConstraints);

        getContentPane().add(jPanel3, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jAcceptButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jAcceptButtonActionPerformed
        updated = true;
        dispose();
    }//GEN-LAST:event_jAcceptButtonActionPerformed

    private void jCancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCancelButtonActionPerformed
        dispose();
    }//GEN-LAST:event_jCancelButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jAcceptButton;
    private javax.swing.JButton jCancelButton;
    private javax.swing.JButton jHelpButton;
    private javax.swing.JCheckBox jIsAutogenerateCheckBox;
    private javax.swing.JCheckBox jIsKeyCheckBox;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JTextField jNameTextField;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JComboBox jTypeComboBox;
    // End of variables declaration//GEN-END:variables

    /**
     * @return the property
     */
    public EntityProperty getProperty() {
        EntityProperty fl = new EntityProperty(jNameTextField.getText(), jTypeComboBox.getSelectedIndex() + 1);
        fl.setPrimaryKeyMode(jIsKeyCheckBox.isSelected());
        fl.setUseAutoSequence(jIsAutogenerateCheckBox.isSelected());
        return fl;
    }

    /**
     * @param property the property to set
     */
    public void setProperty(EntityProperty property) {
        jNameTextField.setText(property.getName());
        jTypeComboBox.setSelectedIndex(property.getType() - 1);
        jIsKeyCheckBox.setSelected(property.getPrimaryKeyMode());
        jIsAutogenerateCheckBox.setSelected(property.getUseAutoSequence());
    }

    public boolean isUpdated() {
        return updated;
    }
}