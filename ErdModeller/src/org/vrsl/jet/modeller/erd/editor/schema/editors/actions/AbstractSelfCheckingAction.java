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
package org.vrsl.jet.modeller.erd.editor.schema.editors.actions;

import java.util.Collection;
import java.util.LinkedList;
import javax.swing.AbstractAction;
import org.openide.util.Exceptions;

public abstract class AbstractSelfCheckingAction extends AbstractAction {

    public static final int SLEEP_INTERVAL = 500;
    private static final Thread monitoringThread;
    private static final Object mtLock = new Object();
    private static Collection<AbstractSelfCheckingAction> observables = new LinkedList<>();

    static {
        monitoringThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    for (;;) {
                        Collection<AbstractSelfCheckingAction> lo;
                        synchronized (mtLock) {
                            lo = new LinkedList<>(observables);
                        }
                        for (AbstractSelfCheckingAction a : lo) {
                            a.observeConditions();
                        }
                        Thread.sleep(SLEEP_INTERVAL);
                    }
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });
        monitoringThread.start();
    }

    public void startObserving() {
        synchronized (mtLock) {
            observables.add(this);
        }
    }

    public void finishObserving() {
        synchronized (mtLock) {
            observables.remove(this);
        }
    }

    public abstract void observeConditions();

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone(); //To change body of generated methods, choose Tools | Templates.
    }
}
