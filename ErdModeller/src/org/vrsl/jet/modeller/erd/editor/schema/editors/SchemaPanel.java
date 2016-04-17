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
package org.vrsl.jet.modeller.erd.editor.schema.editors;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.openide.cookies.EditorCookie;
import org.openide.loaders.MultiDataObject;
import org.openide.util.Exceptions;
import org.vrsl.jet.modeller.erd.ActionsController;
import org.vrsl.jet.modeller.erd.editor.schema.dialogs.AssociationDialog;
import org.vrsl.jet.modeller.erd.editor.schema.dialogs.SchemaTextDialog;
import org.vrsl.jet.modeller.erd.editor.schema.view.SchamaClass;
import org.vrsl.jet.modeller.erd.editor.schema.view.SchemaRectangleElement;
import org.vrsl.jet.modeller.erd.editor.schema.view.associations.CommentAssociation;
import org.vrsl.jet.modeller.erd.editor.schema.view.associations.ElementsAssociation;
import org.vrsl.jet.modeller.erd.editor.schema.view.elements.Comment;
import org.vrsl.jet.modeller.erd.editor.schema.view.elements.Entity;
import org.vrsl.jet.modeller.erd.editor.schema.view.elements.FreeText;
import org.vrsl.jet.modeller.erd.editor.schema.view.transferable.XmlSchemaRepresentation;
import org.vrsl.jet.modeller.erd.editor.utils.CustomFileFilter;
import org.vrsl.jet.models.cim.NamedElement;
import org.vrsl.jet.models.cim.NamedElementNotFoundException;
import org.vrsl.jet.models.cim.Qualifier;
import org.vrsl.jet.models.cim.Schema;
import org.vrsl.jet.models.cim.persistence.ModelPersistenceException;
import org.vrsl.jet.models.cim.persistence.ModelReader;
import org.vrsl.jet.models.cim.persistence.ModelReaderFactory;
import org.vrsl.jet.models.cim.persistence.ModelWriter;
import org.vrsl.jet.models.cim.persistence.ModelWriterFactory;
import org.vrsl.jet.models.erd.ErdModelFactory;
import org.vrsl.jet.models.erd.variants.ErdVariantsFactory;
import org.vrsl.jet.models.erd.variants.associations.ErdModelEntityAssociation;
import org.vrsl.jet.models.erd.variants.associations.ErdModelReferenceMultiplicity;
import org.vrsl.jet.models.erd.variants.associations.ErdModelTextCommentAssociation;
import org.vrsl.jet.models.erd.variants.entities.ErdModelEntity;
import org.vrsl.jet.models.erd.variants.entities.ErdModelText;
import org.vrsl.jet.models.erd.variants.entities.ErdModelTextualComment;
import org.vrsl.jet.models.erd.variants.transformations.SchemaTransformerPreferences;
import org.vrsl.jet.translators.AbstractCimTranslator;
import org.vrsl.jet.translators.CimTranslatorError;
import org.vrsl.jet.translators.TranslatorsRepository;
import org.vrsl.jet.translators.ValidationResult;
import org.vrsl.jet.translators.properties.TranslatorPropertyMetadata;
import org.vrsl.jet.ui.dialogs.PropertiesDialog;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class SchemaPanel extends JPanel implements MouseListener, ActionListener, MouseMotionListener {

    private static final Logger logger = Logger.getLogger(SchemaPanel.class.getName());
    // -------------------------------------------------------------------------------
    static final long serialVersionUID = 240000001;
    // -------------------------------------------------------------------------------
    static final String TRANSLATOR_PROPERTIES_ERROR = "Translator's properties validation";
    // -------------------------------------------------------------------------------
    static final float[] smDash1 = {5.0f};
    static final BasicStroke smDouble = new BasicStroke(1.0f,
            BasicStroke.CAP_BUTT,
            BasicStroke.JOIN_MITER,
            10.0f, smDash1, 0.0f);
    static final Color selectionColor = new Color(63, 63, 63);
    // -------------------------------------------------------------------------------
    private final int cnt = 0;
    // -------------------------------------------------------------------------------
    private List<Entity> data = null;
    private List<ElementsAssociation> associations = null;
    private List<SchemaRectangleElement> texts = null;
    private List<CommentAssociation> commentAssociations = null;
    // -------------------------------------------------------------------------------
    private String curFileName = null;
    // -------------------------------------------------------------------------------
    private EditorMode mode = EditorMode.DEFAULT;
    private AssociationMode associationStage = AssociationMode.ASSOCIATION_IS_CLEAR;
    // -------------------------------------------------------------------------------
    private Entity outgoingAssociation;
    private Entity ingoingAssociation;
    private SchemaRectangleElement commentOutgoingAssociation;
    private SchemaRectangleElement commentIngoingAssociation;
    // -------------------------------------------------------------------------------
    private Cursor normalCursor = null;
    private Cursor moveCursor = null;
    private Cursor deleteCursor = null;
    private Cursor addCursor = null;
    private Cursor addTextCursor = null;
    private Cursor associationCursor = null;
    private Cursor commentCursor = null;
    // -------------------------------------------------------------------------------
    private Font sysFont = null;
    // -------------------------------------------------------------------------------
    private double scale = 1;
    // -------------------------------------------------------------------------------
    private MultiDataObject dataObject = null;
    private javax.swing.text.Document document = null;
    private volatile boolean blockRebuild = false;
    // -------------------------------------------------------------------------------
    private JFrame appFrame = null;
    /////////////////////////////////////////////
    // Here is data's block for objects draggin
    int stPosX = 0;
    int stPosY = 0;
    SchemaRectangleElement movedObject = null;
    ElementsAssociation movedAssociation = null;
    CommentAssociation comMovedAssociation = null;
    // -------------------------------------------------------------------------------
    private boolean dirty = false;
    // -------------------------------------------------------------------------------
    private boolean blocked = false;
    ///////////////////////////////////////////
    // Dragging mode
    protected boolean regionSelection = false;
    protected boolean regionSelected = false;
    protected Point stSelectionPoint = null;
    protected Point endSelectionPoint = null;
    // -------------------------------------------------------------------------------
    private String translatorName = "UNKNOWN";
    private boolean setDirtyAfterMouseDrag = false;
    // -------------------------------------------------------------------------------
    private final ExecutorService longRuningTasks = Executors.newFixedThreadPool(8);

    /**
     * Creates a new instance of ShemaPanel
     */
    public SchemaPanel(Component parent, MultiDataObject obj) {
        this.dataObject = obj;
        if (parent != null) {
            appFrame = (JFrame) SwingUtilities.getWindowAncestor(parent);
        }
        setBackground(Color.white);
        loadCursors();
        setCursorMode();

        addMouseRelatedListeners();

        data = new ArrayList<>();
        associations = new ArrayList<>();
        commentAssociations = new ArrayList<>();
        texts = new ArrayList<>();

        sysFont = new Font("Areal", Font.PLAIN, 12);

        initEditorDocument(obj);
        initDocumentView();
    }

    private void initDocumentView() {
        build();
    }

    private void initEditorDocument(MultiDataObject obj) {
        EditorCookie ec = obj.getLookup().lookup(EditorCookie.class);
        if (ec == null) {
            throw new IllegalStateException("Can't find EditorCookie");
        }
        if (ec.getDocument() == null) {
            try {
                ec.openDocument();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
                return;
            }
        }
        document = ec.getDocument();

        if (document != null) {
            document.addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    build();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    build();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    build();
                }
            });
        }
    }

    private void addMouseRelatedListeners() {
        addMouseListener(this);
        addMouseMotionListener(this);
    }

    public void build() {
        if (blockRebuild) {
            return;
        }
        Schema s;
        try {
            String content = document.getText(0, document.getLength());
            try (InputStream sysIn = new ByteArrayInputStream(content.getBytes())) {
                ModelReader r = ModelReaderFactory.newInstance(new ErdVariantsFactory());
                s = r.read(sysIn);
                buildViewRepresentation(s);
                blocked = false;
            }
        } catch (ModelPersistenceException | NamedElementNotFoundException | IOException | IllegalStateException | NumberFormatException | BadLocationException ex) {
            blocked = true;
        } finally {
            try {
                repaint();
            } catch (Throwable e) {
            }
        }
    }

    public boolean isDirty() {
        return dirty;
    }

    public boolean isRegionSelected() {
        return regionSelected;
    }

    public void setScale(double scale) {
        this.scale = scale;
        for (Entity d : data) {
            d.setScale(scale);
        }
        for (ElementsAssociation a : associations) {
            a.setScale(scale);
        }
        for (SchemaRectangleElement a : texts) {
            a.setScale(scale);
        }
    }

    public void setPointerMode() {
        mode = EditorMode.DEFAULT;
        associationStage = AssociationMode.ASSOCIATION_IS_CLEAR;
        setCursorMode();
        if (outgoingAssociation != null) {
            outgoingAssociation.setAssociationSelected(false);
            outgoingAssociation.setDefaultView();
            outgoingAssociation = null;
        }
        if (ingoingAssociation != null) {
            ingoingAssociation.setAssociationSelected(false);
            ingoingAssociation.setDefaultView();
            ingoingAssociation = null;
        }
        repaint();
    }

    public void setDeleteMode() {
        if (!regionSelected) {
            mode = EditorMode.DELETE_OBJECTS;
            setCursorMode();
        } else {
            deleteGroupSelected();
        }
    }

    public void setAddObjectMode() {
        mode = EditorMode.ADD_NEW_OBJECT;
        setCursorMode();
    }

    public void setAddAssociationMode() {
        mode = EditorMode.ADD_NEW_ASSOCIATION;
        setCursorMode();
    }

    public void setAddTextMode() {
        mode = EditorMode.ADD_NEW_TEXT;
        setCursorMode();
    }

    public void setAddCommentMode() {
        mode = EditorMode.ADD_NEW_COMMENT;
        setCursorMode();
    }

    public void setAddCommentAssociationMode() {
        mode = EditorMode.ADD_NEW_COMMENT_ASSOCIATION;
        setCursorMode();
    }

    public String getFileName() {
        return curFileName;
    }

    public void setFileName(String fName) {
        curFileName = fName;
    }

    private void loadCursors() {
        Image cursorImage = Toolkit.getDefaultToolkit().getImage(SchemaPanel.class.getResource("/Icons/Cursors/DeleteCursor.gif"));
        deleteCursor = Toolkit.getDefaultToolkit().createCustomCursor(cursorImage, new Point(0, 0), "delete");

        cursorImage = Toolkit.getDefaultToolkit().getImage(SchemaPanel.class.getResource("/Icons/Cursors/TextCursor.gif"));
        addTextCursor = Toolkit.getDefaultToolkit().createCustomCursor(cursorImage, new Point(5, 5), "delete");
        commentCursor = loadCursor("agr_uni", "/Icons/Cursors/CommentAddCursor.gif", 6, 1);

        moveCursor = new Cursor(Cursor.MOVE_CURSOR);
        normalCursor = new Cursor(Cursor.DEFAULT_CURSOR);
        addCursor = new Cursor(Cursor.CROSSHAIR_CURSOR);
        associationCursor = new Cursor(Cursor.HAND_CURSOR);
    }

    private Cursor loadCursor(String name, String file, int x, int y) {
        Image cursorImage = Toolkit.getDefaultToolkit().getImage(SchemaPanel.class.getResource(file));
        return Toolkit.getDefaultToolkit().createCustomCursor(cursorImage, new Point(x, y), name);
    }

    private void setCursorMode() {
        switch (mode) {
            case DEFAULT:
                setCursor(normalCursor);
                break;
            case ADD_NEW_OBJECT:
                setCursor(addCursor);
                break;
            case ADD_NEW_ASSOCIATION:
            case ADD_NEW_COMMENT_ASSOCIATION:
                setCursor(associationCursor);
                break;
            case ADD_NEW_TEXT:
                setCursor(addTextCursor);
                break;
            case DELETE_OBJECTS:
                setCursor(deleteCursor);
                break;
            case ADD_NEW_COMMENT:
                setCursor(commentCursor);
                break;
            default:
                throw new UnsupportedOperationException("Cursor mode " + mode + " has not been supported.");
        }
    }

    @Override
    public void paint(Graphics g) {

        if (!(g instanceof Graphics2D)) {
            return;
        }

        int width = getMaxWidth();
        int height = getMaxHeight();

        Rectangle b = this.getBounds();
        g.clearRect(0, 0, b.width, b.height);

        Graphics2D g2d = (Graphics2D) g;
        g2d.scale(scale, scale);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        {
            int rectWidth = width > b.width ? width : b.width;
            int rectHeight = height > b.height ? height : b.height;
            Color pColor = g.getColor();
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, (int) (rectWidth / scale), (int) (rectHeight / scale));
            g.setColor(pColor);
        }

        g.setFont(sysFont);

        for (Entity jd : data) {
            jd.calculateBoxMetrics(g);
        }
        for (SchemaRectangleElement jd : texts) {
            jd.calculateBoxMetrics(g);
        }

        for (ElementsAssociation ld : associations) {
            ld.draw(g);
        }
        for (CommentAssociation ld : commentAssociations) {
            ld.draw(g);
        }

        ////////////////////////////////////////////////////////
        // And the last step is drawing objects
        for (Entity jd : data) {
            jd.draw(g);
        }

        ////////////////////////////////////////////////////////
        // After all we draw texts
        for (SchemaRectangleElement jd : texts) {
            jd.draw(g);
        }

        if (regionSelection) {
            drawGroupSelection(g2d);
        }
        if (blocked) {
            Color pColor = g2d.getColor();
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
            g2d.fillRect(0, 0, (int) (width / scale), (int) (height / scale));
            g2d.setColor(pColor);
        }
    }

    private void drawGroupSelection(Graphics2D g2d) {
        Point a = stSelectionPoint;
        Point b = endSelectionPoint;

        if (a != null && b != null && g2d != null) {

            Color cc = g2d.getColor();
            g2d.setColor(selectionColor);
            Stroke curStroke = g2d.getStroke();
            g2d.setStroke(smDouble);

            g2d.drawLine(a.x, a.y, b.x, a.y);
            g2d.drawLine(b.x, a.y, b.x, b.y);
            g2d.drawLine(b.x, b.y, a.x, b.y);
            g2d.drawLine(a.x, b.y, a.x, a.y);

            g2d.setColor(cc);
            g2d.setStroke(curStroke);
        }
    }

    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    private void addText(Point p) {
        FreeText st = new FreeText((int) (p.getX() / scale), (int) (p.getY() / scale));
        SchemaTextDialog dl = new SchemaTextDialog(appFrame, true);
        dl.setData(st);
        dl.setVisible(true);

        if (dl.isUpdated()) {
            texts.add(st);
            setDirty(true);
        }

        mode = EditorMode.DEFAULT;
        setCursorMode();
        repaint();
    }

    private void addComment(Point p) {
        Comment st = new Comment((int) (p.getX() / scale), (int) (p.getY() / scale));
        SchemaTextDialog dl = new SchemaTextDialog(appFrame, true);
        dl.setData(st);
        dl.setVisible(true);

        if (dl.isUpdated()) {
            texts.add(st);
            setDirty(true);
        }

        mode = EditorMode.DEFAULT;
        setCursorMode();
        repaint();
    }

    public void addEntityObject(Point p) {
        Entity obj = new Entity((int) (p.getX() / scale), (int) (p.getY() / scale), data);
        obj.setScale(scale);
        data.add(obj);
        setDirty(true);
        mode = EditorMode.DEFAULT;
        setCursorMode();
        repaint();
    }

    public void deleteEntityObject(MouseEvent ev) {
        boolean removalHasDetected = false;
        Entity ro = null;
        for (Entity jd : data) {
            if (jd.isAssociationAreaRelated(ev)) {

                if (jd.isReadyToBeDeleted()) {
                    Collection<ElementsAssociation> removalCandidates = new LinkedList<>();
                    for (ElementsAssociation lnk : associations) {
                        if (lnk.getFromObject() == jd || lnk.getToObject() == jd) {
                            removalCandidates.add(lnk);
                        }
                    }
                    associations.removeAll(removalCandidates);

                    removalCandidates.clear();
                    for (ElementsAssociation lnk : commentAssociations) {
                        if (lnk.getFromObject() == jd || lnk.getToObject() == jd) {
                            removalCandidates.add(lnk);
                        }
                    }
                    commentAssociations.removeAll(removalCandidates);

                    ro = jd;
                } else if (jd.hasReadyToBeDeletedField()) {
                    if (!jd.deleteSelectedField()) {
                        return; // Filed try again
                    }
                }
                break;
            }
        }
        if (ro != null) {
            data.remove(ro);
            removalHasDetected = true;
        } else {
            // ----------------------------------------------------------------------------------------------
            ElementsAssociation rcAssociation = null;
            CommentAssociation rcCommentAssociation = null;
            SchemaRectangleElement rcText = null;
            // ----------------------------------------------------------------------------------------------
            for (ElementsAssociation lk : associations) {
                if (lk.processMouseDelete(ev)) {
                    rcAssociation = lk;
                    removalHasDetected = true;
                    break;
                }
            }
            for (CommentAssociation lk : commentAssociations) {
                if (lk.processMouseDelete(ev)) {
                    rcCommentAssociation = lk;
                    removalHasDetected = true;
                    break;
                }
            }
            for (SchemaRectangleElement tx : texts) {
                if (tx.isOwnedAreaRelated(ev)) {

                    Collection<ElementsAssociation> removalCandidates = new LinkedList<>();
                    for (ElementsAssociation lnk : commentAssociations) {
                        if (lnk.getFromObject() == tx || lnk.getToObject() == tx) {
                            removalCandidates.add(lnk);
                        }
                    }
                    commentAssociations.removeAll(removalCandidates);
                    rcText = tx;
                    removalHasDetected = true;
                    break;
                }
            }
            // -- Doing actual deletion --------------------------------------------------------
            if (rcAssociation != null) {
                associations.remove(rcAssociation);
            }
            if (rcCommentAssociation != null) {
                commentAssociations.remove(rcCommentAssociation);
            }
            if (rcText != null) {
                texts.remove(rcText);
            }
        }
        if (removalHasDetected) {
            setDirty(true);
            mode = EditorMode.DEFAULT;
            setCursorMode();
            repaint();
        }
    }

    private void addAssociation(MouseEvent ev) {
        for (Entity jd : data) {
            if (jd.isAssociationAreaRelated(ev)) {
                if (jd.setAssociationSelected(true)) {
                    switch (associationStage) {
                        case ASSOCIATION_IS_CLEAR:
                            outgoingAssociation = jd;
                            associationStage = AssociationMode.ASSOCIATION_FIRST_OBJECT_IS_SELECTED;
                            break;
                        case ASSOCIATION_FIRST_OBJECT_IS_SELECTED:
                            ingoingAssociation = jd;
                            associationStage = AssociationMode.ASSOCIATION_SECOND_OBJECT_IS_SELECTED;
                            break;
                    }
                    repaint();
                }
                break;
            }
        }

        if (associationStage == AssociationMode.ASSOCIATION_SECOND_OBJECT_IS_SELECTED) {

            boolean isCheckOk = true;
            String associationAlert = null;
            int paralelNumber = 0;
            // -- Counting paralel associations number --------------------------
            for (ElementsAssociation lnk : associations) {
                if ((lnk.getFromObject() == outgoingAssociation && lnk.getToObject() == ingoingAssociation)
                        || (lnk.getFromObject() == ingoingAssociation && lnk.getToObject() == outgoingAssociation)) {
                    paralelNumber++;
                }
            }
            ////////////////////////////////////////////////////////////
            // Here we check constraints for associations
            if (outgoingAssociation.getAllFields().isEmpty() || ingoingAssociation.getAllFields().isEmpty()) {
                associationAlert = "You have to determinate minimum one field to create an association";
                isCheckOk = false;
            }
            if (!isCheckOk) {
                outgoingAssociation.setAssociationSelected(false);
                outgoingAssociation.setDefaultView();
                ingoingAssociation.setAssociationSelected(false);
                ingoingAssociation.setDefaultView();
                outgoingAssociation = null;
                ingoingAssociation = null;
                associationStage = AssociationMode.ASSOCIATION_IS_CLEAR;
                mode = EditorMode.DEFAULT;
                repaint();
                setCursorMode();

                JOptionPane.showMessageDialog(appFrame, associationAlert, "Association alret", JOptionPane.ERROR_MESSAGE);
                return;
            }

            ElementsAssociation association = new ElementsAssociation();
            association.setFrom(outgoingAssociation);
            association.setTo(ingoingAssociation);

            AssociationDialog dl = new AssociationDialog(appFrame, true);
            dl.setAssociation(association);
            dl.setVisible(true);
            if (dl.isUpdated()) {
                association.setScale(scale);
                association.setParalelNumber(paralelNumber);
                associations.add(association);
                setDirty(true);
            }

            outgoingAssociation.setAssociationSelected(false);
            outgoingAssociation.setDefaultView();
            outgoingAssociation = null;
            ingoingAssociation.setAssociationSelected(false);
            ingoingAssociation.setDefaultView();
            ingoingAssociation = null;
            associationStage = AssociationMode.ASSOCIATION_IS_CLEAR;
            mode = EditorMode.DEFAULT;
            repaint();
            setCursorMode();
        }
    }

    private void addCommentAssociation(MouseEvent ev) {
        for (SchamaClass jd : data) {
            if (jd.isAssociationAreaRelated(ev)) {
                if (jd.setAssociationSelected(true)) {
                    switch (associationStage) {
                        case ASSOCIATION_IS_CLEAR:
                            commentOutgoingAssociation = jd;
                            associationStage = AssociationMode.ASSOCIATION_FIRST_OBJECT_IS_SELECTED;
                            break;
                        case ASSOCIATION_FIRST_OBJECT_IS_SELECTED:
                            commentIngoingAssociation = jd;
                            associationStage = AssociationMode.ASSOCIATION_SECOND_OBJECT_IS_SELECTED;
                            break;
                    }
                    repaint();
                }
                break;
            }
        }
        for (SchemaRectangleElement jd : texts) {
            if (jd.isAssociationAreaRelated(ev)) {
                if (jd.setAssociationSelected(true)) {
                    switch (associationStage) {
                        case ASSOCIATION_IS_CLEAR:
                            commentOutgoingAssociation = jd;
                            associationStage = AssociationMode.ASSOCIATION_FIRST_OBJECT_IS_SELECTED;
                            break;
                        case ASSOCIATION_FIRST_OBJECT_IS_SELECTED:
                            commentIngoingAssociation = jd;
                            associationStage = AssociationMode.ASSOCIATION_SECOND_OBJECT_IS_SELECTED;
                            break;
                    }
                    repaint();
                }
                break;
            }
        }

        if (associationStage == AssociationMode.ASSOCIATION_SECOND_OBJECT_IS_SELECTED) {

            boolean isCheckOk = true;
            String associationAlert = null;

            // -- Here we check constraints for associations ---------------------------
            if (commentOutgoingAssociation == commentIngoingAssociation) {
                associationAlert = "You can't use recursed association";
                isCheckOk = false;
            }
            if (isCheckOk) {
                for (CommentAssociation lnk : commentAssociations) {
                    if ((lnk.getFromObject() == commentOutgoingAssociation && lnk.getToObject() == commentIngoingAssociation)
                            || (lnk.getFromObject() == commentIngoingAssociation && lnk.getToObject() == commentOutgoingAssociation)) {
                        associationAlert = "You can't define association twice";
                        isCheckOk = false;
                    }
                }
            }
            if (!isCheckOk) {
                commentOutgoingAssociation.setAssociationSelected(false);
                commentOutgoingAssociation.setDefaultView();
                commentIngoingAssociation.setAssociationSelected(false);
                commentIngoingAssociation.setDefaultView();
                commentOutgoingAssociation = null;
                commentIngoingAssociation = null;
                associationStage = AssociationMode.ASSOCIATION_IS_CLEAR;
                mode = EditorMode.DEFAULT;
                repaint();
                setCursorMode();

                JOptionPane.showMessageDialog(appFrame, associationAlert, "Associatin alret", JOptionPane.ERROR_MESSAGE);
                return;
            }

            CommentAssociation commentAssociation = new CommentAssociation();
            commentAssociation.setFrom(commentOutgoingAssociation, "", ErdModelReferenceMultiplicity.Category.NONE_OR_ONE);
            commentAssociation.setTo(commentIngoingAssociation, "", ErdModelReferenceMultiplicity.Category.NONE_OR_ONE);
            commentAssociation.setScale(scale);
            commentAssociations.add(commentAssociation);
            setDirty(true);

            commentOutgoingAssociation.setAssociationSelected(false);
            commentOutgoingAssociation.setDefaultView();
            commentOutgoingAssociation = null;
            commentIngoingAssociation.setAssociationSelected(false);
            commentIngoingAssociation.setDefaultView();
            commentIngoingAssociation = null;
            associationStage = AssociationMode.ASSOCIATION_IS_CLEAR;
            mode = EditorMode.DEFAULT;
            repaint();
            setCursorMode();
        }
    }
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    @Override
    public void mouseClicked(MouseEvent ev) {
        if (mode == EditorMode.ADD_NEW_TEXT) {
            addText(ev.getPoint());
        } else if (mode == EditorMode.ADD_NEW_COMMENT) {
            addComment(ev.getPoint());
        } else if (mode == EditorMode.ADD_NEW_OBJECT) {
            addEntityObject(ev.getPoint());
        } else if (mode == EditorMode.DELETE_OBJECTS) {
            deleteEntityObject(ev);
        } else if (mode == EditorMode.ADD_NEW_ASSOCIATION) {
            addAssociation(ev);
        } else if (mode == EditorMode.ADD_NEW_COMMENT_ASSOCIATION) {
            addCommentAssociation(ev);
        } else {
            for (Entity jd : data) {
                if (jd.processMouseEvent(appFrame, ev)) {
                    if (jd.isDirty()) {
                        setDirty(true);
                    }
                    jd.setDefaultView();
                    repaint();
                    return;
                }
            }
            for (ElementsAssociation lk : associations) {
                if (lk.processMouseEvent(appFrame, ev)) {
                    if (lk.isDirty()) {
                        setDirty(true);
                    }
                    lk.setDefaultView();
                    repaint();
                    return;
                }
            }
            for (SchemaRectangleElement lk : texts) {
                if (lk.processMouseEvent(appFrame, ev)) {
                    if (lk.isDirty()) {
                        setDirty(true);
                    }
                    lk.setDefaultView();
                    repaint();
                    return;
                }
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent ev) {
        stPosX = (int) (ev.getX() / scale);
        stPosY = (int) (ev.getY() / scale);
        movedObject = null;
        movedAssociation = null;
        comMovedAssociation = null;

        boolean isBlank = true;

        for (Entity dt : data) {
            if (dt.isMovingAreaRelated(ev)) {
                movedObject = dt;
                dt.setMoved();
                isBlank = false;
                break;
            }
        }
        for (SchemaRectangleElement dt : texts) {
            if (dt.isMovingAreaRelated(ev)) {
                movedObject = dt;
                dt.setMoved();
                isBlank = false;
                break;
            }
        }
        for (ElementsAssociation lt : associations) {
            if (lt.processMouseStartDragging(ev.getPoint())) {
                movedAssociation = lt;
                isBlank = false;
                break;
            }
        }
        for (CommentAssociation lt : commentAssociations) {
            if (lt.processMouseStartDragging(ev.getPoint())) {
                comMovedAssociation = lt;
                isBlank = false;
                break;
            }
        }

        if (isBlank) {
            beginGroupSelection(ev);
        }
    }

    @Override
    public void mouseReleased(MouseEvent ev) {
        stPosX = 0;
        stPosY = 0;
        if (movedObject != null) {
            movedObject.setDefaultView();
            movedObject = null;
            repaint();
        }
        if (movedAssociation != null) {
            movedAssociation.setDefaultView();
            movedAssociation = null;
            repaint();
        }
        if (comMovedAssociation != null) {
            comMovedAssociation.setDefaultView();
            comMovedAssociation = null;
            repaint();
        }
        if (regionSelection) {
            finishGroupSelection(ev);
        }
        if (setDirtyAfterMouseDrag) {
            setDirty(true);
            setDirtyAfterMouseDrag = false;
        }
    }

    @Override
    public void mouseEntered(MouseEvent ev) {
    }

    @Override
    public void mouseExited(MouseEvent ev) {
    }

    @Override
    public void mouseDragged(MouseEvent ev) {
        int moveX = (int) (ev.getX() / scale) - stPosX;
        int moveY = (int) (ev.getY() / scale) - stPosY;
        if (movedObject != null) {
            if (!regionSelected) {
                movedObject.shift(moveX, moveY);
                repaint();
                //setDirty(true);
                setDirtyAfterMouseDrag = true;
            } else {
                moveGroupSelection(ev);
            }
        }
        if (movedAssociation != null) {
            movedAssociation.processMouseDragged(new Point((int) (ev.getX() / scale), (int) (ev.getY() / scale)));
            repaint();
            //setDirty(true);
            setDirtyAfterMouseDrag = true;
        }
        if (comMovedAssociation != null) {
            comMovedAssociation.processMouseDragged(new Point((int) (ev.getX() / scale), (int) (ev.getY() / scale)));
            repaint();
            //setDirty(true);
            setDirtyAfterMouseDrag = true;
        }
        if (regionSelection) {
            endSelectionPoint = ev.getPoint();
            endSelectionPoint.x /= scale;
            endSelectionPoint.y /= scale;
            this.repaint();
        }
        stPosX = (int) (ev.getX() / scale);
        stPosY = (int) (ev.getY() / scale);
    }

    @Override
    public void mouseMoved(MouseEvent ev) {
        boolean isChanged = false;
        setCursorMode();
        boolean hasToolTip = false;
        for (Entity dt : data) {
            if (mode != EditorMode.DELETE_OBJECTS) {
                // -- Objects movement logic --------------------------------------------------------
                if (dt.isMovingAreaRelated(ev)) {
                    dt.setActiveTitle();
                    setCursor(moveCursor);
                } else {
                    // -- Standart reactions block for selecting associations objects and fields ----
                    if (dt.isTitleAreaRelated(ev)) {
                        if (scale < 1) {
                            setToolTipText(dt.getToolTipText());
                            hasToolTip = true;
                        }
                        dt.setActiveTitle();
                    } else if (dt.isFieldAreaRelated(ev)) {
                        if (scale < 1) {
                            setToolTipText(dt.getToolTipText());
                            hasToolTip = true;
                        }
                        dt.setActiveFeild();
                    } else {
                        dt.setDefaultView();
                    }
                }
            } else {
                ////////////////////////////////////////////////////////////////
                // The same reacion for deleting objects
                if (dt.isTitleAreaRelated(ev)) {
                    dt.setReadyToBeDeleted();
                } else if (dt.isFieldAreaRelated(ev)) {
                    dt.setFieldToBeDeleted();
                } else {
                    dt.setDefaultView();
                }
            }

            if (dt.isChanged()) {
                repaint(dt.getRectangle());
            }
        }

        if (!hasToolTip) {
            setToolTipText(null);
        }

        if (associations != null && !regionSelected) {
            for (ElementsAssociation lk : associations) {
                if (lk.processMouseDelete(ev)) {
                    if (mode == EditorMode.DELETE_OBJECTS) {
                        lk.setReadyToBeDeleted();
                    } else {
                        lk.setSelected();
                    }
                } else {
                    lk.setDefaultView();
                }
                if (lk.isChanged()) {
                    isChanged = true;
                }
            }
        }
        if (commentAssociations != null && !regionSelected) {
            for (ElementsAssociation lk : commentAssociations) {
                if (lk.processMouseDelete(ev)) {
                    if (mode == EditorMode.DELETE_OBJECTS) {
                        lk.setReadyToBeDeleted();
                    } else {
                        lk.setSelected();
                    }
                } else {
                    lk.setDefaultView();
                }
                if (lk.isChanged()) {
                    isChanged = true;
                }
            }
        }
        if (texts != null) {
            for (SchemaRectangleElement lk : texts) {
                if (mode != EditorMode.DELETE_OBJECTS && lk.isMovingAreaRelated(ev)) {
                    lk.setPointed();
                    setCursor(moveCursor);
                } else {
                    if (lk.isOwnedAreaRelated(ev)) {
                        if (mode == EditorMode.DELETE_OBJECTS) {
                            lk.setReadyToBeDeleted();
                        } else {
                            lk.setPointed();
                        }
                    } else {
                        lk.setDefaultView();
                    }
                }
                if (lk.isChanged()) {
                    repaint(lk.getRectangle());
                }
            }
        }
        // -- Repaint whole image if we can't repaint a region -----------------
        if (isChanged) {
            repaint();
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // The block of code for groups selections
    /**
     * Starts group of objects selection.
     */
    protected void beginGroupSelection(MouseEvent ev) {
        for (Entity item : data) {
            item.setSelected(false);
        }
        for (ElementsAssociation item : associations) {
            item.setDefaultView();
        }
        for (SchemaRectangleElement item : texts) {
            item.setSelected(false);
        }
        for (ElementsAssociation item : commentAssociations) {
            item.setDefaultView();
        }
        regionSelection = true;
        regionSelected = false;
        stSelectionPoint = ev.getPoint();

        stSelectionPoint.x /= scale;
        stSelectionPoint.y /= scale;
    }

    /**
     * Ends the group of objects selection process.
     */
    protected void finishGroupSelection(MouseEvent ev) {
        regionSelection = false;
        endSelectionPoint = ev.getPoint();

        endSelectionPoint.x /= scale;
        endSelectionPoint.y /= scale;

        if (!stSelectionPoint.equals(endSelectionPoint)) {
            regionSelected = true;
        }

        int x1 = stSelectionPoint.x;
        int y1 = stSelectionPoint.y;
        int x2 = endSelectionPoint.x;
        int y2 = endSelectionPoint.y;

        if (x1 > x2) {
            x1 = endSelectionPoint.x;
            x2 = stSelectionPoint.x;
        }

        if (y1 > y2) {
            y1 = endSelectionPoint.y;
            y2 = stSelectionPoint.y;
        }

        boolean itemSelected = false;

        for (Entity item : data) {
            if (x1 <= item.getX() && (item.getX() + item.getXSize()) <= x2
                    && y1 <= item.getY() && (item.getY() + item.getYSize()) <= y2) {
                item.setSelected(true);
                itemSelected = true;
            }
        }

        for (ElementsAssociation association : associations) {
            if (association.getFromObject().isSelected() && association.getToObject().isSelected()) {
                association.setSelected();
            }
        }

        for (SchemaRectangleElement t : texts) {
            if (t instanceof FreeText) {
                FreeText st = (FreeText) t;
                if (x1 <= st.getX() && (st.getX() + st.getXSize()) <= x2
                        && y1 <= st.getY() && (st.getY() + st.getYSize()) <= y2) {
                    st.setSelected(true);
                    itemSelected = true;
                }
            }
            if (t instanceof Comment) {
                Comment tc = (Comment) t;
                if (x1 <= tc.getX() && (tc.getX() + tc.getXSize()) <= x2
                        && y1 <= tc.getY() && (tc.getY() + tc.getYSize()) <= y2) {
                    tc.setSelected(true);
                    itemSelected = true;
                }
            }
        }

        for (ElementsAssociation commentAssociation : commentAssociations) {
            if (commentAssociation.getFromObject().isSelected() && commentAssociation.getToObject().isSelected()) {
                commentAssociation.setSelected();
            }
        }

        if (!itemSelected) {
            regionSelected = false;
        }

        repaint();
    }

    /**
     * Moves selected group of objects when the mouse is dragging.
     */
    protected void moveGroupSelection(MouseEvent ev) {
        int moveX = (int) (ev.getX() / scale) - stPosX;
        int moveY = (int) (ev.getY() / scale) - stPosY;

        for (Entity item : data) {
            if (item.isSelected()) {
                item.shift(moveX, moveY);
            }
        }
        for (ElementsAssociation ld : associations) {
            if (ld.isSelected()) {
                ld.shift(moveX, moveY);
            }
        }
        for (SchemaRectangleElement t : texts) {
            if (t instanceof FreeText) {
                FreeText st = (FreeText) t;
                if (st.isSelected()) {
                    st.shift(moveX, moveY);
                }
            }
            if (t instanceof Comment) {
                Comment ti = (Comment) t;
                if (ti.isSelected()) {
                    ti.shift(moveX, moveY);
                }
            }
        }
        for (ElementsAssociation ld : commentAssociations) {
            if (ld.isSelected()) {
                ld.shift(moveX, moveY);
            }
        }
        repaint();
        //setDirty(true);
        setDirtyAfterMouseDrag = true;
    }

    public void deleteGroupSelected() {
        List<SchemaRectangleElement> delete = new ArrayList<>();
        List<ElementsAssociation> associationsToDelete;
        for (Entity item : data) {
            if (!item.isSelected()) {
                continue;
            }

            associationsToDelete = new ArrayList<>();

            for (ElementsAssociation lnk : associations) {
                if (lnk.getFromObject() == item || lnk.getToObject() == item) {
                    associationsToDelete.add(lnk);
                }
            }
            associations.removeAll(associationsToDelete);
            associationsToDelete = new ArrayList<>();

            for (ElementsAssociation lnk : commentAssociations) {
                if (lnk.getFromObject() == item || lnk.getToObject() == item) {
                    associationsToDelete.add(lnk);
                }
            }
            commentAssociations.removeAll(associationsToDelete);
            delete.add(item);
        }
        data.removeAll(delete);

        delete = new ArrayList<>();

        for (SchemaRectangleElement item : texts) {
            if (item.isSelected()) {
                delete.add(item);
                associationsToDelete = new ArrayList<>();
                for (ElementsAssociation lnk : commentAssociations) {
                    if (lnk.getFromObject() == item || lnk.getToObject() == item) {
                        associationsToDelete.add(lnk);
                    }
                }
                commentAssociations.removeAll(associationsToDelete);
            }
            if (item instanceof FreeText) {
                FreeText st = (FreeText) item;
                if (st.isSelected()) {
                    delete.add(st);
                }
            }
        }

        texts.removeAll(delete);

        repaint();
        setDirty(true);
        regionSelection = false;
        regionSelected = false;
    }

    public XmlSchemaRepresentation getGroupSchemaRepresentaion() {
        List<Entity> objectsSelected = new ArrayList<>();
        List<ElementsAssociation> associationsSlected = new ArrayList<>();
        List<SchemaRectangleElement> textsSelected = new ArrayList<>();
        List<ElementsAssociation> comAssociationsSelected = new ArrayList<>();

        for (Entity item : data) {
            if (!item.isSelected()) {
                continue;
            }
            objectsSelected.add(item);
        }

        for (SchemaRectangleElement item : texts) {
            if (item.isSelected()) {
                textsSelected.add(item);
            }
        }

        for (ElementsAssociation lnk : associations) {
            if (lnk.isSelected()) {
                associationsSlected.add(lnk);
            }
        }
        for (ElementsAssociation jl : commentAssociations) {
            if (jl.isSelected()) {
                comAssociationsSelected.add(jl);
            }
        }

        return new XmlSchemaRepresentation(
                objectsSelected.toArray(new Entity[objectsSelected.size()]),
                associationsSlected.toArray(new ElementsAssociation[associationsSlected.size()]),
                textsSelected.<SchamaClass>toArray(new SchamaClass[textsSelected.size()]),
                comAssociationsSelected.<CommentAssociation>toArray(new CommentAssociation[comAssociationsSelected.size()]));
    }

    public void setGroupSechemaRepresentation(XmlSchemaRepresentation rep) {
        Entity[] objectsSelected = rep.getObjects();
        SchamaClass[] textsSelected = rep.getTexts();
        ElementsAssociation[] associationsSelected = rep.getAssociations(objectsSelected, textsSelected);
        CommentAssociation[] commentsAssociations = rep.getCommentAssociations(objectsSelected, textsSelected);

        /////////////////////////////////////////////////////////////////////////////////
        // Here we clearing the selection on the current schema to prepare it for
        // the data insertion from the transferable object
        for (SchamaClass item : data) {
            if (item.isSelected()) {
                item.setSelected(false);
            }
        }

        for (SchemaRectangleElement item : texts) {
            if (item.isSelected()) {
                item.setSelected(false);
            }
        }

        for (ElementsAssociation item : associations) {
            if (item.isSelected()) {
                item.setSelected(false);
            }
        }

        for (Entity objectStlected : objectsSelected) {
            objectStlected.setSelected(true);
            objectStlected.shift(10, 10);
            StringBuilder name = new StringBuilder(objectStlected.getName());
            while (!hasObjectNameUnique(name.toString())) {
                name.append("Copy");
            }
            objectStlected.setName(name.toString());
        }
        for (SchamaClass textSelected : textsSelected) {
            textSelected.setSelected(true);
            textSelected.shift(10, 10);
        }
        for (ElementsAssociation associationSelected : associationsSelected) {
            associationSelected.setSelected();
            associationSelected.shift(10, 10);
        }
        for (CommentAssociation ca : commentsAssociations) {
            ca.setSelected();
            ca.shift(10, 10);
        }

        data.addAll(Arrays.<Entity>asList(objectsSelected));
        texts.addAll(Arrays.<SchamaClass>asList(textsSelected));
        associations.addAll(Arrays.<ElementsAssociation>asList(associationsSelected));
        commentAssociations.addAll(Arrays.<CommentAssociation>asList(commentsAssociations));

        regionSelected = true;

        repaint();
        adjustSize();
        setDirty(true);
    }
    // The block of code for groups selections
    ////////////////////////////////////////////////////////////////////////////

    private boolean hasObjectNameUnique(String name) {
        for (SchamaClass obj : data) {
            if (obj.getName().equals(name)) {
                return false;
            }
        }
        return true;
    }

    private SchemaTransformerPreferences getSchemaTranslatorPreferences(Schema s, AbstractCimTranslator t) throws NamedElementNotFoundException {
        SchemaTransformerPreferences schemaPrefs = null;
        // -- Updating qulifier for the schema -----------------------------
        List<Qualifier<SchemaTransformerPreferences>> schemaQualifiers = s.getByVariantType(SchemaTransformerPreferences.class);
        for (Qualifier<?> q : schemaQualifiers) {
            SchemaTransformerPreferences stp = (SchemaTransformerPreferences) q.getValue();
            if (stp.getTransformerName().equals(translatorName)) {
                schemaPrefs = stp;
            }
        }
        return schemaPrefs;
    }

    private Map<String, Object> getTranslatorProperties(Schema s, AbstractCimTranslator t) throws NamedElementNotFoundException {
        SchemaTransformerPreferences schemaPrefs = getSchemaTranslatorPreferences(s, t);
        if (schemaPrefs != null) {
            Map<String, Object> res = new LinkedHashMap<>();
            // -- Building dialog properties ------------------------------------
            Map<String, TranslatorPropertyMetadata> metadata = t.getPropertiesMetadata();
            for (Entry<String, TranslatorPropertyMetadata> item : metadata.entrySet()) {
                String name = item.getKey();
                TranslatorPropertyMetadata meta = item.getValue();
                if (schemaPrefs.getProperty(name) != null) {
                    TranslatorPropertyMetadata.Type propType = meta.getPropertyType();
                    switch (propType) {
                        case SET:
                        case PATH:
                        case STRING:
                            res.put(name, schemaPrefs.getProperty(name));
                            break;
                        case INTEGER:
                            res.put(name, new Integer(schemaPrefs.getProperty(name)));
                            break;
                        case NUMBER:
                            res.put(name, new Double(schemaPrefs.getProperty(name)));
                            break;
                        case DATE:
                            res.put(name, new Date(schemaPrefs.getProperty(name)));
                            break;
                        case BOOLEAN:
                            res.put(name, Boolean.valueOf(schemaPrefs.getProperty(name)));
                            break;
                    }
                } else {
                    res.put(name, t.getProperties().get(name));
                }
            }
            return res;
        }
        return null;
    }

    private void setTranslatorProperties(Schema s, AbstractCimTranslator t, Map<String, Object> props) throws NamedElementNotFoundException {
        SchemaTransformerPreferences schemaPrefs = getSchemaTranslatorPreferences(s, t);
        if (schemaPrefs != null) {
            for (Entry<String, Object> item : props.entrySet()) {
                schemaPrefs.registerProperty(item.getKey(), item.getValue().toString());
            }
        }
    }

    private void translateSchema() {
        eanbleAction("Transform", false);
        longRuningTasks.execute(new Runnable() {

            @Override
            public void run() {
                String path = getDataObjectPath();
                try (InputStream sysIn = dataObject.getPrimaryFile().getInputStream()) {
                    // -- Reading a schema from the file first -------------------------
                    ModelReader r = ModelReaderFactory.newInstance(new ErdVariantsFactory());
                    Schema s = r.read(sysIn);
                    // -- Finding selected translator ----------------------------------
                    for (AbstractCimTranslator t : TranslatorsRepository.getTranslators()) {
                        if (t.getName().equals(translatorName)) {
                            Map<String, Object> trProps = getTranslatorProperties(s, t);
                            if (trProps != null) {
                                ValidationResult vr = t.validateProperties(path, trProps);
                                if (!vr.isValid()) {
                                    StringBuilder sb = new StringBuilder();
                                    for (String e : vr.getErrors()) {
                                        sb.append(e).append('\n');
                                    }
                                    JOptionPane.showMessageDialog(
                                            appFrame, 
                                            sb.toString() + "Execution aborted.", 
                                            TRANSLATOR_PROPERTIES_ERROR, 
                                            JOptionPane.ERROR_MESSAGE
                                    );
                                    return;
                                }
                                t.setProperties(trProps);
                            } else {
                                t.setProperties(null); // Setting properties by default
                            }
                            t.translate(s, path);
                        }
                    }
                } catch (ModelPersistenceException | IOException | NamedElementNotFoundException | CimTranslatorError e) {
                    Exceptions.printStackTrace(e);
                } finally {
                    SwingUtilities.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            eanbleAction("Transform", true);
                        }

                    });
                }
            }
        });
    }

    private void setupTransaltor() {
        for (AbstractCimTranslator t : TranslatorsRepository.getTranslators()) {
            if (t.getName().equals(translatorName)) {
                Schema s;
                // -- Getting the current content of the schema from the document ----------
                // -- If user made an input of translator's properties ---------------------
                try (InputStream sysIn = new ByteArrayInputStream(document.getText(0, document.getLength()).getBytes())) {
                    // -- Reading a schema from the file first -------------------------
                    ModelReader r = ModelReaderFactory.newInstance(new ErdVariantsFactory());
                    s = r.read(sysIn);
                    // -- Updating qulifier for thechema -----------------------------
                    SchemaTransformerPreferences schemaPrefs;
                    // -- Building dialog properties ------------------------------------
                    Map<String, Object> savedProps = getTranslatorProperties(s, t);
                    if (savedProps == null) {
                        savedProps = t.getProperties();
                        schemaPrefs = new SchemaTransformerPreferences();
                        schemaPrefs.setTransformerName(translatorName);
                        s.add(new Qualifier<>(schemaPrefs));
                        for (Entry<String, Object> item : savedProps.entrySet()) {
                            schemaPrefs.registerProperty(item.getKey(), item.getValue().toString());
                        }
                    }
                    // -- Showing a dialog to edit tenslator's properties ---------------
                    PropertiesDialog d = new PropertiesDialog(this, true);
                    d.initliseControls(savedProps, t.getPropertiesMetadata());
                    for (;;) {
                        d.setVisible(true);
                        // -- Storing the result if user has accepted changes ---------------
                        if (d.getFinalState() == PropertiesDialog.IDOK) {
                            String path = getDataObjectPath();
                            ValidationResult vr = t.validateProperties(path, d.getProperties());
                            if (vr.isValid()) {
                                setTranslatorProperties(s, t, d.getProperties());
                                // -- Setting translator's settings qualifier -------------------
                                try (ByteArrayOutputStream sysOut = new ByteArrayOutputStream()) {
                                    ModelWriter writer = ModelWriterFactory.newInstance();
                                    writer.write(sysOut, s);
                                    // ----------------------------------------------------------
                                    document.remove(0, document.getLength());
                                    document.insertString(cnt, formatXml(sysOut.toString("utf-8")), null);
                                }
                                break;
                            } else {
                                StringBuilder sb = new StringBuilder();
                                for (String e : vr.getErrors()) {
                                    sb.append(e).append('\n');
                                }
                                JOptionPane.showMessageDialog(
                                        appFrame, 
                                        sb.toString(), 
                                        TRANSLATOR_PROPERTIES_ERROR, 
                                        JOptionPane.WARNING_MESSAGE
                                );
                            }
                        } else {
                            break;
                        }
                    }
                } catch (IOException | NamedElementNotFoundException | BadLocationException | ModelPersistenceException e) {
                    Exceptions.printStackTrace(e);
                }
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        String actionCommand = ev.getActionCommand();
        switch (actionCommand) {
            case "Pointer":
                setPointerMode();
                break;
            case "Delete":
                setDeleteMode();
                break;
            case "Remark":
                setAddCommentAssociationMode();
                break;
            case "Relationship":
                setAddAssociationMode();
                break;
            case "Entity":
                setAddObjectMode();
                break;
            case "Text":
                setAddTextMode();
                break;
            case "Comment":
                setAddCommentMode();
                break;
            case "NewObject":
                if (mode == EditorMode.ADD_NEW_OBJECT) {
                    mode = EditorMode.DEFAULT;
                } else {
                    mode = EditorMode.ADD_NEW_OBJECT;
                }
                setCursorMode();
                break;
            case "NewLink":
                if (mode == EditorMode.ADD_NEW_ASSOCIATION) {
                    mode = EditorMode.DEFAULT;
                } else {
                    mode = EditorMode.ADD_NEW_ASSOCIATION;
                }
                setCursorMode();
                break;
            case "DeleteObject":
                if (mode == EditorMode.DELETE_OBJECTS) {
                    mode = EditorMode.DEFAULT;
                } else {
                    mode = EditorMode.DELETE_OBJECTS;
                }
                setCursorMode();
                break;
            case "Transform":
                translateSchema();
                break;
            case "Settings":
                setupTransaltor();
                break;
            case "ToPNG":
                chooseFileAndSaveToPNG();
                break;
            default:
                throw new UnsupportedOperationException("Command " + actionCommand + " has not been supported yet.");
        }
    }

    private String getDataObjectPath() {
        String fullFileName = dataObject.getPrimaryFile().getPath();
        return fullFileName.substring(0, fullFileName.lastIndexOf('/'));
    }

    private String getCimRepresentation() throws Exception {
        List<Qualifier<?>> qualifiers = null;
        // -- Getting the current content of the schema from the document ----------
        // -- If user made an input of translator's properties ---------------------
        try (InputStream sysIn = new ByteArrayInputStream(document.getText(0, document.getLength()).getBytes())) {
            // -- Reading a schema from the file first -------------------------
            ModelReader r = ModelReaderFactory.newInstance(new ErdVariantsFactory());
            Schema s = r.read(sysIn);
            qualifiers = s.getQualifiers();
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
        }
        // -- Building new schema -------------------------------------------------------------
        Schema schema = ErdModelFactory.buildSchema("Test");
        if (qualifiers != null) {
            for (Qualifier<?> q : qualifiers) {
                schema.add(q);
            }
        }
        // -- Storing information about ERD objects -------------------------------------------
        for (Entity ev : data) {
            ev.addTo(schema);
        }
        // -- Storing information about text --------------------------------------------------
        for (SchemaRectangleElement ao : texts) {
            ao.addTo(schema);
        }
        // -- Storing information about ERD objects associations ------------------------------
        for (ElementsAssociation association : associations) {
            association.addTo(schema);
        }
        // -- Storing information about comments associations ---------------------------------
        for (CommentAssociation association : commentAssociations) {
            association.addTo(schema);
        }

        ByteArrayOutputStream sysOut = new ByteArrayOutputStream();
        ModelWriter writer = ModelWriterFactory.newInstance();
        writer.write(sysOut, schema);
        // --------------------------------------------------------------------------------
        return sysOut.toString("utf-8");
    }

    private void buildViewRepresentation(Schema s) throws NamedElementNotFoundException {
        // -- Now we are creating GUI representation for this schema ----------------------
        data = new ArrayList<>();
        associations = new ArrayList<>();
        commentAssociations = new ArrayList<>();
        texts = new ArrayList<>();

        List<NamedElement> entities = s.getElements(new Qualifier<>(new ErdModelEntity()));
        for (NamedElement e : entities) {
            data.add(new Entity(e, data));
        }

        List<NamedElement> txts = s.getElements(new Qualifier<>(new ErdModelText()));
        for (NamedElement t : txts) {
            texts.add(new FreeText(t));
        }

        List<NamedElement> coments = s.getElements(new Qualifier<>(new ErdModelTextualComment()));
        for (NamedElement c : coments) {
            texts.add(new Comment(c));
        }

        List<NamedElement> schemaAssociations = s.getElements(new Qualifier<>(new ErdModelEntityAssociation()));
        for (NamedElement a : schemaAssociations) {
            ElementsAssociation ea = new ElementsAssociation(data, a);
            int paralelNumber = 0;
            for (ElementsAssociation lnk : associations) {
                if (areAssociationsIdentical(lnk, ea)) {
                    paralelNumber++;
                }
            }
            ea.setParalelNumber(paralelNumber);
            associations.add(ea);
        }

        List<NamedElement> schemaCommentAssociations = s.getElements(new Qualifier<>(new ErdModelTextCommentAssociation()));
        for (NamedElement a : schemaCommentAssociations) {
            commentAssociations.add(new CommentAssociation(data, texts, a));
        }
    }

    public int getMaxHeight() {
        int max = Integer.MIN_VALUE;
        for (SchamaClass jd : data) {
            int pos = jd.getY() + jd.getYSize();
            if (pos > max) {
                max = pos;
            }
        }
        for (SchemaRectangleElement jl : texts) {
            int pos = jl.getY() + jl.getYSize();
            if (pos > max) {
                max = pos;
            }
        }
        for (ElementsAssociation ea : associations) {
            if (ea.isAssociationToItself() && ea.getParalelNumber() >= 2) {
                SchemaRectangleElement lo = ea.getFromObject();
                int pos = lo.getY() + lo.getYSize() + 32;
                if (pos > max) {
                    max = pos;
                }
            } else {
                for (Point p : ea.getPath()) {
                    max = p.y > max ? p.y : max;
                }
            }
        }
        return (int) (max * scale) + 10;
    }

    public int getMaxWidth() {
        int max = Integer.MIN_VALUE;
        for (SchamaClass jd : data) {
            int pos = jd.getX() + jd.getXSize();
            if (pos > max) {
                max = pos;
            }
        }
        for (SchemaRectangleElement jl : texts) {
            int pos = jl.getX() + jl.getXSize();
            if (pos > max) {
                max = pos;
            }
        }
        for (ElementsAssociation ea : associations) {
            if (ea.isAssociationToItself()) {
                SchemaRectangleElement lo = ea.getFromObject();
                int pos = lo.getX() + lo.getXSize() + 30;
                if (pos > max) {
                    max = pos;
                }
            } else {
                for (Point p : ea.getPath()) {
                    max = p.x > max ? p.x : max;
                }
            }
        }
        return (int) (max * scale) + 10;
    }

    public int getImageMinY() {
        int min = Integer.MAX_VALUE;
        for (SchamaClass jd : data) {
            int pos = jd.getY();
            if (pos < min) {
                min = pos;
            }
        }
        for (SchemaRectangleElement jl : texts) {
            int pos = jl.getY();
            if (pos < min) {
                min = pos;
            }
        }
        for (ElementsAssociation ea : associations) {
            if (ea.isAssociationToItself() && (ea.getParalelNumber() == 1 || ea.getParalelNumber() == 2)) {
                SchemaRectangleElement lo = ea.getFromObject();
                int pos = lo.getY() - 32;
                if (pos < min) {
                    min = pos;
                }
            } else {
                for (Point p : ea.getPath()) {
                    min = p.y < min ? p.y : min;
                }
            }
        }
        return (int) (min * scale) - 10;
    }

    public int getImageMinX() {
        int min = Integer.MAX_VALUE;
        for (SchamaClass jd : data) {
            int pos = jd.getX();
            if (pos < min) {
                min = pos;
            }
        }
        for (SchemaRectangleElement jl : texts) {
            int pos = jl.getX();
            if (pos < min) {
                min = pos;
            }
        }
        for (ElementsAssociation ea : associations) {
            if (ea.isAssociationToItself() && (ea.getParalelNumber() == 2 || ea.getParalelNumber() == 3)) {
                SchemaRectangleElement lo = ea.getFromObject();
                int pos = lo.getX() - 30;
                if (pos < min) {
                    min = pos;
                }
            } else {
                for (Point p : ea.getPath()) {
                    min = p.x < min ? p.x : min;
                }
            }
        }
        return (int) (min * scale) - 10;
    }

    private String formatXml(String sourceXml) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(sourceXml));
            Document d = db.parse(is);
            // -- Formating XML ----------------------------------------------
            OutputFormat format = new OutputFormat(d);
            format.setIndenting(true);
            format.setIndent(4);
            Writer out = new StringWriter();
            XMLSerializer s = new XMLSerializer(out, format);
            s.serialize(d);
            return out.toString();
        } catch (ParserConfigurationException | SAXException | IOException e) {
            logger.severe(e.getMessage());
        }
        return sourceXml;
    }

    private boolean areAssociationsIdentical(ElementsAssociation lnk1, ElementsAssociation lnk2) {
        return (lnk1.getFromObject() == lnk2.getFromObject() && lnk1.getToObject() == lnk2.getToObject())
                || (lnk1.getFromObject() == lnk2.getToObject() && lnk1.getToObject() == lnk2.getFromObject());
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
        if (dirty) {
            blockRebuild = true;
            longRuningTasks.execute(new Runnable() {

                @Override
                public void run() {
                    try {
                        String newDocumentRepresentation = formatXml(getCimRepresentation());
                        document.remove(0, document.getLength());
                        document.insertString(cnt, newDocumentRepresentation, null);
                    } catch (BadLocationException ex) {
                        Exceptions.printStackTrace(ex);
                    } catch (Exception ex) {
                        Exceptions.printStackTrace(ex);
                    } finally {
                        SwingUtilities.invokeLater(new Runnable() {

                            @Override
                            public void run() {
                                blockRebuild = false;
                            }
                        });
                    }
                }
            });
            adjustSize();
        }
    }

    public String getTranslatorName() {
        return translatorName;
    }

    public void setTranslatorName(String translatorName) {
        this.translatorName = translatorName;
    }

    public void exportToPNG(String fName) {
        int width = getMaxWidth();
        int height = getMaxHeight();

        int minX = getImageMinX();
        int minY = getImageMinY();

        // Create a buffered image in which to draw
        BufferedImage bufferedImage = new BufferedImage(width - minX, height - minY, BufferedImage.TYPE_INT_RGB);

        // Create a graphics contents on the buffered image
        Graphics2D g2d = bufferedImage.createGraphics();

        g2d.translate(-minX, -minY);

        // Draw graphics
        g2d.setColor(Color.black);
        paint(g2d);

        // Graphics context no longer needed so dispose it
        g2d.dispose();

        try {
            // Save as JPEG
            File file = new File(fName);
            ImageIO.write(bufferedImage, "png", file);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private void chooseFileAndSaveToPNG() {
        final JFileChooser fc = new JFileChooser();
        fc.addChoosableFileFilter(new CustomFileFilter("All *.png files", "png"));
        int returnVal = fc.showSaveDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            String nFileName = fc.getSelectedFile().getPath();
            if (!nFileName.contains(".")) {
                nFileName += ".png";
            }
            exportToPNG(nFileName);
        }
    }

    public void adjustSize() {
        int w = getMaxWidth();
        int h = getMaxHeight();

        int pw = getWidth();
        int ph = getHeight();

        int nw = pw < w ? w : pw;
        int nh = ph < h ? h : ph;

        this.setMinimumSize(new Dimension(nw, nh));
        this.setMaximumSize(new Dimension(nw, nh));
        this.setPreferredSize(new Dimension(nw, nh));
        this.getParent().revalidate();
    }

    private void eanbleAction(String name, boolean isEnabled) {
        for (Component c = this; c != null; c = c.getParent()) {
            if (c instanceof ActionsController) {
                ((ActionsController) c).enableAction(name, isEnabled);
            }
        }
    }

    private enum EditorMode {

        DEFAULT, ADD_NEW_OBJECT, ADD_NEW_ASSOCIATION, ADD_NEW_TEXT, DELETE_OBJECTS, ADD_NEW_COMMENT, ADD_NEW_COMMENT_ASSOCIATION
    }

    private enum AssociationMode {

        ASSOCIATION_IS_CLEAR, ASSOCIATION_FIRST_OBJECT_IS_SELECTED, ASSOCIATION_SECOND_OBJECT_IS_SELECTED
    }
}
