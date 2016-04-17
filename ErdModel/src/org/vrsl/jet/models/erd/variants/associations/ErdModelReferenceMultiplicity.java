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
package org.vrsl.jet.models.erd.variants.associations;

import org.vrsl.jet.models.erd.ErdModelVariant;

public class ErdModelReferenceMultiplicity extends ErdModelVariant {

    private Category category;

    public ErdModelReferenceMultiplicity() {
    }

    public ErdModelReferenceMultiplicity(Category category) {
        this.category = category;
    }

    public ErdModelReferenceMultiplicity(int numCategory) {
        switch (numCategory) {
            case 0:
                this.category = Category.NONE_OR_ONE;
                break;
            case 1:
                this.category = Category.ONE;
                break;
            case 2:
                this.category = Category.NONE_OR_MANY;
                break;
            case 3:
                this.category = Category.MANY;
                break;
            default:
                throw new IllegalArgumentException("Can't map a category for value " + numCategory);
        }
    }

    public Category getCategory() {
        return category;
    }
    
    public int getCategoryAsNumber() {
        switch(category) {
            case NONE_OR_ONE:
                return 0;
            case ONE:
                return 1;
            case NONE_OR_MANY:
                return 2;
            case MANY:
                return 3;
        }
        return -1;
    }
    
    public String getCategoryAsString() {   // TODO: Buld enum class right to replace this
        switch(category) {
            case NONE_OR_ONE:
                return "NONE-OR-ONE";
            case ONE:
                return "ONE";
            case NONE_OR_MANY:
                return "NONE-OR-MANY";
            case MANY:
                return "MANY";
        }
        return "UNKNOWN";
    }

    @Override
    public String toString() {
        return "" + getCategoryAsString();
    }

    @Override
    public void initFormText(String text) {
        switch (text) {
            case "NONE-OR-ONE":
                category = Category.NONE_OR_ONE;
                break;
            case "ONE":
                category = Category.ONE;
                break;
            case "NONE-OR-MANY":
                category = Category.NONE_OR_MANY;
                break;
            case "MANY":
                category = Category.MANY;
                break;
        }
    }

    public enum Category {

        NONE_OR_ONE, ONE, NONE_OR_MANY, MANY
    }
}
