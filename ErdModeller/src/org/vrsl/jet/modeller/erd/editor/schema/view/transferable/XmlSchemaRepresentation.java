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
package org.vrsl.jet.modeller.erd.editor.schema.view.transferable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.openide.util.Exceptions;
import org.vrsl.jet.modeller.erd.editor.schema.view.SchamaClass;
import org.vrsl.jet.modeller.erd.editor.schema.view.SchemaRectangleElement;
import org.vrsl.jet.modeller.erd.editor.schema.view.associations.CommentAssociation;
import org.vrsl.jet.modeller.erd.editor.schema.view.associations.ElementsAssociation;
import org.vrsl.jet.modeller.erd.editor.schema.view.elements.Comment;
import org.vrsl.jet.modeller.erd.editor.schema.view.elements.Entity;
import org.vrsl.jet.modeller.erd.editor.schema.view.elements.FreeText;
import org.vrsl.jet.models.cim.NamedElement;
import org.vrsl.jet.models.cim.NamedElementNotFoundException;
import org.vrsl.jet.models.cim.Qualifier;
import org.vrsl.jet.models.cim.Schema;
import org.vrsl.jet.models.cim.persistence.ModelPersistenceException;
import org.vrsl.jet.models.cim.persistence.ModelReader;
import org.vrsl.jet.models.cim.persistence.ModelReaderFactory;
import org.vrsl.jet.models.cim.persistence.ModelWriter;
import org.vrsl.jet.models.cim.persistence.ModelWriterFactory;
import org.vrsl.jet.models.erd.ErdModelFactory;
import org.vrsl.jet.models.erd.variants.ErdVariantsFactory;
import org.vrsl.jet.models.erd.variants.associations.ErdModelEntityAssociation;
import org.vrsl.jet.models.erd.variants.associations.ErdModelTextCommentAssociation;
import org.vrsl.jet.models.erd.variants.entities.ErdModelEntity;
import org.vrsl.jet.models.erd.variants.entities.ErdModelText;
import org.vrsl.jet.models.erd.variants.entities.ErdModelTextualComment;

public class XmlSchemaRepresentation implements Serializable {

    static final long serialVersionUID = 240000002;
    private String content = null;
    private Schema schema = null;

    /**
     * Creates a new instance of XmlSchemaRepresentation
     */
    public XmlSchemaRepresentation() {
    }

    /**
     * Creates a new instance of XmlSchemaRepresentation
     */
    public XmlSchemaRepresentation(Entity[] objects, ElementsAssociation[] associations, SchamaClass[] texts, CommentAssociation[] comAssociations) {
        content = convertToStringBuffer(objects, associations, texts, comAssociations);
    }

    private String convertToStringBuffer(Entity[] objects, ElementsAssociation[] associations, SchamaClass[] texts, CommentAssociation[] comAssociations) {
        try {
            Schema s = ErdModelFactory.buildSchema("Test");

            // -- Storing information about ERD objects -------------------------------------------
            for (Entity ev : objects) {
                ev.addTo(s);
            }
            // -- Storing information about text --------------------------------------------------
            for (SchamaClass ao : texts) {
                ao.addTo(s);
            }
            // -- Storing information about ERD objects associations ------------------------------
            for (ElementsAssociation association : associations) {
                association.addTo(s);
            }
            // -- Storing information about comments associations ---------------------------------
            for (CommentAssociation association : comAssociations) {
                association.addTo(s);
            }

            ByteArrayOutputStream sysOut = new ByteArrayOutputStream();
            ModelWriter writer = ModelWriterFactory.newInstance();
            writer.write(sysOut, s);
            // --------------------------------------------------------------------------------
            return sysOut.toString("utf-8");
        } catch (ModelPersistenceException | UnsupportedEncodingException ex) {
            Exceptions.printStackTrace(ex);
        }

        return null;
    }

    public Entity[] getObjects() {

        List<Entity> data = new ArrayList<>();

        if (schema == null) {
            buildSchema();
        }

        try {
            List<NamedElement> entities = schema.getElements(new Qualifier<>(new ErdModelEntity()));
            for (NamedElement e : entities) {
                data.add(new Entity(e, data));
            }
        } catch (NamedElementNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }

        return data.toArray(new Entity[data.size()]);
    }

    public SchamaClass[] getTexts() {

        List<SchemaRectangleElement> texts = new ArrayList<>();

        if (schema == null) {
            buildSchema();
        }

        try {
            List<NamedElement> txts = schema.getElements(new Qualifier<>(new ErdModelText()));
            for (NamedElement t : txts) {
                texts.add(new FreeText(t));
            }

            List<NamedElement> coments = schema.getElements(new Qualifier<>(new ErdModelTextualComment()));
            for (NamedElement c : coments) {
                texts.add(new Comment(c));
            }
        } catch (NamedElementNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }

        return texts.<SchamaClass>toArray(new SchamaClass[texts.size()]);
    }

    public ElementsAssociation[] getAssociations(Entity[] objects, SchamaClass[] text) {
        List<Entity> data = new ArrayList<>();
        List<ElementsAssociation> resultAssociations = new ArrayList<>();
        data.addAll(Arrays.<Entity>asList(objects));
        try {
            List<NamedElement> associations = schema.getElements(new Qualifier<>(new ErdModelEntityAssociation()));
            for (NamedElement a : associations) {
                ElementsAssociation ea = new ElementsAssociation(data, a);
                int paralelNumber = 0;
                for (ElementsAssociation lnk : resultAssociations) {
                    if (areAssociationsIdentical(lnk, ea)) {
                        paralelNumber++;
                    }
                }
                ea.setParalelNumber(paralelNumber);
                resultAssociations.add(ea);
            }
        } catch (NamedElementNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
        return resultAssociations.toArray(new ElementsAssociation[resultAssociations.size()]);
    }

    public CommentAssociation[] getCommentAssociations(Entity[] objects, SchamaClass[] text) {
        List<Entity> data = new ArrayList<>();
        List<SchemaRectangleElement> texts = new ArrayList<>();
        List<ElementsAssociation> resultAssociations = new ArrayList<>();
        data.addAll(Arrays.<Entity>asList(objects));
        texts.addAll(Arrays.<SchamaClass>asList(text));

        if (schema == null) {
            buildSchema();
        }

        try {
            List<NamedElement> commentAssociations = schema.getElements(new Qualifier<>(new ErdModelTextCommentAssociation()));
            for (NamedElement a : commentAssociations) {
                resultAssociations.add(new CommentAssociation(data, texts, a));
            }
        } catch (NamedElementNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
        return resultAssociations.<CommentAssociation>toArray(new CommentAssociation[resultAssociations.size()]);
    }

    private void buildSchema() {
        try(InputStream sysIn = new ByteArrayInputStream(content.getBytes())) {
            ModelReader r = ModelReaderFactory.newInstance(new ErdVariantsFactory());
            schema = r.read(sysIn);
        } catch (ModelPersistenceException | IOException ex) {
            Exceptions.printStackTrace(ex);
            schema = null;
        }

    }
    
    private boolean areAssociationsIdentical(ElementsAssociation lnk1, ElementsAssociation lnk2) {
        return (lnk1.getFromObject() == lnk2.getFromObject() && lnk1.getToObject() == lnk2.getToObject())
                || (lnk1.getFromObject() == lnk2.getToObject() && lnk1.getToObject() == lnk2.getFromObject());
    }
}
