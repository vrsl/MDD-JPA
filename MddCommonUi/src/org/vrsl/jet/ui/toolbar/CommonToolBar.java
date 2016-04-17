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
package org.vrsl.jet.ui.toolbar;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JToolBar;

public class CommonToolBar extends JToolBar {

    private ActionListener listener;

    public void setListener(ActionListener listener) {
        this.listener = listener;
    }

    public void addButton(String iconResourcePath, String text, String acction) {
        Image i = Toolkit.getDefaultToolkit().getImage(CommonToolBar.class.getResource(iconResourcePath));
        JButton b = new JButton();
        b.setIcon(new ImageIcon(i));
        b.setText(text);
        b.setActionCommand(acction);
        b.setFocusable(false);
        if (listener != null) {
            b.addActionListener(listener);
        }
        add(b);
    }

    public void addScaleSection(final Scalable scalable) {
        addSeparator();
        // -- Adding a lable -------------------------------------------------------------------------
        JLabel l = new JLabel();
        l.setText("Scale : ");
        l.setFocusable(false);
        add(l);
        // -- Adding scale drop down -----------------------------------------------------------------
        final JComboBox<String> cb = new JComboBox<>();
        cb.setToolTipText("Scale");
        cb.setMaximumSize(new java.awt.Dimension(70, 22));
        cb.setMinimumSize(new java.awt.Dimension(70, 22));
        cb.setPreferredSize(new java.awt.Dimension(70, 22));
        cb.setEnabled(false);
        cb.setFocusable(false);
        // -- Here is scale ruler -------------------------------------------------------------------
        for (int k = 1; k <= 20; k++) {
            cb.addItem(("" + k + "0%"));
        }
        cb.setSelectedIndex(9);
        cb.setEnabled(true);
        // -- Add a listener for this scale ruler ---------------------------------------------------
        cb.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                int scaleRate = ((cb.getSelectedIndex() + 1) * 10);
                scalable.setScale(scaleRate);
            }
        });

        add(cb);
    }
}
