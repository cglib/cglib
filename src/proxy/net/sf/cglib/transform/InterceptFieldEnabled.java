package net.sf.cglib.transform;

public interface InterceptFieldEnabled {
    void setInterceptFieldCallback(InterceptFieldCallback callback);
    InterceptFieldCallback getInterceptFieldCallback();
}
