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
package org.vrsl.jet.models.cim;

import java.util.ArrayList;
import java.util.List;

public class Association extends Class {

    private List<Reference> references = new ArrayList<>();

    public Association(Reference ref1, Reference ref2) {
        add(ref1);
        add(ref2);
    }

    public Association(Class parrent, Reference ref1, Reference ref2) {
        for (Qualifier<?> q : parrent.getQualifiers()) {
            add(q);
        }
        for (Property p : parrent.getProperties()) {
            add(p);
        }
        for (Method m : parrent.getMethdos()) {
            add(m);
        }
        // -- Adding references -----------------------------------
        add(ref1);
        add(ref2);
    }

    public final synchronized void add(Reference reference) {
        references.add(reference);
        reference.setAssociation(this);
    }

    public final List<Reference> getReferences() {
        return difensiveCopy(references);
    }

    public final List<Reference> getReferences(Qualifier<?> q) {
        return filterListElements(references, q);
    }
}
