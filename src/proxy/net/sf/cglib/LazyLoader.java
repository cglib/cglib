package net.sf.cglib;

public interface LazyLoader
extends Callback
{
    Object loadObject() throws Exception;
}
