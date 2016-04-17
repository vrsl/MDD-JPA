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
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.io.IOException;
import javax.swing.SwingUtilities;
import org.openide.util.Exceptions;
import org.vrsl.jet.modeller.erd.editor.schema.editors.SchemaPanel;
import org.vrsl.jet.modeller.erd.editor.schema.view.transferable.SchemaTransferable;
import org.vrsl.jet.modeller.erd.editor.schema.view.transferable.XmlSchemaRepresentation;

public class PasteAction extends AbstractSelfCheckingAction {

    private final SchemaPanel sp;

    public PasteAction(SchemaPanel sp) {
        this.sp = sp;
    }

    @Override
    public void observeConditions() {
        Clipboard clip = sp.getToolkit().getSystemClipboard();
        Transferable tr = clip.getContents(sp);
        final boolean isRegionSelected = tr.isDataFlavorSupported(SchemaTransferable.xmlSchemaFlavor);
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
        Transferable tr = clip.getContents(sp);
        try {
            XmlSchemaRepresentation tempClip = (XmlSchemaRepresentation) tr.getTransferData(SchemaTransferable.xmlSchemaFlavor);
            sp.setGroupSechemaRepresentation(tempClip);
        } catch (UnsupportedFlavorException | IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone(); //To change body of generated methods, choose Tools | Templates.
    }
}
