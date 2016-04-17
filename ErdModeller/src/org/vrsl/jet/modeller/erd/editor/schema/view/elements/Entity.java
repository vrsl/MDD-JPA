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
package org.vrsl.jet.modeller.erd.editor.schema.view.elements;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import org.vrsl.jet.modeller.erd.editor.schema.dialogs.ElementPropertyDialog;
import org.vrsl.jet.modeller.erd.editor.schema.dialogs.EntitiyPropertiesDialog;
import org.vrsl.jet.modeller.erd.editor.schema.view.SchamaClass;
import org.vrsl.jet.models.cim.Class;
import org.vrsl.jet.models.cim.NamedElement;
import org.vrsl.jet.models.cim.NamedElementNotFoundException;
import org.vrsl.jet.models.cim.Property;
import org.vrsl.jet.models.cim.Qualifier;
import org.vrsl.jet.models.cim.Schema;
import org.vrsl.jet.models.erd.ErdModelFactory;
import org.vrsl.jet.models.erd.variants.details.ErdModelDescription;
import org.vrsl.jet.models.erd.variants.details.ErdModelLocation;
import org.vrsl.jet.models.erd.variants.details.ErdModelMappingDetails;
import org.vrsl.jet.models.erd.variants.details.ErdModelPrimaryKey;
import org.vrsl.jet.models.erd.variants.details.ErdModelSize;
import org.vrsl.jet.models.erd.variants.entities.ErdModelEntity;
import org.vrsl.jet.models.erd.variants.types.ErdModelType;

public class Entity extends SchamaClass {

    static final long serialVersionUID = 92384798370002L;
    // -------------------------------------------------------------------------------------------------------
    private static Image keyImage = Toolkit.getDefaultToolkit().getImage(Entity.class.getResource("/org/vrsl/jet/modeller/erd/images/Key10x10.png"));
    // -------------------------------------------------------------------------------------------------------
    private static int maxSSN = 0;
    // -------------------------------------------------------------------------------------------------------
    private static String[] fieldTypes = {
        "UNDEFINED",
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
    // -------------------------------------------------------------------------------------------------------
    final static int TR_REQUIRED = 0;
    final static int TR_REQUIRED_NEW = 1;
    final static int TR_MANDRATORY = 2;
    final static int TR_NOT_SUPPORTED = 3;
    final static int TR_SUPPORTS = 4;
    final static int TR_NEVER = 5;
    // -------------------------------------------------------------------------------------------------------
    static private Font boldFont = null;
    static private FontRenderContext fontRenderContext = null;
    static private FontRenderContext boldFontRenderContext = null;
    static final Color defaultModeInstnaceColor = new Color(255, 255, 220);
    static final Color deleteModeInstnaceColor = new Color(255, 95, 95);
    static final Color moveModeInstnaceColor = new Color(220, 220, 220);
    static final Color selectedModeItemColor = new Color(255, 200, 120);
    static final Color deleteModeItemColor = new Color(255, 95, 95);
    static final BasicStroke doubleStroke = new BasicStroke(2.0f);

    private static int getNextItemNumber() {
        return ++maxSSN;
    }
    private String description = null;
    private List<EntityProperty> fields = null;
    private boolean isAssociationSelected = false;
    // -------------------------------------------------------------------------------------------------------
    private int associatedField = -1;
    // -------------------------------------------------------------------------------------------------------
    int[] mapSize = {
        -1,
        1, // BOOLEAN = 1;
        1, // CHAR = 2;
        3, // BYTE = 3;
        5, // SHORT = 4;
        10, // INTEGER = 5;
        19, // LONG = 6;
        2000, // STRING = 7;
        -1, // FLOAT = 8;
        -1, // DOUBLE = 9;
        -1, // DATE = 10;
    };
    // -------------------------------------------------------------------------------------------------------
    int[] mapPrec = {
        -1,
        0, // BOOLEAN = 1;
        -1, // CHAR = 2;
        0, // BYTE = 3;
        0, // SHORT = 4;
        0, // INTEGER = 5;
        0, // LONG = 6;
        -1, // STRING = 7;
        -1, // FLOAT = 8;
        -1, // DOUBLE = 9;
        -1, // DATE = 10;
    };
    // -------------------------------------------------------------------------------------------------------
    private List<Entity> objects = null;
    // -------------------------------------------------------------------------------------------------------
    private String toolTip = null;
    // -----------------------------------------------------------------------------------
    // ================================== Rendering ======================================
    // -----------------------------------------------------------------------------------
    private int fontVSize = 0;
    private int titleSize = 0;

    /**
     * Creates a new instance of Entity
     */
    public Entity(int x, int y, List<Entity> objs) {
        setX(x);
        setY(y);
        fields = new ArrayList<>();
        objects = objs;

        boolean isNameOk = false;
        String theName = null;

        while (!isNameOk) {
            theName = "Entity" + getNextItemNumber();
            isNameOk = true; // We are very optimistyck
            for (SchamaClass theObject : objs) {
                if (theName.equals(theObject.getName())) {
                    isNameOk = false;
                    break;
                }
            }
        }

        setName(theName);
    }
    public Entity(NamedElement ce, List<Entity> objs) throws NamedElementNotFoundException {

        if (!(ce instanceof Class)) {
            throw new IllegalArgumentException("Wrong type of schema " + ce.getName() + " element : " + ce.getClass().getName());
        }

        objects = objs;

        Class cl = (Class) ce;

        ErdModelLocation location = ce.<ErdModelLocation>getFirstByVariantType(ErdModelLocation.class).getValue();

        setX(location.getLoaction().x);
        setY(location.getLoaction().y);
        setName(ce.getName());

        fields = new ArrayList<>();

        for (Property p : cl.getProperties()) {
            ErdModelType type = p.<ErdModelType>getFirstByVariantType(ErdModelType.class).getValue();
            ErdModelDescription dsc = p.<ErdModelDescription>getFirstByVariantType(ErdModelDescription.class).getValue();
            ErdModelPrimaryKey prKey = p.<ErdModelPrimaryKey>getFirstByVariantType(ErdModelPrimaryKey.class).getValue();
            ErdModelMappingDetails mappingDetails = p.<ErdModelMappingDetails>getFirstByVariantType(ErdModelMappingDetails.class).getValue();
            // -- Getting our magic numbers ----------------------------------------------------
            int fldType = 0;
            for (int j = 0; j < fieldTypes.length; j++) {
                if (fieldTypes[j].equals(type.toString())) {
                    fldType = j;
                    break;
                }
            }
            // -- Making entity field representation -------------------------------------------
            EntityProperty fl = new EntityProperty(p.getName(), fldType);
            fl.setPrimaryKeyMode(prKey.isPrimiryKey());
            fl.setUseAutoSequence(prKey.isUseAutoSequence());
            fl.setValueObjectFieldMode(false);
            fl.setMapping(
                    mappingDetails.getMapFieldTo(),
                    mappingDetails.getMapFieldSize(),
                    mappingDetails.getMapFieldPrec());
            fl.setDescription(dsc.toString());
            // -- Storing constructed field ----------------------------------------------------
            fields.add(fl);
        }
    }

    // -----------------------------------------------------------------------------------
    // ======================= Internal data and schema relations ========================
    // -----------------------------------------------------------------------------------
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    // -----------------------------------------------------------------------------------
    @Override
    public String getToolTipText() {
        return toolTip;
    }

    @Override
    public boolean setAssociationSelected(boolean isTrue) {
        if (isTrue && getActiveField() >= 0) {
            associatedField = getActiveField();
            if (associatedField >= fields.size()) {
                associatedField = -1;
                return false;
            }
        } else {
            associatedField = -1;
        }
        isAssociationSelected = isTrue;
        return true;
    }

    @Override
    public boolean deleteSelectedField() {
        if (!hasReadyToBeDeletedField() || getSelectedField() >= fields.size()) {
            return false;
        }
        fields.remove(getSelectedField());
        setDefaultView();
        return true;
    }

    public List<EntityProperty> getAllFields() {
        return new ArrayList<>(fields);
    }

    public String getNewAssociatedField() {
        if (associatedField < 0) {
            return null;
        }
        EntityProperty fl = fields.get(associatedField);
        return fl.getName();
    }

    private int getFieldNumber(int posY) {
        return (int) ((posY - getY() * scale - fontVSize * scale) / (fontVSize * scale));
    }

    public boolean upField(int fNum) {
        EntityProperty curObj = fields.get(fNum);
        EntityProperty prwObj = fields.get(fNum - 1);
        fields.set(fNum, prwObj);
        fields.set(fNum - 1, curObj);
        return true;
    }

    public boolean downField(int fNum) {
        EntityProperty curObj = fields.get(fNum);
        EntityProperty nxtObj = fields.get(fNum + 1);
        fields.set(fNum + 1, curObj);
        fields.set(fNum, nxtObj);
        return true;
    }


    @Override
    public void draw(Graphics g) {

        if (!(g instanceof Graphics2D)) {
            return;
        }

        Graphics2D g2 = (Graphics2D) g;

        Shape clipShape = g2.getClip();

        Font curFont = g.getFont();
        if (boldFont == null) {
            boldFont = new Font(curFont.getFontName(), Font.BOLD, curFont.getSize());
        }

        Color pColor = g.getColor();
        if (!isMoved()) {
            if (!isReadyToBeDeleted()) {
                g.setColor(defaultModeInstnaceColor);
            } else {
                g.setColor(deleteModeInstnaceColor);
            }
        } else {
            g.setColor(moveModeInstnaceColor);
        }

        RoundRectangle2D rd = new RoundRectangle2D.Double(getX(), getY(), getXSize(), getYSize(), 15, 15);
        g2.fill(rd);
        g.setColor(pColor);

        g2.clip(rd);

        ///////////////////////////////////////////////////////////////
        // WARNING !!! You must optimise this part of code
        ///////////////////////////////////////////////////////////////
        if (isActiveTitle()) {
            if (!isMoved()) {
                if (!isReadyToBeDeleted()) {
                    g.setColor(selectedModeItemColor);
                } else {
                    g.setColor(deleteModeItemColor);
                }
                g.fillRect(getX(), getY(), getXSize(), getYSize());
                g.setColor(pColor);
            }
        }
        ///////////////////////////////////////////////////////////////
        if (isActiveField()) {
            if (!isMoved()) {
                if (!isReadyToBeDeleted()) {
                    g.setColor(selectedModeItemColor);
                } else {
                    g.setColor(deleteModeItemColor);
                }
                g.fillRect(getX(), getY() + fontVSize * (getActiveField() + 1), getXSize(), fontVSize);
                g.setColor(pColor);
            }
        }
        ///////////////////////////////////////////////////////////////
        if (associatedField >= 0) {
            if (!isMoved()) {
                if (!isReadyToBeDeleted()) {
                    g.setColor(selectedModeItemColor);
                } else {
                    g.setColor(deleteModeItemColor);
                }
                g.fillRect(getX(), getY() + fontVSize * (associatedField + 1), getXSize(), fontVSize);
                g.setColor(pColor);
            }
        }
        ///////////////////////////////////////////////////////////////
        if (hasReadyToBeDeletedField()) {
            if (!isMoved()) {
                g.setColor(deleteModeItemColor);
                g.fillRect(getX(), getY() + fontVSize * (getSelectedField() + 1), getXSize(), fontVSize);
                g.setColor(pColor);
            }
        }

        g2.setClip(clipShape);

        Stroke curStroke = g2.getStroke();
        if (selected || isAssociationSelected) {
            g2.setStroke(doubleStroke);
        }
        g.drawRoundRect(getX(), getY(), getXSize(), getYSize(), 15, 15);
        g2.setStroke(curStroke);

        int xShift = (getXSize() - titleSize) / 2;
        g.setFont(boldFont);
        g.drawString(getName(), getX() + xShift, getY() + fontVSize - 4);
        g.setFont(curFont);

        int stYPos = getY() + fontVSize;
        for (int i = 0; i < fields.size(); i++) {
            EntityProperty fl = fields.get(i);
            if (fl.getValueObjectFieldMode()) {
                drawValueObjectIcon(i, g);
            }
            if (fl.getPrimaryKeyMode()) {
                drawPrimaryKeyIcon(i, g);
            }
            g.drawString(fl.getName() + " : " + fieldTypes[fl.getType()], getX() + fontVSize, stYPos + fontVSize * i + fontVSize - 4);
        }
        g.drawString(". . .", getX() + fontVSize, stYPos + fontVSize * fields.size() + fontVSize - 4);

    }

    @Override
    public void calculateBoxMetrics(Graphics g) {
        // -- Calculating a title for bold font --------------------------------
        Font f = g.getFont();
        if (boldFont == null) {
            boldFont = new Font(f.getFontName(), Font.BOLD, f.getSize());
        }
        if (boldFontRenderContext == null) {
            boldFontRenderContext = new FontRenderContext(boldFont.getTransform(), false, false);
        }
        if (fontRenderContext == null) {
            fontRenderContext = new FontRenderContext(f.getTransform(), false, false);
        }
        
        Rectangle2D title = boldFont.getStringBounds(getName(), boldFontRenderContext);
        fontVSize = (int) title.getHeight() + 2;
        titleSize = (int) title.getWidth();
        

        setXSize(titleSize + 20);

        for (EntityProperty fl : fields) {
            Rectangle2D fld = f.getStringBounds(fl.getName() + " : " + fieldTypes[fl.getType()], fontRenderContext);
            int newSize = (int) fld.getWidth() + 20;
            if (newSize > getXSize()) {
                setXSize(newSize);
            }
        }

        setYSize((fields.size() + 2) * fontVSize);
    }

    @Override
    public void shift(int shiftX, int shiftY) {
        setX(getX() + shiftX);
        setY(getY() + shiftY);
    }

    private void drawValueObjectIcon(int itemNum, Graphics g) {
        int yTop = getY() + fontVSize + itemNum * fontVSize;
        int xTop = getX() + 4;

        Color pColor = g.getColor();
        g.setColor(new Color(180, 32, 32));
        g.fillRect(xTop, yTop + 2, 10, 10);
        g.setColor(new Color(255, 255, 255));
        g.drawString("v", xTop + 2, yTop + fontVSize - 5);
        g.setColor(pColor);
    }

    private void drawPrimaryKeyIcon(int itemNum, Graphics g) {
        int yTop = getY() + fontVSize + itemNum * fontVSize + 4;
        int xTop = getX() + 5;
        g.drawImage(keyImage, xTop, yTop, null);
    }

    // -----------------------------------------------------------------------------------
    // =========================== Mouse events processing ===============================
    // -----------------------------------------------------------------------------------
    @Override
    public boolean processMouseEvent(JFrame appFrame, MouseEvent ev) {
        int curX = ev.getX();
        int curY = ev.getY();
        ////////////////////////////////////////////////////////
        // Here we processing getName() of our obejct
        if (curX >= getX() * scale && curX <= getX() * scale + getXSize() * scale
                && curY >= getY() * scale && curY <= getY() * scale + fontVSize * scale) {
            EntitiyPropertiesDialog dl = new EntitiyPropertiesDialog(appFrame, true);
            dl.setEntitiy(this);
            dl.setVisible(true);
            if (dl.isUpdated()) {
                ////////////////////////////////////////////
                // Here we check name constraint
                for (Entity theObject : objects) {
                    if (dl.getName().equals(theObject.getName()) && theObject != this) {
                        //JOptionPane.showMessageDialog(EJB2DataTools.curApp, "You can't use this name twice", "Object name alret", JOptionPane.ERROR_MESSAGE);
                        JOptionPane.showMessageDialog(appFrame, "You can't use this name twice", "Object name alret", JOptionPane.ERROR_MESSAGE);
                        return false;
                    }
                }
                setDirty(true);
            }
            return true;
        }
        if (curX >= getX() * scale && curX <= getX() * scale + getXSize() * scale
                && curY >= getY() * scale + fontVSize * scale && curY <= getY() * scale + getYSize() * scale) {
            ElementPropertyDialog dl = new ElementPropertyDialog(appFrame, true);
            int editingPos = -1;
            if (!fields.isEmpty() && getFieldNumber(curY) < fields.size()) {
                EntityProperty fl = fields.get(editingPos = getFieldNumber(curY));
                dl.setProperty(fl);
            }
            dl.setVisible(true);
            if (dl.isUpdated()) {
                EntityProperty fl = dl.getProperty();

                if (editingPos == -1) {
                    fields.add(fl);
                } else {
                    fields.set(editingPos, fl);
                }

                setDirty(true);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean isAssociationAreaRelated(MouseEvent ev) {
        int curX = ev.getX();
        int curY = ev.getY();
        return curX >= getX() * scale && curX <= getX() * scale + getXSize() * scale
                && curY >= getY() * scale && curY <= getY() * scale + getYSize() * scale;
    }

    @Override
    public boolean isMovingAreaRelated(MouseEvent ev) {
        int curX = ev.getX();
        int curY = ev.getY();
        return curX >= getX() * scale && curX <= getX() * scale + getXSize() * scale
                && curY >= getY() * scale && curY <= getY() * scale + 2;
    }

    @Override
    public boolean isTitleAreaRelated(MouseEvent ev) {
        int curX = ev.getX();
        int curY = ev.getY();
        toolTip = null;
        ////////////////////////////////////////////////////////
        // Here we processing name of our obejct
        if (curX >= getX() * scale && curX <= getX() * scale + getXSize() * scale
                && curY >= getY() * scale && curY <= getY() * scale + fontVSize * scale) {
            toolTip = getName();
            return true;
        }
        return false;
    }

    @Override
    public boolean isFieldAreaRelated(MouseEvent ev) {
        int curX = ev.getX();
        int curY = ev.getY();
        toolTip = null;
        if (fontVSize != 0
                && curX >= getX() * scale && curX <= getX() * scale + getXSize() * scale
                && curY >= getY() * scale + fontVSize * scale && curY <= getY() * scale + getYSize() * scale - 1) {
            setNewActiveField((int) ((curY - getY() * scale - fontVSize * scale) / (fontVSize * scale)));

            if (getNewActiveField() < fields.size()) {
                EntityProperty fl = fields.get(getNewActiveField());
                toolTip = fieldTypes[fl.getType()] + " " + fl.getName();
            }

            return true;
        }
        setActiveField(-1);
        return false;
    }

    @Override
    public boolean isOwnedAreaRelated(MouseEvent ev) {
        return false;
    }

    // -----------------------------------------------------------------------------------
    // ============================ Instance persistance =================================
    // -----------------------------------------------------------------------------------
    @Override
    public void addTo(Schema schema) {
        Class ec = ErdModelFactory.buildClass(getName());
        ec.add(new Qualifier<>(new ErdModelEntity()));
        ec.add(new Qualifier<>(new ErdModelLocation(getX(), getY())));
        ec.add(new Qualifier<>(new ErdModelSize(getXSize(), getYSize())));
        for (EntityProperty field : getAllFields()) {
            Property prop = ErdModelFactory.buildProperty();
            prop.setName(field.getName());
            prop.add(new Qualifier<>(new ErdModelType(field.getTypeRepresentation())));
            prop.add(new Qualifier<>(new ErdModelDescription(field.getDescription())));
            prop.add(new Qualifier<>(new ErdModelPrimaryKey(field.getPrimaryKeyMode(), field.getUseAutoSequence())));
            prop.add(new Qualifier<>(new ErdModelMappingDetails(field.getMapFieldTo(), field.getMapFieldSize(), field.getMapFieldPrec())));
            ec.add(prop);
        }
        schema.add(ec);
    }
}
