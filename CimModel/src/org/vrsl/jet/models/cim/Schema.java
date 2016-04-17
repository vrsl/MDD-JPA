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
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class Schema extends NamedElement {

    @SuppressWarnings("unchecked")
    private static <T extends NamedElement> Collection<T> extractTypes(List<NamedElement> elements) {
        Collection<T> res = new LinkedList<>();
        for (NamedElement e : elements) {
            res.add((T) e);
        }
        return res;
    }

    private final List<NamedElement> elements = new ArrayList<>();

    public final synchronized void add(NamedElement element) {
        element.setSchema(this);
        elements.add(element);
    }

    public final synchronized void remove(NamedElement element) {
        elements.remove(element);
    }

    public final List<NamedElement> getElements() {
        return difensiveCopy(elements);
    }

    public final synchronized <T extends NamedElement> List<T> getElements(Qualifier<?> q) {
        Collection<T> e = extractTypes(elements);
        return filterListElements(e, q);
    }

    public final synchronized <T extends NamedElement> List<T> getElements(String name) {
        Collection<T> e = extractTypes(elements);
        return filterListElements(e, name);
    }

    public final synchronized <T extends NamedElement> List<T> getElements(java.lang.Class<T> cl) {
        Collection<T> e = extractTypes(elements);
        return filterListElements(e, cl);
    }

}
