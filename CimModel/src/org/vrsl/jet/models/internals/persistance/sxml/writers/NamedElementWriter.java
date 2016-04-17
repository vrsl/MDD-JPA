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
package org.vrsl.jet.models.internals.persistance.sxml.writers;

import java.util.Collection;
import org.vrsl.jet.models.cim.NamedElement;
import org.vrsl.jet.models.internals.persistance.sxml.NamedElementWriterBuilder;
import org.w3c.dom.Element;

/**
 *
 * @author JET
 */
public class NamedElementWriter extends AbstractNamedElementWriter {

    @Override
    public void write(Element xmlElement, NamedElement e) {
        if (e.getSchema() != null) {
            xmlElement.setAttribute("SchemaName", e.getSchema().getName());
        }
        if (e.getName() != null) {
            xmlElement.setAttribute("Name", e.getName());
        }

        if (e.getQualifiers() != null) {
            write(xmlElement, e.getQualifiers());
        }
        if (e.getTriggers() != null) {
            write(xmlElement, e.getTriggers());
        }
    }

    protected void write(Element xmlElement, Collection<? extends NamedElement> elements) {
        for (NamedElement e : elements) {
            AbstractNamedElementWriter writer = NamedElementWriterBuilder.build(e);
            writer.write(xmlElement, e);
        }
    }
}
