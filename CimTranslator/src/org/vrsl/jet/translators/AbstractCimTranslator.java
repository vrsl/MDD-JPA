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
package org.vrsl.jet.translators;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;
import org.vrsl.jet.models.cim.Association;
import org.vrsl.jet.models.cim.Class;
import org.vrsl.jet.models.cim.Qualifier;
import org.vrsl.jet.models.cim.Reference;
import org.vrsl.jet.models.cim.Schema;
import org.vrsl.jet.translators.properties.TranslatorPropertyMetadata;

public abstract class AbstractCimTranslator {
    
    protected static Collection<Association> findAssociations(Schema s, Class c) {
        LinkedList<Association> res = new LinkedList<>();
        for (Association a : s.<Association>getElements(Association.class)) {
            for (Reference r : a.getReferences()) {
                if(r.getReferedClass().getName().equals(c.getName()) && !res.contains(a)) {
                    res.add(a);
                }
            }
        }
        return res;
    }
    
    protected static Collection<Association> findAssociations(Schema s, Class c, Qualifier<?> q) {
        LinkedList<Association> res = new LinkedList<>();
        for (Association a : s.<Association>getElements(q)) {
            for (Reference r : a.getReferences()) {
                if(r.getReferedClass().getName().equals(c.getName())) {
                    res.add(a);
                }
            }
        }
        return res;
    }
    
    public abstract String getSource();
    
    public abstract String getTarget();
    public abstract String getName();
    public abstract Map<String, Object> getProperties();

    public abstract void setProperties(Map<String, Object> properties);

    public Map<String, TranslatorPropertyMetadata> getPropertiesMetadata() {
        Map<String, TranslatorPropertyMetadata> res = new TreeMap<>();
        for(String key : getProperties().keySet()) {
            res.put(key, new TranslatorPropertyMetadata(TranslatorPropertyMetadata.Type.STRING));
        }
        return res;
    }
    
    public ValidationResult validateProperties(String path, Map<String, Object> properties) {
        return new ValidationResult();
    }
     
    public abstract void translate(Schema schema, String path) throws CimTranslatorError;
}
