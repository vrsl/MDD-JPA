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
package org.vrsl.jet.models.erd.variants.details;

import java.util.Map;
import org.vrsl.jet.models.erd.ErdModelVariant;

public class ErdModelMappingDetails extends ErdModelVariant {

    private String mapFieldTo = "";               // DB firld name for mapping
    private int mapFieldSize = -1;                // Mapping size
    private int mapFieldPrec = -1;
    
    public ErdModelMappingDetails() {
    }

    public ErdModelMappingDetails(String mapFieldTo, int mapFieldSize, int mapFieldPrec) {
        this.mapFieldTo = mapFieldTo;
        this.mapFieldSize = mapFieldSize;
        this.mapFieldPrec = mapFieldPrec;
    }

    /**
     * @return the mapFieldTo
     */
    public String getMapFieldTo() {
        return mapFieldTo;
    }

    /**
     * @return the mapFieldSize
     */
    public int getMapFieldSize() {
        return mapFieldSize;
    }

    /**
     * @return the mapFieldPrec
     */
    public int getMapFieldPrec() {
        return mapFieldPrec;
    }

    @Override
    public String toString() {
        return "fieldName=" + mapFieldTo + ",fieldSize=" + mapFieldSize + ",fieldPrec=" + mapFieldPrec;
    }

    @Override
    public void initFormText(String text) {
        Map<String, String> pairs = parseNameValuePairs(text);
        mapFieldTo = pairs.get("fieldName");
        mapFieldSize = Integer.parseInt(pairs.get("fieldSize"));
        mapFieldPrec = Integer.parseInt(pairs.get("fieldPrec"));
        if("null".endsWith(mapFieldTo)){
            mapFieldTo = "";
        }
    }
}
