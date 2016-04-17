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
package org.vrsl.jet.models.internals.persistance.sxml;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.vrsl.jet.models.cim.Association;
import org.vrsl.jet.models.cim.Class;
import org.vrsl.jet.models.cim.Method;
import org.vrsl.jet.models.cim.NamedElement;
import org.vrsl.jet.models.cim.Property;
import org.vrsl.jet.models.cim.Qualifier;
import org.vrsl.jet.models.cim.Reference;
import org.vrsl.jet.models.cim.Schema;
import org.vrsl.jet.models.cim.persistence.ModelPersistenceException;
import org.vrsl.jet.models.cim.persistence.ModelReader;
import org.vrsl.jet.models.cim.persistence.VariantsFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author JET
 */
public class XmlSimpleReader implements ModelReader {

    private final VariantsFactory vf;
    private final Schema schema;

    public XmlSimpleReader(VariantsFactory vf) {
        this.vf = vf;
        schema = new Schema();
    }

    @Override
    public Schema read(InputStream sysIn) throws ModelPersistenceException {
        try {
            XPath xpath = XPathFactory.newInstance().newXPath();
            // -- Making DOM -------------------------------------------------------------------
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(sysIn);
            // -- Reading Schema information  --------------------------------------------------
            Element schemaNode = (Element) xpath.evaluate("/Model/Schema", doc, XPathConstants.NODE);
            schema.setName(schemaNode.getAttribute("Name"));
            // -- Reading Qualifiers first -----------------------------------------------------
            NodeList nodes = (NodeList) xpath.evaluate("/Model/Schema/Qualifier", doc, XPathConstants.NODESET);
            for (int i = 0; i < nodes.getLength(); i++) {
                Qualifier<?> q = readQualifier((Element) nodes.item(i));
                schema.add(q);
            }
            // -- Starting with constructing classes -------------------------------------------
            nodes = (NodeList) xpath.evaluate("/Model/Schema/Class", doc, XPathConstants.NODESET);
            for (int i = 0; i < nodes.getLength(); i++) {
                schema.add(readClass((Element) nodes.item(i)));
            }
            // -- Then reading associations ----------------------------------------------------
            nodes = (NodeList) xpath.evaluate("/Model/Schema/Association", doc, XPathConstants.NODESET);
            for (int i = 0; i < nodes.getLength(); i++) {
                schema.add(readAssociation((Element) nodes.item(i)));
            }
            // -- Returning the result ---------------------------------------------------------
            return schema;
        } catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException ex) {
            throw new ModelPersistenceException(ex);
        }
    }

    private Class readClass(Element e) throws XPathExpressionException {
        XPath xpath = XPathFactory.newInstance().newXPath();
        // -- Building resulting class -----------------------------------------------------
        Class c = new Class();
        c.setName(e.getAttribute("Name"));
        // -- Reading Qualifiers first -----------------------------------------------------
        NodeList nodes = (NodeList) xpath.evaluate("Qualifier", e, XPathConstants.NODESET);
        for (int i = 0; i < nodes.getLength(); i++) {
            Qualifier<?> q = readQualifier((Element) nodes.item(i));
            c.add(q);
        }
        // -- Then we are reading Properties -----------------------------------------------
        nodes = (NodeList) xpath.evaluate("Property", e, XPathConstants.NODESET);
        for (int i = 0; i < nodes.getLength(); i++) {
            Property p = readProperty((Element) nodes.item(i));
            c.add(p);
        }
        // -- Finally we are reading Properties --------------------------------------------
        nodes = (NodeList) xpath.evaluate("Method", e, XPathConstants.NODESET);
        for (int i = 0; i < nodes.getLength(); i++) {
            Method p = readMethod((Element) nodes.item(i));
            c.add(p);
        }
        // -- Returing the result ----------------------------------------------------------
        return c;
    }

    private Property readProperty(Element e) throws XPathExpressionException {
        XPath xpath = XPathFactory.newInstance().newXPath();
        // -- Building resulting class -----------------------------------------------------
        Property p = new Property();
        p.setName(e.getAttribute("Name"));
        // -- Reading Qualifiers first -----------------------------------------------------
        NodeList nodes = (NodeList) xpath.evaluate("Qualifier", e, XPathConstants.NODESET);
        for (int i = 0; i < nodes.getLength(); i++) {
            Qualifier<?> q = readQualifier((Element) nodes.item(i));
            p.add(q);
        }
        // -- Returing the result ----------------------------------------------------------
        return p;
    }

    private Method readMethod(Element e) throws XPathExpressionException {
        XPath xpath = XPathFactory.newInstance().newXPath();
        // -- Building resulting class -----------------------------------------------------
        Method m = new Method();
        m.setName(e.getAttribute("Name"));
        // -- Reading Qualifiers first -----------------------------------------------------
        NodeList nodes = (NodeList) xpath.evaluate("Qualifier", e, XPathConstants.NODESET);
        for (int i = 0; i < nodes.getLength(); i++) {
            Qualifier<?> q = readQualifier((Element) nodes.item(i));
            m.add(q);
        }
        // -- Returing the result ----------------------------------------------------------
        return m;
    }

    private Association readAssociation(Element e) throws XPathExpressionException {
        XPath xpath = XPathFactory.newInstance().newXPath();
        // -- Reading information for the parret - Class -----------------------------------
        Node cl = (Node) xpath.evaluate("Class", e, XPathConstants.NODE);
        Class parentClass = readClass((Element) cl);
        // -- Building references first ----------------------------------------------------
        List<Reference> refs = new ArrayList<>();
        NodeList nodes = (NodeList) xpath.evaluate("Reference", e, XPathConstants.NODESET);
        for (int i = 0; i < nodes.getLength(); i++) {
            refs.add(readReference((Element) nodes.item(i)));
        }
        // -- Building resulting class -----------------------------------------------------
        Association a = new Association(parentClass, refs.get(0), refs.get(1));
        // -- Adding extra references if we have any ---------------------------------------
        for (int i = 2; i < refs.size(); i++) {
            a.add(refs.get(i));
        }
        // -- Returing the result ----------------------------------------------------------
        return a;
    }

    private Reference readReference(Element e) throws XPathExpressionException {
        XPath xpath = XPathFactory.newInstance().newXPath();
        // -- Looking for refered class first ----------------------------------------------
        String refClassName = e.getAttribute("ClassName");
        List<NamedElement> candidates = schema.getElements(refClassName);
        if (candidates.isEmpty() || candidates.size() > 1) {
            throw new IllegalStateException(
                    "More then one class with name "
                    + refClassName
                    + " has been found in "
                    + schema.getName()
                    + " schema.");
        }
        if (!(candidates.get(0) instanceof Class)) {
            throw new IllegalStateException(
                    "Schema element "
                    + refClassName
                    + " is not a class instance.");
        }
        Class c = (Class) candidates.get(0);
        // -- Building resulting class -----------------------------------------------------
        Reference m = new Reference(c);
        // -- Reading Qualifiers first -----------------------------------------------------
        NodeList nodes = (NodeList) xpath.evaluate("Qualifier", e, XPathConstants.NODESET);
        for (int i = 0; i < nodes.getLength(); i++) {
            Qualifier<?> q = readQualifier((Element) nodes.item(i));
            m.add(q);
        }
        // -- Returing the result ----------------------------------------------------------
        return m;
    }

    private Qualifier<?> readQualifier(Element e) {
        String t = e.getAttribute("Type");
        String c = e.getTextContent();
        return new Qualifier<>(vf.buildVariant(t, c));
    }
}
