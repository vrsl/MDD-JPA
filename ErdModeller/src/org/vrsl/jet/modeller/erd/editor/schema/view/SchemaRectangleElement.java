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

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

public abstract class SchemaRectangleElement extends ShemaElement {

    // -- Location ----------------------------------------------------------------------
    private int xPos = 0;
    private int yPos = 0;
    private int maxHSize = 0;
    private int maxVSize = 0;
    // -- Statuses ----------------------------------------------------------------------
    private boolean isChanged = false;
    private boolean isMoved = false;
    private boolean isReadyToBeDeleted = false;
    private boolean isPointed = false;
    
    // -----------------------------------------------------------------------------------
    // ================================== Location =======================================
    // -----------------------------------------------------------------------------------
    public int getX() {
        return xPos;
    }

    public int getY() {
        return yPos;
    }

    public int getXSize() {
        return maxHSize;
    }

    public int getYSize() {
        return maxVSize;
    }

    protected void setX(int value) {
        xPos = value;
    }

    protected void setY(int value) {
        yPos = value;
    }

    protected void setXSize(int value) {
        maxHSize = value;
    }

    protected void setYSize(int value) {
        maxVSize = value;
    }
    
    public Rectangle getRectangle() {
        return new Rectangle(
                (int) (xPos * scale), 
                (int) (yPos * scale), 
                (int) (maxHSize * scale), 
                (int) (maxVSize * scale)
                );
    }
    
    // -----------------------------------------------------------------------------------
    // ================================== Statuses =======================================
    // -----------------------------------------------------------------------------------
    @Override
    public void setDefaultView() {
        isChanged = isMoved || isReadyToBeDeleted || isPointed;
        isMoved = false;
        isReadyToBeDeleted = false;
        isPointed = false;
    }

    @Override
    public boolean isChanged() {
        return isChanged;
    }
    protected void setChanged(boolean value) {
        isChanged = value;
    }

    @Override
    public void setReadyToBeDeleted() {
        isChanged = !isReadyToBeDeleted;
        isReadyToBeDeleted = true;
    }
    public boolean isReadyToBeDeleted() {
        return isReadyToBeDeleted;
    }
    protected void setReadyToBeDeleted(boolean value) {
        isReadyToBeDeleted = value;
    }

    public void setPointed() {
        isChanged = !isPointed;
        isPointed = true;
    }
    public boolean isPointed(){
        return isPointed;
    }
    
    public void setMoved() {
        isChanged = !isMoved;
        isMoved = true;
    }
    protected boolean isMoved() {
        return isMoved;
    }
    
    // -----------------------------------------------------------------------------------
    // ================================== Rendering ======================================
    // -----------------------------------------------------------------------------------
    abstract public void calculateBoxMetrics(Graphics g);
    
    // -----------------------------------------------------------------------------------
    // =========================== Mouse events processing ===============================
    // -----------------------------------------------------------------------------------
    abstract public boolean isOwnedAreaRelated(MouseEvent ev);

    abstract public boolean isAssociationAreaRelated(MouseEvent ev);

    abstract public boolean isMovingAreaRelated(MouseEvent ev);
    
    // -----------------------------------------------------------------------------------
    // ======================= Internal data and schema relations ========================
    // -----------------------------------------------------------------------------------
    abstract public boolean setAssociationSelected(boolean isTrue);
}
