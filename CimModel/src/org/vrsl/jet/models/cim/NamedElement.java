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
import java.util.List;

public abstract class NamedElement {

    private String name;
    private final List<Qualifier<?>> qualifiers = new ArrayList<>();
    private final List<Trigger> triggers = new ArrayList<>();
    private Schema schema;

    public final synchronized String getName() {
        return name;
    }

    public final synchronized void setName(String value) {
        name = value;
    }

    public final List<Qualifier<?>> getQualifiers() {
        List<Qualifier<?>> res;
        synchronized (this) {
            res = new ArrayList<>(qualifiers);
        }
        return res;
    }

    @SuppressWarnings("unchecked")
    public final <T> Qualifier<T> getFirstByVariantType(java.lang.Class<T> variantType) throws NamedElementNotFoundException {
        synchronized (this) {
            for (Qualifier<?> q : qualifiers) {
                if (q.getValue().getClass().equals(variantType)) {
                    return (Qualifier<T>) q;
                }
            }
        }
        throw new NamedElementNotFoundException("Qualifier with " + variantType.getName() + " has not been found.");
    }

    @SuppressWarnings("unchecked")
    public final <T> List<Qualifier<T>> getByVariantType(java.lang.Class<T> variantType) throws NamedElementNotFoundException {
        List<Qualifier<T>> res = new ArrayList<>();
        synchronized (this) {
            for (Qualifier<?> q : qualifiers) {
                if (q.getValue().getClass().equals(variantType)) {
                    res.add((Qualifier<T>) q);
                }
            }
        }
        return res;
    }

    public final <T> boolean hasVariantTypeQualifier(java.lang.Class<T> variantType) {
        synchronized (this) {
            for (Qualifier<?> q : qualifiers) {
                if (q.getValue().getClass().equals(variantType)) {
                    return true;
                }
            }
        }
        return false;
    }

    public final synchronized <T> void add(Qualifier<T> quail) {
        qualifiers.add(quail);
    }

    public final synchronized <T> void remove(Qualifier<T> quail) {
        qualifiers.remove(quail);
    }

    public final List<Trigger> getTriggers() {
        List<Trigger> res;
        synchronized (this) {
            res = new ArrayList<>(triggers);
        }
        return res;
    }

    public final synchronized void add(Trigger trigger) {
        triggers.add(trigger);
    }

    public final synchronized void remove(Trigger trigger) {
        triggers.remove(trigger);
    }

    public final synchronized void setSchema(Schema schema) {
        this.schema = schema;
    }

    public final synchronized Schema getSchema() {
        return schema;
    }

    protected final synchronized <T extends NamedElement> List<T> filterListElements(Collection<T> elements, Qualifier<?> q) {
        List<T> res = new ArrayList<>();
        for (T e : elements) {
            for (Qualifier<?> eq : e.getQualifiers()) {
                if (eq.getValue().getClass().equals(q.getValue().getClass())) {
                    res.add(e);
                }
            }
        }
        return res;
    }

    protected final synchronized <T extends NamedElement> List<T> filterListElements(Collection<T> elements, String name) {
        List<T> res = new ArrayList<>();
        for (T e : elements) {
            if (name.equals(e.getName())) {
                res.add(e);
            }
        }
        return res;
    }

    protected final synchronized <T extends NamedElement> List<T> filterListElements(Collection<T> elements, java.lang.Class<T> c) {
        List<T> res = new ArrayList<>();
        for (T e : elements) {
            if (c.equals(e.getClass())) {
                res.add(e);
            }
        }
        return res;
    }

    protected final <T> List<T> difensiveCopy(List<T> elements) {
        List<T> res;
        synchronized (this) {
            res = new ArrayList<>(elements);
        }
        return res;
    }
}
