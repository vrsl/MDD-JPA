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
package org.vrsl.jet.models.erd.variants.transformations;

import java.util.LinkedHashMap;
import java.util.Map;
import org.vrsl.jet.models.erd.ErdModelVariant;

public class SchemaTransformerPreferences extends ErdModelVariant {

    private String transformerName;
    private Map<String, String> properties;

    public SchemaTransformerPreferences() {
        properties = new LinkedHashMap<>();
    }

    public String getTransformerName() {
        return transformerName;
    }

    public void setTransformerName(String transformerName) {
        this.transformerName = transformerName;
    }

    public void registerProperty(String name, String value) {
        properties.put(name, value);
    }

    public String getProperty(String name) {
        return properties.get(name);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("transformer").append('=').append(transformerName).append('{');
        if (!properties.isEmpty()) {
            for (String key : properties.keySet()) {
                sb.append(key).append('=').append(properties.get(key)).append(',');
            }
            sb.setLength(sb.length() - 1);
        }
        sb.append('}');
        return sb.toString();
    }

    @Override
    public void initFormText(String text) {
        String transformerNameKeyPair = text.substring(0, text.indexOf('{'));
        String propertyesKeyPairs = text.substring(text.indexOf('{'), text.length()).replace('{', ' ').replace('}', ' ').trim();

        transformerName = parseNameValuePairs(transformerNameKeyPair).get("transformer");
        properties = parseNameValuePairs(propertyesKeyPairs);
    }
}
