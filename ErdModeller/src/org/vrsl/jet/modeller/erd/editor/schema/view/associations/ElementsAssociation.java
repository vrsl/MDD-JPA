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
package org.vrsl.jet.modeller.erd.editor.schema.view.associations;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JFrame;
import org.openide.util.Exceptions;
import org.vrsl.jet.modeller.erd.editor.schema.dialogs.AssociationDialog;
import org.vrsl.jet.modeller.erd.editor.schema.view.SchemaRectangleElement;
import org.vrsl.jet.modeller.erd.editor.schema.view.ShemaElement;
import org.vrsl.jet.modeller.erd.editor.schema.view.elements.Entity;
import org.vrsl.jet.modeller.erd.editor.utils.CommonUtils;
import org.vrsl.jet.models.cim.Association;
import org.vrsl.jet.models.cim.NamedElement;
import org.vrsl.jet.models.cim.NamedElementNotFoundException;
import org.vrsl.jet.models.cim.Qualifier;
import org.vrsl.jet.models.cim.Reference;
import org.vrsl.jet.models.cim.Schema;
import org.vrsl.jet.models.erd.ErdModelFactory;
import org.vrsl.jet.models.erd.variants.associations.ErdModelEntityAssociation;
import org.vrsl.jet.models.erd.variants.associations.ErdModelReferenceMultiplicity;
import org.vrsl.jet.models.erd.variants.associations.ErdModelReferenceSuggestedName;
import org.vrsl.jet.models.erd.variants.details.ErdModelPath;
import org.vrsl.jet.models.erd.variants.entities.ErdModelEntity;

/**
 * ***************************************************************************************************************
 * The next diagram is used to indicate sides of the schema object NORTH
 * +-----------------+ WEST | | EAST +-----------------+ SOUTH
 * **************************************************************************************************************
 */
public class ElementsAssociation extends ShemaElement implements Serializable {

    static final long serialVersionUID = 92384798370006L;

    // ----------------------------------------------------------------------------------------------------------
    private final static int MULTI_ASSOCIATION_SPACE = 32;
    // ----------------------------------------------------------------------------------------------------------
    private final static double LINE_SELECTION_RADIUS = 3.0;
    // ----------------------------------------------------------------------------------------------------------
    static final BasicStroke doubleStroke = new BasicStroke(2.0f);
    static final Color readyToDeleteColor = new Color(220, 0, 0);
    // ----------------------------------------------------------------------------------------------------------
    static final int DRAG_NONE = 0;
    static final int DRAG_POINT = 1;
    static final int DRAG_BEAM = 2;
    // ----------------------------------------------------------------------------------------------------------
    private static Font boldFont = null;
    // ----------------------------------------------------------------------------------------------------------
    protected SchemaRectangleElement from = null;
    protected SchemaRectangleElement to = null;
    // ----------------------------------------------------------------------------------------------------------
    protected ErdModelReferenceMultiplicity.Category fromMode = null;
    protected ErdModelReferenceMultiplicity.Category toMode = null;
    protected Rectangle2D activeZone1 = null;
    protected Rectangle2D activeZone2 = null;
    // ----------------------------------------------------------------------------------------------------------
    protected boolean isReadyToBeDeleted = false;
    protected boolean isSelected = false;
    protected boolean wasChanged = false;
    // ----------------------------------------------------------------------------------------------------------
    private int paralelNumber = 0;
    // ----------------------------------------------------------------------------------------------------------
    protected List<Point> navPoints = new ArrayList<>();
    // ----------------------------------------------------------------------------------------------------------
    protected int dragMode = DRAG_NONE;
    protected int dragPointNum = -1;
    // ----------------------------------------------------------------------------------------------------------
    protected IntersectionSide fromCross = IntersectionSide.NORTH;
    protected IntersectionSide toCross = IntersectionSide.NORTH;
    // ----------------------------------------------------------------------------------------------------------
    private String nameFrom = "";
    private String nameTo = "";

    /**
     * Creates a new instance of ElementsAssociation
     */
    public ElementsAssociation() {
    }

    public ElementsAssociation(List<Entity> objects, NamedElement ce) throws NamedElementNotFoundException {
        if (!(ce instanceof Association)) {
            throw new IllegalArgumentException("Wrong type of schema " + ce.getName() + " element : " + ce.getClass().getName());
        }

        Association a = (Association) ce;

        Reference refFr = a.getReferences().get(0);
        Reference refTo = a.getReferences().get(1);

        String frName = refFr.getReferedClass().getName();
        String toName = refTo.getReferedClass().getName();

        ErdModelReferenceMultiplicity frMulti = refFr.<ErdModelReferenceMultiplicity>getFirstByVariantType(ErdModelReferenceMultiplicity.class).getValue();
        ErdModelReferenceMultiplicity toMulti = refTo.<ErdModelReferenceMultiplicity>getFirstByVariantType(ErdModelReferenceMultiplicity.class).getValue();

        fromMode = frMulti.getCategory();
        toMode = toMulti.getCategory();

        from = findByName(objects, frName);
        to = findByName(objects, toName);

        if (refFr.hasVariantTypeQualifier(ErdModelReferenceSuggestedName.class)) {
            nameFrom = refFr.<ErdModelReferenceSuggestedName>getFirstByVariantType(ErdModelReferenceSuggestedName.class).getValue().toString();
        }
        if (refTo.hasVariantTypeQualifier(ErdModelReferenceSuggestedName.class)) {
            nameTo = refTo.<ErdModelReferenceSuggestedName>getFirstByVariantType(ErdModelReferenceSuggestedName.class).getValue().toString();
        }

        try {
            ErdModelPath path = a.<ErdModelPath>getFirstByVariantType(ErdModelPath.class).getValue();
            navPoints.addAll(path.get());
        } catch (NamedElementNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    protected <T extends SchemaRectangleElement> SchemaRectangleElement findByName(List<T> objects, String name) {
        for (SchemaRectangleElement e : objects) {
            if (e != null && e.getName() != null && e.getName().equals(name)) {
                return e;
            }
        }
        return null;
    }

    // -----------------------------------------------------------------------------------
    // ======================= Internal data and schema relations ========================
    // -----------------------------------------------------------------------------------
    public void setFrom(SchemaRectangleElement from) {
        this.from = from;
        nameFrom = "";
        fromMode = ErdModelReferenceMultiplicity.Category.ONE;
    }

    public void setFrom(SchemaRectangleElement from, String fNameFrom, ErdModelReferenceMultiplicity.Category fromMode) {
        this.from = from;
        nameFrom = fNameFrom;
        this.fromMode = fromMode;
    }

    public void setTo(SchemaRectangleElement to) {
        this.to = to;
        nameFrom = "";
        toMode = ErdModelReferenceMultiplicity.Category.ONE;
    }

    public void setTo(SchemaRectangleElement to, String fNameTo, ErdModelReferenceMultiplicity.Category toMode) {
        this.to = to;
        nameTo = fNameTo;
        this.toMode = toMode;
    }

    public SchemaRectangleElement getFromObject() {
        return from;
    }

    public String getNameFrom() {
        return nameFrom;
    }

    public void setNameFrom(String nameFrom) {
        this.nameFrom = nameFrom;
    }

    public ErdModelReferenceMultiplicity.Category getFromMode() {
        return fromMode;
    }

    public SchemaRectangleElement getToObject() {
        return to;
    }

    public String getNameTo() {
        return nameTo;
    }

    public void setNameTo(String nameTo) {
        this.nameTo = nameTo;
    }

    public ErdModelReferenceMultiplicity.Category getToMode() {
        return toMode;
    }

    public int getParalelNumber() {
        return paralelNumber;
    }

    public void setParalelNumber(int paralelNumber) {
        this.paralelNumber = paralelNumber;
    }

    public List<Point> getPath() {
        return Collections.unmodifiableList(navPoints);
    }
    // -----------------------------------------------------------------------------------
    // ================================== Statuses =======================================
    // -----------------------------------------------------------------------------------

    @Override
    public void setReadyToBeDeleted() {
        wasChanged = !isReadyToBeDeleted;
        isReadyToBeDeleted = true;
    }

    public void setSelected() {
        wasChanged = !isSelected;
        isSelected = true;
    }

    @Override
    public void setSelected(boolean selected) {
        wasChanged = true;
        isSelected = selected;
    }

    @Override
    public void setDefaultView() {
        wasChanged = isReadyToBeDeleted || isSelected;
        isReadyToBeDeleted = false;
        isSelected = false;
    }

    @Override
    public boolean isChanged() {
        return wasChanged;
    }

    @Override
    public boolean isSelected() {
        return isSelected;
    }

    public boolean isAssociationToItself() {
        return from == to;
    }

    // -----------------------------------------------------------------------------------
    // ================================== Rendering ======================================
    // -----------------------------------------------------------------------------------
    @Override
    public void draw(Graphics g) {
        if (from == to) {
            drawAssociationToItself(g);
            return;
        }

        Point p1 = getStartPoint();
        Point p2 = getFinalPoint();

        if (!(g instanceof Graphics2D) || p1 == null || p2 == null) {
            return;
        }

        Graphics2D g2 = (Graphics2D) g;

        Font curFont = g.getFont();
        if (boldFont == null) {
            boldFont = new Font(curFont.getFontName(), Font.BOLD, curFont.getSize());
        }

        Color curColor = g.getColor();
        if (isReadyToBeDeleted) {
            g.setColor(readyToDeleteColor);
            g.setFont(boldFont);
        }
        if (isSelected) {
            g.setFont(boldFont);
        }

        Stroke curStroke = null;
        if (isReadyToBeDeleted || isSelected) {
            curStroke = g2.getStroke();
            g2.setStroke(doubleStroke);
        }

        if (navPoints.isEmpty()) {
            g.drawLine((int) p1.getX(), (int) p1.getY(), (int) p2.getX(), (int) p2.getY());
        } else {
            Point curPoint = p1; //Stroke curStroke=g2.getStroke();
            for (Point p : navPoints) {
                if (isReadyToBeDeleted || isSelected) {
                    g2.drawLine(p.x - 2, p.y - 2, p.x + 2, p.y - 2);
                    g2.drawLine(p.x - 2, p.y - 2, p.x - 2, p.y + 2);
                    g2.drawLine(p.x - 2, p.y + 2, p.x + 2, p.y + 2);
                    g2.drawLine(p.x + 2, p.y + 2, p.x + 2, p.y - 2);
                }
                g.drawLine((int) curPoint.getX(), (int) curPoint.getY(), (int) p.getX(), (int) p.getY());
                curPoint = p;
            }
            g.drawLine((int) curPoint.getX(), (int) curPoint.getY(), (int) p2.getX(), (int) p2.getY());
            //if(g2.getStroke()!=curStroke)g2.setStroke(curStroke);
        }

        if (fromMode == ErdModelReferenceMultiplicity.Category.NONE_OR_ONE || fromMode == ErdModelReferenceMultiplicity.Category.ONE) {
            drawAssociationReferenceOne(g, p1, fromCross, fromMode == ErdModelReferenceMultiplicity.Category.ONE);
        }
        if (toMode == ErdModelReferenceMultiplicity.Category.NONE_OR_ONE || toMode == ErdModelReferenceMultiplicity.Category.ONE) {
            drawAssociationReferenceOne(g, p2, toCross, toMode == ErdModelReferenceMultiplicity.Category.ONE);
        }

        if (fromMode == ErdModelReferenceMultiplicity.Category.NONE_OR_MANY) {
            drawAssociationReferenceMany(g, p1, fromCross, false);
        }
        if (toMode == ErdModelReferenceMultiplicity.Category.NONE_OR_MANY) {
            drawAssociationReferenceMany(g, p2, toCross, false);
        }

        if (fromMode == ErdModelReferenceMultiplicity.Category.MANY) {
            drawAssociationReferenceMany(g, p1, fromCross, true);
        }
        if (toMode == ErdModelReferenceMultiplicity.Category.MANY) {
            drawAssociationReferenceMany(g, p2, toCross, true);
        }

        if (isReadyToBeDeleted || isSelected) {
            g2.setStroke(curStroke);
        }

        activeZone1 = getActiveZone(p1, fromCross);
        activeZone2 = getActiveZone(p2, toCross);

        Point pFrom = getStartTextPoint(from, g, nameFrom);
        Point pTo = getStartTextPoint(to, g, nameTo);

        g.drawString(nameFrom, pFrom.x, pFrom.y);
        g.drawString(nameTo, pTo.x, pTo.y);

        ////////////////////////////////////////////////////////////////////////
        if (isReadyToBeDeleted || isSelected) {
            g.setColor(curColor);
            g.setFont(curFont);
        }
    }

    @Override
    public void shift(int shiftX, int shiftY) {
        for (Point p : navPoints) {
            p.x += shiftX;
            p.y += shiftY;
        }
    }

    private void drawAssociationToItself(Graphics g) {
        Point p1;
        Point p2;
        int[] xPath;
        int[] yPath;
        IntersectionSide crSide1;
        IntersectionSide crSide2;
        // -- Getting new graphics for funcky stuff -------------------------------
        Graphics2D g2 = (Graphics2D) g;
        // -- Keeping a current font ----------------------------------------------
        Font curFont = g.getFont();
        // -- Setting line color --------------------------------------------------
        Color curColor = g.getColor();
        if (isReadyToBeDeleted) {
            g.setColor(readyToDeleteColor);
        }
        // -- Setting line stroke -------------------------------------------------
        Stroke curStroke = null;
        if (isReadyToBeDeleted || isSelected) {
            curStroke = g2.getStroke();
            g2.setStroke(doubleStroke);
            g.setFont(boldFont);
        }
        // -- Calculation positions first -----------------------------------------
        switch (paralelNumber) {
            case 1:
                p1 = new Point(from.getX() + 10, from.getY() - 22);
                p2 = new Point(from.getX() - 20, from.getY() + 10);
                xPath = new int[]{
                    p1.x,
                    p1.x,
                    p2.x - 10,
                    p2.x - 10,
                    p2.x
                };
                yPath = new int[]{
                    p1.y,
                    p1.y - 10,
                    p1.y - 10,
                    p2.y,
                    p2.y
                };
                crSide1 = IntersectionSide.NORTH;
                crSide2 = IntersectionSide.WEST;
                break;
            case 2:
                p1 = new Point(from.getX() + 10, from.getY() + from.getYSize() + 22);
                p2 = new Point(from.getX() - 20, from.getY() + from.getYSize() - 10);
                xPath = new int[]{
                    p1.x,
                    p1.x,
                    p2.x - 10,
                    p2.x - 10,
                    p2.x
                };
                yPath = new int[]{
                    p1.y,
                    p1.y + 10,
                    p1.y + 10,
                    p2.y,
                    p2.y
                };
                crSide1 = IntersectionSide.SOUTH;
                crSide2 = IntersectionSide.WEST;
                break;
            case 3:
                p1 = new Point(from.getX() + from.getXSize() - 10, from.getY() + from.getYSize() + 22);
                p2 = new Point(from.getX() + from.getXSize() + 20, from.getY() + from.getYSize() - 10);
                xPath = new int[]{
                    p1.x,
                    p1.x,
                    p2.x + 10,
                    p2.x + 10,
                    p2.x
                };
                yPath = new int[]{
                    p1.y,
                    p1.y + 10,
                    p1.y + 10,
                    p2.y,
                    p2.y
                };
                crSide1 = IntersectionSide.SOUTH;
                crSide2 = IntersectionSide.EAST;
                break;
            default:
                p1 = new Point(from.getX() + from.getXSize() - 10, from.getY() - 22);
                p2 = new Point(from.getX() + from.getXSize() + 20, from.getY() + 10);
                xPath = new int[]{
                    p1.x,
                    p1.x,
                    p2.x + 10,
                    p2.x + 10,
                    p2.x
                };
                yPath = new int[]{
                    p1.y,
                    p1.y - 10,
                    p1.y - 10,
                    p2.y,
                    p2.y
                };
                crSide1 = IntersectionSide.NORTH;
                crSide2 = IntersectionSide.EAST;
                break;
        }
        // -- Drawing association's graphical representation ----------------------
        if (fromMode == ErdModelReferenceMultiplicity.Category.NONE_OR_ONE || fromMode == ErdModelReferenceMultiplicity.Category.ONE) {
            drawAssociationReferenceOne(g, p1, crSide1, fromMode == ErdModelReferenceMultiplicity.Category.ONE);
            activeZone1 = getActiveZone(p1, crSide1);
        }
        if (fromMode == ErdModelReferenceMultiplicity.Category.NONE_OR_MANY || fromMode == ErdModelReferenceMultiplicity.Category.MANY) {
            drawAssociationReferenceMany(g, p1, crSide1, fromMode == ErdModelReferenceMultiplicity.Category.MANY);
            activeZone1 = getActiveZone(p1, crSide1);
        }
        if (toMode == ErdModelReferenceMultiplicity.Category.NONE_OR_ONE || toMode == ErdModelReferenceMultiplicity.Category.ONE) {
            drawAssociationReferenceOne(g, p2, crSide2, toMode == ErdModelReferenceMultiplicity.Category.ONE);
            activeZone2 = getActiveZone(p2, crSide2);
        }
        if (toMode == ErdModelReferenceMultiplicity.Category.NONE_OR_MANY || toMode == ErdModelReferenceMultiplicity.Category.MANY) {
            drawAssociationReferenceMany(g, p2, crSide2, toMode == ErdModelReferenceMultiplicity.Category.MANY);
            activeZone2 = getActiveZone(p2, crSide2);
        }
        g.drawPolyline(xPath, yPath, xPath.length);

        if (curStroke != null) {
            g2.setStroke(curStroke);
        }

        Rectangle2D title = g.getFont().getStringBounds(
                nameTo,
                new FontRenderContext(g.getFont().getTransform(), false, false));

        switch (paralelNumber) {
            case 1:
                p2.translate(18 - (int) title.getWidth(), 20);
                break;
            case 2:
                p2.translate(18 - (int) title.getWidth(), -7);
                break;
            case 3:
                p2.translate(-18, -7);
                break;
            default:
                p2.translate(-18, 20);
        }
        g.drawString(nameTo, p2.x, p2.y);

        if (isReadyToBeDeleted || isSelected) {
            g.setColor(curColor);
            g.setFont(curFont);
        }
    }

    private Point getStartTextPoint(SchemaRectangleElement obj, Graphics g, String name) {
        int x = 0;
        int y = 0;

        Point cp1;
        Point cp2;

        Point p = getIntersectionPoint(obj);
        translateParalelAssociationStartingPoint(p);
        if (navPoints.isEmpty()) {
            if (obj == from) {
                cp1 = getCenter(from);
                cp2 = getCenter(to);
            } else {
                cp1 = getCenter(to);
                cp2 = getCenter(from);
            }
        } else {
            if (obj == from) {
                cp1 = getCenter(from);
                cp2 = navPoints.get(0);
            } else {
                cp2 = navPoints.get(navPoints.size() - 1);
                cp1 = getCenter(to);
            }
        }

        Rectangle2D title = g.getFont().getStringBounds(name,
                new FontRenderContext(g.getFont().getTransform(), false, false));

        switch (intersectionSide(obj)) {
            case NORTH:
                if (cp1.getX() > cp2.getX()) {
                    x = p.x + 7;
                    y = p.y - 6;
                } else {
                    x = p.x - (int) title.getWidth() - 9;
                    y = p.y - 6;
                }
                break;
            case EAST:
                if (cp1.getY() > cp2.getY()) {
                    x = p.x + 2;
                    y = p.y + (int) title.getHeight() + 3;
                } else {
                    x = p.x + 2;
                    y = p.y - 9;
                }
                break;
            case SOUTH:
                if (cp1.getX() > cp2.getX()) {
                    x = p.x + 7;
                    y = p.y + (int) title.getHeight();
                } else {
                    x = p.x - (int) title.getWidth() - 9;
                    y = p.y + (int) title.getHeight();
                }
                break;
            case WEST:
                if (cp1.getY() > cp2.getY()) {
                    x = p.x - 2 - (int) title.getWidth();
                    y = p.y + 3 + (int) title.getHeight();
                } else {
                    x = p.x - 2 - (int) title.getWidth();
                    y = p.y - 9;
                }
                break;
            default:
                break;
        }
        return new Point(x, y);
    }

    protected void drawAssociationReferenceOne(Graphics g, Point p, IntersectionSide crossSide, boolean mandatory) {
        p = (Point) p.clone();
        switch (crossSide) {
            case NORTH:
                g.drawLine((int) p.getX(), (int) p.getY(), (int) p.getX(), (int) p.getY() + 22);
                if (mandatory) {
                    g.drawLine((int) p.getX() - 5, (int) p.getY(), (int) p.getX() + 5, (int) p.getY());
                } else {
                    drawFilledOval(g, (int) p.getX() - 5, (int) p.getY(), 10, 10);
                }
                p.translate(0, 11);
                g.drawLine((int) p.getX() - 5, (int) p.getY(), (int) p.getX() + 5, (int) p.getY());
                break;
            case EAST:
                g.drawLine((int) p.getX(), (int) p.getY(), (int) p.getX() - 20, (int) p.getY());
                if (mandatory) {
                    g.drawLine((int) p.getX(), (int) p.getY() - 5, (int) p.getX(), (int) p.getY() + 5);
                } else {
                    drawFilledOval(g, (int) p.getX() - 10, (int) p.getY() - 5, 10, 10);
                }
                p.translate(-11, 0);
                g.drawLine((int) p.getX(), (int) p.getY() - 5, (int) p.getX(), (int) p.getY() + 5);
                break;
            case SOUTH:
                g.drawLine((int) p.getX(), (int) p.getY(), (int) p.getX(), (int) p.getY() - 22);
                if (mandatory) {
                    g.drawLine((int) p.getX() - 5, (int) p.getY(), (int) p.getX() + 5, (int) p.getY());
                } else {
                    drawFilledOval(g, (int) p.getX() - 5, (int) p.getY() - 10, 10, 10);
                }
                p.translate(0, -11);
                g.drawLine((int) p.getX() - 5, (int) p.getY(), (int) p.getX() + 5, (int) p.getY());
                break;
            case WEST:
                g.drawLine((int) p.getX(), (int) p.getY(), (int) p.getX() + 20, (int) p.getY());
                if (mandatory) {
                    g.drawLine((int) p.getX(), (int) p.getY() - 5, (int) p.getX(), (int) p.getY() + 5);
                } else {
                    drawFilledOval(g, (int) p.getX(), (int) p.getY() - 5, 10, 10);
                }
                p.translate(11, 0);
                g.drawLine((int) p.getX(), (int) p.getY() - 5, (int) p.getX(), (int) p.getY() + 5);
                break;
            default:
                break;
        }

    }

    protected void drawAssociationReferenceMany(Graphics g, Point p, IntersectionSide crossSide, boolean mandatory) {
        p = (Point) p.clone();
        switch (crossSide) {
            case NORTH:
                g.drawLine((int) p.getX(), (int) p.getY(), (int) p.getX(), (int) p.getY() + 22);
                g.drawLine((int) p.getX(), (int) p.getY() + 10, (int) p.getX() - 5, (int) p.getY() + 22);
                g.drawLine((int) p.getX(), (int) p.getY() + 10, (int) p.getX() + 5, (int) p.getY() + 22);
                if (mandatory) {
                    g.drawLine((int) p.getX() - 5, (int) p.getY(), (int) p.getX() + 5, (int) p.getY());
                } else {
                    drawFilledOval(g, (int) p.getX() - 5, (int) p.getY() + 1, 10, 10);
                }
                break;
            case EAST:
                g.drawLine((int) p.getX(), (int) p.getY(), (int) p.getX() - 20, (int) p.getY());
                g.drawLine((int) p.getX() - 10, (int) p.getY(), (int) p.getX() - 20, (int) p.getY() - 5);
                g.drawLine((int) p.getX() - 10, (int) p.getY(), (int) p.getX() - 20, (int) p.getY() + 5);
                if (mandatory) {
                    g.drawLine((int) p.getX(), (int) p.getY() - 5, (int) p.getX(), (int) p.getY() + 5);
                } else {
                    drawFilledOval(g, (int) p.getX() - 10, (int) p.getY() - 5, 10, 10);
                }
                break;
            case SOUTH:
                g.drawLine((int) p.getX(), (int) p.getY(), (int) p.getX(), (int) p.getY() - 22);
                g.drawLine((int) p.getX(), (int) p.getY() - 10, (int) p.getX() - 5, (int) p.getY() - 22);
                g.drawLine((int) p.getX(), (int) p.getY() - 10, (int) p.getX() + 5, (int) p.getY() - 22);
                if (mandatory) {
                    g.drawLine((int) p.getX() - 5, (int) p.getY(), (int) p.getX() + 5, (int) p.getY());
                } else {
                    drawFilledOval(g, (int) p.getX() - 5, (int) p.getY() - 11, 10, 10);
                }
                break;
            case WEST:
                g.drawLine((int) p.getX(), (int) p.getY(), (int) p.getX() + 20, (int) p.getY());
                g.drawLine((int) p.getX() + 10, (int) p.getY(), (int) p.getX() + 20, (int) p.getY() - 5);
                g.drawLine((int) p.getX() + 10, (int) p.getY(), (int) p.getX() + 20, (int) p.getY() + 5);
                if (mandatory) {
                    g.drawLine((int) p.getX(), (int) p.getY() - 5, (int) p.getX(), (int) p.getY() + 5);
                } else {
                    drawFilledOval(g, (int) p.getX(), (int) p.getY() - 5, 10, 10);
                }
                break;
            default:
                break;
        }

    }

    protected void drawFilledOval(Graphics g, int x, int y, int xs, int ys) {
        Color curColor = g.getColor();
        g.setColor(Color.WHITE);
        g.fillOval(x, y, xs, ys);
        g.setColor(curColor);
        g.drawOval(x, y, xs, ys);
    }

    protected Rectangle2D getActiveZone(Point p, IntersectionSide crossSide) {
        switch (crossSide) {
            case NORTH:
                return new Rectangle((int) p.getX() - 5, (int) p.getY(), 10, 22);
            case EAST:
                return new Rectangle((int) p.getX() - 20, (int) p.getY() - 5, 20, 10);
            case SOUTH:
                return new Rectangle((int) p.getX() - 5, (int) p.getY() - 22, 10, 22);
            case WEST:
                return new Rectangle((int) p.getX(), (int) p.getY() - 5, 20, 10);
        }
        return null;
    }

    protected Point getCenter(SchemaRectangleElement obj) {
        return new Point(obj.getX() + obj.getXSize() / 2, obj.getY() + obj.getYSize() / 2);
    }

    protected IntersectionSide intersectionSide(SchemaRectangleElement obj) {
        Point p1 = new Point(obj.getX(), obj.getY());
        Point p2 = new Point(obj.getX() + obj.getXSize(), obj.getY());
        Point p3 = new Point(obj.getX() + obj.getXSize(), obj.getY() + obj.getYSize());
        Point p4 = new Point(obj.getX(), obj.getY() + obj.getYSize());

        Point cp1;
        Point cp2;
        if (navPoints.isEmpty()) {
            if (obj == from) {
                cp1 = getCenter(from);
                cp2 = getCenter(to);
            } else {
                cp1 = getCenter(to);
                cp2 = getCenter(from);
            }
        } else {
            if (obj == from) {
                cp1 = getCenter(from);
                cp2 = navPoints.get(0);
            } else {
                cp2 = navPoints.get(navPoints.size() - 1);
                cp1 = getCenter(to);
            }
        }

        if (intersectsWith(p1, p2, cp1, cp2)) {
            return IntersectionSide.NORTH;
        } else if (intersectsWith(p2, p3, cp1, cp2)) {
            return IntersectionSide.EAST;
        } else if (intersectsWith(p4, p3, cp1, cp2)) {
            return IntersectionSide.SOUTH;
        } else if (intersectsWith(p1, p4, cp1, cp2)) {
            return IntersectionSide.WEST;
        }
        return IntersectionSide.UNKNOWN;
    }

    protected boolean intersectsWith(Point p1, Point p2, Point pA, Point pB) {
        Point cp = getIntersectionPoint(p1, p2, pA, pB);

        if (cp == null) {
            return false;
        }

        return betweenPoints(pA.getY(), pB.getY(), cp.getY())
                && betweenPoints(pA.getX(), pB.getX(), cp.getX())
                && betweenPoints(p1.getY(), p2.getY(), cp.getY())
                && betweenPoints(p1.getX(), p2.getX(), cp.getX());
    }

    protected boolean betweenPoints(double p1, double p2, double cp) {
        double d1;
        double d2;
        if (p1 >= p2) {
            d1 = p2;
            d2 = p1;
        } else {
            d1 = p1;
            d2 = p2;
        }
        return d1 <= cp && cp <= d2;
    }

    protected Point getIntersectionPoint(SchemaRectangleElement obj) {
        Point p1 = new Point(obj.getX(), obj.getY());
        Point p2 = new Point(obj.getX() + obj.getXSize(), obj.getY());
        Point p3 = new Point(obj.getX() + obj.getXSize(), obj.getY() + obj.getYSize());
        Point p4 = new Point(obj.getX(), obj.getY() + obj.getYSize());

        Point cp1;
        Point cp2;
        if (navPoints.isEmpty()) {
            if (obj == from) {
                cp1 = getCenter(from);
                cp2 = getCenter(to);
            } else {
                cp1 = getCenter(to);
                cp2 = getCenter(from);
            }
        } else {
            if (obj == from) {
                cp1 = getCenter(from);
                cp2 = navPoints.get(0);
            } else {
                cp2 = navPoints.get(navPoints.size() - 1);
                cp1 = getCenter(to);
            }
        }

        Point res = null;
        if (intersectsWith(p1, p2, cp1, cp2)) {
            res = getIntersectionPoint(p1, p2, cp1, cp2);
        } else if (intersectsWith(p2, p3, cp1, cp2)) {
            res = getIntersectionPoint(p2, p3, cp1, cp2);
        } else if (intersectsWith(p4, p3, cp1, cp2)) {
            res = getIntersectionPoint(p4, p3, cp1, cp2);
        } else if (intersectsWith(p1, p4, cp1, cp2)) {
            res = getIntersectionPoint(p1, p4, cp1, cp2);
        }
        return res;
    }

    protected Point getIntersectionPoint(Point p1, Point p2, Point pA, Point pB) {
        if (Math.abs(pB.getX() - pA.getX()) > 0.000001) {       // pB.getX() != pA.getX()
            double k = (pB.getY() - pA.getY()) / (pB.getX() - pA.getX());
            double l = (pA.getY() * pB.getX() - pB.getY() * pA.getX()) / (pB.getX() - pA.getX());

            if (p1.getX() == p2.getX()) {
                double y = p1.getX() * k + l;
                return new Point((int) p1.getX(), (int) y);
            } else if (p1.getY() == p2.getY()) {
                if (k != 0) {
                    double x = (p1.getY() - l) / k;
                    return new Point((int) x, (int) p1.getY());
                }
                return new Point((int) pA.getX(), (int) p1.getY());
            }
        } else {
            if (p1.getX() == p2.getX()) {
                return null;    // This is the sime line or it don't have any common points
            } else if (p1.getY() == p2.getY()) {
                return new Point((int) pA.getX(), (int) p1.getY());
            }
        }
        return null;
    }

    protected boolean isAboveLine(Point cp, Point pFrom, Point pTo) {

        if (cp == null || pFrom == null || pTo == null) {
            return false;
        }

        int curX = cp.x;
        int curY = cp.y;

        int x1 = pFrom.x;
        int y1 = pFrom.y;
        int x2 = pTo.x;
        int y2 = pTo.y;

        if (x1 > x2) {
            x1 = pTo.x;
            x2 = pFrom.x;
        }
        if (y1 > y2) {
            y1 = pTo.y;
            y2 = pFrom.y;
        }

        if (curX >= x1 - LINE_SELECTION_RADIUS && curX <= x2 + LINE_SELECTION_RADIUS && curY >= y1 - LINE_SELECTION_RADIUS && curY <= y2 + LINE_SELECTION_RADIUS) {

            if (x2 == x1 || Math.abs(x2 - x1) <= LINE_SELECTION_RADIUS * 2) {
                return curX >= x1 - LINE_SELECTION_RADIUS && curX <= x1 + LINE_SELECTION_RADIUS;
            }

            x1 = pFrom.x;
            y1 = pFrom.y;
            x2 = pTo.x;
            y2 = pTo.y;

            double k = ((double) y2 - y1) / (x2 - x1);
            double l = y2 - k * x2;
            double pX = cp.x;
            double pY = k * curX + l;

            double r = Math.sqrt(Math.pow(pX - cp.x, 2) + Math.pow(pY - cp.y, 2));

            if (r <= LINE_SELECTION_RADIUS) {
                return true;
            }
        }

        return false;
    }

    protected Point getStartPoint() {
        //return getCenter(from);
        int x = 0;
        int y = 0;

        Point p = getIntersectionPoint(from);

        switch (fromCross = intersectionSide(from)) {
            case NORTH:
                p.translate(0, -22);
                break;
            case EAST:
                p.translate(20, 0);
                break;
            case SOUTH:
                p.translate(0, 22);
                break;
            case WEST:
                p.translate(-20, 0);
                break;
            default:
                break;
        }
        // -- Moving start point if it is a paralel association -----------------------
        translateParalelAssociationStartingPoint(p);
        // -- Returing point found ----------------------------------------------------
        return p;
    }

    protected Point getFinalPoint() {
        int x = 0;
        int y = 0;

        Point p = getIntersectionPoint(to);

        switch (toCross = intersectionSide(to)) {
            case NORTH:
                p.translate(0, -22);
                break;
            case EAST:
                p.translate(20, 0);
                break;
            case SOUTH:
                p.translate(0, 22);
                break;
            case WEST:
                p.translate(-20, 0);
                break;
            default:
                break;
        }
        // -- Moving start point if it is a paralel association ------------------------
        translateParalelAssociationStartingPoint(p);
        return p;
    }

    private void translateParalelAssociationStartingPoint(Point p) {
        if (paralelNumber > 0) {
            int shift = MULTI_ASSOCIATION_SPACE * ((paralelNumber + 1) / 2) * (paralelNumber % 2 == 0 ? -1 : 1);
            switch (toCross = intersectionSide(to)) {
                case NORTH:
                case SOUTH:
                    p.translate(shift, 0);
                    break;
                case EAST:
                case WEST:
                    p.translate(0, shift);
                    break;
                default:
                    break;
            }
        }
    }

    // -----------------------------------------------------------------------------------
    // =========================== Mouse events processing ===============================
    // -----------------------------------------------------------------------------------
    @Override
    public boolean processMouseEvent(JFrame appFrame, MouseEvent ev) {
        if (activeZone1 == null || activeZone2 == null) {
            return false;
        }
        int curX = (int) (ev.getX() / scale);
        int curY = (int) (ev.getY() / scale);
        ////////////////////////////////////////////////////////////
        // Here we processing name of our obejct
        if (activeZone1.contains(curX, curY) || activeZone2.contains(curX, curY) || isSelected) {
            AssociationDialog dl = new AssociationDialog(appFrame, true);
            dl.setAssociation(this);
            dl.setVisible(true);
            return true;
        }
        return false;
    }

    public boolean processMouseDelete(MouseEvent ev) {
        if (activeZone1 == null || activeZone2 == null) {
            return false;
        }
        int curX = (int) (ev.getX() / scale);
        int curY = (int) (ev.getY() / scale);
        ////////////////////////////////////////////////////////////
        // Here we are processing field names of the our association
        if (activeZone1.contains(curX, curY) || activeZone2.contains(curX, curY)) {
            return true;
        }
        ///////////////////////////////////////////////////////////
        // Here we are processing our arrow image
        Point pFrom = getStartPoint();
        Point pTo = getFinalPoint();

        if (isAssociationToItself()) {
            return false;
        }

        if (navPoints.isEmpty()) {
            return isAboveLine(new Point(curX, curY), pFrom, pTo);
        } else {
            Point curPoint = pFrom;
            Point tp = new Point(curX, curY);
            for (Point p : navPoints) {
                if (isAboveLine(tp, curPoint, p)) {
                    return true;
                }
                curPoint = p;
            }
            if (isAboveLine(tp, curPoint, pTo)) {
                return true;
            }
        }
        return false;
    }

    public boolean processMouseStartDragging(Point scp) {
        Point cp = new Point((int) (scp.getX() / scale), (int) (scp.getY() / scale));
        ///////////////////////////////////////////////////////////////////////////
        // The first of all we have to check nav points areas
        for (int i = 0; i < navPoints.size(); i++) {
            Point p = navPoints.get(i);
            Rectangle r = new Rectangle(p.x - 2, p.y - 2, 4, 4);
            if (r.contains(cp)) {
                dragMode = DRAG_POINT;
                dragPointNum = i;
                return true;
            }
        }

        if (isAssociationToItself()) {
            return false;
        }

        ///////////////////////////////////////////////////////////////////////////
        // if it is not any nav point are we have to check beams of the association
        Point pFrom = getStartPoint();
        Point pTo = getFinalPoint();

        if (navPoints.isEmpty()) {
            if (isAboveLine(cp, pFrom, pTo)) {
                navPoints.add(new Point(cp));
                dragMode = DRAG_POINT;
                dragPointNum = 0;
                return true;
            }
        } else {
            Point curPoint = pFrom;
            for (int i = 0; i < navPoints.size(); i++) {
                Point p = navPoints.get(i);
                if (isAboveLine(cp, curPoint, p)) {
                    navPoints.add(i, new Point(cp));
                    dragMode = DRAG_POINT;
                    dragPointNum = i;
                    return true;
                }
                curPoint = p;
            }
            if (isAboveLine(cp, curPoint, pTo)) {
                navPoints.add(new Point(cp));
                dragMode = DRAG_POINT;
                dragPointNum = navPoints.size() - 1;
                return true;
            }
        }
        return false;
    }

    public void processMouseDragged(Point p) {
        if (dragMode == DRAG_POINT) {
            Point np = navPoints.get(dragPointNum);
            np.setLocation(p);
        }
    }

    // -----------------------------------------------------------------------------------
    // ============================ Instance persistance =================================
    // -----------------------------------------------------------------------------------
    @Override
    public void addTo(Schema schema) {
        Entity classFrom = getFromObject() instanceof Entity ? (Entity) getFromObject() : null;
        Entity classTo = getToObject() instanceof Entity ? (Entity) getToObject() : null;
        // --------------------------------------------------------------------------------
        List<NamedElement> entities = schema.getElements(new Qualifier<>(new ErdModelEntity()));
        Map<String, org.vrsl.jet.models.cim.Class> erdObjects = new HashMap<>();
        for (NamedElement e : entities) {
            if (e instanceof org.vrsl.jet.models.cim.Class) {
                erdObjects.put(e.getName(), (org.vrsl.jet.models.cim.Class) e);
            }
        }
        // --------------------------------------------------------------------------------
        assert (classFrom != null && classTo != null);
        org.vrsl.jet.models.cim.Class c1 = erdObjects.get(classFrom.getName());
        org.vrsl.jet.models.cim.Class c2 = erdObjects.get(classTo.getName());
        assert (c1 != null && c2 != null);
        // --------------------------------------------------------------------------------
        Reference r1 = ErdModelFactory.buildReference(c1, new ErdModelReferenceMultiplicity(getFromMode()));
        Reference r2 = ErdModelFactory.buildReference(c2, new ErdModelReferenceMultiplicity(getToMode()));
        assert (r1 != null && r2 != null);
        if (!CommonUtils.isNullOrEmpty(getNameFrom())) {
            r1.add(new Qualifier<>(new ErdModelReferenceSuggestedName(getNameFrom())));
        }
        if (!CommonUtils.isNullOrEmpty(getNameTo())) {
            r2.add(new Qualifier<>(new ErdModelReferenceSuggestedName(getNameTo())));
        }
        // --------------------------------------------------------------------------------
        Association ea = ErdModelFactory.buildAssociation(r1, r2);
        ea.add(new Qualifier<>(new ErdModelEntityAssociation()));
        ea.add(new Qualifier<>(new ErdModelPath(getPath())));
        // --------------------------------------------------------------------------------
        schema.add(ea);
    }

    // ----------------------------------------------------------------------------------------------------------
    public enum IntersectionSide {

        NORTH, EAST, SOUTH, WEST, UNKNOWN
    }
}
