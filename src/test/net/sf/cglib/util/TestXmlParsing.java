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
package net.sf.cglib.util;

import net.sf.cglib.CodeGenTestCase;
import junit.framework.*;

public class TestXmlParsing extends CodeGenTestCase {
    private static final String ATTRIBUTE_STR = "attribute";
    private static final String ATTRIBUTE_DIRECTIVE_STR = "directive.attribute";
    private static final String BODY_STR = "body";
    private static final String DECLARATION_STR = "declaration";
    private static final String DOBODY_STR = "doBody";
    private static final String ELEMENT_STR = "element";
    private static final String FALLBACK_STR = "fallback";
    private static final String FORWARD_STR = "forward";
    private static final String GET_PROPERTY_STR = "getProperty";
    private static final String INCLUDE_STR = "include";
    private static final String INCLUDE_DIRECTIVE_STR = "directive.include";
    private static final String INVOKE_STR = "invoke";
    private static final String OUTPUT_STR = "output";
    private static final String PAGE_DIRECTIVE_STR = "directive.page";
    private static final String PARAMS_STR = "params";
    private static final String PARAM_STR = "param";
    private static final String PLUGIN_STR = "plugin";
    private static final String ROOT_STR = "root";
    private static final String SET_PROPERTY_STR = "setProperty";
    private static final String TAG_DIRECTIVE_STR = "directive.tag";
    private static final String TEXT_STR = "text";
    private static final String USE_BEAN_STR = "useBean";
    private static final String VARIABLE_DIRECTIVE_STR = "variable";

    private static final int ATTRIBUTE_IDX = 0;
    private static final int ATTRIBUTE_DIRECTIVE_IDX = 1;
    private static final int BODY_IDX = 2;
    private static final int DECLARATION_IDX = 3;
    private static final int DOBODY_IDX = 4;
    private static final int ELEMENT_IDX = 5;
    private static final int FALLBACK_IDX = 6;
    private static final int FORWARD_IDX = 7;
    private static final int GET_PROPERTY_IDX = 8;
    private static final int INCLUDE_IDX = 9;
    private static final int INCLUDE_DIRECTIVE_IDX = 10;
    private static final int INVOKE_IDX = 11;
    private static final int OUTPUT_IDX = 12;
    private static final int PAGE_DIRECTIVE_IDX = 13;
    private static final int PARAMS_IDX = 14;
    private static final int PARAM_IDX = 15;
    private static final int PLUGIN_IDX = 16;
    private static final int ROOT_IDX = 17;
    private static final int SET_PROPERTY_IDX = 18;
    private static final int TAG_DIRECTIVE_IDX = 19;
    private static final int TEXT_IDX = 20;
    private static final int USE_BEAN_IDX = 21;
    private static final int VARIABLE_DIRECTIVE_IDX = 22;

    private static final String[] M1 = {
        ATTRIBUTE_STR,
        ATTRIBUTE_DIRECTIVE_STR,
        BODY_STR,
        DECLARATION_STR,
        DOBODY_STR,
        ELEMENT_STR,
        FALLBACK_STR,
        FORWARD_STR,
        GET_PROPERTY_STR,
        INCLUDE_STR,
        INCLUDE_DIRECTIVE_STR,
        INVOKE_STR,
        OUTPUT_STR,
        PAGE_DIRECTIVE_STR,
        PARAMS_STR,
        PARAM_STR,
        PLUGIN_STR,
        ROOT_STR,
        SET_PROPERTY_STR,
        TAG_DIRECTIVE_STR,
        TEXT_STR,
        USE_BEAN_STR,
        VARIABLE_DIRECTIVE_STR
    };

    private static final int[] M2 = {
        ATTRIBUTE_IDX,
        ATTRIBUTE_DIRECTIVE_IDX,
        BODY_IDX,
        DECLARATION_IDX,
        DOBODY_IDX,
        ELEMENT_IDX,
        FALLBACK_IDX,
        FORWARD_IDX,
        GET_PROPERTY_IDX,
        INCLUDE_IDX,
        INCLUDE_DIRECTIVE_IDX,
        INVOKE_IDX,
        OUTPUT_IDX,
        PAGE_DIRECTIVE_IDX,
        PARAMS_IDX,
        PARAM_IDX,
        PLUGIN_IDX,
        ROOT_IDX,
        SET_PROPERTY_IDX,
        TAG_DIRECTIVE_IDX,
        TEXT_IDX,
        USE_BEAN_IDX,
        VARIABLE_DIRECTIVE_IDX
    };

    private static final StringSwitcher SWITCHER = StringSwitcher.create(M1, M2, true);

    public int switcher(String s) {
        return SWITCHER.intValue(s);
    }

    public int interned(String s) {
        if (s == ATTRIBUTE_STR) {
            return ATTRIBUTE_IDX;
        } else if (s == ATTRIBUTE_DIRECTIVE_STR) {
            return ATTRIBUTE_DIRECTIVE_IDX;
        } else if (s == BODY_STR) {
            return BODY_IDX;
        } else if (s == DECLARATION_STR) {
            return DECLARATION_IDX;
        } else if (s == DOBODY_STR) {
            return DOBODY_IDX;
        } else if (s == ELEMENT_STR) {
            return ELEMENT_IDX;
        } else if (s == FALLBACK_STR) {
            return FALLBACK_IDX;
        } else if (s == FORWARD_STR) {
            return FORWARD_IDX;
        } else if (s == GET_PROPERTY_STR) {
            return GET_PROPERTY_IDX;
        } else if (s == INCLUDE_STR) {
            return INCLUDE_IDX;
        } else if (s == INCLUDE_DIRECTIVE_STR) {
            return INCLUDE_DIRECTIVE_IDX;
        } else if (s == INVOKE_STR) {
            return INVOKE_IDX;
        } else if (s == OUTPUT_STR) {
            return OUTPUT_IDX;
        } else if (s == PAGE_DIRECTIVE_STR) {
            return PAGE_DIRECTIVE_IDX;
        } else if (s == PARAMS_STR) {
            return PARAMS_IDX;
        } else if (s == PARAM_STR) {
            return PARAM_IDX;
        } else if (s == PLUGIN_STR) {
            return PLUGIN_IDX;
        } else if (s == ROOT_STR) {
            return ROOT_IDX;
        } else if (s == SET_PROPERTY_STR) {
            return SET_PROPERTY_IDX;
        } else if (s == TAG_DIRECTIVE_STR) {
            return TAG_DIRECTIVE_IDX;
        } else if (s == TEXT_STR) {
            return TEXT_IDX;
        } else if (s == USE_BEAN_STR) {
            return USE_BEAN_IDX;
        } else if (s == VARIABLE_DIRECTIVE_STR) {
            return VARIABLE_DIRECTIVE_IDX;
        }
        return -1;
    }
    
    public int elseIf(String s) {
        if (s.equals(ATTRIBUTE_STR)) {
            return ATTRIBUTE_IDX;
        } else if (s.equals(ATTRIBUTE_DIRECTIVE_STR)) {
            return ATTRIBUTE_DIRECTIVE_IDX;
        } else if (s.equals(BODY_STR)) {
            return BODY_IDX;
        } else if (s.equals(DECLARATION_STR)) {
            return DECLARATION_IDX;
        } else if (s.equals(DOBODY_STR)) {
            return DOBODY_IDX;
        } else if (s.equals(ELEMENT_STR)) {
            return ELEMENT_IDX;
        } else if (s.equals(FALLBACK_STR)) {
            return FALLBACK_IDX;
        } else if (s.equals(FORWARD_STR)) {
            return FORWARD_IDX;
        } else if (s.equals(GET_PROPERTY_STR)) {
            return GET_PROPERTY_IDX;
        } else if (s.equals(INCLUDE_STR)) {
            return INCLUDE_IDX;
        } else if (s.equals(INCLUDE_DIRECTIVE_STR)) {
            return INCLUDE_DIRECTIVE_IDX;
        } else if (s.equals(INVOKE_STR)) {
            return INVOKE_IDX;
        } else if (s.equals(OUTPUT_STR)) {
            return OUTPUT_IDX;
        } else if (s.equals(PAGE_DIRECTIVE_STR)) {
            return PAGE_DIRECTIVE_IDX;
        } else if (s.equals(PARAMS_STR)) {
            return PARAMS_IDX;
        } else if (s.equals(PARAM_STR)) {
            return PARAM_IDX;
        } else if (s.equals(PLUGIN_STR)) {
            return PLUGIN_IDX;
        } else if (s.equals(ROOT_STR)) {
            return ROOT_IDX;
        } else if (s.equals(SET_PROPERTY_STR)) {
            return SET_PROPERTY_IDX;
        } else if (s.equals(TAG_DIRECTIVE_STR)) {
            return TAG_DIRECTIVE_IDX;
        } else if (s.equals(TEXT_STR)) {
            return TEXT_IDX;
        } else if (s.equals(USE_BEAN_STR)) {
            return USE_BEAN_IDX;
        } else if (s.equals(VARIABLE_DIRECTIVE_STR)) {
            return VARIABLE_DIRECTIVE_IDX;
        }
        return -1;
    }

    public void testStartElement() throws Throwable {
        int numWords = 10000;
        int reps = 1000;

        String[] words = new String[numWords];
        String[] interned = new String[numWords];
        for (int i = 0; i < words.length; i++) {
            interned[i] = M1[(int)(Math.random() * M1.length)].intern();
            words[i] = new String(interned[i]);
        }
        long total1 = 0;
        long total2 = 0;
        long total3 = 0;
        // warm-up
        for (int i = 0; i < reps; i++) {
            for (int j = 0; j < numWords; j++) {
                total1 += elseIf(words[j]);
                total2 += interned(interned[j]);
                total3 += switcher(words[j]);
            }
        }
        if (total1 != total2 || total1 != total3) {
            fail("totals are not equal");
        }

        long t0 = System.currentTimeMillis();
        for (int i = 0; i < reps; i++) {
            for (int j = 0; j < numWords; j++) {
                elseIf(words[j]);
            }
        }
        long t1 = System.currentTimeMillis();
        for (int i = 0; i < reps; i++) {
            for (int j = 0; j < numWords; j++) {
                interned(interned[j]);
            }
        }
        long t2 = System.currentTimeMillis();
        for (int i = 0; i < reps; i++) {
            for (int j = 0; j < numWords; j++) {
                switcher(words[j]);
            }
        }
        long t3 = System.currentTimeMillis();

        System.err.println("elseif: " + (t1 - t0) + "ms");
        System.err.println("intern: " + (t2 - t1) + "ms");
        System.err.println("switch: " + (t3 - t2) + "ms");
    }

    public TestXmlParsing(String testName) {
        super(testName);
    }
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        return new TestSuite(TestXmlParsing.class);
    }
    
    public void perform(ClassLoader loader) throws Throwable {
    }
    
    public void testFailOnMemoryLeak() throws Throwable {
    }
    
}
