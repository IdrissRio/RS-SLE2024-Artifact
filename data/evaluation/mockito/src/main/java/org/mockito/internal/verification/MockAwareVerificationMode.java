/*
 * Copyright (c) 2007 Mockito contributors
 * This program is made available under the terms of the MIT License.
 */
package org.mockito.internal.verification;

import java.util.Set;

import org.mockito.internal.verification.api.VerificationData;
import org.mockito.listeners.VerificationListener;
import org.mockito.verification.VerificationEvent;
import org.mockito.verification.VerificationMode;

public class MockAwareVerificationMode implements VerificationMode {

    private final Object mock;
    private final VerificationMode mode;
    private final Set<VerificationListener> listeners;

    public MockAwareVerificationMode(
            Object mock, VerificationMode mode, Set<VerificationListener> listeners) {
        this.mock = mock;
        this.mode = mode;
        this.listeners = listeners;
    }

    @Override
    public void verify(VerificationData data) {
        try {
            mode.verify(data);
            notifyListeners(new VerificationEventImpl(mock, mode, data, null));
        } catch (RuntimeException | Error e) {
            notifyListeners(new VerificationEventImpl(mock, mode, data, e));
            throw e;
        }
    }

    private void notifyListeners(VerificationEvent event) {
        for (VerificationListener listener : listeners) {
            listener.onVerification(event);
        }
    }

    public Object getMock() {
        return mock;
    }
}
