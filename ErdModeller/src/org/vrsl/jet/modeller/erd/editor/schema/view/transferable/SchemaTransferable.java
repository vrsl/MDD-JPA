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
package org.vrsl.jet.modeller.erd.editor.schema.view.transferable;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SchemaTransferable implements Transferable, ClipboardOwner {

    private static final Logger logger = Logger.getLogger(SchemaTransferable.class.getName());
    // -------------------------------------------------------------------------------
    public static final transient DataFlavor xmlSchemaFlavor;
    public static final transient DataFlavor jvmSchemaFlavor;
    private static final transient DataFlavor[] supportedFlavors;
    private static final transient List<DataFlavor> flavors;

    static {
        List<DataFlavor> initialisedFlavors = new ArrayList<>();
        DataFlavor jvmFlavor = null;
        DataFlavor xmlFlavor = new DataFlavor(XmlSchemaRepresentation.class, "JArchitect Classes XML Schema Representation");
        initialisedFlavors.add(xmlFlavor);
        try {
            jvmFlavor = new DataFlavor(
                    DataFlavor.javaJVMLocalObjectMimeType + ";class=" + SchemaTransferable.class.getName(),
                    "JArchitect Classes MIME Schema Representation",
                    SchemaTransferable.class.getClassLoader());
            initialisedFlavors.add(jvmFlavor);
        } catch (ClassNotFoundException ex) {
            logger.log(Level.SEVERE, "Can''t create DnD MIME DataFlavor for XML Schema Representation because: {0}", ex);
        }
        xmlSchemaFlavor = xmlFlavor;
        jvmSchemaFlavor = jvmFlavor;
        supportedFlavors = initialisedFlavors.toArray(new DataFlavor[initialisedFlavors.size()]);
        flavors = initialisedFlavors;
    }
    private XmlSchemaRepresentation xmlRepresentation = null;

    /**
     * Creates a new instance of SchemaTransferable
     */
    public SchemaTransferable(XmlSchemaRepresentation xmlRepresentation) {
        this.xmlRepresentation = xmlRepresentation;
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return supportedFlavors.clone();
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return flavors.contains(flavor);
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if (flavors.contains(flavor)) {
            return xmlRepresentation;
        }
        throw new UnsupportedFlavorException(flavor);
    }

    @Override
    public void lostOwnership(Clipboard clipboard, Transferable contents) {
    }
}
