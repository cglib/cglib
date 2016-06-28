package net.sf.cglib.core;

import org.objectweb.asm.Type;

public interface HashCodeCustomizer extends KeyFactoryCustomizer {
    /**
     * Customizes calculation of hashcode. {@link Object#hashCode()} might be slow for certain classes,
     * thus a customizer might be used to hash objects faster (e.g. by using just a subset of relevant
     * fields).
     * <p>A default implementation uses {@code e.invoke_virtual(Constants.TYPE_OBJECT, HASH_CODE)}
     * @param e code emitter
     * @param type parameter type
     * @return true if the customizer supports given {@link Type}
     */
    boolean customize(CodeEmitter e, Type type);
}
