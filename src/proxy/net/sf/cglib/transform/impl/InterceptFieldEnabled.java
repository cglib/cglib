package net.sf.cglib.transform.impl;

public interface InterceptFieldEnabled {
    void setInterceptFieldCallback(InterceptFieldCallback callback);
    InterceptFieldCallback getInterceptFieldCallback();
}
