/*
 * Copyright 2003,2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.sf.cglib.reflect;

public class MemberSwitchBean {
    public int init = -1;
    
    public MemberSwitchBean() { init = 0; }
    public MemberSwitchBean(double foo) { init = 1; }
    public MemberSwitchBean(int foo) { init = 2; }
    public MemberSwitchBean(int foo, String bar, String baz) { init = 3; }
    public MemberSwitchBean(int foo, String bar, double baz) { init = 4; }
    public MemberSwitchBean(int foo, short bar, long baz) { init = 5; }
    public MemberSwitchBean(int foo, String bar) { init = 6; }

    public int foo() { return 0; }
    public int foo(double foo) { return 1; }
    public int foo(int foo) { return 2; }
    public int foo(int foo, String bar, String baz) { return 3; }
    public int foo(int foo, String bar, double baz) { return 4; }
    public int foo(int foo, short bar, long baz) { return 5; }
    public int foo(int foo, String bar) { return 6; }

    public int bar() { return 7; }
    public int bar(double foo) { return 8; }

    int pkg() { return 9; }

    public static int staticMethod() { return 10; }
}

