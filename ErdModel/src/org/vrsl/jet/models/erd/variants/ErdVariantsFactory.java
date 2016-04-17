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
package org.vrsl.jet.models.erd.variants;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.vrsl.jet.models.cim.persistence.VariantsFactory;
import org.vrsl.jet.models.erd.ErdModelVariant;
import org.vrsl.jet.models.erd.variants.associations.ErdModelCommentAssociation;
import org.vrsl.jet.models.erd.variants.associations.ErdModelEntityAssociation;
import org.vrsl.jet.models.erd.variants.associations.ErdModelReferenceMultiplicity;
import org.vrsl.jet.models.erd.variants.associations.ErdModelReferenceSuggestedName;
import org.vrsl.jet.models.erd.variants.associations.ErdModelTextCommentAssociation;
import org.vrsl.jet.models.erd.variants.details.ErdModelColor;
import org.vrsl.jet.models.erd.variants.details.ErdModelDescription;
import org.vrsl.jet.models.erd.variants.details.ErdModelFont;
import org.vrsl.jet.models.erd.variants.details.ErdModelLocation;
import org.vrsl.jet.models.erd.variants.details.ErdModelMappingDetails;
import org.vrsl.jet.models.erd.variants.details.ErdModelPath;
import org.vrsl.jet.models.erd.variants.details.ErdModelPhysicalLocation;
import org.vrsl.jet.models.erd.variants.details.ErdModelPrimaryKey;
import org.vrsl.jet.models.erd.variants.details.ErdModelSize;
import org.vrsl.jet.models.erd.variants.entities.ErdModelEntity;
import org.vrsl.jet.models.erd.variants.entities.ErdModelText;
import org.vrsl.jet.models.erd.variants.entities.ErdModelTextualComment;
import org.vrsl.jet.models.erd.variants.transformations.SchemaTransformerPreferences;
import org.vrsl.jet.models.erd.variants.types.ErdModelType;

public class ErdVariantsFactory implements VariantsFactory {

    private static final Map<String, Class<?>> variantTagsResolver = new HashMap<>();

    static {
        registerVariantClass(ErdModelCommentAssociation.class);
        registerVariantClass(ErdModelEntityAssociation.class);
        registerVariantClass(ErdModelReferenceMultiplicity.class);
        registerVariantClass(ErdModelTextCommentAssociation.class);
        registerVariantClass(ErdModelReferenceSuggestedName.class);

        registerVariantClass(ErdModelDescription.class);
        registerVariantClass(ErdModelLocation.class);
        registerVariantClass(ErdModelMappingDetails.class);
        registerVariantClass(ErdModelPhysicalLocation.class);
        registerVariantClass(ErdModelPrimaryKey.class);
        registerVariantClass(ErdModelSize.class);
        registerVariantClass(ErdModelFont.class);
        registerVariantClass(ErdModelColor.class);
        registerVariantClass(ErdModelPath.class);

        registerVariantClass(ErdModelEntity.class);
        registerVariantClass(ErdModelText.class);
        registerVariantClass(ErdModelTextualComment.class);

        registerVariantClass(ErdModelType.class);
        
        registerVariantClass(SchemaTransformerPreferences.class);
    }

    static private void registerVariantClass(Class<?> cl) {
        variantTagsResolver.put(cl.getSimpleName(), cl);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T buildVariant(String type, String details) {
        Class<?> cl = variantTagsResolver.get(type);
        if (cl != null) {
            try {
                T res = (T) cl.newInstance();
                if (res instanceof ErdModelVariant) {
                    ((ErdModelVariant) res).initFormText(details);
                }
                return res;
            } catch (InstantiationException | IllegalAccessException ex) {
                Logger.getLogger(ErdVariantsFactory.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        throw new IllegalArgumentException("Type " + type + " has not been supported yet.");
    }
}
