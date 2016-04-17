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


public class Class extends NamedElement {

    // -- Methods and properties ------------------------------
    private final List<Property> properties = new ArrayList<>();
    private final List<Method> methods = new ArrayList<>();
    // -- Class relations -------------------------------------
    private Class supertype;
    private final List<Class> subtypes = new ArrayList<>();
    private final List<Reference> range = new ArrayList<>();

    // == Properties ==========================================
    public final synchronized void add(Property property) {
        properties.add(property);
    }
    
    public final synchronized void remove(Property property) {
        properties.remove(property);
    }
    
    public final List<Property> getProperties() {
        return difensiveCopy(properties);
    }
    
    public final List<Property> getProperties(Qualifier<?> q) {
        return filterListElements(properties, q);
    }
    
    // == Methods =============================================
    public final synchronized void add(Method method) {
        methods.add(method);
    }
    
    public final synchronized void remove(Method method) {
        methods.remove(method);
    }
    
    public final List<Method> getMethdos() {
        return difensiveCopy(methods);
    }
    
    public final List<Method> getMethods(Qualifier<?> q) {
        return filterListElements(methods, q);
    }

    // == Subtypes ============================================
    public final synchronized void add(Class subtype) {
        subtypes.add(subtype);
    }
    
    public final synchronized void remove(Class subtype) {
        subtypes.remove(subtype);
    }
    
    public final List<Class> getSubTypes() {
        return difensiveCopy(subtypes);
    }

    public final List<Class> getSubTypes(Qualifier<?> q) {
        return filterListElements(subtypes, q);
    }
    
    // == Supertype ===========================================
    public final synchronized void setSuperType(Class value) {
        supertype = value;
    }
    
    public final synchronized Class getSuperType() {
        return supertype;
    }
    
    // == Range ===============================================
    public final synchronized void addRange(Reference reference) {
        range.add(reference);
    }
    
    public final synchronized void removeRange(Reference reference) {
        range.remove(reference);
    }
    
    public final List<Reference> getRange() {
        return difensiveCopy(range);
    }
    
    public final List<Reference> getRange(Qualifier<?> q) {
        return filterListElements(range, q);
    }
}
