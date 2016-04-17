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

import java.awt.event.MouseEvent;
import java.io.Serializable;

abstract public class SchamaClass extends SchemaRectangleElement implements Serializable {

    // -- Statuses ----------------------------------------------------------------------
    private boolean isActiveTitle = false;
    private boolean isActiveField = false;
    private boolean hasReadyToBeDeletedField = false;
    private int activeField = -1;
    private int selectedField = -1;
    private int newActiveField = -1;

    // -----------------------------------------------------------------------------------
    // ================================== Statuses =======================================
    // -----------------------------------------------------------------------------------
    @Override
    public void setDefaultView() {
        super.setDefaultView();
        if (!isChanged()) {
            setChanged(isActiveTitle || isActiveField || hasReadyToBeDeletedField);
        }
        isActiveTitle = false;

        isActiveField = false;
        hasReadyToBeDeletedField = false;

        selectedField = activeField = newActiveField = -1;
    }

    @Override
    public void setReadyToBeDeleted() {
        super.setReadyToBeDeleted();
        if (hasReadyToBeDeletedField) {
            hasReadyToBeDeletedField = false;
        }
    }

    @Override
    public void setPointed() {
    }

    public void setActiveTitle() {
        setChanged(!isActiveTitle);
        if (isActiveField) {
            isActiveField = false;
        }
        isActiveTitle = true;
    }

    protected boolean isActiveTitle() {
        return isActiveTitle;
    }

    public void setActiveFeild() {
        setChanged(!isActiveField);
        setChanged(activeField != newActiveField);
        if (isActiveTitle) {
            isActiveTitle = false;
        }
        activeField = newActiveField;
        isActiveField = true;
    }

    protected boolean isActiveField() {
        return isActiveField;
    }

    public void setFieldToBeDeleted() {
        setChanged(!hasReadyToBeDeletedField);
        setChanged(selectedField != newActiveField);
        if (isReadyToBeDeleted()) {
            setReadyToBeDeleted(false);
        }
        selectedField = newActiveField;
        hasReadyToBeDeletedField = true;
    }

    public boolean hasReadyToBeDeletedField() {
        return hasReadyToBeDeletedField;
    }

    protected void setActiveField(int value) {
        activeField = value;
    }

    protected int getActiveField() {
        return activeField;
    }

    protected int getSelectedField() {
        return selectedField;
    }

    protected void setNewActiveField(int value) {
        newActiveField = value;
    }

    protected int getNewActiveField() {
        return newActiveField;
    }

    // -----------------------------------------------------------------------------------
    // ======================= Internal data and schema relations ========================
    // -----------------------------------------------------------------------------------
    public String getToolTipText() {
        return null;
    }

    // -----------------------------------------------------------------------------------
    // =========================== Mouse events processing ===============================
    // -----------------------------------------------------------------------------------
    public boolean isTitleAreaRelated(MouseEvent ev) {
        return false;
    }

    public boolean isFieldAreaRelated(MouseEvent ev) {
        return false;
    }

    public boolean deleteSelectedField() {
        return false;
    }
}
