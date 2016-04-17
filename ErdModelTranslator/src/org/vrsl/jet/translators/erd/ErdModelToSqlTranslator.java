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
package org.vrsl.jet.translators.erd;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.lookup.ServiceProvider;
import org.vrsl.jet.models.cim.Association;
import org.vrsl.jet.models.cim.Class;
import org.vrsl.jet.models.cim.NamedElementNotFoundException;
import org.vrsl.jet.models.cim.Property;
import org.vrsl.jet.models.cim.Qualifier;
import org.vrsl.jet.models.cim.Reference;
import org.vrsl.jet.models.cim.Schema;
import org.vrsl.jet.models.erd.variants.associations.ErdModelReferenceMultiplicity;
import org.vrsl.jet.models.erd.variants.details.ErdModelPrimaryKey;
import org.vrsl.jet.models.erd.variants.entities.ErdModelEntity;
import org.vrsl.jet.translators.AbstractCimTranslator;
import org.vrsl.jet.translators.CimTranslatorError;
import org.vrsl.jet.translators.erd.internals.AbstractErdTranslator;
import org.vrsl.jet.translators.erd.internals.sql.MySQLDialectWriter;
import org.vrsl.jet.translators.erd.internals.sql.OracleDialectWriter;
import org.vrsl.jet.translators.erd.internals.sql.PostgreSqlDialectWriter;
import org.vrsl.jet.translators.erd.internals.sql.SqlDialectWriter;
import org.vrsl.jet.translators.erd.utilities.ForeignKeyInfo;
import org.vrsl.jet.translators.erd.utilities.PathResolver;
import org.vrsl.jet.translators.properties.TranslatorPropertyMetadata;

@ServiceProvider(service = AbstractCimTranslator.class)
public class ErdModelToSqlTranslator extends AbstractErdTranslator {

    // -- SQL dialect writer ------------------------------------------------
    private SqlDialectWriter dialectWriter;

    private final static Map<String, SqlDialectWriter> dialectsTable = new HashMap<>();
    static {
        dialectsTable.put("MySQL", new MySQLDialectWriter());
        dialectsTable.put("Oracle", new OracleDialectWriter());
        dialectsTable.put("PostgreSQL", new PostgreSqlDialectWriter());
    }
    // -- Translator property names -----------------------------------------
    private static final String SQL_DIALECT = "SQL Dialect";
    private static final String SQL_SCRIPT_NAME = "SQL DDL Script Name";
    private static final String SQL_SCRIPT_PATH = "SQL DDL Script Path";
    // -- Default values for translator properties --------------------------
    private static final String DEFAULT_SQL_DIALECT = "MySQL";
    private static final String DEFAULT_SQL_SCRIPT_NAME = "GeneratedDDL.sql";
    private static final String DEFAULT_SQL_SCRIPT_PATH = ".";
    // -- Current translator propertis settings -----------------------------
    private String dialect = "MySQL";
    private String sqlScriptName = "GeneratedDDL.sql";
    private String sqlScriptPath = ".";
    private final List<Class> processingOrder = new ArrayList<>();

    @Override
    public String getSource() {
        return "xem";
    }

    @Override
    public String getTarget() {
        return "sql";
    }

    @Override
    public String getName() {
        return "ERD to SQL";
    }

    @Override
    public Map<String, Object> getProperties() {
        Map<String, Object> res = new LinkedHashMap<>();
        res.put(SQL_SCRIPT_NAME, sqlScriptName);
        res.put(SQL_SCRIPT_PATH, sqlScriptPath);
        res.put(SQL_DIALECT, dialect);
        return res;
    }

    @Override
    public void setProperties(Map<String, Object> properties) {
        sqlScriptName = getPropertyValue(properties, SQL_SCRIPT_NAME, DEFAULT_SQL_SCRIPT_NAME);
        sqlScriptPath = getPropertyValue(properties, SQL_SCRIPT_PATH, DEFAULT_SQL_SCRIPT_PATH);
        dialect = getPropertyValue(properties, SQL_DIALECT, DEFAULT_SQL_DIALECT);
        dialectWriter = dialectsTable.get(dialect);
    }

    @Override
    public Map<String, TranslatorPropertyMetadata> getPropertiesMetadata() {
        Collection<String> dialects = new ArrayList<>();
        dialects.addAll(dialectsTable.keySet());
        // ----------------------------------------------------------------
        Map<String, TranslatorPropertyMetadata> res = new LinkedHashMap<>();
        res.put(SQL_SCRIPT_NAME, new TranslatorPropertyMetadata(TranslatorPropertyMetadata.Type.STRING));
        res.put(SQL_SCRIPT_PATH, new TranslatorPropertyMetadata(TranslatorPropertyMetadata.Type.STRING));
        res.put(SQL_DIALECT, new TranslatorPropertyMetadata(TranslatorPropertyMetadata.Type.SET, dialects));
        return res;
    }

    @Override
    public void translate(Schema schema, String path) throws CimTranslatorError {
        PathResolver.resolve(path + "/" + sqlScriptPath);
        try (OutputStream f = new FileOutputStream(path + "/" + sqlScriptPath + "/" + sqlScriptName)) {
            try (PrintWriter prn = new PrintWriter(f)) {
                Collection<Class> buildOrder = buildProcessingOrder(schema);
                // -- Dropping tables the first of all --------------------------------------
                List<Class> backwardBuildOrder = new ArrayList<>(buildOrder);
                Collections.reverse(backwardBuildOrder);
                for (Class c : backwardBuildOrder) {
                    prn.println("DROP TABLE " + c.getName() + ';');
                }
                prn.println();
                // -- Then generate new DDL for all classes ---------------------------------
                for (Class c : buildOrder) {
                    generateClassRepresentation(c, prn);
                }
            }
        } catch (NamedElementNotFoundException | IOException ex) {
            Logger.getLogger(ErdModelToSqlTranslator.class.getName()).log(Level.WARNING, null, ex);
            throw new CimTranslatorError("Fail to translate " + schema.getName(), ex);
        }
    }

    private Collection<Class> buildProcessingOrder(Schema schema) throws NamedElementNotFoundException {
        Collection<Class> res = new LinkedList<>();
        // -- Reading call classes from the schema and trying to place them ------------------
        Map<String, Boolean> placements = new HashMap<>();
        for (Class c : schema.<Class>getElements(new Qualifier<>(new ErdModelEntity()))) {
            placements.put(c.getName(), Boolean.FALSE);
        }
        // -- Checking if we have anything in schema -----------------------------------------
        if (placements.isEmpty()) {
            return res;
        }
        // -- Trying to place all classes in the order ---------------------------------------
        boolean placementIsDone = false;
        while (!placementIsDone) {
            for (String className : placements.keySet()) {
                Class c = schema.<Class>getElements(className).get(0);
                Collection<Association> classAssociations = AbstractCimTranslator.findAssociations(schema, c);
                // -- Looking if all related classes are placed already ----------------------
                boolean relatedClassesArePlaced = true;
                for (Association a : classAssociations) {
                    if (!isOneToOneAssociation(a)) {
                        for (Reference r : a.getReferences()) {
                            // -- Skiping references to itself -----------------------------------
                            if (r.getReferedClass() != c && r.getReferedClass().hasVariantTypeQualifier(ErdModelEntity.class)) {
                                ErdModelReferenceMultiplicity.Category assocCategory = r.getFirstByVariantType(ErdModelReferenceMultiplicity.class).getValue().getCategory();
                                if (assocCategory != ErdModelReferenceMultiplicity.Category.NONE_OR_MANY && assocCategory != ErdModelReferenceMultiplicity.Category.MANY) {
                                    if (!placements.get(r.getReferedClass().getName())) {
                                        relatedClassesArePlaced = false;
                                    }
                                }
                            }
                        }
                    } else {
                        for (Reference r : a.getReferences()) {
                            // -- Skiping references to itself -----------------------------------
                            if (r.getReferedClass() != c && r.getReferedClass().hasVariantTypeQualifier(ErdModelEntity.class)) {
                                ErdModelReferenceMultiplicity.Category assocCategory = r.getFirstByVariantType(ErdModelReferenceMultiplicity.class).getValue().getCategory();
                                if (assocCategory != ErdModelReferenceMultiplicity.Category.NONE_OR_ONE) {
                                    if (!placements.get(r.getReferedClass().getName())) {
                                        relatedClassesArePlaced = false;
                                    }
                                }
                            }
                        }
                    }
                }
                // -- Test if we are ok here to place a current class ------------------------
                if (!placements.get(c.getName()) && relatedClassesArePlaced) {
                    placements.put(c.getName(), Boolean.TRUE);
                    res.add(c);
                }
                // -- Testing if we have any classes left for a placement --------------------
                placementIsDone = true;
                for (Boolean b : placements.values()) {
                    if (!b) {
                        placementIsDone = false;
                        break;
                    }
                }
            }
        }
        // -- Returing found classes in an order ---------------------------------------------
        return res;
    }

    private void generateClassRepresentation(Class c, PrintWriter prn) throws NamedElementNotFoundException {
        Collection<TableColumn> prKeyColumns = new LinkedList<>();
        prn.println("CREATE TABLE " + c.getName() + " (");
        for (Property p : c.getProperties()) {
            // -- Reading mapping information firts --------------------------------------
            TableColumn col = deriveTableColumn(p, dialectWriter);
            // -- Cheking if the column is a primary key ---------------------------------
            boolean isKey = p.getName().equals(findKeyName(c));
            if(isKey) {
                prKeyColumns.add(col);
            }
            // -- Printing a fiald's representation --------------------------------------
            dialectWriter.writeDataColumn(c, col, p, isKey, prn);
        }
        // -- Adding fields for many-to-one associations ---------------------------------
        Map<String, ForeignKeyInfo> frKeyNames = new HashMap<>();
        for (Association a : AbstractCimTranslator.findAssociations(c.getSchema(), c)) {
            if (isAssociationToItself(a)) {
                if (!isOneToOneAssociation(a)) {
                    buildSelfOneToManyAssociation(c, a, frKeyNames, prn);
                } else {
                    buildSelfOneToOneAssociation(c, a, frKeyNames, prn);
                }
            } else {
                if (!isOneToOneAssociation(a)) {
                    buildOneToManyAssociation(c, a, frKeyNames, prn);
                } else {
                    buildOneToOneAssociation(c, a, frKeyNames, prn);
                }
            }
        }
        // -- Adding a field for optimistic locking --------------------------------------
        dialectWriter.writeVersionColumn(prn);
        // -- Processing primary key constraints -----------------------------------------
        dialectWriter.writePrimaryKeyConstaint(c, prKeyColumns, prn);
        // -- Adding foreign keys --------------------------------------------------------
        if (!frKeyNames.isEmpty()) {
            for (Entry<String, ForeignKeyInfo> item : frKeyNames.entrySet()) {
                prn.println(",");
                ForeignKeyInfo fki = item.getValue();
                String pkNameUpper = fki.getIdName().substring(0, 1).toUpperCase(Locale.getDefault()) + fki.getIdName().substring(1, fki.getIdName().length());
                String frKeyName = fki.getSuggestedName() != null ? fki.getSuggestedName() : (fki.getTableName() + pkNameUpper);
                dialectWriter.writeForeignConstraint(frKeyName, fki, prn);
            }
        }
        prn.println();
        prn.println(");\n");
    }

    private void buildOneToOneAssociation(Class c, Association a, Map<String, ForeignKeyInfo> frKeyNames, PrintWriter prn) throws NamedElementNotFoundException {
        if (isMultiWayCorrespondingMultiplicityTemplate(a,
                ErdModelReferenceMultiplicity.Category.NONE_OR_ONE,
                ErdModelReferenceMultiplicity.Category.ONE)) {
            Class refClassMandatory = getReferedClass(a, ErdModelReferenceMultiplicity.Category.ONE);
            Class refClassOptional = getReferedClass(a, ErdModelReferenceMultiplicity.Category.NONE_OR_ONE);
            if (refClassOptional == c) {
                buildForeignKey(a, c, refClassMandatory, " UNIQUE ", frKeyNames, prn);
            }
        } else if (isCorrespondingMultiplicityTemplate(a, ErdModelReferenceMultiplicity.Category.ONE, ErdModelReferenceMultiplicity.Category.ONE)
                || isCorrespondingMultiplicityTemplate(a, ErdModelReferenceMultiplicity.Category.NONE_OR_ONE, ErdModelReferenceMultiplicity.Category.NONE_OR_ONE)) {
            List<Reference> ref = a.getReferences(new Qualifier<>(new ErdModelReferenceMultiplicity()));
            Class primaryClass = ref.get(0).getReferedClass();
            Class detailsClass = ref.get(1).getReferedClass();
            if (detailsClass == c) {
                buildForeignKey(a, c, primaryClass, " UNIQUE ", frKeyNames, prn);
            }
        }
    }

    private void buildOneToManyAssociation(Class c, Association a, Map<String, ForeignKeyInfo> frKeyNames, PrintWriter prn) throws NamedElementNotFoundException {
        if (isMultiWayCorrespondingMultiplicityTemplate(a,
                ErdModelReferenceMultiplicity.Category.NONE_OR_MANY,
                ErdModelReferenceMultiplicity.Category.ONE)) {
            Class refClassMany = getReferedClass(a, ErdModelReferenceMultiplicity.Category.NONE_OR_MANY);
            Class refClassOne = getReferedClass(a, ErdModelReferenceMultiplicity.Category.ONE);
            if (refClassMany == c) {
                buildForeignKey(a, c, refClassOne, " NOT NULL", frKeyNames, prn);
            }
        } else if (isMultiWayCorrespondingMultiplicityTemplate(a,
                ErdModelReferenceMultiplicity.Category.NONE_OR_MANY,
                ErdModelReferenceMultiplicity.Category.NONE_OR_ONE)) {
            Class refClassMany = getReferedClass(a, ErdModelReferenceMultiplicity.Category.NONE_OR_MANY);
            Class refClassOne = getReferedClass(a, ErdModelReferenceMultiplicity.Category.NONE_OR_ONE);
            if (refClassMany == c) {
                buildForeignKey(a, c, refClassOne, "", frKeyNames, prn);
            }
        } else if (isMultiWayCorrespondingMultiplicityTemplate(a,
                ErdModelReferenceMultiplicity.Category.ONE,
                ErdModelReferenceMultiplicity.Category.MANY)) {
            Class refClassMany = getReferedClass(a, ErdModelReferenceMultiplicity.Category.MANY);
            Class refClassOne = getReferedClass(a, ErdModelReferenceMultiplicity.Category.ONE);
            if (refClassMany == c) {
                buildForeignKey(a, c, refClassOne, " NOT NULL", frKeyNames, prn);
            }
        }
    }

    private void buildSelfOneToOneAssociation(Class c, Association a, Map<String, ForeignKeyInfo> frKeyNames, PrintWriter prn) throws NamedElementNotFoundException {
        if (isMultiWayCorrespondingMultiplicityTemplate(a,
                ErdModelReferenceMultiplicity.Category.NONE_OR_ONE,
                ErdModelReferenceMultiplicity.Category.NONE_OR_ONE)) {
            buildAssociationToItself(a, c, " UNIQUE", prn);
        } else if (isCorrespondingMultiplicityTemplate(a,
                ErdModelReferenceMultiplicity.Category.ONE,
                ErdModelReferenceMultiplicity.Category.NONE_OR_ONE)) {
            buildAssociationToItself(a, c, " UNIQUE NOT NULL", prn);
        }
    }

    private void buildSelfOneToManyAssociation(Class c, Association a, Map<String, ForeignKeyInfo> frKeyNames, PrintWriter prn) throws NamedElementNotFoundException {
        if (isMultiWayCorrespondingMultiplicityTemplate(a,
                ErdModelReferenceMultiplicity.Category.NONE_OR_MANY,
                ErdModelReferenceMultiplicity.Category.NONE_OR_ONE)) {
            buildAssociationToItself(a, c, "", prn);
        } else if (isMultiWayCorrespondingMultiplicityTemplate(a,
                ErdModelReferenceMultiplicity.Category.ONE,
                ErdModelReferenceMultiplicity.Category.NONE_OR_MANY)) {
            buildAssociationToItself(a, c, " NOT NULL", prn);
        }
    }

    private void buildForeignKey(Association a, Class c, Class referedClass, String modifiers, Map<String, ForeignKeyInfo> frKeyNames, PrintWriter prn) throws NamedElementNotFoundException {
        Property p = findKeyProperty(referedClass);
        ErdModelPrimaryKey pk = p.getFirstByVariantType(ErdModelPrimaryKey.class).getValue();
        Reference r = extractReferenceToClass(a, c);
        String suggestedName = getSuggestdReferenceItemName(r);
        String key = suggestedName != null ? suggestedName : referedClass.getName();
        if (pk.isPrimiryKey()) {
            // -- Obtaining information about PK field -------------------
            TableColumn col = deriveTableColumn(p, dialectWriter);
            // -- Stroring primary key -----------------------------------
            frKeyNames.put(key, new ForeignKeyInfo(referedClass.getName(), col.getName(), suggestedName));
            // -- Making a field's name for this table -------------------
            String frKeyName = deriveForeignKeyName(r, referedClass, p);
            // -- Printing a fiald's representation ----------------------
            dialectWriter.writeForeignKeyColumn(frKeyName, col, modifiers, prn);
        }
    }

    private void buildAssociationToItself(Association a, Class c, String modifiers, PrintWriter prn) throws NamedElementNotFoundException {
        Property p = findKeyProperty(c);
        List<Reference> refs = a.getReferences(new Qualifier<>(new ErdModelReferenceMultiplicity()));
        Reference r = refs.get(1);
        // -- Deriving table column name -----------------------------
        TableColumn col = deriveTableColumn(p, dialectWriter);
        // -- Making a field's name for this table -------------------
        String frKeyName = deriveForeignKeyName(r, c, p);
        // -- Printing a fiald's representation ----------------------
        dialectWriter.writeForeignKeyColumn(frKeyName, col, modifiers, prn);
    }
}
