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
package org.vrsl.jet.modeller.erd;

import java.io.IOException;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.text.MultiViewEditorElement;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.MIMEResolver;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;

@Messages({
    "LBL_ErdModeller_LOADER=Files of ErdModeller"
})
@MIMEResolver.ExtensionRegistration(
    displayName = "#LBL_ErdModeller_LOADER",
    mimeType = "text/cim+erd+xml",
    extension = {"xem"}
    )
@DataObject.Registration(
    mimeType = "text/cim+erd+xml",
    iconBase = "org/vrsl/jet/modeller/erd/ERDFile.gif",
    displayName = "#LBL_ErdModeller_LOADER",
    position = 300
    )
@ActionReferences({
    @ActionReference(
        path = "Loaders/text/cim+erd+xml/Actions",
        id = 
            @ActionID(category = "System", id = "org.openide.actions.OpenAction"),
            position = 100,
            separatorAfter = 200
            ),
    @ActionReference(
        path = "Loaders/text/cim+erd+xml/Actions",
        id = 
            @ActionID(category = "Edit", id = "org.openide.actions.CutAction"),
            position = 300
            ),
    @ActionReference(
        path = "Loaders/text/cim+erd+xml/Actions",
        id =
            @ActionID(category = "Edit", id = "org.openide.actions.CopyAction"),
            position = 400,
            separatorAfter = 500
        ),
    @ActionReference(
        path = "Loaders/text/cim+erd+xml/Actions",
        id =
            @ActionID(category = "Edit", id = "org.openide.actions.DeleteAction"),
            position = 600
            ),
    @ActionReference(
        path = "Loaders/text/cim+erd+xml/Actions",
        id =
            @ActionID(category = "System", id = "org.openide.actions.RenameAction"),
            position = 700,
            separatorAfter = 800
            ),
    @ActionReference(
        path = "Loaders/text/cim+erd+xml/Actions",
        id =
            @ActionID(category = "System", id = "org.openide.actions.SaveAsTemplateAction"),
            position = 900,
            separatorAfter = 1000
            ),
    @ActionReference(
        path = "Loaders/text/cim+erd+xml/Actions",
        id =
            @ActionID(category = "System", id = "org.openide.actions.FileSystemAction"),
            position = 1100,
            separatorAfter = 1200
            ),
    @ActionReference(
        path = "Loaders/text/cim+erd+xml/Actions",
        id =
            @ActionID(category = "System", id = "org.openide.actions.ToolsAction"),
            position = 1300
            ),
    @ActionReference(
        path = "Loaders/text/cim+erd+xml/Actions",
        id =
            @ActionID(category = "System", id = "org.openide.actions.PropertiesAction"),
            position = 1400
            )
})
public class ErdModellerDataObject extends MultiDataObject {
    
    static final long serialVersionUID = 112940239042390L;

    public ErdModellerDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException, IOException {
        super(pf, loader);
        registerEditor("text/cim+erd+xml", true);
    }

    @Override
    protected int associateLookup() {
        return 1;
    }
    
    @MultiViewElement.Registration(
        displayName = "#LBL_ErdModeller_EDITOR",
        iconBase = "org/vrsl/jet/modeller/erd/ERDFile.gif",
        mimeType = "text/cim+erd+xml",
        persistenceType = TopComponent.PERSISTENCE_ONLY_OPENED,
        preferredID = "ErdModeller",
        position = 1000
        )
    @Messages("LBL_ErdModeller_EDITOR=Source")
    public static MultiViewEditorElement createEditor(Lookup lkp) {
        return new MultiViewEditorElement(lkp);
    }
}
