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
package org.vrsl.jet.ui.components;

import java.text.DecimalFormat;
import java.text.ParseException;
import javax.swing.JFormattedTextField;
import javax.swing.text.NumberFormatter;

public class JIntegerTextField extends JFormattedTextField {

    private NumberFormatter nf = null;

    public JIntegerTextField() {
        super();
        DecimalFormat df = new DecimalFormat("####");
        nf = new NumberFormatter(df) {
            @Override
            public String valueToString(Object iv) throws ParseException {
                if ((iv == null) || (((Number) iv).intValue() == -1)) {
                    return "";
                }
                return super.valueToString(iv);
            }

            @Override
            public Object stringToValue(String text) throws ParseException {
                if ("".equals(text)) {
                    return null;
                }
                return super.stringToValue(text);
            }
        };
        nf.setValueClass(Integer.class);
        this.setFormatter(nf);
    }

    public void setMinimum(int min) {
        nf.setMaximum(min);
    }

    public void setMaximum(int max) {
        nf.setMaximum(max);
    }
}
