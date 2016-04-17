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
package org.vrsl.jet.modeller.erd.editor.schema.view;

import java.awt.Color;
import java.awt.Font;
import java.awt.font.TextAttribute;
import java.util.Map;

abstract public class SchemaText extends SchemaRectangleElement {

    private String text = null;
    private String fontName = null;
    private int fontSize = 0;
    private int fontType = 0;
    private Color theTextColor = null;
    protected Font boldFont;
    
    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void setFont(String fName, int sz, int type) {
        fontName = fName;
        fontSize = sz;
        fontType = type;
        boldFont = new Font(fontName, fontType & 255, fontSize);
        if ((fontType & 256) != 0) 
        {
            Map attributes = boldFont.getAttributes();
            attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
            boldFont = new Font(attributes);
        }
    }

    public String getFontName() {
        return fontName;
    }

    public int getFontSize() {
        return fontSize;
    }

    public int getFontType() {
        return fontType;
    }

    public void setColor(Color cl) {
        theTextColor = cl;
    }

    public Color getColor() {
        return theTextColor;
    }
}
