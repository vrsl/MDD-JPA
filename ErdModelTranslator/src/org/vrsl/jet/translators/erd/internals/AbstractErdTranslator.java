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
package org.vrsl.jet.translators.erd.internals;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.vrsl.jet.models.cim.Association;
import org.vrsl.jet.models.cim.Class;
import org.vrsl.jet.models.cim.NamedElementNotFoundException;
import org.vrsl.jet.models.cim.Property;
import org.vrsl.jet.models.cim.Reference;
import org.vrsl.jet.models.erd.variants.associations.ErdModelReferenceMultiplicity;
import org.vrsl.jet.models.erd.variants.associations.ErdModelReferenceSuggestedName;
import org.vrsl.jet.models.erd.variants.details.ErdModelMappingDetails;
import org.vrsl.jet.models.erd.variants.details.ErdModelPrimaryKey;
import org.vrsl.jet.models.erd.variants.types.ErdModelType;
import org.vrsl.jet.translators.AbstractCimTranslator;
import org.vrsl.jet.translators.erd.TableColumn;
import org.vrsl.jet.translators.erd.internals.sql.MySQLDialectWriter;
import org.vrsl.jet.translators.erd.internals.sql.SqlDialectWriter;

public abstract class AbstractErdTranslator extends AbstractCimTranslator {

    // -- SQL dialect writer ------------------------------------------------
    private static SqlDialectWriter defaultDialectWriter = new MySQLDialectWriter();

    protected static boolean isOneToOneAssociation(Association a) {
        try {
            Reference r1 = a.getReferences().get(0);
            Reference r2 = a.getReferences().get(1);
            ErdModelReferenceMultiplicity.Category c1 = r1.getFirstByVariantType(ErdModelReferenceMultiplicity.class).getValue().getCategory();
            ErdModelReferenceMultiplicity.Category c2 = r2.getFirstByVariantType(ErdModelReferenceMultiplicity.class).getValue().getCategory();
            return (c1 == ErdModelReferenceMultiplicity.Category.ONE || c1 == ErdModelReferenceMultiplicity.Category.NONE_OR_ONE)
                    && (c2 == ErdModelReferenceMultiplicity.Category.ONE || c2 == ErdModelReferenceMultiplicity.Category.NONE_OR_ONE);
        } catch (NamedElementNotFoundException e) {
            return false;
        }
    }

    protected static boolean isAssociationToItself(Association a) {
        Reference r1 = a.getReferences().get(0);
        Reference r2 = a.getReferences().get(1);
        return r1.getReferedClass() == r2.getReferedClass();
    }

    protected static boolean isCorrespondingMultiplicityTemplate(Association a, ErdModelReferenceMultiplicity.Category c1, ErdModelReferenceMultiplicity.Category c2) {
        try {
            Reference r1 = a.getReferences().get(0);
            Reference r2 = a.getReferences().get(1);
            ErdModelReferenceMultiplicity.Category tc1 = r1.getFirstByVariantType(ErdModelReferenceMultiplicity.class).getValue().getCategory();
            ErdModelReferenceMultiplicity.Category tc2 = r2.getFirstByVariantType(ErdModelReferenceMultiplicity.class).getValue().getCategory();
            return (c1 == tc1) && (c2 == tc2);
        } catch (NamedElementNotFoundException e) {
            return false;
        }
    }

    protected static boolean isMultiWayCorrespondingMultiplicityTemplate(Association a, ErdModelReferenceMultiplicity.Category c1, ErdModelReferenceMultiplicity.Category c2) {
        return isCorrespondingMultiplicityTemplate(a, c1, c2) || isCorrespondingMultiplicityTemplate(a, c2, c1);
    }

    protected static org.vrsl.jet.models.cim.Class getReferedClass(Association a, ErdModelReferenceMultiplicity.Category c) throws NamedElementNotFoundException {
        for (Reference r : a.getReferences()) {
            if (r.hasVariantTypeQualifier(ErdModelReferenceMultiplicity.class)) {
                if (r.getFirstByVariantType(ErdModelReferenceMultiplicity.class).getValue().getCategory() == c) {
                    return r.getReferedClass();
                }
            }
        }
        return null;
    }

    protected static TableColumn deriveTableColumn(Property p, SqlDialectWriter dialectWriter) throws NamedElementNotFoundException {
        TableColumn res = new TableColumn();
        // -- Defining types which requres precision ---------------------------------
        List<String> precTypes = new ArrayList<>(2);
        precTypes.add("float");
        precTypes.add("double");
        // -- Getting information about columun from the property --------------------
        String suggestedName = p.getFirstByVariantType(ErdModelMappingDetails.class).getValue().getMapFieldTo();
        int suggestedSize = p.getFirstByVariantType(ErdModelMappingDetails.class).getValue().getMapFieldSize();
        int suggestedPrec = p.getFirstByVariantType(ErdModelMappingDetails.class).getValue().getMapFieldPrec();
        String originalType = p.getFirstByVariantType(ErdModelType.class).getValue().toString();
        // -- Defining SQL DDL atributes ---------------------------------------------
        String columnName = (suggestedName != null && !suggestedName.isEmpty()) ? suggestedName : p.getName();
        String sqlType = dialectWriter.suggestSqlType(originalType);
        String sqlSize = suggestedSize >= 0
                ? " (" + suggestedSize
                + (precTypes.contains(originalType)
                        ? ((suggestedPrec >= 0) ? "," + suggestedPrec : "")
                        : "")
                + ")"
                : dialectWriter.suggestSqlSize(originalType);
        // -- Stroring calculated data into result -----------------------------------
        res.setName(columnName);
        res.setSize(sqlSize);
        res.setType(sqlType);
        res.setOriginalType(originalType);
        // -- Returning the result ---------------------------------------------------
        return res;
    }

    protected static String findKeyName(Class c) throws NamedElementNotFoundException {
        for (Property p : c.getProperties()) {
            if (p.hasVariantTypeQualifier(ErdModelPrimaryKey.class) && p.getFirstByVariantType(ErdModelPrimaryKey.class).getValue().isPrimiryKey()) {
                return p.getName();
            }
        }
        return "NONE";
    }

    protected static Property findKeyProperty(Class c) throws NamedElementNotFoundException {
        for (Property p : c.getProperties()) {
            if (p.hasVariantTypeQualifier(ErdModelPrimaryKey.class)) {
                return p;
            }
        }
        return null;
    }

    protected static String getSuggestdReferenceItemName(Reference r) throws NamedElementNotFoundException {
        if (r.hasVariantTypeQualifier(ErdModelReferenceSuggestedName.class)) {
            return r.getFirstByVariantType(ErdModelReferenceSuggestedName.class).getValue().toString();
        }
        return null;
    }

    protected static String deriveForeignKeyNameByClass(Class c, Property p) throws NamedElementNotFoundException {
        TableColumn col = deriveTableColumn(p, defaultDialectWriter);
        // -- Stroring primary key -----------------------------------
        String pkName = col.getName();
        // -- Making a field's name for this table -------------------
        pkName = pkName.substring(0, 1).toUpperCase(Locale.getDefault()) + pkName.substring(1, pkName.length());
        return c.getName() + pkName;
    }

    protected static String deriveForeignKeyName(Reference r, Class c, Property p) throws NamedElementNotFoundException {
        // -- Looking for suggested name first -----------------------
        String suggestedName = getSuggestdReferenceItemName(r);
        if (suggestedName != null) {
            return suggestedName;
        }
        TableColumn col = deriveTableColumn(p, defaultDialectWriter);
        // -- Stroring primary key -----------------------------------
        String pkName = col.getName();
        // -- Making a field's name for this table -------------------
        pkName = pkName.substring(0, 1).toUpperCase(Locale.getDefault()) + pkName.substring(1, pkName.length());
        return c.getName() + pkName;
    }

    protected static Collection<String> getMandatoryCheckableCounterparts(Class c) {
        Collection<String> res = new LinkedList<>();
        for (Association a : findAssociations(c.getSchema(), c)) {
            for (Reference r : a.getReferences()) {
                Class refClass = r.getReferedClass();
                if (refClass != c) {
                    if (r.hasVariantTypeQualifier(ErdModelReferenceMultiplicity.class)) {
                        ErdModelReferenceMultiplicity.Category ct;
                        try {
                            ct = r.getFirstByVariantType(ErdModelReferenceMultiplicity.class).getValue().getCategory();
                            if (ct == ErdModelReferenceMultiplicity.Category.MANY) {
                                res.add(refClass.getName());
                            }
                        } catch (NamedElementNotFoundException ex) {
                            Logger.getLogger(AbstractErdTranslator.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }
        }
        return res;
    }

    @SuppressWarnings("unchecked")
    protected static <T> T getPropertyValue(Map<String, Object> props, String name, T defaultValue) {
        if (props == null) {
            return defaultValue;
        }
        T res = (T) props.get(name);
        return res == null ? defaultValue : res;
    }

    protected static Reference extractReferenceToClass(Association a, Class c) {
        for (Reference r : a.getReferences()) {
            if (r.getReferedClass() == c) {
                return r;
            }
        }
        return null;
    }

    protected static String asLowerCamelCaseNotation(String str) {
        return str.substring(0, 1).toLowerCase(Locale.getDefault()) + str.substring(1, str.length());
    }

    protected static String asUpperCamelCaseNotation(String str) {
        return str.substring(0, 1).toUpperCase(Locale.getDefault()) + str.substring(1, str.length());
    }
}
