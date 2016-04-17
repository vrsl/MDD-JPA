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

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.font.FontRenderContext;
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
import org.vrsl.jet.models.erd.variants.entities.ErdModelText;

public final class FreeText extends SchemaText {

    static final long serialVersionUID = 92384798370003L;
    private static int cnt = 1;
    static final Color pointedModeInstnaceColor = new Color(230, 230, 230);
    static final Color deleteModeInstnaceColor = new Color(255, 95, 95);

    private static int getNextItemNumber() {
        return ++cnt;
    }
    private List<String> cont = null;
    // -----------------------------------------------------------------------------------
    // ================================== Rendering ======================================
    // -----------------------------------------------------------------------------------
    private int fontVSize = 0;

    /**
     * Creates a new instance of FreeText
     */
    public FreeText() {
    }

    public FreeText(int x, int y) {
        setText("Text " + getNextItemNumber());
        setFont("Arial", 14, Font.PLAIN);
        setX(x);
        setY(y);
        cont = new ArrayList<>();
        cont.add(getText());
        setColor(new Color(0, 0, 0));
    }
    public FreeText(NamedElement ce) throws NamedElementNotFoundException {
        if (!(ce instanceof org.vrsl.jet.models.cim.Class)) {
            throw new IllegalArgumentException("Wrong type of schema " + ce.getName() + " element : " + ce.getClass().getName());
        }

        ErdModelLocation location = ce.<ErdModelLocation>getFirstByVariantType(ErdModelLocation.class).getValue();
        ErdModelText txt = ce.<ErdModelText>getFirstByVariantType(ErdModelText.class).getValue();

        setX(location.getLoaction().x);
        setY(location.getLoaction().y);

        ErdModelFont font = ce.<ErdModelFont>getFirstByVariantType(ErdModelFont.class).getValue();
        ErdModelColor color = ce.<ErdModelColor>getFirstByVariantType(ErdModelColor.class).getValue();
        setFont(font.getFontName(), font.getFontSize(), font.getFontType());
        setColor(new Color(color.getcR(), color.getcG(), color.getcB()));
        
        
        setText(txt.toString());
        cont = new ArrayList<>();
        cont.add(getText());
    }

    // -----------------------------------------------------------------------------------
    // ======================= Internal data and schema relations ========================
    // -----------------------------------------------------------------------------------
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
    public boolean setAssociationSelected(boolean isTrue) {
        return false;
    }

    @Override
    public void draw(Graphics g) {

        Color pColor = g.getColor();

        Font curFont = g.getFont();

        if (isPointed()) {
            g.setColor(pointedModeInstnaceColor);
            g.fillRect(getX(), getY(), getXSize(), getYSize());
            g.setColor(pColor);
        } else if (isReadyToBeDeleted()) {
            g.setColor(deleteModeInstnaceColor);
            g.fillRect(getX(), getY(), getXSize(), getYSize());
            g.setColor(pColor);
        }

        g.setFont(boldFont);
        g.setColor(getColor());

        for (int i = 0; i < cont.size(); i++) {
            String str = cont.get(i);
            g.drawString(str, getX(), getY() + i * fontVSize + fontVSize);
            if ((getFontType() & 256) != 0) {
                Rectangle2D fld = boldFont.getStringBounds(str,
                        new FontRenderContext(boldFont.getTransform(), false, false));
                int newHSize = (int) fld.getWidth();
                g.drawLine(getX(), getY() + i * fontVSize + fontVSize + 1, getX() + newHSize, getY() + i * fontVSize + fontVSize + 1);
            }
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
            Rectangle2D fld = boldFont.getStringBounds(fl,
                    new FontRenderContext(boldFont.getTransform(), false, false));
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
        return false;
    }

    // -----------------------------------------------------------------------------------
    // ============================ Instance persistance =================================
    // -----------------------------------------------------------------------------------
    @Override
    public void addTo(Schema schema) {
        org.vrsl.jet.models.cim.Class tc = ErdModelFactory.buildClass();
        tc.add(new Qualifier<>(new ErdModelText(getText())));
        tc.add(new Qualifier<>(new ErdModelLocation(getX(), getY())));
        tc.add(new Qualifier<>(new ErdModelSize(getXSize(), getYSize())));
        tc.add(new Qualifier<>(new ErdModelFont(getFontName(), getFontSize(), getFontType())));
        tc.add(new Qualifier<>(new ErdModelColor(getColor().getRed(), getColor().getGreen(), getColor().getBlue())));
        schema.add(tc);
    }
}
