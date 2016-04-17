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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
import org.vrsl.jet.models.erd.variants.associations.ErdModelEntityAssociation;
import org.vrsl.jet.models.erd.variants.associations.ErdModelReferenceMultiplicity;
import org.vrsl.jet.models.erd.variants.details.ErdModelMappingDetails;
import org.vrsl.jet.models.erd.variants.details.ErdModelPrimaryKey;
import org.vrsl.jet.models.erd.variants.entities.ErdModelEntity;
import org.vrsl.jet.models.erd.variants.types.ErdModelType;
import org.vrsl.jet.translators.AbstractCimTranslator;
import org.vrsl.jet.translators.CimTranslatorError;
import org.vrsl.jet.translators.ValidationResult;
import org.vrsl.jet.translators.erd.internals.AbstractErdTranslator;
import org.vrsl.jet.translators.erd.utilities.PathResolver;
import org.vrsl.jet.translators.properties.TranslatorPropertyMetadata;

@ServiceProvider(service = AbstractCimTranslator.class)
public class ErdModelToHibernateTranslator extends AbstractErdTranslator {

    private static final Map<String, String> temporalTypes = new HashMap<>();
    
    // -- Translator property names -----------------------------------------
    private static final String PACKAGE_NAME = "Package Name";
    private static final String PACKAGE_PATH = "Package Path";
    // -- Default values for translator properties --------------------------
    private static final String DEFAULT_PACKAGE_NAME = "idetest.data";
    private static final String DEFAULT_PACKAGE_PATH = "./data";

    static {
        temporalTypes.put("Date", "javax.persistence.TemporalType.TIMESTAMP");
    }
    // -- Current translator propertis settings -----------------------------
    private String packageName = DEFAULT_PACKAGE_NAME;
    private String packagePath = DEFAULT_PACKAGE_PATH;

    @Override
    public String getSource() {
        return "xem";
    }

    @Override
    public String getTarget() {
        return "java";
    }

    @Override
    public String getName() {
        return "ERD to JPA";
    }

    @Override
    public Map<String, Object> getProperties() {
        Map<String, Object> res = new LinkedHashMap<>();
        res.put(PACKAGE_NAME, packageName);
        res.put(PACKAGE_PATH, packagePath);
        // --------------------------------------------------------------------
        /*
         res.put("Int", 1);
         res.put("Double", 1.0);
         res.put("Date", new Date());
         res.put("Boolean", false);
         */
        return res;
    }

    @Override
    public Map<String, TranslatorPropertyMetadata> getPropertiesMetadata() {
        Map<String, TranslatorPropertyMetadata> res = new LinkedHashMap<>();
        res.put(PACKAGE_NAME, new TranslatorPropertyMetadata(TranslatorPropertyMetadata.Type.STRING));
        res.put(PACKAGE_PATH, new TranslatorPropertyMetadata(TranslatorPropertyMetadata.Type.PATH));
        // --------------------------------------------------------------------
        /*
         res.put("Int", new TranslatorPropertyMetadata(TranslatorPropertyMetadata.Type.INTEGER));
         res.put("Double", new TranslatorPropertyMetadata(TranslatorPropertyMetadata.Type.NUMBER));
         res.put("Date", new TranslatorPropertyMetadata(TranslatorPropertyMetadata.Type.DATE));
         res.put("Boolean", new TranslatorPropertyMetadata(TranslatorPropertyMetadata.Type.BOOLEAN));
         */
        return res;
    }

    @Override
    public void setProperties(Map<String, Object> properties) {
        packageName = getPropertyValue(properties, PACKAGE_NAME, DEFAULT_PACKAGE_NAME);
        packagePath = getPropertyValue(properties, PACKAGE_PATH, DEFAULT_PACKAGE_PATH);
    }

    @Override
    public ValidationResult validateProperties(String path, Map<String, Object> properties) {
        String proposedPackagePath = getPropertyValue(properties, PACKAGE_PATH, DEFAULT_PACKAGE_PATH);
        File f = new File(path + "/" + proposedPackagePath);
        if(f.exists()) {
            return new ValidationResult();
        }
        return new ValidationResult(
                new String[]{
                    "Proposed path " + proposedPackagePath + "is incorrect.\nPlease use another one or create a path which is already\navaialable in your application."
                }
        );
    }

    @Override
    public void translate(Schema schema, String path) throws CimTranslatorError {
        try {
            PathResolver.resolve(path + "/" + packagePath);
            for (Class c : schema.<Class>getElements(new Qualifier<>(new ErdModelEntity()))) {
                try (OutputStream f = new FileOutputStream(path + "/" + packagePath + "/" + c.getName() + ".java")) {
                    try (PrintWriter prn = new PrintWriter(f)) {
                        // -- Then generate new DDL for all classes ---------------------------------
                        generateClassRepresentation(c, prn);
                    }
                } catch (IOException ex) {
                    Logger.getLogger(ErdModelToSqlTranslator.class
                            .getName()).log(Level.WARNING, null, ex);
                    throw new CimTranslatorError(
                            "Fail to translate " + schema.getName(), ex);
                }
            }
        } catch (NamedElementNotFoundException ex) {
            Logger.getLogger(ErdModelToSqlTranslator.class
                    .getName()).log(Level.WARNING, null, ex);
            throw new CimTranslatorError(
                    "Fail to translate " + schema.getName(), ex);
        }
    }

    private void generateClassRepresentation(Class c, PrintWriter prn) throws NamedElementNotFoundException {
        prn.println("package " + packageName + ";");
        generateIncludes(c, prn);
        prn.println();
        prn.println("@Entity");
        prn.println("@Table(name=\"" + c.getName() + "\")");
        prn.println("public class " + c.getName() + " implements Serializable {");
        prn.println();
        buldClassFields(c, prn);
        buildRelationshipsFields(c, prn);
        buildClassSettersAndGetters(c, prn);
        buildRelationships(c, prn);
        prn.println("}");
    }

    private void buldClassFields(Class c, PrintWriter prn) throws NamedElementNotFoundException {
        for (Property p : c.getProperties()) {
            String type = p.getFirstByVariantType(ErdModelType.class).getValue().toString();
            String name = p.getName();
            boolean isKey = p.getFirstByVariantType(ErdModelPrimaryKey.class).getValue().isPrimiryKey();
            boolean isAutogenerated = p.getFirstByVariantType(ErdModelPrimaryKey.class).getValue().isUseAutoSequence();
            String suggestedName = p.getFirstByVariantType(ErdModelMappingDetails.class).getValue().getMapFieldTo();
            // -- Getting real names for the field and column ---------------------------------------------
            String columnName = (suggestedName != null && !suggestedName.isEmpty()) ? suggestedName : p.getName();
            // -- Checking if it is a key or just a regular field -----------------------------------------
            if (isKey) {
                prn.println("\t@Id");
                if (isAutogenerated) {
                    prn.println("\t@GeneratedValue(strategy=GenerationType.AUTO)");
                }
            } else {
                if (temporalTypes.keySet().contains(type)) {
                    prn.println("\t@Temporal(" + temporalTypes.get(type) + ")");
                } else {
                    prn.println("\t@Basic");
                }
            }
            // -- Generating a common annotations and the field -------------------------------------------
            prn.println("\t@Column(name=\"" + columnName + "\")");
            prn.println("\tprivate " + type + " " + name + ";");
            prn.println();
        }
        prn.println("\t@Version");
        prn.println("\t@Column(name=\"theVersionOfTheRecord\")");
        prn.println("\tprivate int theVersionOfTheRecord;");
        prn.println();
    }

    private void buildClassSettersAndGetters(Class c, PrintWriter prn) throws NamedElementNotFoundException {
        for (Property p : c.getProperties()) {
            String type = p.getFirstByVariantType(ErdModelType.class).getValue().toString();
            // -- Getting real names for the field and column ---------------------------------------------
            String name = p.getName();
            // -- Deriving getter and setter names --------------------------------------------------------
            String getName = "get" + name.substring(0, 1).toUpperCase(Locale.getDefault()) + name.substring(1, name.length());
            String setName = "set" + name.substring(0, 1).toUpperCase(Locale.getDefault()) + name.substring(1, name.length());
            // -- Printlng section ------------------------------------------------------------------------
            prn.println();
            prn.println("\tpublic " + type + " " + getName + "(){");
            prn.println("\t\treturn " + name + ";");
            prn.println("\t}");
            prn.println("\tpublic void " + setName + "(" + type + " " + name + "){");
            prn.println("\t\tthis." + name + " = " + name + ";");
            prn.println("\t}");
        }
        prn.println();
        prn.println("\tpublic int getRecordVersion(){");
        prn.println("\t\treturn theVersionOfTheRecord;");
        prn.println("\t}");
    }

    private void generateIncludes(Class c, PrintWriter prn) throws NamedElementNotFoundException {
        boolean hasDateDef = false;
        boolean hasKey = false;
        boolean hasAutogenerated = false;
        boolean hasManyToOne = false;
        boolean hasOneToMany = false;
        boolean hasBasic = false;
        boolean hasOneToOne = false;
        boolean hasJoinColumn = false;
        boolean hasSizeValidation = false;
        boolean hasNotNullValidation = false;

        for (Property p : c.getProperties()) {
            String type = p.getFirstByVariantType(ErdModelType.class).getValue().toString();
            if ("Date".equals(type)) {
                hasDateDef = true;
            }
            if (p.getFirstByVariantType(ErdModelPrimaryKey.class).getValue().isPrimiryKey()) {
                hasKey = true;
                if (p.getFirstByVariantType(ErdModelPrimaryKey.class).getValue().isUseAutoSequence()) {
                    hasAutogenerated = true;
                }
            } else {
                hasBasic = true;
            }
            for (Association a : AbstractCimTranslator.findAssociations(c.getSchema(), c)) {
                if (isAssociationToItself(a)) {
                    if (!isOneToOneAssociation(a)) {
                        hasManyToOne = true;
                        hasOneToMany = true;
                        hasJoinColumn = true;
                    } else {
                        hasOneToOne = true;
                        hasJoinColumn = true;
                    }
                } else {
                    if (!isOneToOneAssociation(a)) {
                        // -- Finding reffered classses first ------------------------------------------------
                        Class refClassMany = null;
                        Class refClassOne = null;
                        // -- Applying diffrenet possible combinations ---------------------------------------
                        if (isMultiWayCorrespondingMultiplicityTemplate(a,
                                ErdModelReferenceMultiplicity.Category.NONE_OR_MANY,
                                ErdModelReferenceMultiplicity.Category.ONE)) {
                            refClassMany = getReferedClass(a, ErdModelReferenceMultiplicity.Category.NONE_OR_MANY);
                            refClassOne = getReferedClass(a, ErdModelReferenceMultiplicity.Category.ONE);
                        } else if (isMultiWayCorrespondingMultiplicityTemplate(a,
                                ErdModelReferenceMultiplicity.Category.NONE_OR_MANY,
                                ErdModelReferenceMultiplicity.Category.NONE_OR_ONE)) {
                            refClassMany = getReferedClass(a, ErdModelReferenceMultiplicity.Category.NONE_OR_MANY);
                            refClassOne = getReferedClass(a, ErdModelReferenceMultiplicity.Category.NONE_OR_ONE);
                        } else if (isMultiWayCorrespondingMultiplicityTemplate(a,
                                ErdModelReferenceMultiplicity.Category.MANY,
                                ErdModelReferenceMultiplicity.Category.ONE)) {
                            refClassMany = getReferedClass(a, ErdModelReferenceMultiplicity.Category.MANY);
                            refClassOne = getReferedClass(a, ErdModelReferenceMultiplicity.Category.ONE);
                            if (c == refClassOne) {
                                hasSizeValidation = true;
                            }
                        }
                        // -- Processing ManyToOne realtionship ---------------------------------------------
                        if (refClassMany == c) {
                            hasManyToOne = true;
                            hasJoinColumn = true;
                        }
                        // -- Processing OneToMany relationship ----------------------------------
                        if (refClassOne == c) {
                            hasOneToMany = true;
                        }
                    } else {
                        hasOneToOne = true;
                        if (isCorrespondingMultiplicityTemplate(a, ErdModelReferenceMultiplicity.Category.NONE_OR_ONE, ErdModelReferenceMultiplicity.Category.ONE)
                                || isCorrespondingMultiplicityTemplate(a, ErdModelReferenceMultiplicity.Category.ONE, ErdModelReferenceMultiplicity.Category.NONE_OR_ONE)) {
                            Class refClassOptional = getReferedClass(a, ErdModelReferenceMultiplicity.Category.NONE_OR_ONE);
                            if (refClassOptional == c) {
                                hasJoinColumn = true;
                            }
                        }
                        if (isCorrespondingMultiplicityTemplate(a, ErdModelReferenceMultiplicity.Category.ONE, ErdModelReferenceMultiplicity.Category.ONE)) {
                            List<Reference> ref = a.getReferences(new Qualifier<>(new ErdModelReferenceMultiplicity()));
                            Class detailsClass = ref.get(1).getReferedClass();
                            if (c == detailsClass) {
                                hasJoinColumn = true;
                            }
                            if (c != detailsClass) {
                                hasNotNullValidation = true;
                            }
                        }
                        if (isCorrespondingMultiplicityTemplate(a, ErdModelReferenceMultiplicity.Category.NONE_OR_ONE, ErdModelReferenceMultiplicity.Category.NONE_OR_ONE)) {
                            List<Reference> ref = a.getReferences(new Qualifier<>(new ErdModelReferenceMultiplicity()));
                            Class detailsClass = ref.get(1).getReferedClass();
                            if (c == detailsClass) {
                                hasJoinColumn = true;
                                hasOneToOne = false;
                                hasManyToOne = true;
                            }
                        }
                    }
                }
            }
        }
        // -- Writing includes -----------------------------------------------------------------------------
        prn.println();
        prn.println("import java.io.Serializable;");
        if (hasDateDef) {
            prn.println("import java.util.Date;");
        }
        if (hasOneToMany || hasManyToOne) {
            prn.println("import java.util.Collection;");
            prn.println("import java.util.ArrayList;");
        }
        if (hasBasic) {
            prn.println("import javax.persistence.Basic;");
        }
        prn.println("import javax.persistence.Column;");
        prn.println("import javax.persistence.Entity;");
        if (hasAutogenerated) {
            prn.println("import javax.persistence.GeneratedValue;");
            prn.println("import javax.persistence.GenerationType;");
        }
        if (hasKey) {
            prn.println("import javax.persistence.Id;");
        }
        if (hasOneToMany) {
            prn.println("import javax.persistence.OneToMany;");
        }
        if (hasJoinColumn) {
            prn.println("import javax.persistence.JoinColumn;");
        }
        if (hasManyToOne) {
            prn.println("import javax.persistence.ManyToOne;");
        }
        if (hasOneToOne) {
            prn.println("import javax.persistence.OneToOne;");
        }
        prn.println("import javax.persistence.Table;");
        if (hasDateDef) {
            prn.println("import javax.persistence.Temporal;");
        }
        prn.println("import javax.persistence.Version;");
        if (hasNotNullValidation) {
            prn.println("import javax.validation.constraints.NotNull;");
        }
        if (hasSizeValidation) {
            prn.println("import javax.validation.constraints.Size;");
        }
    }

    private void buildRelationshipsFields(Class c, PrintWriter prn) throws NamedElementNotFoundException {
        for (Association a : AbstractCimTranslator.findAssociations(c.getSchema(), c)) {
            if (isAssociationToItself(a)) {
                if (!isOneToOneAssociation(a)) {
                    buildSelfOneToManyClassField(c, a, prn);
                } else {
                    buildSelfOneToOneClassField(c, a, prn);
                }
            } else {
                if (!isOneToOneAssociation(a)) {
                    buildOneToManyClassField(c, a, prn);
                } else {
                    buildOneToOneClassField(c, a, prn);
                }
            }
        }
    }

    private void buildOneToManyClassField(Class c, Association a, PrintWriter prn) throws NamedElementNotFoundException {
        // -- Finding reffered classses first ------------------------------------------------
        Class refClassMany = null;
        Class refClassOne = null;
        // -- Values to find if counterpart is mandatory -------------------------------------
        boolean isCounterpartMandatoryMany = false;
        boolean isCounterpartMandatoryOne = false;
        // -- Applying diffrenet possible combinations ---------------------------------------
        if (isMultiWayCorrespondingMultiplicityTemplate(a,
                ErdModelReferenceMultiplicity.Category.NONE_OR_MANY,
                ErdModelReferenceMultiplicity.Category.ONE)) {
            refClassMany = getReferedClass(a, ErdModelReferenceMultiplicity.Category.NONE_OR_MANY);
            refClassOne = getReferedClass(a, ErdModelReferenceMultiplicity.Category.ONE);
            isCounterpartMandatoryMany = true;
            isCounterpartMandatoryOne = false;
        } else if (isMultiWayCorrespondingMultiplicityTemplate(a,
                ErdModelReferenceMultiplicity.Category.NONE_OR_MANY,
                ErdModelReferenceMultiplicity.Category.NONE_OR_ONE)) {
            refClassMany = getReferedClass(a, ErdModelReferenceMultiplicity.Category.NONE_OR_MANY);
            refClassOne = getReferedClass(a, ErdModelReferenceMultiplicity.Category.NONE_OR_ONE);
            isCounterpartMandatoryMany = false;
            isCounterpartMandatoryOne = false;
        } else if (isMultiWayCorrespondingMultiplicityTemplate(a,
                ErdModelReferenceMultiplicity.Category.MANY,
                ErdModelReferenceMultiplicity.Category.ONE)) {
            refClassMany = getReferedClass(a, ErdModelReferenceMultiplicity.Category.MANY);
            refClassOne = getReferedClass(a, ErdModelReferenceMultiplicity.Category.ONE);
            isCounterpartMandatoryMany = true;
            isCounterpartMandatoryOne = true;
        }
        // -- Looking for suggested reference item name --------------------------------------
        Reference r = extractReferenceToClass(a, c);
        String suggestedName = getSuggestdReferenceItemName(r);
        // -- Processing ManyToOne realtionship ----------------------------------------------
        if (refClassMany == c) {
            String className = refClassOne.getName();
            // -- Finding key property first --------------------------------------------------------------
            Property p = findKeyProperty(refClassOne);
            // -- Obtaining information about FK field ----------------------------------------------------
            String frKeyName = deriveForeignKeyName(r, refClassOne, p);
            // -- Deriving field name --------------------------------------------------------
            String fieldName = asLowerCamelCaseNotation(suggestedName != null ? suggestedName : className);
            buildAnnotationsManyToOneRelationships(frKeyName, isCounterpartMandatoryMany, prn);
            prn.println("\tprivate " + className + " " + fieldName + ";");
            prn.println();
        }
        // -- Processing OneToMany relationship ----------------------------------------------
        if (refClassOne == c) {
            String className = refClassMany.getName();
            // -- Getting name from many side of association ---------------------------------
            Reference mr = extractReferenceToClass(a, refClassMany);
            String suggestedNameForMany = getSuggestdReferenceItemName(mr);
            // -- Deriving field name --------------------------------------------------------
            String fieldName = asLowerCamelCaseNotation(suggestedName != null ? suggestedName : className);
            String refName = suggestedNameForMany != null ? suggestedNameForMany : refClassOne.getName();
            buildAnnotationsOneToManyRelationships(refName, isCounterpartMandatoryOne, prn);
            prn.println("\tprivate Collection<" + className + "> " + fieldName + " = new ArrayList<>();");
            prn.println();
        }
    }

    private void buildOneToOneClassField(Class c, Association a, PrintWriter prn) throws NamedElementNotFoundException {
        if (a.hasVariantTypeQualifier(ErdModelEntityAssociation.class)) {
            for (Reference r : a.getReferences()) {
                if (c != r.getReferedClass()) {
                    String suggestedName = getSuggestdReferenceItemName(extractReferenceToClass(a, c));
                    String className = r.getReferedClass().getName();
                    // -- Deriving field name ----------------------------------------------------
                    String fieldName = asLowerCamelCaseNotation(suggestedName != null ? suggestedName : className);
                    // -- Building annotations ---------------------------------------------------
                    if (isCorrespondingMultiplicityTemplate(a, ErdModelReferenceMultiplicity.Category.NONE_OR_ONE, ErdModelReferenceMultiplicity.Category.ONE)
                            || isCorrespondingMultiplicityTemplate(a, ErdModelReferenceMultiplicity.Category.ONE, ErdModelReferenceMultiplicity.Category.NONE_OR_ONE)) {
                        Class refClassMandatory = getReferedClass(a, ErdModelReferenceMultiplicity.Category.ONE);
                        Class refClassOptional = getReferedClass(a, ErdModelReferenceMultiplicity.Category.NONE_OR_ONE);
                        if (refClassOptional == c) {
                            // -- Finding key property first --------------------------------------------------------------
                            Property p = findKeyProperty(refClassMandatory);
                            // -- Obtaining information about FK field ----------------------------------------------------
                            String frKeyName = deriveForeignKeyName(r, refClassMandatory, p);
                            buildAnnotationsDetailsOneToOneRelationship(frKeyName, false, true, prn);
                        }
                        if (refClassMandatory == c) {
                            // -- Looking for mapped class name -----------------------------------------------------------
                            String mappedClassName = asUpperCamelCaseNotation(suggestedName != null ? suggestedName : c.getName());
                            buildAnnotationsPrimaryOneToOneRelationships(mappedClassName, false, prn);
                        }
                    }
                    if (isCorrespondingMultiplicityTemplate(a, ErdModelReferenceMultiplicity.Category.ONE, ErdModelReferenceMultiplicity.Category.ONE)) {
                        List<Reference> ref = a.getReferences(new Qualifier<>(new ErdModelReferenceMultiplicity()));
                        Class primaryClass = ref.get(0).getReferedClass();
                        Class detailsClass = ref.get(1).getReferedClass();
                        if (primaryClass == c) {
                            // -- Looking for mapped class name -----------------------------------------------------------
                            String mappedClassName = asUpperCamelCaseNotation(suggestedName != null ? suggestedName : c.getName());
                            buildAnnotationsPrimaryOneToOneRelationships(mappedClassName, true, prn);
                        }
                        if (detailsClass == c) {
                            // -- Finding key property first --------------------------------------------------------------
                            Property p = findKeyProperty(primaryClass);
                            // -- Obtaining information about FK field ----------------------------------------------------
                            String frKeyName = deriveForeignKeyName(r, primaryClass, p);
                            buildAnnotationsDetailsOneToOneRelationship(frKeyName, false, true, prn);
                        }
                    }
                    if (isCorrespondingMultiplicityTemplate(a, ErdModelReferenceMultiplicity.Category.NONE_OR_ONE, ErdModelReferenceMultiplicity.Category.NONE_OR_ONE)) {
                        List<Reference> ref = a.getReferences(new Qualifier<>(new ErdModelReferenceMultiplicity()));
                        Class primaryClass = ref.get(0).getReferedClass();
                        Class detailsClass = ref.get(1).getReferedClass();
                        if (primaryClass == c) {
                            // -- Looking for mapped class name -----------------------------------------------------------
                            String mappedClassName = asUpperCamelCaseNotation(suggestedName != null ? suggestedName : c.getName());
                            buildAnnotationsPrimaryOneToOneRelationships(mappedClassName, false, prn);
                        }
                        if (detailsClass == c) {
                            // -- Finding key property first --------------------------------------------------------------
                            Property p = findKeyProperty(primaryClass);
                            // -- Obtaining information about FK field ----------------------------------------------------
                            String frKeyName = deriveForeignKeyName(r, primaryClass, p);
                            buildAnnotationsDetailsOneToOneRelationship(frKeyName, true, false, prn);
                        }
                    }
                    // -- Createing a class fiend ------------------------------------------------
                    prn.println("\tprivate " + className + " " + fieldName + ";");
                    prn.println();
                }
            }
        }
    }

    private void buildSelfOneToManyClassField(Class c, Association a, PrintWriter prn) throws NamedElementNotFoundException {
        List<Reference> refs = a.getReferences(new Qualifier<>(new ErdModelReferenceMultiplicity()));
        Reference r = refs.get(1);
        String suggestedName = getSuggestdReferenceItemName(r);
        String className = c.getName();
        String fieldName = asLowerCamelCaseNotation(suggestedName != null ? suggestedName : className);

        boolean mandatory = refs.get(0).getFirstByVariantType(ErdModelReferenceMultiplicity.class).getValue().getCategory() == ErdModelReferenceMultiplicity.Category.ONE;

        // -- Finding key property first --------------------------------------------------------------
        Property p = findKeyProperty(c);
        // -- Obtaining information about FK field ----------------------------------------------------
        String frKeyName = deriveForeignKeyName(r, c, p);
        String parentFieldName = "parent" + asUpperCamelCaseNotation(fieldName);
        // -- Writing annotations ---------------------------------------------------------------------
        buildAnnotationsSelfManyToOneRelationships(frKeyName, mandatory, prn);
        prn.println("\tprivate " + className + " " + parentFieldName + ";");
        prn.println();
        buildAnnotationsSelfOneToManyRelationships(parentFieldName, prn);
        prn.println("\tprivate Collection<" + className + "> " + fieldName + " = new ArrayList<>();");
        prn.println();
    }

    private void buildSelfOneToOneClassField(Class c, Association a, PrintWriter prn) throws NamedElementNotFoundException {
        List<Reference> refs = a.getReferences(new Qualifier<>(new ErdModelReferenceMultiplicity()));
        Reference r = refs.get(1);
        String suggestedName = getSuggestdReferenceItemName(r);
        String className = c.getName();
        String fieldName = asLowerCamelCaseNotation(suggestedName != null ? suggestedName : className);

        boolean mandatory = refs.get(0).getFirstByVariantType(ErdModelReferenceMultiplicity.class).getValue().getCategory() == ErdModelReferenceMultiplicity.Category.ONE;

        // -- Finding key property first --------------------------------------------------------------
        Property p = findKeyProperty(c);
        // -- Obtaining information about FK field ----------------------------------------------------
        String frKeyName = deriveForeignKeyName(r, c, p);
        // -- Writing annotations ---------------------------------------------------------------------
        buildAnnotationsSelfOneToOneRelationships(frKeyName, mandatory, prn);
        prn.println("\tprivate " + className + " parent" + asUpperCamelCaseNotation(fieldName) + ";");
        prn.println();

        prn.println("\t@OneToOne(mappedBy=\"" + asLowerCamelCaseNotation(className) + "\")");
        prn.println("\tprivate " + className + " " + fieldName + ";");
    }

    private void buildRelationships(Class c, PrintWriter prn) throws NamedElementNotFoundException {
        for (Association a : AbstractCimTranslator.findAssociations(c.getSchema(), c)) {
            if (isAssociationToItself(a)) {
                if (!isOneToOneAssociation(a)) {
                    buildSelfOneToManyRelationships(c, a, prn);
                } else {
                    buildSelfOneToOneRelationships(c, a, prn);
                }
            } else {
                if (!isOneToOneAssociation(a)) {
                    if (isMultiWayCorrespondingMultiplicityTemplate(a,
                            ErdModelReferenceMultiplicity.Category.NONE_OR_MANY,
                            ErdModelReferenceMultiplicity.Category.ONE)) {
                        Class refClassMany = getReferedClass(a, ErdModelReferenceMultiplicity.Category.NONE_OR_MANY);
                        Class refClassOne = getReferedClass(a, ErdModelReferenceMultiplicity.Category.ONE);
                        // -- Processing ManyToOne realtionship -----------------------------------------------------------
                        if (refClassMany == c) {
                            buildManyToOneRelationships(a, c, refClassOne, prn, true);
                        }
                        // -- Processing OneToMany relationship -----------------------------------------------------------
                        if (refClassOne == c) {
                            buildOneToManyRelationships(a, c, refClassMany, prn, false);
                        }
                    } else if (isMultiWayCorrespondingMultiplicityTemplate(a,
                            ErdModelReferenceMultiplicity.Category.NONE_OR_MANY,
                            ErdModelReferenceMultiplicity.Category.NONE_OR_ONE)) {
                        Class refClassMany = getReferedClass(a, ErdModelReferenceMultiplicity.Category.NONE_OR_MANY);
                        Class refClassOne = getReferedClass(a, ErdModelReferenceMultiplicity.Category.NONE_OR_ONE);
                        // -- Processing ManyToOne realtionship -----------------------------------------------------------
                        if (refClassMany == c) {
                            buildManyToOneRelationships(a, c, refClassOne, prn, false);
                        }
                        // -- Processing OneToMany relationship -----------------------------------------------------------
                        if (refClassOne == c) {
                            buildOneToManyRelationships(a, c, refClassMany, prn, false);
                        }
                    } else if (isMultiWayCorrespondingMultiplicityTemplate(a,
                            ErdModelReferenceMultiplicity.Category.MANY,
                            ErdModelReferenceMultiplicity.Category.ONE)) {
                        Class refClassMany = getReferedClass(a, ErdModelReferenceMultiplicity.Category.MANY);
                        Class refClassOne = getReferedClass(a, ErdModelReferenceMultiplicity.Category.ONE);
                        // -- Processing ManyToOne realtionship -----------------------------------------------------------
                        if (refClassMany == c) {
                            buildManyToOneRelationships(a, c, refClassOne, prn, true);
                        }
                        // -- Processing OneToMany relationship -----------------------------------------------------------
                        if (refClassOne == c) {
                            buildOneToManyRelationships(a, c, refClassMany, prn, true);
                        }
                    }
                } else {
                    if (isCorrespondingMultiplicityTemplate(a, ErdModelReferenceMultiplicity.Category.NONE_OR_ONE, ErdModelReferenceMultiplicity.Category.ONE)
                            || isCorrespondingMultiplicityTemplate(a, ErdModelReferenceMultiplicity.Category.ONE, ErdModelReferenceMultiplicity.Category.NONE_OR_ONE)) {
                        Class refClassMandatory = getReferedClass(a, ErdModelReferenceMultiplicity.Category.ONE);
                        Class refClassOptional = getReferedClass(a, ErdModelReferenceMultiplicity.Category.NONE_OR_ONE);
                        if (refClassOptional == c) {
                            buildDetailsOneToOneRelationship(a, c, refClassMandatory, prn, false, true);
                        }
                        if (refClassMandatory == c) {
                            buildPrimaryOneToOneRelationships(a, c, refClassOptional, prn, false);
                        }
                    }
                    if (isCorrespondingMultiplicityTemplate(a, ErdModelReferenceMultiplicity.Category.ONE, ErdModelReferenceMultiplicity.Category.ONE)) {
                        List<Reference> ref = a.getReferences(new Qualifier<>(new ErdModelReferenceMultiplicity()));
                        Class primaryClass = ref.get(0).getReferedClass();
                        Class detailsClass = ref.get(1).getReferedClass();
                        if (primaryClass == c) {
                            buildPrimaryOneToOneRelationships(a, c, detailsClass, prn, true);
                        }
                        if (detailsClass == c) {
                            buildDetailsOneToOneRelationship(a, c, primaryClass, prn, false, true);
                        }
                    }
                    if (isCorrespondingMultiplicityTemplate(a, ErdModelReferenceMultiplicity.Category.NONE_OR_ONE, ErdModelReferenceMultiplicity.Category.NONE_OR_ONE)) {
                        List<Reference> ref = a.getReferences(new Qualifier<>(new ErdModelReferenceMultiplicity()));
                        Class primaryClass = ref.get(0).getReferedClass();
                        Class detailsClass = ref.get(1).getReferedClass();
                        if (primaryClass == c) {
                            buildPrimaryOneToOneRelationships(a, c, detailsClass, prn, false);
                        }
                        if (detailsClass == c) {
                            buildDetailsOneToOneRelationship(a, c, primaryClass, prn, true, false);
                        }
                    }
                }
            }
        }
    }

    private void buildSelfOneToManyRelationships(Class c, Association a, PrintWriter prn) throws NamedElementNotFoundException {
        List<Reference> refs = a.getReferences(new Qualifier<>(new ErdModelReferenceMultiplicity()));
        Reference r = refs.get(1);
        String suggestedName = getSuggestdReferenceItemName(r);
        String name = c.getName();
        // -- Deriving names of association to itself fields ------------------------------------------
        String fieldName = asLowerCamelCaseNotation(suggestedName != null ? suggestedName : name);
        String parentFieldName = "parent" + asUpperCamelCaseNotation(fieldName);
        // -- Deriving getter and setter names --------------------------------------------------------
        String getParentName = "get" + asUpperCamelCaseNotation(parentFieldName);
        String setParentName = "set" + asUpperCamelCaseNotation(parentFieldName);
        // -- Making a space before methods -----------------------------------------------------------
        prn.println();
        // -- Generation of getters and setters -------------------------------------------------------
        prn.println("\tpublic " + name + " " + getParentName + "(){");
        prn.println("\t\treturn " + parentFieldName + ";");
        prn.println("\t}");
        prn.println("\tpublic void " + setParentName + "(" + name + " " + parentFieldName + "){");
        prn.println("\t\tthis." + parentFieldName + " = " + parentFieldName + ";");
        prn.println("\t}");
        // -- Deriving getter and setter names --------------------------------------------------------
        String getName = "get" + asUpperCamelCaseNotation(fieldName);
        String setName = "set" + asUpperCamelCaseNotation(fieldName);
        // -- Populating getters and setters for this relation ----------------------------------------
        prn.println("\tpublic Collection<" + name + "> " + getName + "(){");
        prn.println("\t\treturn " + fieldName + ";");
        prn.println("\t}");
        prn.println("\tpublic void " + setName + "(" + name + " " + parentFieldName + "){");
        prn.println("\t\tthis." + fieldName + ".add(" + parentFieldName + ");");
        prn.println("\t\tif(" + parentFieldName + "." + getParentName + "() != this" + "){");
        prn.println("\t\t\t" + parentFieldName + "." + setParentName + "(this);");
        prn.println("\t\t}");
        prn.println("\t}");
    }

    private void buildSelfOneToOneRelationships(Class c, Association a, PrintWriter prn) throws NamedElementNotFoundException {
        List<Reference> refs = a.getReferences(new Qualifier<>(new ErdModelReferenceMultiplicity()));
        Reference r = refs.get(1);
        String suggestedName = getSuggestdReferenceItemName(r);
        String name = c.getName();
        // -- Deriving names of association to itself fields ------------------------------------------
        String fieldName = asLowerCamelCaseNotation(suggestedName != null ? suggestedName : name);
        String parentFieldName = "parent" + asUpperCamelCaseNotation(fieldName);
        // -- Deriving getter and setter names --------------------------------------------------------
        String getParentName = "get" + asUpperCamelCaseNotation(parentFieldName);
        String setParentName = "set" + asUpperCamelCaseNotation(parentFieldName);
        // -- Making a space before methods -----------------------------------------------------------
        prn.println();
        // -- Generation of getters and setters -------------------------------------------------------
        prn.println("\tpublic " + name + " " + getParentName + "(){");
        prn.println("\t\treturn " + parentFieldName + ";");
        prn.println("\t}");
        prn.println("\tpublic void " + setParentName + "(" + name + " " + parentFieldName + "){");
        prn.println("\t\tthis." + parentFieldName + " = " + parentFieldName + ";");
        prn.println("\t}");
        // -- Deriving getter and setter names --------------------------------------------------------
        String getName = "get" + asUpperCamelCaseNotation(fieldName);
        String setName = "set" + asUpperCamelCaseNotation(fieldName);
        // -- Populating getters and setters for this relation ----------------------------------------
        prn.println("\tpublic " + name + " " + getName + "(){");
        prn.println("\t\treturn " + fieldName + ";");
        prn.println("\t}");
        prn.println("\tpublic void " + setName + "(" + name + " " + fieldName + "){");
        prn.println("\t\tthis." + fieldName + " = " + fieldName + ";");
        prn.println("\t}");
    }

    private void buildManyToOneRelationships(Association a, Class c, Class refClassOne, PrintWriter prn, boolean isCounterpartMandatory) throws NamedElementNotFoundException {
        // -- Finding a reference ---------------------------------------------------------------------
        Reference r = extractReferenceToClass(a, c);
        String suggestedName = getSuggestdReferenceItemName(r);
        // -- Building getters and setters ------------------------------------------------------------
        String name = refClassOne.getName();
        // -- Deriving getter and setter names --------------------------------------------------------
        String upperCaseSuggestedName = asUpperCamelCaseNotation(suggestedName != null ? suggestedName : name);
        String getName = "get" + upperCaseSuggestedName;
        String setName = "set" + upperCaseSuggestedName;
        String fieldName = asLowerCamelCaseNotation(suggestedName != null ? suggestedName : name);
        // -- Populating getters and setters for this relation ----------------------------------------
        prn.println("\tpublic " + name + " " + getName + "(){");
        prn.println("\t\treturn " + fieldName + ";");
        prn.println("\t}");
        prn.println("\tpublic void " + setName + "(" + name + " " + fieldName + "){");
        prn.println("\t\tthis." + fieldName + " = " + fieldName + ";");
        prn.println("\t}");
    }

    private void buildOneToManyRelationships(Association a, Class c, Class refClassMany, PrintWriter prn, boolean isCounterpartMandatory) throws NamedElementNotFoundException {
        // -- Looking for the refernece before the everything -----------------------------------------
        Reference r = extractReferenceToClass(a, c);
        String suggestedName = getSuggestdReferenceItemName(r);
        // -- Looking for related suggested name ------------------------------------------------------
        Reference rr = extractReferenceToClass(a, refClassMany);
        String relatedSuggestedName = getSuggestdReferenceItemName(rr);
        String mappedClassName = asUpperCamelCaseNotation(relatedSuggestedName != null ? relatedSuggestedName : c.getName());
        // -- Finding key property first --------------------------------------------------------------
        String refClassName = refClassMany.getName();
        // -- Obtaining information about FK field ----------------------------------------------------
        String mappedByField = asLowerCamelCaseNotation(refClassName);
        // -- Deriving getter and setter names --------------------------------------------------------
        String upperCaseSuggestedName = asUpperCamelCaseNotation(suggestedName != null ? suggestedName : refClassName);
        String getName = "get" + upperCaseSuggestedName;
        String addName = "add" + upperCaseSuggestedName;
        String fieldName = asLowerCamelCaseNotation(suggestedName != null ? suggestedName : mappedByField);
        // -- Populating getters and setters for this relation ----------------------------------------
        prn.println("\tpublic Collection<" + refClassName + "> " + getName + "(){");
        prn.println("\t\treturn " + fieldName + ";");
        prn.println("\t}");
        prn.println("\tpublic void " + addName + "(" + refClassName + " " + mappedByField + "){");
        prn.println("\t\tthis." + fieldName + ".add(" + mappedByField + ");");
        prn.println("\t\tif(" + mappedByField + ".get" + mappedClassName + "() != this" + "){");
        prn.println("\t\t\t" + mappedByField + ".set" + mappedClassName + "(this);");
        prn.println("\t\t}");
        prn.println("\t}");
    }

    private void buildDetailsOneToOneRelationship(Association a, Class c, Class refClassOne, PrintWriter prn, boolean useUniqueManyToOne, boolean isUnique) throws NamedElementNotFoundException {
        Reference r = extractReferenceToClass(a, c);
        String suggestedName = getSuggestdReferenceItemName(r);
        // -- Building getters and setters ------------------------------------------------------------
        String name = refClassOne.getName();
        // -- Deriving getter and setter names --------------------------------------------------------
        String getName = "get" + asUpperCamelCaseNotation(suggestedName != null ? suggestedName : name);
        String setName = "set" + asUpperCamelCaseNotation(suggestedName != null ? suggestedName : name);
        String fieldName = asLowerCamelCaseNotation(suggestedName != null ? suggestedName : name);
        // -- Populating getters and setters for this relation ----------------------------------------
        prn.println("\tpublic " + name + " " + getName + "(){");
        prn.println("\t\treturn " + fieldName + ";");
        prn.println("\t}");
        prn.println("\tpublic void " + setName + "(" + name + " " + fieldName + "){");
        prn.println("\t\tthis." + fieldName + " = " + fieldName + ";");
        prn.println("\t}");
    }

    private void buildPrimaryOneToOneRelationships(Association a, Class c, Class refClassMany, PrintWriter prn, boolean isCounterpartMandatory) throws NamedElementNotFoundException {
        // -- Looking for related suggested name ------------------------------------------------------
        Reference r = extractReferenceToClass(a, c);
        String suggestedName = getSuggestdReferenceItemName(r);
        // -- Finding key property first --------------------------------------------------------------
        String refClassName = refClassMany.getName();
        // -- Obtaining information about FK field ----------------------------------------------------
        String mappedByField = asLowerCamelCaseNotation(refClassName);
        // -- Deriving getter and setter names --------------------------------------------------------
        String getName = "get" + asUpperCamelCaseNotation(suggestedName != null ? suggestedName : refClassName);
        String setName = "set" + asUpperCamelCaseNotation(suggestedName != null ? suggestedName : refClassName);
        String fieldName = asLowerCamelCaseNotation(suggestedName != null ? suggestedName : mappedByField);
        // -- Populating getters and setters for this relation ----------------------------------------
        prn.println("\tpublic " + refClassName + " " + getName + "(){");
        prn.println("\t\treturn " + fieldName + ";");
        prn.println("\t}");
        prn.println("\tpublic void " + setName + "(" + refClassName + " " + mappedByField + "){");
        prn.println("\t\tthis." + fieldName + " = " + mappedByField + ";");
        prn.println("\t}");
    }

    // ------------------------------------------------------------------------------------------------
    // Annotations building methods
    // ------------------------------------------------------------------------------------------------
    private void buildAnnotationsSelfManyToOneRelationships(String fieldName, boolean mandatory, PrintWriter prn) {
        prn.println("\t@ManyToOne(optional=true)"); // TODO: fetch=FetchType.LAZY
        if (mandatory) {
            prn.println("\t@JoinColumn(name=\"" + fieldName + "\", nullable = false)");
        } else {
            prn.println("\t@JoinColumn(name=\"" + fieldName + "\")");
        }
    }

    private void buildAnnotationsSelfOneToManyRelationships(String mappedParentFiledName, PrintWriter prn) {
        prn.println("\t@OneToMany(mappedBy=\"" + asLowerCamelCaseNotation(mappedParentFiledName) + "\")");
    }

    private void buildAnnotationsSelfOneToOneRelationships(String fieldName, boolean mandatory, PrintWriter prn) {
        prn.println("\t@OneToOne(optional=true)"); // TODO: fetch=FetchType.LAZY
        if (mandatory) {
            prn.println("\t@JoinColumn(name=\"" + fieldName + "\", nullable = false)");
        } else {
            prn.println("\t@JoinColumn(name=\"" + fieldName + "\")");
        }
    }

    // --------------------------------------------------------------------------------------------------------
    private void buildAnnotationsManyToOneRelationships(String frKeyName, boolean isCounterpartMandatory, PrintWriter prn) {
        prn.println("\t@ManyToOne");
        if (isCounterpartMandatory) {
            prn.println("\t@JoinColumn(name=\"" + frKeyName + "\", nullable = false)");
        } else {
            prn.println("\t@JoinColumn(name=\"" + frKeyName + "\")");
        }
    }

    private void buildAnnotationsOneToManyRelationships(String mappedClassName, boolean isCounterpartMandatory, PrintWriter prn) {
        prn.println("\t@OneToMany(mappedBy=\"" + asLowerCamelCaseNotation(mappedClassName) + "\")");
        if (isCounterpartMandatory) {
            prn.println("\t@Size(min = 1)");
        }
    }

    private void buildAnnotationsDetailsOneToOneRelationship(String frKeyName, boolean useUniqueManyToOne, boolean isUnique, PrintWriter prn) {
        if (useUniqueManyToOne) {
            prn.println();
            prn.println("\t@ManyToOne");
            prn.println("\t@JoinColumn(name=\"" + frKeyName + "\", unique = true)");
        } else {
            prn.println();
            prn.println("\t@OneToOne");
            if (isUnique) {
                prn.println("\t@JoinColumn(name=\"" + frKeyName + "\", nullable = false)");
            } else {
                prn.println("\t@JoinColumn(name=\"" + frKeyName + "\")");
            }
        }
    }

    private void buildAnnotationsPrimaryOneToOneRelationships(String mappedClassName, boolean isCounterpartMandatory, PrintWriter prn) {
        prn.println("\t@OneToOne(mappedBy=\"" + asLowerCamelCaseNotation(mappedClassName) + "\")");
        if (isCounterpartMandatory) {
            prn.println("\t@NotNull");
        }
    }
}
