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
package org.vrsl.jet.modeller.erd.editor.schema.editors.actions;

import java.awt.datatransfer.Clipboard;
import java.awt.event.ActionEvent;
import javax.swing.SwingUtilities;
import org.vrsl.jet.modeller.erd.editor.schema.editors.SchemaPanel;
import org.vrsl.jet.modeller.erd.editor.schema.view.transferable.SchemaTransferable;

public class CutAction extends AbstractSelfCheckingAction {

    private final SchemaPanel sp;

    public CutAction(SchemaPanel sp) {
        this.sp = sp;
    }

    @Override
    public void observeConditions() {
        final boolean isRegionSelected = sp.isRegionSelected();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                setEnabled(isRegionSelected);
            }
        });
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Clipboard clip = sp.getToolkit().getSystemClipboard();
        SchemaTransferable tr = new SchemaTransferable(sp.getGroupSchemaRepresentaion());
        clip.setContents(tr, tr);
        sp.deleteGroupSelected();
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone(); //To change body of generated methods, choose Tools | Templates.
    }
}
