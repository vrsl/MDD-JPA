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
package org.vrsl.jet.translators.erd.internals.sql;

import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.vrsl.jet.models.cim.NamedElementNotFoundException;
import org.vrsl.jet.models.cim.Property;
import org.vrsl.jet.translators.erd.TableColumn;
import org.vrsl.jet.translators.erd.utilities.ForeignKeyInfo;

public class PostgreSqlDialectWriter implements SqlDialectWriter {

    protected static final Map<String, String> model2sqlType = new HashMap<>();
    protected static final Map<String, String> model2sqlSize = new HashMap<>();

    static {
        // -- Types --------------------------------------------------
        model2sqlType.put("boolean", "DECIMAL");
        model2sqlType.put("char", "CHAR");
        model2sqlType.put("byte", "DECIMAL");
        model2sqlType.put("short", "DECIMAL");
        model2sqlType.put("int", "DECIMAL");
        model2sqlType.put("long", "DECIMAL");
        model2sqlType.put("String", "VARCHAR");
        model2sqlType.put("float", "DECIMAL");
        model2sqlType.put("double", "DECIMAL");
        model2sqlType.put("time", "TIMESTAMP");
        model2sqlType.put("Date", "TIMESTAMP");
        // -- Sizes --------------------------------------------------
        model2sqlSize.put("boolean", "(1,0)");
        model2sqlSize.put("char", "(1)");
        model2sqlSize.put("byte", "(3,0)");
        model2sqlSize.put("short", "(5,0)");
        model2sqlSize.put("int", "(10,0)");
        model2sqlSize.put("long", "(19, 0)");
        model2sqlSize.put("String", "(2000)");
        model2sqlSize.put("float", "");
        model2sqlSize.put("double", "");
        model2sqlSize.put("time", "");
        model2sqlSize.put("Date", "");
    }

    @Override
    public void writeDataColumn(org.vrsl.jet.models.cim.Class c, TableColumn col, Property p, boolean isKey, PrintWriter prn) throws NamedElementNotFoundException {
        prn.print("\t" + col.getName() + " " + col.getType() + col.getSize());
        // -- Checking if the field is a key -----------------------------------------
        if (isKey) {
            prn.print(" NOT NULL UNIQUE PRIMARY KEY");
        }
        prn.println(',');
    }

    @Override
    public void writeForeignKeyColumn(String frKeyName, TableColumn col, String modifiers, PrintWriter prn) {
        prn.println("\t" + frKeyName + " " + col.getType() + col.getSize() + modifiers + ",");
    }

    @Override
    public void writeVersionColumn(PrintWriter prn) {
        prn.print("\ttheVersionOfTheRecord DECIMAL(19,0) NOT NULL");
    }

    @Override
    public void writePrimaryKeyConstaint(org.vrsl.jet.models.cim.Class c, Collection<TableColumn> pkCols, PrintWriter prn) {
        // We do not support it here
    }

    @Override
    public void writeForeignConstraint(String frKeyName, ForeignKeyInfo fki, PrintWriter prn) {
        prn.print("\tFOREIGN KEY (" + frKeyName + ") REFERENCES " + fki.getTableName() + " (" + fki.getIdName() + ")");
    }

    @Override
    public String suggestSqlType(String originalType) {
        return model2sqlType.get(originalType);
    }

    @Override
    public String suggestSqlSize(String originalType) {
        return model2sqlSize.get(originalType);
    }
}
