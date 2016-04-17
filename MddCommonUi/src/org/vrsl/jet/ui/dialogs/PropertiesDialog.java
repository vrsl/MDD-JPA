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
package org.vrsl.jet.ui.dialogs;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;
import org.openide.windows.WindowManager;
import org.vrsl.jet.translators.properties.TranslatorPropertyMetadata;
import org.vrsl.jet.ui.components.JIntegerTextField;
import org.vrsl.jet.ui.components.JNumberTextField;
import org.vrsl.jet.ui.toolbar.CommonToolBar;

public class PropertiesDialog extends javax.swing.JDialog {

    static final public long serialVersionUID = 200000004;
    private Map<String, Object> props = null;
    private List<JComponent> fields = null;
    private List<String> fnames = null;
    public static final int IDCANCEL = 0;
    public static final int IDOK = 1;
    private int finalState = IDCANCEL;

    /**
     * Creates new form PropertiesDialog
     */
    public PropertiesDialog(Component parent, boolean modal) {
        super((Frame) SwingUtilities.getWindowAncestor(parent), modal);
        initComponents();

        String title = WindowManager.getDefault().getMainWindow().getTitle();
        setTitle(title);
    }

    /**
     * Creates new form PropertiesDialog
     */
    public PropertiesDialog(Dialog parent, boolean modal) {
        super(parent, modal);
        initComponents();
    }

    public void initliseControls(Map<String, Object> props, Map<String, TranslatorPropertyMetadata> metadata) {
        java.awt.Rectangle screenRect = this.getGraphicsConfiguration().getBounds();
        this.setLocation(
                screenRect.x + screenRect.width / 2 - this.getSize().width / 2,
                screenRect.y + screenRect.height / 2 - this.getSize().height / 2);

        fields = new CopyOnWriteArrayList<>();
        fnames = new CopyOnWriteArrayList<>();
        this.props = props;

        GridBagLayout gb = (GridBagLayout) jPropertiesPanel.getLayout();
        GridBagConstraints c = new GridBagConstraints();

        for (String name : props.keySet()) {

            c.fill = GridBagConstraints.BOTH;
            c.weightx = 1.0;
            JLabel l = new JLabel(" " + name);
            c.weightx = 1.0;
            c.gridwidth = GridBagConstraints.RELATIVE;
            gb.setConstraints(l, c);
            jPropertiesPanel.add(l);

            JComponent fl = buildUiComponent(metadata.get(name));
            c.gridwidth = GridBagConstraints.REMAINDER;
            c.weightx = 1.0;
            gb.setConstraints(fl, c);
            jPropertiesPanel.add(fl);

            setValue(fl, this.props.get(name));

            fnames.add(name);
            fields.add(fl);
        }

        int sizeY = props.size() * 24 + 10;
        if (jScrollPane.getSize().getHeight() > sizeY) {
            jScrollPane.setPreferredSize(new Dimension((int) jScrollPane.getSize().getWidth(), sizeY));
        }
        pack();
    }

    private JComponent buildUiComponent(TranslatorPropertyMetadata meta) {
        TranslatorPropertyMetadata.Type propType = meta.getPropertyType();
        switch (propType) {
            case STRING:
                return new JTextField();
            case INTEGER:
                return new JIntegerTextField();
            case NUMBER:
                return new JNumberTextField();
            case DATE:
                return new JTextField();
            case PATH:
                return new JTextField();
            case BOOLEAN:
                return new JCheckBox();
            case SET:
                JComboBox<String> res = new JComboBox<>();
                for (String item : meta.getApplicableValues()) {
                    res.addItem(item);
                }
                return res;
        }
        throw new IllegalArgumentException("TranslatorPropertyMetadata " + meta.getPropertyType() + " has not been supported yet.");
    }

    private void setValue(JComponent c, Object value) {
        if (c instanceof JCheckBox) {
            ((AbstractButton) c).setSelected("TRUE".equals(value.toString().toUpperCase(Locale.getDefault())));
        } else if (c instanceof JIntegerTextField) {
            ((JFormattedTextField) c).setValue(value);
        } else if (c instanceof JNumberTextField) {
            ((JFormattedTextField) c).setValue(value);
        } else if (c instanceof JTextField) {
            ((JTextComponent) c).setText(value.toString());
        } else if (c instanceof JComboBox) {
            ((JComboBox) c).setSelectedItem(value);
        }
    }

    private Object getValue(JComponent c) {
        if (c instanceof JCheckBox) {
            return ((AbstractButton) c).isSelected();
        } else if (c instanceof JIntegerTextField) {
            return ((JFormattedTextField) c).getValue();
        } else if (c instanceof JNumberTextField) {
            return ((JFormattedTextField) c).getValue();
        } else if (c instanceof JTextField) {
            return ((JTextComponent) c).getText();
        } else if (c instanceof JComboBox) {
            return ((JComboBox) c).getSelectedItem().toString();
        }
        return "N/A";
    }

    private void initComponents() {
        jScrollPane = new javax.swing.JScrollPane();
        jPropertiesPanel = new javax.swing.JPanel();
        jButtonsPanel = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();

        setTitle("Properties");
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        jScrollPane.setPreferredSize(new java.awt.Dimension(593, 160));
        jPropertiesPanel.setLayout(new java.awt.GridBagLayout());

        jScrollPane.setViewportView(jPropertiesPanel);

        getContentPane().add(jScrollPane, java.awt.BorderLayout.CENTER);

        jButton1.setText("Accept");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButtonsPanel.add(jButton1);

        jButton2.setText("Cancel");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButtonsPanel.add(jButton2);

        getContentPane().add(jButtonsPanel, java.awt.BorderLayout.SOUTH);

        JPanel jBannerPanel = new JPanel();
        jBannerPanel.setPreferredSize(new java.awt.Dimension(593, 100));
        JLabel jBannerLabel = new JLabel();
        Image i = Toolkit.getDefaultToolkit().getImage(CommonToolBar.class.getResource("/Icons/ModelTransformationOptions.png"));
        jBannerLabel.setIcon(new ImageIcon(i));
        jBannerPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        getContentPane().add(jBannerLabel, java.awt.BorderLayout.NORTH);

        pack();
    }

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {
        finalState = IDCANCEL;
        setVisible(false);
        dispose();
    }

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {

        for (int i = 0; i < fnames.size(); i++) {
            String name = fnames.get(i);
            JComponent fl = fields.get(i);
            props.put(name, getValue(fl));
        }

        finalState = IDOK;

        setVisible(false);
        dispose();
    }

    /**
     * Closes the dialog
     */
    private void closeDialog(java.awt.event.WindowEvent evt) {
        setVisible(false);
        dispose();
    }

    public int getFinalState() {
        return finalState;
    }

    public Map<String, Object> getProperties() {
        return Collections.unmodifiableMap(props);
    }
    // Variables declaration - do not modify
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton1;
    private javax.swing.JScrollPane jScrollPane;
    private javax.swing.JPanel jPropertiesPanel;
    private javax.swing.JPanel jButtonsPanel;
    // End of variables declaration
}
