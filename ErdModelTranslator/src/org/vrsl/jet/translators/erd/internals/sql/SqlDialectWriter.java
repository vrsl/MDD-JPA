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
import org.vrsl.jet.models.cim.Class;
import org.vrsl.jet.models.cim.NamedElementNotFoundException;
import org.vrsl.jet.models.cim.Property;
import org.vrsl.jet.translators.erd.TableColumn;
import org.vrsl.jet.translators.erd.utilities.ForeignKeyInfo;

public interface SqlDialectWriter {

    String suggestSqlType(String originalType);
    
    String suggestSqlSize(String originalType);

    void writeDataColumn(Class c, TableColumn col, Property p, boolean isKey, PrintWriter prn) throws NamedElementNotFoundException;

    void writeForeignKeyColumn(String frKeyName, TableColumn col, String modifiers, PrintWriter prn);

    void writeVersionColumn(PrintWriter prn);

    void writePrimaryKeyConstaint(Class c, Collection<TableColumn> pkCols, PrintWriter prn);

    void writeForeignConstraint(String frKeyName, ForeignKeyInfo fki, PrintWriter prn);
}
