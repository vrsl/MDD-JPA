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
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.font.FontRenderContext;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;
import org.vrsl.jet.modeller.erd.editor.schema.dialogs.SchemaTextDialog;
import org.vrsl.jet.modeller.erd.editor.schema.view.SchemaText;
import org.vrsl.jet.models.cim.NamedElement;
import org.vrsl.jet.models.cim.NamedElementNotFoundException;
import org.vrsl.jet.models.cim.Qualifier;
import org.vrsl.jet.models.cim.Schema;
import org.vrsl.jet.models.erd.ErdModelFactory;
import org.vrsl.jet.models.erd.variants.details.ErdModelColor;
import org.vrsl.jet.models.erd.variants.details.ErdModelFont;
import org.vrsl.jet.models.erd.variants.details.ErdModelLocation;
import org.vrsl.jet.models.erd.variants.details.ErdModelSize;
import org.vrsl.jet.models.erd.variants.entities.ErdModelTextualComment;

public final class Comment extends SchemaText {

    static final long serialVersionUID = 92384798370004L;
    private static int cnt = 1;
    // -----------------------------------------------------------
    final static BasicStroke doubleStroke = new BasicStroke(2.0f);
    static final Color pointedModeInstnaceColor = new Color(230, 230, 230);
    static final Color deleteModeInstnaceColor = new Color(255, 95, 95);
    static final Color defaultModeInstnaceColor = new Color(255, 255, 255);

    private static int getNextItemNumber() {
        return ++cnt;
    }
    private List<String> cont = null;
    private boolean associationSelected;
    // -----------------------------------------------------------
    private transient FontRenderContext fontRenderContext = null;


    // -----------------------------------------------------------------------------------
    // ================================== Rendering ======================================
    // -----------------------------------------------------------------------------------
    private int fontVSize = 0;
    private final GeneralPath filledPolygon = new GeneralPath(GeneralPath.WIND_EVEN_ODD, 6);

    /**
     * Creates a new instance of JScreenText
     */
    public Comment() {
    }

    public Comment(int x, int y) {
        this.setX(x);
        this.setY(y);
        setText("Text " + getNextItemNumber());
        setFont("Arial", 14, Font.PLAIN);
        cont = new ArrayList<>();
        cont.add(getText());
        setColor(new Color(0, 0, 0));
        fontRenderContext = new FontRenderContext(boldFont.getTransform(), false, false);
        makeName();
    }

    public Comment(NamedElement ce) throws NamedElementNotFoundException {
        if (!(ce instanceof org.vrsl.jet.models.cim.Class)) {
            throw new IllegalArgumentException("Wrong type of schema " + ce.getName() + " element : " + ce.getClass().getName());
        }

        setName(ce.getName());

        ErdModelLocation location = ce.<ErdModelLocation>getFirstByVariantType(ErdModelLocation.class).getValue();
        ErdModelTextualComment txt = ce.<ErdModelTextualComment>getFirstByVariantType(ErdModelTextualComment.class).getValue();

        setX(location.getLoaction().x);
        setY(location.getLoaction().y);

        ErdModelFont font = ce.<ErdModelFont>getFirstByVariantType(ErdModelFont.class).getValue();
        ErdModelColor color = ce.<ErdModelColor>getFirstByVariantType(ErdModelColor.class).getValue();
        setFont(font.getFontName(), font.getFontSize(),font.getFontType());
        setColor(new Color(color.getcR(), color.getcG(), color.getcB()));

        fontRenderContext = new FontRenderContext(boldFont.getTransform(), false, false);
        setText(txt.toString());
    }
    // -----------------------------------------------------------------------------------
    // ======================= Internal data and schema relations ========================
    // -----------------------------------------------------------------------------------
    private void makeName() {
        setName("comment" + System.currentTimeMillis());
    }
    
    @Override
    public void setText(String text) {
        super.setText(text);
        cont = new ArrayList<>();

        StringBuffer buf = new StringBuffer();

        int i = 0;

        for (int k = 0; k < text.length(); k++) {
            char ch = text.charAt(k);
            if (ch != '\n') {
                buf.append(ch);
            } else {
                cont.add(buf.toString());
                buf = new StringBuffer();
            }
        }
        cont.add(buf.toString()); // And we must store the last one
    }
    
    @Override
    public void draw(Graphics g) {

        int connerSize = 12;

        if (!(g instanceof Graphics2D)) {
            return;
        }

        Graphics2D g2 = (Graphics2D) g;

        Color pColor = g.getColor();

        Font curFont = g.getFont();

        filledPolygon.reset();

        filledPolygon.moveTo(getX(), getY());
        filledPolygon.lineTo(getX() + getXSize() - connerSize, getY());
        filledPolygon.lineTo(getX() + getXSize(), getY() + connerSize);
        filledPolygon.lineTo(getX() + getXSize(), getY() + getYSize());
        filledPolygon.lineTo(getX(), getY() + getYSize());
        filledPolygon.lineTo(getX(), getY());

        filledPolygon.closePath();
        Paint p = g2.getPaint();

        if (isPointed()) {
            g2.setPaint(pointedModeInstnaceColor);
        } else if (isReadyToBeDeleted()) {
            g2.setPaint(deleteModeInstnaceColor);
        } else {
            g2.setPaint(defaultModeInstnaceColor);
        }

        g2.fill(filledPolygon);
        g2.setPaint(p);

        Stroke curStroke = g2.getStroke();
        if (selected || associationSelected) {
            g2.setStroke(doubleStroke);
        }

        g.drawLine(getX(), getY(), getX() + getXSize() - connerSize, getY());
        g.drawLine(getX(), getY(), getX(), getY() + getYSize());
        g.drawLine(getX(), getY() + getYSize(), getX() + getXSize(), getY() + getYSize());
        g.drawLine(getX() + getXSize(), getY() + getYSize(), getX() + getXSize(), getY() + connerSize);
        g.drawLine(getX() + getXSize() - connerSize, getY(), getX() + getXSize(), getY() + connerSize);

        g2.setStroke(curStroke);

        g.setFont(boldFont);
        g.setColor(getColor());

        for (int i = 0; i < cont.size(); i++) {
            String str = cont.get(i);
            g.drawString(str, getX() + 10, getY() + i * fontVSize + fontVSize + 5);
            /*
            if ((getFontType() & 256) != 0) {
            Rectangle2D fld = boldFont.getStringBounds(str, fontRenderContext);
            int newHSize = (int) fld.getWidth();
            g.drawLine(getX(), getY() + i * fontVSize + fontVSize + 1, getX() + newHSize, getY() + i * fontVSize + fontVSize + 1);
            }
            */ 
        }

        g.setFont(curFont);
        g.setColor(pColor);
    }

    @Override
    public void calculateBoxMetrics(Graphics g) {

        fontVSize = 0;
        setXSize(0);
        setYSize(0);

        for (String fl : cont) {
            Rectangle2D fld = boldFont.getStringBounds(fl, fontRenderContext);
            int newSize = (int) fld.getWidth();
            int newFontVSize = (int) fld.getHeight();

            if (newSize > getXSize()) {
                setXSize(newSize);
            }
            if (newFontVSize > fontVSize) {
                fontVSize = newFontVSize;
            }
        }

        setYSize((int) (cont.size() * fontVSize + fontVSize * 0.3));

        setYSize(getYSize() + 10);
        setXSize(getXSize() + 20);
    }

    @Override
    public void shift(int shiftX, int shiftY) {
        setX(getX() + shiftX);
        setY(getY() + shiftY);
    }
    // -----------------------------------------------------------------------------------
    // =========================== Mouse events processing ===============================
    // -----------------------------------------------------------------------------------
    @Override
    public boolean processMouseEvent(JFrame appFrame, MouseEvent ev) {
        int curX = (int) (ev.getX() / scale);
        int curY = (int) (ev.getY() / scale);
        ////////////////////////////////////////////////////////
        // Here we processing name of our obejct
        if (curX >= getX() && curX <= getX() + getXSize()
                && curY >= getY() && curY <= getY() + getYSize()) {

            SchemaTextDialog dl = new SchemaTextDialog(appFrame, true);
            dl.setData(this);
            dl.setVisible(true);

            if (dl.isUpdated()) {
                fontRenderContext = new FontRenderContext(boldFont.getTransform(), false, false);
                setDirty(true);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean isOwnedAreaRelated(MouseEvent ev) {
        int curX = (int) (ev.getX() / scale);
        int curY = (int) (ev.getY() / scale);
        return curX >= getX() && curX <= getX() + getXSize()
                && curY >= getY() && curY <= getY() + getYSize();
    }

    @Override
    public boolean isMovingAreaRelated(MouseEvent ev) {
        int curX = (int) (ev.getX() / scale);
        int curY = (int) (ev.getY() / scale);
        return curX >= getX() && curX <= getX() + getXSize()
                && curY >= getY() && curY <= getY() + 2;
    }

    @Override
    public boolean isAssociationAreaRelated(MouseEvent ev) {
        return isOwnedAreaRelated(ev);
    }
    
    @Override
    public boolean setAssociationSelected(boolean isTrue) {
        associationSelected = isTrue;
        return isTrue;
    }

    // -----------------------------------------------------------------------------------
    // ============================ Instance persistance =================================
    // -----------------------------------------------------------------------------------
    @Override
    public void addTo(Schema schema) {
        org.vrsl.jet.models.cim.Class tc = ErdModelFactory.buildClass(getName());
        tc.add(new Qualifier<>(new ErdModelTextualComment(getText())));
        tc.add(new Qualifier<>(new ErdModelLocation(getX(), getY())));
        tc.add(new Qualifier<>(new ErdModelSize(getXSize(), getYSize())));
        tc.add(new Qualifier<>(new ErdModelFont(getFontName(), getFontSize(), getFontType())));
        tc.add(new Qualifier<>(new ErdModelColor(getColor().getRed(), getColor().getGreen(), getColor().getBlue())));
        schema.add(tc);
    }
}
