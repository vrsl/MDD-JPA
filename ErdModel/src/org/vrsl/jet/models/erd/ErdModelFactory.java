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
package org.vrsl.jet.models.erd;

import org.vrsl.jet.models.cim.Association;
import org.vrsl.jet.models.cim.Class;
import org.vrsl.jet.models.cim.Property;
import org.vrsl.jet.models.cim.Qualifier;
import org.vrsl.jet.models.cim.Reference;
import org.vrsl.jet.models.cim.Schema;
import org.vrsl.jet.models.erd.variants.associations.ErdModelReferenceMultiplicity;
import org.vrsl.jet.models.erd.variants.details.ErdModelPhysicalLocation;

public class ErdModelFactory {
    
    public static Class buildClass() {
        return new Class();
    }
    
    public static Class buildClass(String name) {
        Class res = new Class();
        res.setName(name);
        return res;
    }
    
    public static Property buildProperty() {
        return new Property();
    }
    
    public static Reference buildReference(Class referedClass, ErdModelReferenceMultiplicity multiplicity) {
        Reference res = new Reference(referedClass);
        res.add(new Qualifier<>(multiplicity));
        return res;
    }
    
    public static Reference buildReference(Class referedClass) {
        return new Reference(referedClass);
    }
    
    public static Association buildAssociation(Reference ref1, Reference ref2) {
        return new Association(ref1, ref2);
    }
    
    public static Schema buildSchema(String name) {
        Schema res = new Schema();
        res.setName(name);
        res.setSchema(res);
        return res;
    }
    
    public static Schema buildSchema(String name, String filePath) {
        Schema res = buildSchema(name);
        ErdModelPhysicalLocation location = new ErdModelPhysicalLocation(filePath);
        res.add(new Qualifier<>(location));
        return res;
    }

    private ErdModelFactory() {
    }
}
