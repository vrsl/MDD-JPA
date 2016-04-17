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

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.util.Date;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.RepaintManager;
import javax.swing.text.DefaultEditorKit;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.netbeans.spi.print.PrintPage;
import org.netbeans.spi.print.PrintProvider;
import org.openide.awt.UndoRedo;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.windows.TopComponent;
import org.vrsl.jet.modeller.erd.editor.schema.editors.SchemaPanel;
import org.vrsl.jet.modeller.erd.editor.schema.editors.actions.AbstractSelfCheckingAction;
import org.vrsl.jet.modeller.erd.editor.schema.editors.actions.CopyAction;
import org.vrsl.jet.modeller.erd.editor.schema.editors.actions.CutAction;
import org.vrsl.jet.modeller.erd.editor.schema.editors.actions.PasteAction;
import org.vrsl.jet.translators.AbstractCimTranslator;
import org.vrsl.jet.translators.TranslatorsRepository;
import org.vrsl.jet.ui.toolbar.CommonToolBar;
import org.vrsl.jet.ui.toolbar.Scalable;

@MultiViewElement.Registration(
    displayName = "#LBL_ErdModeller_VISUAL",
    iconBase = "org/vrsl/jet/modeller/erd/ERDFile.gif",
    mimeType = "text/cim+erd+xml",
    persistenceType = TopComponent.PERSISTENCE_NEVER,
    preferredID = "ErdModellerVisual",
    position = 2000
    )
@Messages("LBL_ErdModeller_VISUAL=Visual")
public class ErdModellerVisualElement extends TopComponent implements MultiViewElement, Scalable, PrintProvider, ActionsController {

    private ErdModellerDataObject obj;
    private CommonToolBar toolbar = new CommonToolBar();
    private transient MultiViewElementCallback callback;
    private SchemaPanel panel;
    private JComboBox<String> trnslatorComboBox;
    // -- Cut/Copy/Paste actions -------------------------------------------------------
    private AbstractSelfCheckingAction cutAction;
    private AbstractSelfCheckingAction copyAction;
    private AbstractSelfCheckingAction pasteAction;
    // -- Print provider ---------------------------------------------------------------
    private InstanceContent content = new InstanceContent();
    private Lookup componetLookup = new AbstractLookup(content);
    // -- First run check --------------------------------------------------------------
    private boolean isFirstRun = true;

    public ErdModellerVisualElement() {
        throw new UnsupportedOperationException("Void constructor has been not supported yet.");
    }

    public ErdModellerVisualElement(Lookup lkp) {
        obj = lkp.lookup(ErdModellerDataObject.class);
        assert obj != null;
        initComponents();
        initEditorPanel();
        initToolBar();
    }

    private void initEditorPanel() {
        // -- Intialising schema editor ------------------------------------------------
        panel = new SchemaPanel(
                this,
                obj);
        jScrollPane.setViewportView(panel);
        jScrollPane.getViewport().setBackground(Color.WHITE);
        // -- Also creating actions to support this UI panel ---------------------------
        cutAction = new CutAction(panel);
        copyAction = new CopyAction(panel);
        pasteAction = new PasteAction(panel);
        // -----------------------------------------------------------------------------
        content.add(this);
    }

    private void initToolBar() {
        toolbar.setListener(panel);
        // -- Adding buttons into the toolbar ------------------------------------------
        toolbar.addSeparator();
        toolbar.addButton("/Icons/Mono/fa-mouse-pointer-16x16.png", "", "Pointer");
        toolbar.addButton("/Icons/Mono/fa-trash-o-16x16.png", "", "Delete");
        toolbar.addSeparator();
        toolbar.addButton("/Icons/Mono/fa-file-text-16x16.png", "", "Text");
        toolbar.addButton("/Icons/Mono/fa-commenting-16x16.png", "", "Comment");
        toolbar.addButton("/Icons/Mono/fa-table-16x16.png", "", "Entity");
        toolbar.addButton("/Icons/Links.gif", "", "Relationship");
        toolbar.addButton("/Icons/CommentsLink.gif", "", "Remark");
        // -- Adding scale controls ----------------------------------------------------
        toolbar.addScaleSection(this);
        // -- Adding available transformers stuff --------------------------------------
        toolbar.addSeparator();
        toolbar.add(new JLabel("Translator: "));

        trnslatorComboBox = new JComboBox<>();
        trnslatorComboBox.setToolTipText("Scale");
        trnslatorComboBox.setMaximumSize(new java.awt.Dimension(250, 22));
        trnslatorComboBox.setMinimumSize(new java.awt.Dimension(250, 22));
        trnslatorComboBox.setPreferredSize(new java.awt.Dimension(250, 22));
        trnslatorComboBox.setFocusable(false);

        // -- Addding action listener for 
        trnslatorComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                panel.setTranslatorName(trnslatorComboBox.getSelectedItem().toString());
            }
        });
        // -- Adding a content into the combobox ----------------------------------------
        for (AbstractCimTranslator t : TranslatorsRepository.getTranslators()) {
            trnslatorComboBox.addItem(t.getName());
        }
        // -- Adding a combobox into toolbar --------------------------------------------
        toolbar.add(trnslatorComboBox);
        // -- Adding buttons for transformation and settings ----------------------------
        toolbar.addButton("/Icons/Mono/fa-flag-checkered-16x16.png", "", "Transform");
        toolbar.addButton("/Icons/Mono/fa-cog-16x16.png", "", "Settings");
        // -- Adding botton for PNG export ----------------------------------------------
        toolbar.addSeparator();
        toolbar.addButton("/Icons/Mono/fa-camera-retro-16x16.png", "", "ToPNG");

    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of
     * this method is always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane = new javax.swing.JScrollPane();

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jScrollPane, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane;
    // End of variables declaration//GEN-END:variables

    @Override
    public JComponent getVisualRepresentation() {
        return this;
    }

    @Override
    public JComponent getToolbarRepresentation() {
        return toolbar;
    }

    @Override
    public Action[] getActions() {
        return new Action[0];
    }

    @Override
    public Lookup getLookup() {
        return componetLookup;
    }

    @Override
    public void componentOpened() {
    }

    @Override
    public void componentClosed() {
    }

    @Override
    public void componentShowing() {
    }

    @Override
    public void componentHidden() {
    }

    @Override
    public void componentActivated() {
        cutAction.setEnabled(false);
        copyAction.setEnabled(false);
        pasteAction.setEnabled(false);
        // -- Updateing Cut/Copy/Paste acctions for this panel -------------------
        ActionMap actionMap = getActionMap();
        actionMap.put(DefaultEditorKit.cutAction, cutAction);
        actionMap.put(DefaultEditorKit.copyAction, copyAction);
        actionMap.put(DefaultEditorKit.pasteAction, pasteAction);
        // -- Start observing ----------------------------------------------------
        cutAction.startObserving();
        copyAction.startObserving();
        pasteAction.startObserving();
        // -- Adjusting size of the panel ----------------------------------------
        if (isFirstRun) {
            Graphics g = getGraphics();
            panel.paint(g);
            panel.adjustSize();
        }
    }

    @Override
    public void componentDeactivated() {
        cutAction.setEnabled(false);
        copyAction.setEnabled(false);
        pasteAction.setEnabled(false);
        // -----------------------------------------------------------------------
        cutAction.finishObserving();
        copyAction.finishObserving();
        pasteAction.finishObserving();
        // -- Returning an origianl actions --------------------------------------
        ActionMap actionMap = getActionMap();
        actionMap.put(DefaultEditorKit.cutAction, SystemAction.get(org.openide.actions.CutAction.class));
        actionMap.put(DefaultEditorKit.copyAction, SystemAction.get(org.openide.actions.CopyAction.class));
        actionMap.put(DefaultEditorKit.pasteAction, SystemAction.get(org.openide.actions.PasteAction.class));
    }

    @Override
    public UndoRedo getUndoRedo() {
        return UndoRedo.NONE;
    }

    @Override
    public void setMultiViewCallback(MultiViewElementCallback callback) {
        this.callback = callback;
    }

    @Override
    public CloseOperationState canCloseElement() {
        return CloseOperationState.STATE_OK;
    }

    @Override
    public void setScale(int scale) {
        panel.setScale(scale / 100.0);
        panel.repaint();
    }

    @Override
    public PrintPage[][] getPages(final int width, final int height, double d) {
        int printMaxHor = panel.getMaxWidth() / width + (panel.getMaxWidth() % width != 0 ? 1 : 0);
        int printMaxVert = panel.getMaxHeight() / height + (panel.getMaxHeight() % height != 0 ? 1 : 0);
        PrintPage[][] res = new PrintPage[printMaxHor][printMaxVert];
        for (int pv = 0; pv < printMaxVert; pv++) {
            for (int ph = 0; ph < printMaxHor; ph++) {
                final int pageHor = ph;
                final int pageVer = pv;
                PrintPage pp = new PrintPage() {
                    @Override
                    public void print(Graphics graphics) {
                        if (!(graphics instanceof Graphics2D)) {
                            return;
                        }
                        Graphics2D g2d = (Graphics2D) graphics;

                        AffineTransform tx = g2d.getTransform();

                        g2d.translate(-pageHor * width, -pageVer * height);

                        RepaintManager currentManager = RepaintManager.currentManager(panel);
                        currentManager.setDoubleBufferingEnabled(false);
                        g2d.setColor(Color.BLACK);
                        panel.paint(g2d);
                        currentManager.setDoubleBufferingEnabled(true);

                        g2d.setTransform(tx);
                    }
                };
                res[pv][ph] = pp;
            }
        }
        return res;
    }

    @Override
    public Date lastModified() {
        return obj.getPrimaryFile().lastModified();
    }

    @Override
    public String getName() {
        return obj.getPrimaryEntry().getFile().getName();
    }

    @Override
    public void enableAction(String name, boolean isEnabled) {
        for(Component c : toolbar.getComponents()) {
            if(c instanceof JButton && ((AbstractButton)c).getActionCommand().equals(name)) {
                c.setEnabled(isEnabled);
            }
        }
    }
}
