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

import java.io.OutputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.vrsl.jet.models.cim.Schema;
import org.vrsl.jet.models.cim.persistence.ModelPersistenceException;
import org.vrsl.jet.models.cim.persistence.ModelWriter;
import org.vrsl.jet.models.internals.persistance.sxml.writers.SchemaWriter;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class XmlSimpleWriter implements ModelWriter {

    @Override
    public void write(OutputStream sysOut, Schema schema) throws ModelPersistenceException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();  // Create from whole cloth

            Element root = doc.createElement("Model");
            doc.appendChild(root);

            SchemaWriter writer = (SchemaWriter) NamedElementWriterBuilder.build(schema);
            writer.write(root, schema);

            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer tr = tf.newTransformer();
            DOMSource src = new DOMSource(doc);
            StreamResult result = new StreamResult(sysOut);

            tr.transform(src, result);
        } catch (ParserConfigurationException | DOMException | TransformerFactoryConfigurationError | TransformerException ex) {
            throw new ModelPersistenceException(ex);
        }
    }
}
