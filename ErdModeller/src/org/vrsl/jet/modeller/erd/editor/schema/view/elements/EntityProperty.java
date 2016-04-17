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

import java.io.Serializable;

public class EntityProperty implements Serializable {

    static final long serialVersionUID = 92384798370001L;
    
    final static int UNDEFINED = 0;
    final static int BOOLEAN = 1;
    final static int CHAR = 2;
    final static int BYTE = 3;
    final static int SHORT = 4;
    final static int INTEGER = 5;
    final static int LONG = 6;
    final static int STRING = 7;
    final static int FLOAT = 8;
    final static int DOUBLE = 9;
    final static int DATE = 10;
    
    final static int TR_REQUIRED = 0;
    final static int TR_REQUIRED_NEW = 1;
    final static int TR_MANDRATORY = 2;
    final static int TR_NOT_SUPPORTED = 3;
    final static int TR_SUPPORTS = 4;
    final static int TR_NEVER = 5;
    
    String name = null;                   // Field name
    int type = UNDEFINED;                 // Field type
    int arrLenght = 0;                    // Array length (if this field is array)
    int tr_type = TR_REQUIRED;            // Transaction type for this field ???
    boolean isPrimaryKey = false;         // This field is primary key
    boolean isValueObjectField = false;   // This field is part of value object
    boolean useAutoSequence = false;      // TRUE if the auto sequencing is used
    
    private String mapFieldTo = null;             // DB firld name for mapping
    private int mapFieldSize = -1;                // Mapping size
    private int mapFieldPrec = -1;
    
    String description;                 // Fiel'd description (Can be used for documentation generation)
    
    private final String[] typeRepresentation = new String[]{
        "undefined",
        "boolean",
        "char",
        "byte",
        "short",
        "integer",
        "long",
        "String",
        "float",
        "double",
        "Date",
        "boolean",
    };

    public EntityProperty(String nm, int tp) {
        name = nm;
        type = tp;
    }

    public EntityProperty(String nm, int tp, int al) {
        name = nm;
        type = tp;
        arrLenght = al;
    }

    void setTransactionType(int tr_t) {
        tr_type = tr_t;
    }

    public int getTransactionType() {
        return tr_type;
    }

    public String getName() {
        return name;
    }

    public int getType() {
        return type;
    }
    
    public String getTypeRepresentation() {
         return typeRepresentation[type];
    }

    public int getArrayLenght() {
        return arrLenght;
    }

    public void setPrimaryKeyMode(boolean isTrue) {
        isPrimaryKey = isTrue;
    }

    public boolean getPrimaryKeyMode() {
        return isPrimaryKey;
    }

    public void setValueObjectFieldMode(boolean isTrue) {
        isValueObjectField = isTrue;
    }

    public boolean getValueObjectFieldMode() {
        return isValueObjectField;
    }

    public void setMapping(String fName, int size, int prec) {
        mapFieldTo = fName;
        mapFieldSize = size;
        mapFieldPrec = prec;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setUseAutoSequence(boolean mode) {
        useAutoSequence = mode;
    }

    public boolean getUseAutoSequence() {
        return useAutoSequence;
    }

    public String getMapFieldTo() {
        return mapFieldTo;
    }

    public int getMapFieldSize() {
        return mapFieldSize;
    }

    public int getMapFieldPrec() {
        return mapFieldPrec;
    }
}
