package com.didi.aoe.library;

import android.os.RemoteException;

/**
 * @author noctis
 */
@SuppressWarnings("WeakerAccess")
public class AoeRemoteException extends RemoteException {
    public AoeRemoteException(String message) {
        super(message);
    }
}
