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
import java.awt.geom.Line2D;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openide.util.Exceptions;
import org.vrsl.jet.modeller.erd.editor.schema.view.SchemaRectangleElement;
import org.vrsl.jet.modeller.erd.editor.schema.view.elements.Entity;
import org.vrsl.jet.models.cim.Association;
import org.vrsl.jet.models.cim.NamedElement;
import org.vrsl.jet.models.cim.NamedElementNotFoundException;
import org.vrsl.jet.models.cim.Qualifier;
import org.vrsl.jet.models.cim.Reference;
import org.vrsl.jet.models.cim.Schema;
import org.vrsl.jet.models.erd.ErdModelFactory;
import org.vrsl.jet.models.erd.variants.associations.ErdModelTextCommentAssociation;
import org.vrsl.jet.models.erd.variants.details.ErdModelPath;

public class CommentAssociation extends ElementsAssociation {

    final static float smDash1[] = {5.0f};
    final static BasicStroke smDashed = new BasicStroke(1.0f,
            BasicStroke.CAP_BUTT,
            BasicStroke.JOIN_MITER,
            10.0f, smDash1, 0.0f);
    final static BasicStroke smDoubleDashed = new BasicStroke(2.0f,
            BasicStroke.CAP_BUTT,
            BasicStroke.JOIN_MITER,
            10.0f, smDash1, 0.0f);
    private static Font boldFont = null;

    /**
     * Creates a new instance of ComentAssociation
     */
    public CommentAssociation() {
    }

    public CommentAssociation(List<Entity> objects, List<SchemaRectangleElement> texts, NamedElement ce) throws NamedElementNotFoundException {
        if (!(ce instanceof Association)) {
            throw new IllegalArgumentException("Wrong type of schema " + ce.getName() + " element : " + ce.getClass().getName());
        }

        Association a = (Association) ce;

        Reference refFr = a.getReferences().get(0);
        Reference refTo = a.getReferences().get(1);

        String frName = refFr.getReferedClass().getName();
        String toName = refTo.getReferedClass().getName();

        from = findByName(objects, frName);
        if (from == null) {
            from = findByName(texts, frName);
        }
        to = findByName(objects, toName);
        if (to == null) {
            to = findByName(texts, toName);
        }

        try {
            ErdModelPath path = a.<ErdModelPath>getFirstByVariantType(ErdModelPath.class).getValue();
            navPoints.addAll(path.get());
        } catch (NamedElementNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    protected Point getStartPoint() {
        return getCenter(from);
    }

    @Override
    protected Point getFinalPoint() {
        return getCenter(to);
    }

    @Override
    public void draw(Graphics g) {
        Point p1 = getStartPoint();
        Point p2 = getFinalPoint();

        if (!(g instanceof Graphics2D)) {
            return;
        }

        Graphics2D g2 = (Graphics2D) g;

        Font curFont = g.getFont();
        if (boldFont == null) {
            boldFont = new Font(curFont.getFontName(), Font.BOLD, curFont.getSize());
        }

        Color cyrColor = g.getColor();
        if (isReadyToBeDeleted) {
            g.setColor(readyToDeleteColor);
            g.setFont(boldFont);
        }
        if (isSelected) {
            g.setFont(boldFont);
        }

        if (navPoints.isEmpty()) {
            Stroke curStroke = g2.getStroke();
            if (isReadyToBeDeleted || isSelected) {
                g2.setStroke(smDoubleDashed);
                g2.draw(new Line2D.Double((int) p1.getX(), (int) p1.getY(), (int) p2.getX(), (int) p2.getY()));
            } else {
                g2.setStroke(smDashed);
                g.drawLine((int) p1.getX(), (int) p1.getY(), (int) p2.getX(), (int) p2.getY());
            }
            g2.setStroke(curStroke);
        } else {
            Point curPoint = p1;
            Stroke curStroke = g2.getStroke();
            if (isSelected || isReadyToBeDeleted) {
                g2.setStroke(smDoubleDashed);
            } else {
                g2.setStroke(smDashed);
            }
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
            if (g2.getStroke() != curStroke) {
                g2.setStroke(curStroke);
            }
        }

        ////////////////////////////////////////////////////////////////////////
        if (isReadyToBeDeleted || isSelected) {
            g.setColor(cyrColor);
            g.setFont(curFont);
        }
    }

    @Override
    public boolean processMouseDelete(MouseEvent ev) {
        int curX = (int) (ev.getX() / scale);
        int curY = (int) (ev.getY() / scale);

        ////////////////////////////////////////////////////////
        // Here we are processing our arrow image
        Point pFrom = getIntersectionPoint(from);
        Point pTo = getIntersectionPoint(to);

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

    @Override
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

        ///////////////////////////////////////////////////////////////////////////
        // if it is not any nav point are we have to check beams of the association
        Point pFrom = getIntersectionPoint(from);
        Point pTo = getIntersectionPoint(to);

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

    @Override
    public void addTo(Schema schema) {
        String classFromName = getFromObject() instanceof Entity ? getFromObject().getName() : getFromObject().getName();
        String classToName = getToObject() instanceof Entity ? getToObject().getName() : getToObject().getName();
        // --------------------------------------------------------------------------------
        List<NamedElement> entities = schema.getElements();
        Map<String, org.vrsl.jet.models.cim.Class> erdObjects = new HashMap<>();
        for (NamedElement e : entities) {
            if (e instanceof org.vrsl.jet.models.cim.Class) {
                erdObjects.put(e.getName(), (org.vrsl.jet.models.cim.Class) e);
            }
        }
        // --------------------------------------------------------------------------------
        org.vrsl.jet.models.cim.Class c1 = erdObjects.get(classFromName);
        org.vrsl.jet.models.cim.Class c2 = erdObjects.get(classToName);
        // --------------------------------------------------------------------------------
        Reference r1 = ErdModelFactory.buildReference(c1);
        Reference r2 = ErdModelFactory.buildReference(c2);
        // --------------------------------------------------------------------------------
        Association ea = ErdModelFactory.buildAssociation(r1, r2);
        ea.add(new Qualifier<>(new ErdModelTextCommentAssociation()));
        ea.add(new Qualifier<>(new ErdModelPath(getPath())));
        // --------------------------------------------------------------------------------
        schema.add(ea);
    }
}
