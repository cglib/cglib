package net.sf.cglib.core;

import java.util.*;
import java.lang.reflect.*;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;

public class ReflectOps {
    private static final Signature GET_NAME =
      TypeUtils.parseSignature("String getName()");
    private static final Signature EQUALS =
      TypeUtils.parseSignature("boolean equals(Object)");

    private ReflectOps() {
    }

    private interface ParameterTyper {
        Class[] getParameterTypes(Object member);
    }

    public static void method_switch(CodeEmitter e,
                                     Method[] methods,
                                     ObjectSwitchCallback callback) {
        member_switch_helper(e, Arrays.asList(methods), callback, true, new ParameterTyper() {
            public Class[] getParameterTypes(Object member) {
                return ((Method)member).getParameterTypes();
            }
        });
    }

    public static void constructor_switch(CodeEmitter e,
                                          Constructor[] cstructs,
                                          ObjectSwitchCallback callback) {
        member_switch_helper(e, Arrays.asList(cstructs), callback, false, new ParameterTyper() {
            public Class[] getParameterTypes(Object member) {
                return ((Constructor)member).getParameterTypes();
            }
        });
    }

    private static void member_switch_helper(final CodeEmitter e,
                                             List members,
                                             final ObjectSwitchCallback callback,
                                             boolean useName,
                                             final ParameterTyper typer) {
        try {
            final Map cache = new HashMap();
            final ParameterTyper cached = new ParameterTyper() {
                    public Class[] getParameterTypes(Object member) {
                        Class[] types = (Class[])cache.get(member);
                        if (types == null) {
                            cache.put(member, types = typer.getParameterTypes(member));
                        }
                        return types;
                    }
                };
            final Label def = e.make_label();
            final Label end = e.make_label();
            if (useName) {
                e.swap();
                final Map buckets = CollectionUtils.bucket(members, new Transformer() {
                        public Object transform(Object value) {
                            return ((Member)value).getName();
                        }
                    });
                String[] names = (String[])buckets.keySet().toArray(new String[buckets.size()]);
                ComplexOps.string_switch(e, names, Constants.SWITCH_STYLE_HASH, new ObjectSwitchCallback() {
                        public void processCase(Object key, Label dontUseEnd) throws Exception {
                            member_helper_size(e, (List)buckets.get(key), callback, cached, def, end);
                        }
                        public void processDefault() throws Exception {
                            e.goTo(def);
                        }
                    });
            } else {
                member_helper_size(e, members, callback, cached, def, end);
            }
            e.mark(def);
            e.pop();
            callback.processDefault();
            e.mark(end);
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Error ex) {
            throw ex;
        } catch (Exception ex) {
            throw new CodeGenerationException(ex);
        }
    }

    private static void member_helper_size(final CodeEmitter e,
                                           List members,
                                           final ObjectSwitchCallback callback,
                                           final ParameterTyper typer,
                                           final Label def,
                                           final Label end) throws Exception {
        final Map buckets = CollectionUtils.bucket(members, new Transformer() {
            public Object transform(Object value) {
                return new Integer(typer.getParameterTypes(value).length);
            }
        });
        e.dup();
        e.arraylength();
        e.process_switch(ComplexOps.getSwitchKeys(buckets), new ProcessSwitchCallback() {
            public void processCase(int key, Label dontUseEnd) throws Exception {
                List bucket = (List)buckets.get(new Integer(key));
                Class[] types = typer.getParameterTypes(bucket.get(0));
                member_helper_type(e, bucket, callback, typer, def, end, new TinyBitSet());
            }
            public void processDefault() throws Exception {
                e.goTo(def);
            }
        });
    }

    private static void member_helper_type(final CodeEmitter e,
                                           List members,
                                           final ObjectSwitchCallback callback,
                                           final ParameterTyper typer,
                                           final Label def,
                                           final Label end,
                                           final TinyBitSet checked) throws Exception {
        if (members.size() == 1) {
            // need to check classes that have not already been checked via switches
            Member member = (Member)members.get(0);
            Class[] types = typer.getParameterTypes(member);
            for (int i = 0; i < types.length; i++) {
                if (checked == null || !checked.get(i)) {
                    e.dup();
                    e.aaload(i);
                    e.invoke_virtual(Constants.TYPE_CLASS, GET_NAME);
                    e.push(types[i].getName());
                    e.invoke_virtual(Constants.TYPE_OBJECT, EQUALS);
                    e.if_jump(e.EQ, def);
                }
            }
            e.pop();
            callback.processCase(member, end);
        } else {
            // choose the index that has the best chance of uniquely identifying member
            Class[] example = typer.getParameterTypes(members.get(0));
            Map buckets = null;
            int index = -1;
            for (int i = 0; i < example.length; i++) {
                final int j = i;
                Map test = CollectionUtils.bucket(members, new Transformer() {
                    public Object transform(Object value) {
                        return typer.getParameterTypes(value)[j].getName();
                    }
                });
                if (buckets == null || test.size() > buckets.size()) {
                    buckets = test;
                    index = i;
                }
            }
            if (buckets == null) {
                // TODO: switch by returnType
                // must have two methods with same name, types, and different return types
                e.goTo(def);
            } else {
                checked.set(index);

                e.dup();
                e.aaload(index);
                e.invoke_virtual(Constants.TYPE_CLASS, GET_NAME);

                final Map fbuckets = buckets;
                String[] names = (String[])buckets.keySet().toArray(new String[buckets.size()]);
                ComplexOps.string_switch(e, names, Constants.SWITCH_STYLE_HASH, new ObjectSwitchCallback() {
                    public void processCase(Object key, Label dontUseEnd) throws Exception {
                        member_helper_type(e, (List)fbuckets.get(key), callback, typer, def, end, checked);
                    }
                    public void processDefault() throws Exception {
                        e.goTo(def);
                    }
                });
            }
        }
    }
}
