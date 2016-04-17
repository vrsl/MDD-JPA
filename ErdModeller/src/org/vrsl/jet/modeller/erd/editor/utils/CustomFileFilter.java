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
package org.vrsl.jet.modeller.erd.editor.utils;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Locale;

public class CustomFileFilter extends javax.swing.filechooser.FileFilter {

    private static String getExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');
        
        if (i > 0 && i < s.length() - 1) {
            ext = s.substring(i + 1).toLowerCase(Locale.getDefault());
        }
        return ext;
    }

    private final Collection<String> ext;
    private String name;

    public CustomFileFilter() {
        ext = new LinkedList<>();
    }

    public CustomFileFilter(String name, String fExt) {
        ext = new LinkedList<>();
        this.name = name;
        ext.add(fExt);
    }

    public void addExt(String newExt) {
        ext.add(newExt);
    }

    public void setDescription(String name) {
        this.name = name;
    }

    @Override
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }
        String extension = getExtension(f);
        if (extension != null) {
            for(String ex : ext) {
                if(extension.equals(ex)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String getDescription() {
        return name;
    }

}
