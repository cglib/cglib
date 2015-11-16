package net.sf.cglib.core;

import java.lang.ref.WeakReference;

/**
 * Allows to check for reference identity, yet the class does not keep strong reference to the target.
 * {@link #equals(Object)} returns true if and only if the reference is not yet expired and other
 * object is referencing exactly the same object.
 *
 * @param <T> type of the reference
 */
public class WeakIdentityKey<T> extends WeakReference<T> {
    private final int hash;

    public WeakIdentityKey(T referent) {
        super(referent);
        this.hash = System.identityHashCode(referent);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof WeakIdentityKey)) {
            return false;
        }
        Object ours = get();
        Object theirs = ((WeakIdentityKey) obj).get();
        return ours == theirs && ours != null;
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public String toString() {
        T t = get();
        return t == null ? "Clean WeakIdentityKey, hash: " + hash : t.toString();
    }
}
