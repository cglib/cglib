package net.sf.cglib;

import java.lang.reflect.Member;

public interface CallbackFilter {
    int accept(Member member);
}
