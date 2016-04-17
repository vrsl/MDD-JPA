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

import org.vrsl.jet.models.cim.Association;
import org.vrsl.jet.models.cim.Class;
import org.vrsl.jet.models.cim.Method;
import org.vrsl.jet.models.cim.NamedElement;
import org.vrsl.jet.models.cim.Property;
import org.vrsl.jet.models.cim.Qualifier;
import org.vrsl.jet.models.cim.Reference;
import org.vrsl.jet.models.cim.Schema;
import org.vrsl.jet.models.cim.Trigger;
import org.vrsl.jet.models.internals.persistance.sxml.writers.AbstractNamedElementWriter;
import org.vrsl.jet.models.internals.persistance.sxml.writers.AssociationWriter;
import org.vrsl.jet.models.internals.persistance.sxml.writers.ClassWriter;
import org.vrsl.jet.models.internals.persistance.sxml.writers.MethodWriter;
import org.vrsl.jet.models.internals.persistance.sxml.writers.PropertyWriter;
import org.vrsl.jet.models.internals.persistance.sxml.writers.QualifierWriter;
import org.vrsl.jet.models.internals.persistance.sxml.writers.ReferenceWriter;
import org.vrsl.jet.models.internals.persistance.sxml.writers.SchemaWriter;
import org.vrsl.jet.models.internals.persistance.sxml.writers.TriggerWriter;

/**
 *
 * @author JET
 */
public class NamedElementWriterBuilder {

    public static AbstractNamedElementWriter build(NamedElement e) {
        if (e instanceof Association) {
            return new AssociationWriter();
        } else if (e instanceof Class) {
            return new ClassWriter();
        } else if (e instanceof Method) {
            return new MethodWriter();
        } else if (e instanceof Reference) {
            return new ReferenceWriter();
        } else if (e instanceof Property) {
            return new PropertyWriter();
        } else if (e instanceof Qualifier) {
            return new QualifierWriter();
        } else if (e instanceof Schema) {
            return new SchemaWriter();
        } else if (e instanceof Trigger) {
            return new TriggerWriter();
        }
        return null;
    }

    private NamedElementWriterBuilder() {
    }
}
