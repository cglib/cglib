// Decompiled by DJ v3.2.2.67 Copyright 2002 Atanas Neshkov  Date: 2002.10.16 20:52:38
// Home Page : http://members.fortunecity.com/neshkov/dj.html  - Check often for new version!
// Decompiler options: packimports(3) disassembler 
// Source File Name:   <generated>

package net.sf.cglib.proxyjava.util;

import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.*;
import net.sf.cglib.proxy.Factory;
import net.sf.cglib.proxy.MethodInterceptor;

public final class Vector$$EnhancedByCGLIB$$0 extends Vector
    implements Factory
{

    public Vector$$EnhancedByCGLIB$$0(MethodInterceptor methodinterceptor)
    {
    //    0    0:aload_0         
    //    1    1:invokespecial   #12  <Method void Vector()>
    //    2    4:aload_0         
    //    3    5:aload_1         
    //    4    6:putfield        #14  <Field MethodInterceptor h>
    //    5    9:return          
    }

    public final Object newInstance(MethodInterceptor methodinterceptor)
    {
    //    0    0:new             #4   <Class Vector$$EnhancedByCGLIB$$0>
    //    1    3:dup             
    //    2    4:aload_1         
    //    3    5:invokespecial   #18  <Method void Vector$$EnhancedByCGLIB$$0(MethodInterceptor)>
    //    4    8:areturn         
    }

    public final MethodInterceptor getInterceptor()
    {
    //    0    0:aload_0         
    //    1    1:getfield        #14  <Field MethodInterceptor h>
    //    2    4:areturn         
    }

    private Object writeReplace()
    {
    //    0    0:aload_0         
    //    1    1:invokestatic    #28  <Method Object net.sf.cglib.proxy.Enhancer$InternalReplace.writeReplace(Object)>
    //    2    4:areturn         
    }

    public final boolean equals(Object obj)
    {
    // try 0 94 handler(s) 92 97 102
    //    0    0:iconst_1        
    //    1    1:anewarray       Object[]
    //    2    4:dup             
    //    3    5:iconst_0        
    //    4    6:aload_1         
    //    5    7:aastore         
    //    6    8:astore_2        
    //    7    9:aconst_null     
    //    8   10:astore_3        
    //    9   11:iconst_0        
    //   10   12:istore          4
    //   11   14:aconst_null     
    //   12   15:astore          5
    //   13   17:aload_0         
    //   14   18:getfield        #14  <Field MethodInterceptor h>
    //   15   21:aload_0         
    //   16   22:getstatic       #45  <Field Method METHOD_0>
    //   17   25:aload_2         
    //   18   26:invokeinterface #39  <Method boolean MethodInterceptor.invokeSuper(Object, Method, Object[])>
    //   19   31:ifeq            55
    //   20   34:iconst_1        
    //   21   35:istore          4
    // try 37 50 handler(s) 53
    //   22   37:new             #47  <Class Boolean>
    //   23   40:dup             
    //   24   41:aload_0         
    //   25   42:aload_1         
    //   26   43:invokespecial   #51  <Method boolean Vector.equals(Object)>
    //   27   46:invokespecial   #54  <Method void Boolean(boolean)>
    //   28   49:astore_3        
    //   29   50:goto            55
    // catch Throwable
    //   30   53:astore          5
    //   31   55:aload_0         
    //   32   56:getfield        #14  <Field MethodInterceptor h>
    //   33   59:aload_0         
    //   34   60:getstatic       #45  <Field Method METHOD_0>
    //   35   63:aload_2         
    //   36   64:iload           4
    //   37   66:aload_3         
    //   38   67:aload           5
    //   39   69:invokeinterface #35  <Method Object MethodInterceptor.afterReturn(Object, Method, Object[], boolean, Object, Throwable)>
    //   40   74:astore          6
    //   41   76:aload           6
    //   42   78:ifnonnull       83
    //   43   81:iconst_0        
    //   44   82:ireturn         
    //   45   83:aload           6
    //   46   85:checkcast       #47  <Class Boolean>
    //   47   88:invokevirtual   #58  <Method boolean Boolean.booleanValue()>
    //   48   91:ireturn         
    // catch RuntimeException
    //   49   92:astore          7
    //   50   94:aload           7
    //   51   96:athrow          
    // catch Error
    //   52   97:astore          8
    //   53   99:aload           8
    //   54  101:athrow          
    // catch Throwable
    //   55  102:astore          9
    //   56  104:new             #60  <Class UndeclaredThrowableException>
    //   57  107:dup             
    //   58  108:aload           9
    //   59  110:invokespecial   #63  <Method void UndeclaredThrowableException(Throwable)>
    //   60  113:athrow          
    }

    public final List subList(int i, int j)
    {
    // try 0 99 handler(s) 97 102 107
    //    0    0:iconst_2        
    //    1    1:anewarray       Object[]
    //    2    4:dup             
    //    3    5:iconst_0        
    //    4    6:new             #72  <Class Integer>
    //    5    9:dup             
    //    6   10:iload_1         
    //    7   11:invokespecial   #75  <Method void Integer(int)>
    //    8   14:aastore         
    //    9   15:dup             
    //   10   16:iconst_1        
    //   11   17:new             #72  <Class Integer>
    //   12   20:dup             
    //   13   21:iload_2         
    //   14   22:invokespecial   #75  <Method void Integer(int)>
    //   15   25:aastore         
    //   16   26:astore_3        
    //   17   27:aconst_null     
    //   18   28:astore          4
    //   19   30:iconst_0        
    //   20   31:istore          5
    //   21   33:aconst_null     
    //   22   34:astore          6
    //   23   36:aload_0         
    //   24   37:getfield        #14  <Field MethodInterceptor h>
    //   25   40:aload_0         
    //   26   41:getstatic       #77  <Field Method METHOD_1>
    //   27   44:aload_3         
    //   28   45:invokeinterface #39  <Method boolean MethodInterceptor.invokeSuper(Object, Method, Object[])>
    //   29   50:ifeq            69
    //   30   53:iconst_1        
    // try 54 64 handler(s) 67
    //   31   54:istore          5
    //   32   56:aload_0         
    //   33   57:iload_1         
    //   34   58:iload_2         
    //   35   59:invokespecial   #81  <Method List Vector.subList(int, int)>
    //   36   62:astore          4
    //   37   64:goto            69
    // catch Throwable
    //   38   67:astore          6
    //   39   69:aload_0         
    //   40   70:getfield        #14  <Field MethodInterceptor h>
    //   41   73:aload_0         
    //   42   74:getstatic       #77  <Field Method METHOD_1>
    //   43   77:aload_3         
    //   44   78:iload           5
    //   45   80:aload           4
    //   46   82:aload           6
    //   47   84:invokeinterface #35  <Method Object MethodInterceptor.afterReturn(Object, Method, Object[], boolean, Object, Throwable)>
    //   48   89:astore          7
    //   49   91:aload           7
    //   50   93:checkcast       #83  <Class List>
    //   51   96:areturn         
    // catch RuntimeException
    //   52   97:astore          8
    //   53   99:aload           8
    //   54  101:athrow          
    // catch Error
    //   55  102:astore          9
    //   56  104:aload           9
    //   57  106:athrow          
    // catch Throwable
    //   58  107:astore          10
    //   59  109:new             #60  <Class UndeclaredThrowableException>
    //   60  112:dup             
    //   61  113:aload           10
    //   62  115:invokespecial   #63  <Method void UndeclaredThrowableException(Throwable)>
    //   63  118:athrow          
    }

    public final boolean removeAll(Collection collection)
    {
    // try 0 94 handler(s) 92 97 102
    //    0    0:iconst_1        
    //    1    1:anewarray       Object[]
    //    2    4:dup             
    //    3    5:iconst_0        
    //    4    6:aload_1         
    //    5    7:aastore         
    //    6    8:astore_2        
    //    7    9:aconst_null     
    //    8   10:astore_3        
    //    9   11:iconst_0        
    //   10   12:istore          4
    //   11   14:aconst_null     
    //   12   15:astore          5
    //   13   17:aload_0         
    //   14   18:getfield        #14  <Field MethodInterceptor h>
    //   15   21:aload_0         
    //   16   22:getstatic       #86  <Field Method METHOD_2>
    //   17   25:aload_2         
    //   18   26:invokeinterface #39  <Method boolean MethodInterceptor.invokeSuper(Object, Method, Object[])>
    //   19   31:ifeq            55
    //   20   34:iconst_1        
    //   21   35:istore          4
    // try 37 50 handler(s) 53
    //   22   37:new             #47  <Class Boolean>
    //   23   40:dup             
    //   24   41:aload_0         
    //   25   42:aload_1         
    //   26   43:invokespecial   #90  <Method boolean Vector.removeAll(Collection)>
    //   27   46:invokespecial   #54  <Method void Boolean(boolean)>
    //   28   49:astore_3        
    //   29   50:goto            55
    // catch Throwable
    //   30   53:astore          5
    //   31   55:aload_0         
    //   32   56:getfield        #14  <Field MethodInterceptor h>
    //   33   59:aload_0         
    //   34   60:getstatic       #86  <Field Method METHOD_2>
    //   35   63:aload_2         
    //   36   64:iload           4
    //   37   66:aload_3         
    //   38   67:aload           5
    //   39   69:invokeinterface #35  <Method Object MethodInterceptor.afterReturn(Object, Method, Object[], boolean, Object, Throwable)>
    //   40   74:astore          6
    //   41   76:aload           6
    //   42   78:ifnonnull       83
    //   43   81:iconst_0        
    //   44   82:ireturn         
    //   45   83:aload           6
    //   46   85:checkcast       #47  <Class Boolean>
    //   47   88:invokevirtual   #58  <Method boolean Boolean.booleanValue()>
    //   48   91:ireturn         
    // catch RuntimeException
    //   49   92:astore          7
    //   50   94:aload           7
    //   51   96:athrow          
    // catch Error
    //   52   97:astore          8
    //   53   99:aload           8
    //   54  101:athrow          
    // catch Throwable
    //   55  102:astore          9
    //   56  104:new             #60  <Class UndeclaredThrowableException>
    //   57  107:dup             
    //   58  108:aload           9
    //   59  110:invokespecial   #63  <Method void UndeclaredThrowableException(Throwable)>
    //   60  113:athrow          
    }

    public final int capacity()
    {
    // try 0 86 handler(s) 84 89 94
    //    0    0:iconst_0        
    //    1    1:anewarray       Object[]
    //    2    4:astore_1        
    //    3    5:aconst_null     
    //    4    6:astore_2        
    //    5    7:iconst_0        
    //    6    8:istore_3        
    //    7    9:aconst_null     
    //    8   10:astore          4
    //    9   12:aload_0         
    //   10   13:getfield        #14  <Field MethodInterceptor h>
    //   11   16:aload_0         
    //   12   17:getstatic       #93  <Field Method METHOD_3>
    //   13   20:aload_1         
    //   14   21:invokeinterface #39  <Method boolean MethodInterceptor.invokeSuper(Object, Method, Object[])>
    //   15   26:ifeq            48
    //   16   29:iconst_1        
    //   17   30:istore_3        
    // try 31 43 handler(s) 46
    //   18   31:new             #72  <Class Integer>
    //   19   34:dup             
    //   20   35:aload_0         
    //   21   36:invokespecial   #97  <Method int Vector.capacity()>
    //   22   39:invokespecial   #75  <Method void Integer(int)>
    //   23   42:astore_2        
    //   24   43:goto            48
    // catch Throwable
    //   25   46:astore          4
    //   26   48:aload_0         
    //   27   49:getfield        #14  <Field MethodInterceptor h>
    //   28   52:aload_0         
    //   29   53:getstatic       #93  <Field Method METHOD_3>
    //   30   56:aload_1         
    //   31   57:iload_3         
    //   32   58:aload_2         
    //   33   59:aload           4
    //   34   61:invokeinterface #35  <Method Object MethodInterceptor.afterReturn(Object, Method, Object[], boolean, Object, Throwable)>
    //   35   66:astore          5
    //   36   68:aload           5
    //   37   70:ifnonnull       75
    //   38   73:iconst_0        
    //   39   74:ireturn         
    //   40   75:aload           5
    //   41   77:checkcast       #99  <Class Number>
    //   42   80:invokevirtual   #102 <Method int Number.intValue()>
    //   43   83:ireturn         
    // catch RuntimeException
    //   44   84:astore          6
    //   45   86:aload           6
    //   46   88:athrow          
    // catch Error
    //   47   89:astore          7
    //   48   91:aload           7
    //   49   93:athrow          
    // catch Throwable
    //   50   94:astore          8
    //   51   96:new             #60  <Class UndeclaredThrowableException>
    //   52   99:dup             
    //   53  100:aload           8
    //   54  102:invokespecial   #63  <Method void UndeclaredThrowableException(Throwable)>
    //   55  105:athrow          
    }

    public final Enumeration elements()
    {
    // try 0 69 handler(s) 67 72 77
    //    0    0:iconst_0        
    //    1    1:anewarray       Object[]
    //    2    4:astore_1        
    //    3    5:aconst_null     
    //    4    6:astore_2        
    //    5    7:iconst_0        
    //    6    8:istore_3        
    //    7    9:aconst_null     
    //    8   10:astore          4
    //    9   12:aload_0         
    //   10   13:getfield        #14  <Field MethodInterceptor h>
    //   11   16:aload_0         
    //   12   17:getstatic       #105 <Field Method METHOD_4>
    //   13   20:aload_1         
    //   14   21:invokeinterface #39  <Method boolean MethodInterceptor.invokeSuper(Object, Method, Object[])>
    //   15   26:ifeq            41
    //   16   29:iconst_1        
    // try 30 36 handler(s) 39
    //   17   30:istore_3        
    //   18   31:aload_0         
    //   19   32:invokespecial   #109 <Method Enumeration Vector.elements()>
    //   20   35:astore_2        
    //   21   36:goto            41
    // catch Throwable
    //   22   39:astore          4
    //   23   41:aload_0         
    //   24   42:getfield        #14  <Field MethodInterceptor h>
    //   25   45:aload_0         
    //   26   46:getstatic       #105 <Field Method METHOD_4>
    //   27   49:aload_1         
    //   28   50:iload_3         
    //   29   51:aload_2         
    //   30   52:aload           4
    //   31   54:invokeinterface #35  <Method Object MethodInterceptor.afterReturn(Object, Method, Object[], boolean, Object, Throwable)>
    //   32   59:astore          5
    //   33   61:aload           5
    //   34   63:checkcast       #111 <Class Enumeration>
    //   35   66:areturn         
    // catch RuntimeException
    //   36   67:astore          6
    //   37   69:aload           6
    //   38   71:athrow          
    // catch Error
    //   39   72:astore          7
    //   40   74:aload           7
    //   41   76:athrow          
    // catch Throwable
    //   42   77:astore          8
    //   43   79:new             #60  <Class UndeclaredThrowableException>
    //   44   82:dup             
    //   45   83:aload           8
    //   46   85:invokespecial   #63  <Method void UndeclaredThrowableException(Throwable)>
    //   47   88:athrow          
    }

    public final void copyInto(Object aobj[])
    {
    // try 0 69 handler(s) 67 72 77
    //    0    0:iconst_1        
    //    1    1:anewarray       Object[]
    //    2    4:dup             
    //    3    5:iconst_0        
    //    4    6:aload_1         
    //    5    7:aastore         
    //    6    8:astore_2        
    //    7    9:aconst_null     
    //    8   10:astore_3        
    //    9   11:iconst_0        
    //   10   12:istore          4
    //   11   14:aconst_null     
    //   12   15:astore          5
    //   13   17:aload_0         
    //   14   18:getfield        #14  <Field MethodInterceptor h>
    //   15   21:aload_0         
    //   16   22:getstatic       #114 <Field Method METHOD_5>
    //   17   25:aload_2         
    //   18   26:invokeinterface #39  <Method boolean MethodInterceptor.invokeSuper(Object, Method, Object[])>
    //   19   31:ifeq            47
    //   20   34:iconst_1        
    // try 35 45 handler(s) 45
    //   21   35:istore          4
    //   22   37:aload_0         
    //   23   38:aload_1         
    //   24   39:invokespecial   #118 <Method void Vector.copyInto(Object[])>
    //   25   42:goto            47
    // catch Throwable
    //   26   45:astore          5
    //   27   47:aload_0         
    //   28   48:getfield        #14  <Field MethodInterceptor h>
    //   29   51:aload_0         
    //   30   52:getstatic       #114 <Field Method METHOD_5>
    //   31   55:aload_2         
    //   32   56:iload           4
    //   33   58:aload_3         
    //   34   59:aload           5
    //   35   61:invokeinterface #35  <Method Object MethodInterceptor.afterReturn(Object, Method, Object[], boolean, Object, Throwable)>
    //   36   66:return          
    // catch RuntimeException
    //   37   67:astore          7
    //   38   69:aload           7
    //   39   71:athrow          
    // catch Error
    //   40   72:astore          8
    //   41   74:aload           8
    //   42   76:athrow          
    // catch Throwable
    //   43   77:astore          9
    //   44   79:new             #60  <Class UndeclaredThrowableException>
    //   45   82:dup             
    //   46   83:aload           9
    //   47   85:invokespecial   #63  <Method void UndeclaredThrowableException(Throwable)>
    //   48   88:athrow          
    }

    public final Iterator iterator()
    {
    // try 0 69 handler(s) 67 72 77
    //    0    0:iconst_0        
    //    1    1:anewarray       Object[]
    //    2    4:astore_1        
    //    3    5:aconst_null     
    //    4    6:astore_2        
    //    5    7:iconst_0        
    //    6    8:istore_3        
    //    7    9:aconst_null     
    //    8   10:astore          4
    //    9   12:aload_0         
    //   10   13:getfield        #14  <Field MethodInterceptor h>
    //   11   16:aload_0         
    //   12   17:getstatic       #121 <Field Method METHOD_6>
    //   13   20:aload_1         
    //   14   21:invokeinterface #39  <Method boolean MethodInterceptor.invokeSuper(Object, Method, Object[])>
    //   15   26:ifeq            41
    //   16   29:iconst_1        
    // try 30 36 handler(s) 39
    //   17   30:istore_3        
    //   18   31:aload_0         
    //   19   32:invokespecial   #125 <Method Iterator Vector.iterator()>
    //   20   35:astore_2        
    //   21   36:goto            41
    // catch Throwable
    //   22   39:astore          4
    //   23   41:aload_0         
    //   24   42:getfield        #14  <Field MethodInterceptor h>
    //   25   45:aload_0         
    //   26   46:getstatic       #121 <Field Method METHOD_6>
    //   27   49:aload_1         
    //   28   50:iload_3         
    //   29   51:aload_2         
    //   30   52:aload           4
    //   31   54:invokeinterface #35  <Method Object MethodInterceptor.afterReturn(Object, Method, Object[], boolean, Object, Throwable)>
    //   32   59:astore          5
    //   33   61:aload           5
    //   34   63:checkcast       #127 <Class Iterator>
    //   35   66:areturn         
    // catch RuntimeException
    //   36   67:astore          6
    //   37   69:aload           6
    //   38   71:athrow          
    // catch Error
    //   39   72:astore          7
    //   40   74:aload           7
    //   41   76:athrow          
    // catch Throwable
    //   42   77:astore          8
    //   43   79:new             #60  <Class UndeclaredThrowableException>
    //   44   82:dup             
    //   45   83:aload           8
    //   46   85:invokespecial   #63  <Method void UndeclaredThrowableException(Throwable)>
    //   47   88:athrow          
    }

    public final ListIterator listIterator(int i)
    {
    // try 0 84 handler(s) 82 87 92
    //    0    0:iconst_1        
    //    1    1:anewarray       Object[]
    //    2    4:dup             
    //    3    5:iconst_0        
    //    4    6:new             #72  <Class Integer>
    //    5    9:dup             
    //    6   10:iload_1         
    //    7   11:invokespecial   #75  <Method void Integer(int)>
    //    8   14:aastore         
    //    9   15:astore_2        
    //   10   16:aconst_null     
    //   11   17:astore_3        
    //   12   18:iconst_0        
    //   13   19:istore          4
    //   14   21:aconst_null     
    //   15   22:astore          5
    //   16   24:aload_0         
    //   17   25:getfield        #14  <Field MethodInterceptor h>
    //   18   28:aload_0         
    //   19   29:getstatic       #130 <Field Method METHOD_7>
    //   20   32:aload_2         
    //   21   33:invokeinterface #39  <Method boolean MethodInterceptor.invokeSuper(Object, Method, Object[])>
    //   22   38:ifeq            55
    //   23   41:iconst_1        
    // try 42 50 handler(s) 53
    //   24   42:istore          4
    //   25   44:aload_0         
    //   26   45:iload_1         
    //   27   46:invokespecial   #134 <Method ListIterator Vector.listIterator(int)>
    //   28   49:astore_3        
    //   29   50:goto            55
    // catch Throwable
    //   30   53:astore          5
    //   31   55:aload_0         
    //   32   56:getfield        #14  <Field MethodInterceptor h>
    //   33   59:aload_0         
    //   34   60:getstatic       #130 <Field Method METHOD_7>
    //   35   63:aload_2         
    //   36   64:iload           4
    //   37   66:aload_3         
    //   38   67:aload           5
    //   39   69:invokeinterface #35  <Method Object MethodInterceptor.afterReturn(Object, Method, Object[], boolean, Object, Throwable)>
    //   40   74:astore          6
    //   41   76:aload           6
    //   42   78:checkcast       #136 <Class ListIterator>
    //   43   81:areturn         
    // catch RuntimeException
    //   44   82:astore          7
    //   45   84:aload           7
    //   46   86:athrow          
    // catch Error
    //   47   87:astore          8
    //   48   89:aload           8
    //   49   91:athrow          
    // catch Throwable
    //   50   92:astore          9
    //   51   94:new             #60  <Class UndeclaredThrowableException>
    //   52   97:dup             
    //   53   98:aload           9
    //   54  100:invokespecial   #63  <Method void UndeclaredThrowableException(Throwable)>
    //   55  103:athrow          
    }

    public final ListIterator listIterator()
    {
    // try 0 69 handler(s) 67 72 77
    //    0    0:iconst_0        
    //    1    1:anewarray       Object[]
    //    2    4:astore_1        
    //    3    5:aconst_null     
    //    4    6:astore_2        
    //    5    7:iconst_0        
    //    6    8:istore_3        
    //    7    9:aconst_null     
    //    8   10:astore          4
    //    9   12:aload_0         
    //   10   13:getfield        #14  <Field MethodInterceptor h>
    //   11   16:aload_0         
    //   12   17:getstatic       #139 <Field Method METHOD_8>
    //   13   20:aload_1         
    //   14   21:invokeinterface #39  <Method boolean MethodInterceptor.invokeSuper(Object, Method, Object[])>
    //   15   26:ifeq            41
    //   16   29:iconst_1        
    // try 30 36 handler(s) 39
    //   17   30:istore_3        
    //   18   31:aload_0         
    //   19   32:invokespecial   #142 <Method ListIterator Vector.listIterator()>
    //   20   35:astore_2        
    //   21   36:goto            41
    // catch Throwable
    //   22   39:astore          4
    //   23   41:aload_0         
    //   24   42:getfield        #14  <Field MethodInterceptor h>
    //   25   45:aload_0         
    //   26   46:getstatic       #139 <Field Method METHOD_8>
    //   27   49:aload_1         
    //   28   50:iload_3         
    //   29   51:aload_2         
    //   30   52:aload           4
    //   31   54:invokeinterface #35  <Method Object MethodInterceptor.afterReturn(Object, Method, Object[], boolean, Object, Throwable)>
    //   32   59:astore          5
    //   33   61:aload           5
    //   34   63:checkcast       #136 <Class ListIterator>
    //   35   66:areturn         
    // catch RuntimeException
    //   36   67:astore          6
    //   37   69:aload           6
    //   38   71:athrow          
    // catch Error
    //   39   72:astore          7
    //   40   74:aload           7
    //   41   76:athrow          
    // catch Throwable
    //   42   77:astore          8
    //   43   79:new             #60  <Class UndeclaredThrowableException>
    //   44   82:dup             
    //   45   83:aload           8
    //   46   85:invokespecial   #63  <Method void UndeclaredThrowableException(Throwable)>
    //   47   88:athrow          
    }

    public final Object firstElement()
    {
    // try 0 66 handler(s) 64 69 74
    //    0    0:iconst_0        
    //    1    1:anewarray       Object[]
    //    2    4:astore_1        
    //    3    5:aconst_null     
    //    4    6:astore_2        
    //    5    7:iconst_0        
    //    6    8:istore_3        
    //    7    9:aconst_null     
    //    8   10:astore          4
    //    9   12:aload_0         
    //   10   13:getfield        #14  <Field MethodInterceptor h>
    //   11   16:aload_0         
    //   12   17:getstatic       #145 <Field Method METHOD_9>
    //   13   20:aload_1         
    //   14   21:invokeinterface #39  <Method boolean MethodInterceptor.invokeSuper(Object, Method, Object[])>
    //   15   26:ifeq            41
    //   16   29:iconst_1        
    // try 30 36 handler(s) 39
    //   17   30:istore_3        
    //   18   31:aload_0         
    //   19   32:invokespecial   #148 <Method Object Vector.firstElement()>
    //   20   35:astore_2        
    //   21   36:goto            41
    // catch Throwable
    //   22   39:astore          4
    //   23   41:aload_0         
    //   24   42:getfield        #14  <Field MethodInterceptor h>
    //   25   45:aload_0         
    //   26   46:getstatic       #145 <Field Method METHOD_9>
    //   27   49:aload_1         
    //   28   50:iload_3         
    //   29   51:aload_2         
    //   30   52:aload           4
    //   31   54:invokeinterface #35  <Method Object MethodInterceptor.afterReturn(Object, Method, Object[], boolean, Object, Throwable)>
    //   32   59:astore          5
    //   33   61:aload           5
    //   34   63:areturn         
    // catch RuntimeException
    //   35   64:astore          6
    //   36   66:aload           6
    //   37   68:athrow          
    // catch Error
    //   38   69:astore          7
    //   39   71:aload           7
    //   40   73:athrow          
    // catch Throwable
    //   41   74:astore          8
    //   42   76:new             #60  <Class UndeclaredThrowableException>
    //   43   79:dup             
    //   44   80:aload           8
    //   45   82:invokespecial   #63  <Method void UndeclaredThrowableException(Throwable)>
    //   46   85:athrow          
    }

    public final String toString()
    {
    // try 0 69 handler(s) 67 72 77
    //    0    0:iconst_0        
    //    1    1:anewarray       Object[]
    //    2    4:astore_1        
    //    3    5:aconst_null     
    //    4    6:astore_2        
    //    5    7:iconst_0        
    //    6    8:istore_3        
    //    7    9:aconst_null     
    //    8   10:astore          4
    //    9   12:aload_0         
    //   10   13:getfield        #14  <Field MethodInterceptor h>
    //   11   16:aload_0         
    //   12   17:getstatic       #151 <Field Method METHOD_10>
    //   13   20:aload_1         
    //   14   21:invokeinterface #39  <Method boolean MethodInterceptor.invokeSuper(Object, Method, Object[])>
    //   15   26:ifeq            41
    //   16   29:iconst_1        
    // try 30 36 handler(s) 39
    //   17   30:istore_3        
    //   18   31:aload_0         
    //   19   32:invokespecial   #155 <Method String Vector.toString()>
    //   20   35:astore_2        
    //   21   36:goto            41
    // catch Throwable
    //   22   39:astore          4
    //   23   41:aload_0         
    //   24   42:getfield        #14  <Field MethodInterceptor h>
    //   25   45:aload_0         
    //   26   46:getstatic       #151 <Field Method METHOD_10>
    //   27   49:aload_1         
    //   28   50:iload_3         
    //   29   51:aload_2         
    //   30   52:aload           4
    //   31   54:invokeinterface #35  <Method Object MethodInterceptor.afterReturn(Object, Method, Object[], boolean, Object, Throwable)>
    //   32   59:astore          5
    //   33   61:aload           5
    //   34   63:checkcast       #157 <Class String>
    //   35   66:areturn         
    // catch RuntimeException
    //   36   67:astore          6
    //   37   69:aload           6
    //   38   71:athrow          
    // catch Error
    //   39   72:astore          7
    //   40   74:aload           7
    //   41   76:athrow          
    // catch Throwable
    //   42   77:astore          8
    //   43   79:new             #60  <Class UndeclaredThrowableException>
    //   44   82:dup             
    //   45   83:aload           8
    //   46   85:invokespecial   #63  <Method void UndeclaredThrowableException(Throwable)>
    //   47   88:athrow          
    }

    public final void removeElementAt(int i)
    {
    // try 0 76 handler(s) 74 79 84
    //    0    0:iconst_1        
    //    1    1:anewarray       Object[]
    //    2    4:dup             
    //    3    5:iconst_0        
    //    4    6:new             #72  <Class Integer>
    //    5    9:dup             
    //    6   10:iload_1         
    //    7   11:invokespecial   #75  <Method void Integer(int)>
    //    8   14:aastore         
    //    9   15:astore_2        
    //   10   16:aconst_null     
    //   11   17:astore_3        
    //   12   18:iconst_0        
    //   13   19:istore          4
    //   14   21:aconst_null     
    //   15   22:astore          5
    //   16   24:aload_0         
    //   17   25:getfield        #14  <Field MethodInterceptor h>
    //   18   28:aload_0         
    //   19   29:getstatic       #160 <Field Method METHOD_11>
    //   20   32:aload_2         
    //   21   33:invokeinterface #39  <Method boolean MethodInterceptor.invokeSuper(Object, Method, Object[])>
    //   22   38:ifeq            54
    //   23   41:iconst_1        
    // try 42 52 handler(s) 52
    //   24   42:istore          4
    //   25   44:aload_0         
    //   26   45:iload_1         
    //   27   46:invokespecial   #163 <Method void Vector.removeElementAt(int)>
    //   28   49:goto            54
    // catch Throwable
    //   29   52:astore          5
    //   30   54:aload_0         
    //   31   55:getfield        #14  <Field MethodInterceptor h>
    //   32   58:aload_0         
    //   33   59:getstatic       #160 <Field Method METHOD_11>
    //   34   62:aload_2         
    //   35   63:iload           4
    //   36   65:aload_3         
    //   37   66:aload           5
    //   38   68:invokeinterface #35  <Method Object MethodInterceptor.afterReturn(Object, Method, Object[], boolean, Object, Throwable)>
    //   39   73:return          
    // catch RuntimeException
    //   40   74:astore          7
    //   41   76:aload           7
    //   42   78:athrow          
    // catch Error
    //   43   79:astore          8
    //   44   81:aload           8
    //   45   83:athrow          
    // catch Throwable
    //   46   84:astore          9
    //   47   86:new             #60  <Class UndeclaredThrowableException>
    //   48   89:dup             
    //   49   90:aload           9
    //   50   92:invokespecial   #63  <Method void UndeclaredThrowableException(Throwable)>
    //   51   95:athrow          
    }

    public final Object set(int i, Object obj)
    {
    // try 0 89 handler(s) 87 92 97
    //    0    0:iconst_2        
    //    1    1:anewarray       Object[]
    //    2    4:dup             
    //    3    5:iconst_0        
    //    4    6:new             #72  <Class Integer>
    //    5    9:dup             
    //    6   10:iload_1         
    //    7   11:invokespecial   #75  <Method void Integer(int)>
    //    8   14:aastore         
    //    9   15:dup             
    //   10   16:iconst_1        
    //   11   17:aload_2         
    //   12   18:aastore         
    //   13   19:astore_3        
    //   14   20:aconst_null     
    //   15   21:astore          4
    //   16   23:iconst_0        
    //   17   24:istore          5
    //   18   26:aconst_null     
    //   19   27:astore          6
    //   20   29:aload_0         
    //   21   30:getfield        #14  <Field MethodInterceptor h>
    //   22   33:aload_0         
    //   23   34:getstatic       #166 <Field Method METHOD_12>
    //   24   37:aload_3         
    //   25   38:invokeinterface #39  <Method boolean MethodInterceptor.invokeSuper(Object, Method, Object[])>
    //   26   43:ifeq            62
    //   27   46:iconst_1        
    // try 47 57 handler(s) 60
    //   28   47:istore          5
    //   29   49:aload_0         
    //   30   50:iload_1         
    //   31   51:aload_2         
    //   32   52:invokespecial   #170 <Method Object Vector.set(int, Object)>
    //   33   55:astore          4
    //   34   57:goto            62
    // catch Throwable
    //   35   60:astore          6
    //   36   62:aload_0         
    //   37   63:getfield        #14  <Field MethodInterceptor h>
    //   38   66:aload_0         
    //   39   67:getstatic       #166 <Field Method METHOD_12>
    //   40   70:aload_3         
    //   41   71:iload           5
    //   42   73:aload           4
    //   43   75:aload           6
    //   44   77:invokeinterface #35  <Method Object MethodInterceptor.afterReturn(Object, Method, Object[], boolean, Object, Throwable)>
    //   45   82:astore          7
    //   46   84:aload           7
    //   47   86:areturn         
    // catch RuntimeException
    //   48   87:astore          8
    //   49   89:aload           8
    //   50   91:athrow          
    // catch Error
    //   51   92:astore          9
    //   52   94:aload           9
    //   53   96:athrow          
    // catch Throwable
    //   54   97:astore          10
    //   55   99:new             #60  <Class UndeclaredThrowableException>
    //   56  102:dup             
    //   57  103:aload           10
    //   58  105:invokespecial   #63  <Method void UndeclaredThrowableException(Throwable)>
    //   59  108:athrow          
    }

    public final void add(int i, Object obj)
    {
    // try 0 83 handler(s) 81 86 91
    //    0    0:iconst_2        
    //    1    1:anewarray       Object[]
    //    2    4:dup             
    //    3    5:iconst_0        
    //    4    6:new             #72  <Class Integer>
    //    5    9:dup             
    //    6   10:iload_1         
    //    7   11:invokespecial   #75  <Method void Integer(int)>
    //    8   14:aastore         
    //    9   15:dup             
    //   10   16:iconst_1        
    //   11   17:aload_2         
    //   12   18:aastore         
    //   13   19:astore_3        
    //   14   20:aconst_null     
    //   15   21:astore          4
    //   16   23:iconst_0        
    //   17   24:istore          5
    //   18   26:aconst_null     
    //   19   27:astore          6
    //   20   29:aload_0         
    //   21   30:getfield        #14  <Field MethodInterceptor h>
    //   22   33:aload_0         
    //   23   34:getstatic       #173 <Field Method METHOD_13>
    //   24   37:aload_3         
    //   25   38:invokeinterface #39  <Method boolean MethodInterceptor.invokeSuper(Object, Method, Object[])>
    //   26   43:ifeq            60
    //   27   46:iconst_1        
    // try 47 58 handler(s) 58
    //   28   47:istore          5
    //   29   49:aload_0         
    //   30   50:iload_1         
    //   31   51:aload_2         
    //   32   52:invokespecial   #177 <Method void Vector.add(int, Object)>
    //   33   55:goto            60
    // catch Throwable
    //   34   58:astore          6
    //   35   60:aload_0         
    //   36   61:getfield        #14  <Field MethodInterceptor h>
    //   37   64:aload_0         
    //   38   65:getstatic       #173 <Field Method METHOD_13>
    //   39   68:aload_3         
    //   40   69:iload           5
    //   41   71:aload           4
    //   42   73:aload           6
    //   43   75:invokeinterface #35  <Method Object MethodInterceptor.afterReturn(Object, Method, Object[], boolean, Object, Throwable)>
    //   44   80:return          
    // catch RuntimeException
    //   45   81:astore          8
    //   46   83:aload           8
    //   47   85:athrow          
    // catch Error
    //   48   86:astore          9
    //   49   88:aload           9
    //   50   90:athrow          
    // catch Throwable
    //   51   91:astore          10
    //   52   93:new             #60  <Class UndeclaredThrowableException>
    //   53   96:dup             
    //   54   97:aload           10
    //   55   99:invokespecial   #63  <Method void UndeclaredThrowableException(Throwable)>
    //   56  102:athrow          
    }

    public final boolean add(Object obj)
    {
    // try 0 94 handler(s) 92 97 102
    //    0    0:iconst_1        
    //    1    1:anewarray       Object[]
    //    2    4:dup             
    //    3    5:iconst_0        
    //    4    6:aload_1         
    //    5    7:aastore         
    //    6    8:astore_2        
    //    7    9:aconst_null     
    //    8   10:astore_3        
    //    9   11:iconst_0        
    //   10   12:istore          4
    //   11   14:aconst_null     
    //   12   15:astore          5
    //   13   17:aload_0         
    //   14   18:getfield        #14  <Field MethodInterceptor h>
    //   15   21:aload_0         
    //   16   22:getstatic       #180 <Field Method METHOD_14>
    //   17   25:aload_2         
    //   18   26:invokeinterface #39  <Method boolean MethodInterceptor.invokeSuper(Object, Method, Object[])>
    //   19   31:ifeq            55
    //   20   34:iconst_1        
    //   21   35:istore          4
    // try 37 50 handler(s) 53
    //   22   37:new             #47  <Class Boolean>
    //   23   40:dup             
    //   24   41:aload_0         
    //   25   42:aload_1         
    //   26   43:invokespecial   #182 <Method boolean Vector.add(Object)>
    //   27   46:invokespecial   #54  <Method void Boolean(boolean)>
    //   28   49:astore_3        
    //   29   50:goto            55
    // catch Throwable
    //   30   53:astore          5
    //   31   55:aload_0         
    //   32   56:getfield        #14  <Field MethodInterceptor h>
    //   33   59:aload_0         
    //   34   60:getstatic       #180 <Field Method METHOD_14>
    //   35   63:aload_2         
    //   36   64:iload           4
    //   37   66:aload_3         
    //   38   67:aload           5
    //   39   69:invokeinterface #35  <Method Object MethodInterceptor.afterReturn(Object, Method, Object[], boolean, Object, Throwable)>
    //   40   74:astore          6
    //   41   76:aload           6
    //   42   78:ifnonnull       83
    //   43   81:iconst_0        
    //   44   82:ireturn         
    //   45   83:aload           6
    //   46   85:checkcast       #47  <Class Boolean>
    //   47   88:invokevirtual   #58  <Method boolean Boolean.booleanValue()>
    //   48   91:ireturn         
    // catch RuntimeException
    //   49   92:astore          7
    //   50   94:aload           7
    //   51   96:athrow          
    // catch Error
    //   52   97:astore          8
    //   53   99:aload           8
    //   54  101:athrow          
    // catch Throwable
    //   55  102:astore          9
    //   56  104:new             #60  <Class UndeclaredThrowableException>
    //   57  107:dup             
    //   58  108:aload           9
    //   59  110:invokespecial   #63  <Method void UndeclaredThrowableException(Throwable)>
    //   60  113:athrow          
    }

    public final boolean contains(Object obj)
    {
    // try 0 94 handler(s) 92 97 102
    //    0    0:iconst_1        
    //    1    1:anewarray       Object[]
    //    2    4:dup             
    //    3    5:iconst_0        
    //    4    6:aload_1         
    //    5    7:aastore         
    //    6    8:astore_2        
    //    7    9:aconst_null     
    //    8   10:astore_3        
    //    9   11:iconst_0        
    //   10   12:istore          4
    //   11   14:aconst_null     
    //   12   15:astore          5
    //   13   17:aload_0         
    //   14   18:getfield        #14  <Field MethodInterceptor h>
    //   15   21:aload_0         
    //   16   22:getstatic       #185 <Field Method METHOD_15>
    //   17   25:aload_2         
    //   18   26:invokeinterface #39  <Method boolean MethodInterceptor.invokeSuper(Object, Method, Object[])>
    //   19   31:ifeq            55
    //   20   34:iconst_1        
    //   21   35:istore          4
    // try 37 50 handler(s) 53
    //   22   37:new             #47  <Class Boolean>
    //   23   40:dup             
    //   24   41:aload_0         
    //   25   42:aload_1         
    //   26   43:invokespecial   #188 <Method boolean Vector.contains(Object)>
    //   27   46:invokespecial   #54  <Method void Boolean(boolean)>
    //   28   49:astore_3        
    //   29   50:goto            55
    // catch Throwable
    //   30   53:astore          5
    //   31   55:aload_0         
    //   32   56:getfield        #14  <Field MethodInterceptor h>
    //   33   59:aload_0         
    //   34   60:getstatic       #185 <Field Method METHOD_15>
    //   35   63:aload_2         
    //   36   64:iload           4
    //   37   66:aload_3         
    //   38   67:aload           5
    //   39   69:invokeinterface #35  <Method Object MethodInterceptor.afterReturn(Object, Method, Object[], boolean, Object, Throwable)>
    //   40   74:astore          6
    //   41   76:aload           6
    //   42   78:ifnonnull       83
    //   43   81:iconst_0        
    //   44   82:ireturn         
    //   45   83:aload           6
    //   46   85:checkcast       #47  <Class Boolean>
    //   47   88:invokevirtual   #58  <Method boolean Boolean.booleanValue()>
    //   48   91:ireturn         
    // catch RuntimeException
    //   49   92:astore          7
    //   50   94:aload           7
    //   51   96:athrow          
    // catch Error
    //   52   97:astore          8
    //   53   99:aload           8
    //   54  101:athrow          
    // catch Throwable
    //   55  102:astore          9
    //   56  104:new             #60  <Class UndeclaredThrowableException>
    //   57  107:dup             
    //   58  108:aload           9
    //   59  110:invokespecial   #63  <Method void UndeclaredThrowableException(Throwable)>
    //   60  113:athrow          
    }

    protected final void finalize()
        throws Throwable
    {
    // try 0 61 handler(s) 59 64 69 74
    //    0    0:iconst_0        
    //    1    1:anewarray       Object[]
    //    2    4:astore_1        
    //    3    5:aconst_null     
    //    4    6:astore_2        
    //    5    7:iconst_0        
    //    6    8:istore_3        
    //    7    9:aconst_null     
    //    8   10:astore          4
    //    9   12:aload_0         
    //   10   13:getfield        #14  <Field MethodInterceptor h>
    //   11   16:aload_0         
    //   12   17:getstatic       #191 <Field Method METHOD_16>
    //   13   20:aload_1         
    //   14   21:invokeinterface #39  <Method boolean MethodInterceptor.invokeSuper(Object, Method, Object[])>
    //   15   26:ifeq            40
    //   16   29:iconst_1        
    // try 30 38 handler(s) 38
    //   17   30:istore_3        
    //   18   31:aload_0         
    //   19   32:invokespecial   #194 <Method void Vector.finalize()>
    //   20   35:goto            40
    // catch Throwable
    //   21   38:astore          4
    //   22   40:aload_0         
    //   23   41:getfield        #14  <Field MethodInterceptor h>
    //   24   44:aload_0         
    //   25   45:getstatic       #191 <Field Method METHOD_16>
    //   26   48:aload_1         
    //   27   49:iload_3         
    //   28   50:aload_2         
    //   29   51:aload           4
    //   30   53:invokeinterface #35  <Method Object MethodInterceptor.afterReturn(Object, Method, Object[], boolean, Object, Throwable)>
    //   31   58:return          
    // catch RuntimeException
    //   32   59:astore          6
    //   33   61:aload           6
    //   34   63:athrow          
    // catch Error
    //   35   64:astore          7
    //   36   66:aload           7
    //   37   68:athrow          
    // catch Throwable
    //   38   69:astore          8
    //   39   71:aload           8
    //   40   73:athrow          
    // catch Throwable
    //   41   74:astore          9
    //   42   76:new             #60  <Class UndeclaredThrowableException>
    //   43   79:dup             
    //   44   80:aload           9
    //   45   82:invokespecial   #63  <Method void UndeclaredThrowableException(Throwable)>
    //   46   85:athrow          
    }

    public final void trimToSize()
    {
    // try 0 61 handler(s) 59 64 69
    //    0    0:iconst_0        
    //    1    1:anewarray       Object[]
    //    2    4:astore_1        
    //    3    5:aconst_null     
    //    4    6:astore_2        
    //    5    7:iconst_0        
    //    6    8:istore_3        
    //    7    9:aconst_null     
    //    8   10:astore          4
    //    9   12:aload_0         
    //   10   13:getfield        #14  <Field MethodInterceptor h>
    //   11   16:aload_0         
    //   12   17:getstatic       #198 <Field Method METHOD_17>
    //   13   20:aload_1         
    //   14   21:invokeinterface #39  <Method boolean MethodInterceptor.invokeSuper(Object, Method, Object[])>
    //   15   26:ifeq            40
    //   16   29:iconst_1        
    // try 30 38 handler(s) 38
    //   17   30:istore_3        
    //   18   31:aload_0         
    //   19   32:invokespecial   #201 <Method void Vector.trimToSize()>
    //   20   35:goto            40
    // catch Throwable
    //   21   38:astore          4
    //   22   40:aload_0         
    //   23   41:getfield        #14  <Field MethodInterceptor h>
    //   24   44:aload_0         
    //   25   45:getstatic       #198 <Field Method METHOD_17>
    //   26   48:aload_1         
    //   27   49:iload_3         
    //   28   50:aload_2         
    //   29   51:aload           4
    //   30   53:invokeinterface #35  <Method Object MethodInterceptor.afterReturn(Object, Method, Object[], boolean, Object, Throwable)>
    //   31   58:return          
    // catch RuntimeException
    //   32   59:astore          6
    //   33   61:aload           6
    //   34   63:athrow          
    // catch Error
    //   35   64:astore          7
    //   36   66:aload           7
    //   37   68:athrow          
    // catch Throwable
    //   38   69:astore          8
    //   39   71:new             #60  <Class UndeclaredThrowableException>
    //   40   74:dup             
    //   41   75:aload           8
    //   42   77:invokespecial   #63  <Method void UndeclaredThrowableException(Throwable)>
    //   43   80:athrow          
    }

    public final Object[] toArray(Object aobj[])
    {
    // try 0 77 handler(s) 75 80 85
    //    0    0:iconst_1        
    //    1    1:anewarray       Object[]
    //    2    4:dup             
    //    3    5:iconst_0        
    //    4    6:aload_1         
    //    5    7:aastore         
    //    6    8:astore_2        
    //    7    9:aconst_null     
    //    8   10:astore_3        
    //    9   11:iconst_0        
    //   10   12:istore          4
    //   11   14:aconst_null     
    //   12   15:astore          5
    //   13   17:aload_0         
    //   14   18:getfield        #14  <Field MethodInterceptor h>
    //   15   21:aload_0         
    //   16   22:getstatic       #204 <Field Method METHOD_18>
    //   17   25:aload_2         
    //   18   26:invokeinterface #39  <Method boolean MethodInterceptor.invokeSuper(Object, Method, Object[])>
    //   19   31:ifeq            48
    //   20   34:iconst_1        
    // try 35 43 handler(s) 46
    //   21   35:istore          4
    //   22   37:aload_0         
    //   23   38:aload_1         
    //   24   39:invokespecial   #208 <Method Object[] Vector.toArray(Object[])>
    //   25   42:astore_3        
    //   26   43:goto            48
    // catch Throwable
    //   27   46:astore          5
    //   28   48:aload_0         
    //   29   49:getfield        #14  <Field MethodInterceptor h>
    //   30   52:aload_0         
    //   31   53:getstatic       #204 <Field Method METHOD_18>
    //   32   56:aload_2         
    //   33   57:iload           4
    //   34   59:aload_3         
    //   35   60:aload           5
    //   36   62:invokeinterface #35  <Method Object MethodInterceptor.afterReturn(Object, Method, Object[], boolean, Object, Throwable)>
    //   37   67:astore          6
    //   38   69:aload           6
    //   39   71:checkcast       #210 <Class Object[]>
    //   40   74:areturn         
    // catch RuntimeException
    //   41   75:astore          7
    //   42   77:aload           7
    //   43   79:athrow          
    // catch Error
    //   44   80:astore          8
    //   45   82:aload           8
    //   46   84:athrow          
    // catch Throwable
    //   47   85:astore          9
    //   48   87:new             #60  <Class UndeclaredThrowableException>
    //   49   90:dup             
    //   50   91:aload           9
    //   51   93:invokespecial   #63  <Method void UndeclaredThrowableException(Throwable)>
    //   52   96:athrow          
    }

    public final Object[] toArray()
    {
    // try 0 69 handler(s) 67 72 77
    //    0    0:iconst_0        
    //    1    1:anewarray       Object[]
    //    2    4:astore_1        
    //    3    5:aconst_null     
    //    4    6:astore_2        
    //    5    7:iconst_0        
    //    6    8:istore_3        
    //    7    9:aconst_null     
    //    8   10:astore          4
    //    9   12:aload_0         
    //   10   13:getfield        #14  <Field MethodInterceptor h>
    //   11   16:aload_0         
    //   12   17:getstatic       #213 <Field Method METHOD_19>
    //   13   20:aload_1         
    //   14   21:invokeinterface #39  <Method boolean MethodInterceptor.invokeSuper(Object, Method, Object[])>
    //   15   26:ifeq            41
    //   16   29:iconst_1        
    // try 30 36 handler(s) 39
    //   17   30:istore_3        
    //   18   31:aload_0         
    //   19   32:invokespecial   #216 <Method Object[] Vector.toArray()>
    //   20   35:astore_2        
    //   21   36:goto            41
    // catch Throwable
    //   22   39:astore          4
    //   23   41:aload_0         
    //   24   42:getfield        #14  <Field MethodInterceptor h>
    //   25   45:aload_0         
    //   26   46:getstatic       #213 <Field Method METHOD_19>
    //   27   49:aload_1         
    //   28   50:iload_3         
    //   29   51:aload_2         
    //   30   52:aload           4
    //   31   54:invokeinterface #35  <Method Object MethodInterceptor.afterReturn(Object, Method, Object[], boolean, Object, Throwable)>
    //   32   59:astore          5
    //   33   61:aload           5
    //   34   63:checkcast       #210 <Class Object[]>
    //   35   66:areturn         
    // catch RuntimeException
    //   36   67:astore          6
    //   37   69:aload           6
    //   38   71:athrow          
    // catch Error
    //   39   72:astore          7
    //   40   74:aload           7
    //   41   76:athrow          
    // catch Throwable
    //   42   77:astore          8
    //   43   79:new             #60  <Class UndeclaredThrowableException>
    //   44   82:dup             
    //   45   83:aload           8
    //   46   85:invokespecial   #63  <Method void UndeclaredThrowableException(Throwable)>
    //   47   88:athrow          
    }

    public final Object clone()
    {
    // try 0 66 handler(s) 64 69 74
    //    0    0:iconst_0        
    //    1    1:anewarray       Object[]
    //    2    4:astore_1        
    //    3    5:aconst_null     
    //    4    6:astore_2        
    //    5    7:iconst_0        
    //    6    8:istore_3        
    //    7    9:aconst_null     
    //    8   10:astore          4
    //    9   12:aload_0         
    //   10   13:getfield        #14  <Field MethodInterceptor h>
    //   11   16:aload_0         
    //   12   17:getstatic       #219 <Field Method METHOD_20>
    //   13   20:aload_1         
    //   14   21:invokeinterface #39  <Method boolean MethodInterceptor.invokeSuper(Object, Method, Object[])>
    //   15   26:ifeq            41
    //   16   29:iconst_1        
    // try 30 36 handler(s) 39
    //   17   30:istore_3        
    //   18   31:aload_0         
    //   19   32:invokespecial   #222 <Method Object Vector.clone()>
    //   20   35:astore_2        
    //   21   36:goto            41
    // catch Throwable
    //   22   39:astore          4
    //   23   41:aload_0         
    //   24   42:getfield        #14  <Field MethodInterceptor h>
    //   25   45:aload_0         
    //   26   46:getstatic       #219 <Field Method METHOD_20>
    //   27   49:aload_1         
    //   28   50:iload_3         
    //   29   51:aload_2         
    //   30   52:aload           4
    //   31   54:invokeinterface #35  <Method Object MethodInterceptor.afterReturn(Object, Method, Object[], boolean, Object, Throwable)>
    //   32   59:astore          5
    //   33   61:aload           5
    //   34   63:areturn         
    // catch RuntimeException
    //   35   64:astore          6
    //   36   66:aload           6
    //   37   68:athrow          
    // catch Error
    //   38   69:astore          7
    //   39   71:aload           7
    //   40   73:athrow          
    // catch Throwable
    //   41   74:astore          8
    //   42   76:new             #60  <Class UndeclaredThrowableException>
    //   43   79:dup             
    //   44   80:aload           8
    //   45   82:invokespecial   #63  <Method void UndeclaredThrowableException(Throwable)>
    //   46   85:athrow          
    }

    public final boolean retainAll(Collection collection)
    {
    // try 0 94 handler(s) 92 97 102
    //    0    0:iconst_1        
    //    1    1:anewarray       Object[]
    //    2    4:dup             
    //    3    5:iconst_0        
    //    4    6:aload_1         
    //    5    7:aastore         
    //    6    8:astore_2        
    //    7    9:aconst_null     
    //    8   10:astore_3        
    //    9   11:iconst_0        
    //   10   12:istore          4
    //   11   14:aconst_null     
    //   12   15:astore          5
    //   13   17:aload_0         
    //   14   18:getfield        #14  <Field MethodInterceptor h>
    //   15   21:aload_0         
    //   16   22:getstatic       #225 <Field Method METHOD_21>
    //   17   25:aload_2         
    //   18   26:invokeinterface #39  <Method boolean MethodInterceptor.invokeSuper(Object, Method, Object[])>
    //   19   31:ifeq            55
    //   20   34:iconst_1        
    //   21   35:istore          4
    // try 37 50 handler(s) 53
    //   22   37:new             #47  <Class Boolean>
    //   23   40:dup             
    //   24   41:aload_0         
    //   25   42:aload_1         
    //   26   43:invokespecial   #228 <Method boolean Vector.retainAll(Collection)>
    //   27   46:invokespecial   #54  <Method void Boolean(boolean)>
    //   28   49:astore_3        
    //   29   50:goto            55
    // catch Throwable
    //   30   53:astore          5
    //   31   55:aload_0         
    //   32   56:getfield        #14  <Field MethodInterceptor h>
    //   33   59:aload_0         
    //   34   60:getstatic       #225 <Field Method METHOD_21>
    //   35   63:aload_2         
    //   36   64:iload           4
    //   37   66:aload_3         
    //   38   67:aload           5
    //   39   69:invokeinterface #35  <Method Object MethodInterceptor.afterReturn(Object, Method, Object[], boolean, Object, Throwable)>
    //   40   74:astore          6
    //   41   76:aload           6
    //   42   78:ifnonnull       83
    //   43   81:iconst_0        
    //   44   82:ireturn         
    //   45   83:aload           6
    //   46   85:checkcast       #47  <Class Boolean>
    //   47   88:invokevirtual   #58  <Method boolean Boolean.booleanValue()>
    //   48   91:ireturn         
    // catch RuntimeException
    //   49   92:astore          7
    //   50   94:aload           7
    //   51   96:athrow          
    // catch Error
    //   52   97:astore          8
    //   53   99:aload           8
    //   54  101:athrow          
    // catch Throwable
    //   55  102:astore          9
    //   56  104:new             #60  <Class UndeclaredThrowableException>
    //   57  107:dup             
    //   58  108:aload           9
    //   59  110:invokespecial   #63  <Method void UndeclaredThrowableException(Throwable)>
    //   60  113:athrow          
    }

    public final int hashCode()
    {
    // try 0 86 handler(s) 84 89 94
    //    0    0:iconst_0        
    //    1    1:anewarray       Object[]
    //    2    4:astore_1        
    //    3    5:aconst_null     
    //    4    6:astore_2        
    //    5    7:iconst_0        
    //    6    8:istore_3        
    //    7    9:aconst_null     
    //    8   10:astore          4
    //    9   12:aload_0         
    //   10   13:getfield        #14  <Field MethodInterceptor h>
    //   11   16:aload_0         
    //   12   17:getstatic       #231 <Field Method METHOD_22>
    //   13   20:aload_1         
    //   14   21:invokeinterface #39  <Method boolean MethodInterceptor.invokeSuper(Object, Method, Object[])>
    //   15   26:ifeq            48
    //   16   29:iconst_1        
    //   17   30:istore_3        
    // try 31 43 handler(s) 46
    //   18   31:new             #72  <Class Integer>
    //   19   34:dup             
    //   20   35:aload_0         
    //   21   36:invokespecial   #234 <Method int Vector.hashCode()>
    //   22   39:invokespecial   #75  <Method void Integer(int)>
    //   23   42:astore_2        
    //   24   43:goto            48
    // catch Throwable
    //   25   46:astore          4
    //   26   48:aload_0         
    //   27   49:getfield        #14  <Field MethodInterceptor h>
    //   28   52:aload_0         
    //   29   53:getstatic       #231 <Field Method METHOD_22>
    //   30   56:aload_1         
    //   31   57:iload_3         
    //   32   58:aload_2         
    //   33   59:aload           4
    //   34   61:invokeinterface #35  <Method Object MethodInterceptor.afterReturn(Object, Method, Object[], boolean, Object, Throwable)>
    //   35   66:astore          5
    //   36   68:aload           5
    //   37   70:ifnonnull       75
    //   38   73:iconst_0        
    //   39   74:ireturn         
    //   40   75:aload           5
    //   41   77:checkcast       #99  <Class Number>
    //   42   80:invokevirtual   #102 <Method int Number.intValue()>
    //   43   83:ireturn         
    // catch RuntimeException
    //   44   84:astore          6
    //   45   86:aload           6
    //   46   88:athrow          
    // catch Error
    //   47   89:astore          7
    //   48   91:aload           7
    //   49   93:athrow          
    // catch Throwable
    //   50   94:astore          8
    //   51   96:new             #60  <Class UndeclaredThrowableException>
    //   52   99:dup             
    //   53  100:aload           8
    //   54  102:invokespecial   #63  <Method void UndeclaredThrowableException(Throwable)>
    //   55  105:athrow          
    }

    public final void addElement(Object obj)
    {
    // try 0 69 handler(s) 67 72 77
    //    0    0:iconst_1        
    //    1    1:anewarray       Object[]
    //    2    4:dup             
    //    3    5:iconst_0        
    //    4    6:aload_1         
    //    5    7:aastore         
    //    6    8:astore_2        
    //    7    9:aconst_null     
    //    8   10:astore_3        
    //    9   11:iconst_0        
    //   10   12:istore          4
    //   11   14:aconst_null     
    //   12   15:astore          5
    //   13   17:aload_0         
    //   14   18:getfield        #14  <Field MethodInterceptor h>
    //   15   21:aload_0         
    //   16   22:getstatic       #237 <Field Method METHOD_23>
    //   17   25:aload_2         
    //   18   26:invokeinterface #39  <Method boolean MethodInterceptor.invokeSuper(Object, Method, Object[])>
    //   19   31:ifeq            47
    //   20   34:iconst_1        
    // try 35 45 handler(s) 45
    //   21   35:istore          4
    //   22   37:aload_0         
    //   23   38:aload_1         
    //   24   39:invokespecial   #241 <Method void Vector.addElement(Object)>
    //   25   42:goto            47
    // catch Throwable
    //   26   45:astore          5
    //   27   47:aload_0         
    //   28   48:getfield        #14  <Field MethodInterceptor h>
    //   29   51:aload_0         
    //   30   52:getstatic       #237 <Field Method METHOD_23>
    //   31   55:aload_2         
    //   32   56:iload           4
    //   33   58:aload_3         
    //   34   59:aload           5
    //   35   61:invokeinterface #35  <Method Object MethodInterceptor.afterReturn(Object, Method, Object[], boolean, Object, Throwable)>
    //   36   66:return          
    // catch RuntimeException
    //   37   67:astore          7
    //   38   69:aload           7
    //   39   71:athrow          
    // catch Error
    //   40   72:astore          8
    //   41   74:aload           8
    //   42   76:athrow          
    // catch Throwable
    //   43   77:astore          9
    //   44   79:new             #60  <Class UndeclaredThrowableException>
    //   45   82:dup             
    //   46   83:aload           9
    //   47   85:invokespecial   #63  <Method void UndeclaredThrowableException(Throwable)>
    //   48   88:athrow          
    }

    protected final void removeRange(int i, int j)
    {
    // try 0 90 handler(s) 88 93 98
    //    0    0:iconst_2        
    //    1    1:anewarray       Object[]
    //    2    4:dup             
    //    3    5:iconst_0        
    //    4    6:new             #72  <Class Integer>
    //    5    9:dup             
    //    6   10:iload_1         
    //    7   11:invokespecial   #75  <Method void Integer(int)>
    //    8   14:aastore         
    //    9   15:dup             
    //   10   16:iconst_1        
    //   11   17:new             #72  <Class Integer>
    //   12   20:dup             
    //   13   21:iload_2         
    //   14   22:invokespecial   #75  <Method void Integer(int)>
    //   15   25:aastore         
    //   16   26:astore_3        
    //   17   27:aconst_null     
    //   18   28:astore          4
    //   19   30:iconst_0        
    //   20   31:istore          5
    //   21   33:aconst_null     
    //   22   34:astore          6
    //   23   36:aload_0         
    //   24   37:getfield        #14  <Field MethodInterceptor h>
    //   25   40:aload_0         
    //   26   41:getstatic       #244 <Field Method METHOD_24>
    //   27   44:aload_3         
    //   28   45:invokeinterface #39  <Method boolean MethodInterceptor.invokeSuper(Object, Method, Object[])>
    //   29   50:ifeq            67
    //   30   53:iconst_1        
    // try 54 65 handler(s) 65
    //   31   54:istore          5
    //   32   56:aload_0         
    //   33   57:iload_1         
    //   34   58:iload_2         
    //   35   59:invokespecial   #248 <Method void Vector.removeRange(int, int)>
    //   36   62:goto            67
    // catch Throwable
    //   37   65:astore          6
    //   38   67:aload_0         
    //   39   68:getfield        #14  <Field MethodInterceptor h>
    //   40   71:aload_0         
    //   41   72:getstatic       #244 <Field Method METHOD_24>
    //   42   75:aload_3         
    //   43   76:iload           5
    //   44   78:aload           4
    //   45   80:aload           6
    //   46   82:invokeinterface #35  <Method Object MethodInterceptor.afterReturn(Object, Method, Object[], boolean, Object, Throwable)>
    //   47   87:return          
    // catch RuntimeException
    //   48   88:astore          8
    //   49   90:aload           8
    //   50   92:athrow          
    // catch Error
    //   51   93:astore          9
    //   52   95:aload           9
    //   53   97:athrow          
    // catch Throwable
    //   54   98:astore          10
    //   55  100:new             #60  <Class UndeclaredThrowableException>
    //   56  103:dup             
    //   57  104:aload           10
    //   58  106:invokespecial   #63  <Method void UndeclaredThrowableException(Throwable)>
    //   59  109:athrow          
    }

    public final boolean removeElement(Object obj)
    {
    // try 0 94 handler(s) 92 97 102
    //    0    0:iconst_1        
    //    1    1:anewarray       Object[]
    //    2    4:dup             
    //    3    5:iconst_0        
    //    4    6:aload_1         
    //    5    7:aastore         
    //    6    8:astore_2        
    //    7    9:aconst_null     
    //    8   10:astore_3        
    //    9   11:iconst_0        
    //   10   12:istore          4
    //   11   14:aconst_null     
    //   12   15:astore          5
    //   13   17:aload_0         
    //   14   18:getfield        #14  <Field MethodInterceptor h>
    //   15   21:aload_0         
    //   16   22:getstatic       #251 <Field Method METHOD_25>
    //   17   25:aload_2         
    //   18   26:invokeinterface #39  <Method boolean MethodInterceptor.invokeSuper(Object, Method, Object[])>
    //   19   31:ifeq            55
    //   20   34:iconst_1        
    //   21   35:istore          4
    // try 37 50 handler(s) 53
    //   22   37:new             #47  <Class Boolean>
    //   23   40:dup             
    //   24   41:aload_0         
    //   25   42:aload_1         
    //   26   43:invokespecial   #254 <Method boolean Vector.removeElement(Object)>
    //   27   46:invokespecial   #54  <Method void Boolean(boolean)>
    //   28   49:astore_3        
    //   29   50:goto            55
    // catch Throwable
    //   30   53:astore          5
    //   31   55:aload_0         
    //   32   56:getfield        #14  <Field MethodInterceptor h>
    //   33   59:aload_0         
    //   34   60:getstatic       #251 <Field Method METHOD_25>
    //   35   63:aload_2         
    //   36   64:iload           4
    //   37   66:aload_3         
    //   38   67:aload           5
    //   39   69:invokeinterface #35  <Method Object MethodInterceptor.afterReturn(Object, Method, Object[], boolean, Object, Throwable)>
    //   40   74:astore          6
    //   41   76:aload           6
    //   42   78:ifnonnull       83
    //   43   81:iconst_0        
    //   44   82:ireturn         
    //   45   83:aload           6
    //   46   85:checkcast       #47  <Class Boolean>
    //   47   88:invokevirtual   #58  <Method boolean Boolean.booleanValue()>
    //   48   91:ireturn         
    // catch RuntimeException
    //   49   92:astore          7
    //   50   94:aload           7
    //   51   96:athrow          
    // catch Error
    //   52   97:astore          8
    //   53   99:aload           8
    //   54  101:athrow          
    // catch Throwable
    //   55  102:astore          9
    //   56  104:new             #60  <Class UndeclaredThrowableException>
    //   57  107:dup             
    //   58  108:aload           9
    //   59  110:invokespecial   #63  <Method void UndeclaredThrowableException(Throwable)>
    //   60  113:athrow          
    }

    public final void ensureCapacity(int i)
    {
    // try 0 76 handler(s) 74 79 84
    //    0    0:iconst_1        
    //    1    1:anewarray       Object[]
    //    2    4:dup             
    //    3    5:iconst_0        
    //    4    6:new             #72  <Class Integer>
    //    5    9:dup             
    //    6   10:iload_1         
    //    7   11:invokespecial   #75  <Method void Integer(int)>
    //    8   14:aastore         
    //    9   15:astore_2        
    //   10   16:aconst_null     
    //   11   17:astore_3        
    //   12   18:iconst_0        
    //   13   19:istore          4
    //   14   21:aconst_null     
    //   15   22:astore          5
    //   16   24:aload_0         
    //   17   25:getfield        #14  <Field MethodInterceptor h>
    //   18   28:aload_0         
    //   19   29:getstatic       #257 <Field Method METHOD_26>
    //   20   32:aload_2         
    //   21   33:invokeinterface #39  <Method boolean MethodInterceptor.invokeSuper(Object, Method, Object[])>
    //   22   38:ifeq            54
    //   23   41:iconst_1        
    // try 42 52 handler(s) 52
    //   24   42:istore          4
    //   25   44:aload_0         
    //   26   45:iload_1         
    //   27   46:invokespecial   #260 <Method void Vector.ensureCapacity(int)>
    //   28   49:goto            54
    // catch Throwable
    //   29   52:astore          5
    //   30   54:aload_0         
    //   31   55:getfield        #14  <Field MethodInterceptor h>
    //   32   58:aload_0         
    //   33   59:getstatic       #257 <Field Method METHOD_26>
    //   34   62:aload_2         
    //   35   63:iload           4
    //   36   65:aload_3         
    //   37   66:aload           5
    //   38   68:invokeinterface #35  <Method Object MethodInterceptor.afterReturn(Object, Method, Object[], boolean, Object, Throwable)>
    //   39   73:return          
    // catch RuntimeException
    //   40   74:astore          7
    //   41   76:aload           7
    //   42   78:athrow          
    // catch Error
    //   43   79:astore          8
    //   44   81:aload           8
    //   45   83:athrow          
    // catch Throwable
    //   46   84:astore          9
    //   47   86:new             #60  <Class UndeclaredThrowableException>
    //   48   89:dup             
    //   49   90:aload           9
    //   50   92:invokespecial   #63  <Method void UndeclaredThrowableException(Throwable)>
    //   51   95:athrow          
    }

    public final void insertElementAt(Object obj, int i)
    {
    // try 0 83 handler(s) 81 86 91
    //    0    0:iconst_2        
    //    1    1:anewarray       Object[]
    //    2    4:dup             
    //    3    5:iconst_0        
    //    4    6:aload_1         
    //    5    7:aastore         
    //    6    8:dup             
    //    7    9:iconst_1        
    //    8   10:new             #72  <Class Integer>
    //    9   13:dup             
    //   10   14:iload_2         
    //   11   15:invokespecial   #75  <Method void Integer(int)>
    //   12   18:aastore         
    //   13   19:astore_3        
    //   14   20:aconst_null     
    //   15   21:astore          4
    //   16   23:iconst_0        
    //   17   24:istore          5
    //   18   26:aconst_null     
    //   19   27:astore          6
    //   20   29:aload_0         
    //   21   30:getfield        #14  <Field MethodInterceptor h>
    //   22   33:aload_0         
    //   23   34:getstatic       #263 <Field Method METHOD_27>
    //   24   37:aload_3         
    //   25   38:invokeinterface #39  <Method boolean MethodInterceptor.invokeSuper(Object, Method, Object[])>
    //   26   43:ifeq            60
    //   27   46:iconst_1        
    // try 47 58 handler(s) 58
    //   28   47:istore          5
    //   29   49:aload_0         
    //   30   50:aload_1         
    //   31   51:iload_2         
    //   32   52:invokespecial   #267 <Method void Vector.insertElementAt(Object, int)>
    //   33   55:goto            60
    // catch Throwable
    //   34   58:astore          6
    //   35   60:aload_0         
    //   36   61:getfield        #14  <Field MethodInterceptor h>
    //   37   64:aload_0         
    //   38   65:getstatic       #263 <Field Method METHOD_27>
    //   39   68:aload_3         
    //   40   69:iload           5
    //   41   71:aload           4
    //   42   73:aload           6
    //   43   75:invokeinterface #35  <Method Object MethodInterceptor.afterReturn(Object, Method, Object[], boolean, Object, Throwable)>
    //   44   80:return          
    // catch RuntimeException
    //   45   81:astore          8
    //   46   83:aload           8
    //   47   85:athrow          
    // catch Error
    //   48   86:astore          9
    //   49   88:aload           9
    //   50   90:athrow          
    // catch Throwable
    //   51   91:astore          10
    //   52   93:new             #60  <Class UndeclaredThrowableException>
    //   53   96:dup             
    //   54   97:aload           10
    //   55   99:invokespecial   #63  <Method void UndeclaredThrowableException(Throwable)>
    //   56  102:athrow          
    }

    public final Object get(int i)
    {
    // try 0 81 handler(s) 79 84 89
    //    0    0:iconst_1        
    //    1    1:anewarray       Object[]
    //    2    4:dup             
    //    3    5:iconst_0        
    //    4    6:new             #72  <Class Integer>
    //    5    9:dup             
    //    6   10:iload_1         
    //    7   11:invokespecial   #75  <Method void Integer(int)>
    //    8   14:aastore         
    //    9   15:astore_2        
    //   10   16:aconst_null     
    //   11   17:astore_3        
    //   12   18:iconst_0        
    //   13   19:istore          4
    //   14   21:aconst_null     
    //   15   22:astore          5
    //   16   24:aload_0         
    //   17   25:getfield        #14  <Field MethodInterceptor h>
    //   18   28:aload_0         
    //   19   29:getstatic       #270 <Field Method METHOD_28>
    //   20   32:aload_2         
    //   21   33:invokeinterface #39  <Method boolean MethodInterceptor.invokeSuper(Object, Method, Object[])>
    //   22   38:ifeq            55
    //   23   41:iconst_1        
    // try 42 50 handler(s) 53
    //   24   42:istore          4
    //   25   44:aload_0         
    //   26   45:iload_1         
    //   27   46:invokespecial   #274 <Method Object Vector.get(int)>
    //   28   49:astore_3        
    //   29   50:goto            55
    // catch Throwable
    //   30   53:astore          5
    //   31   55:aload_0         
    //   32   56:getfield        #14  <Field MethodInterceptor h>
    //   33   59:aload_0         
    //   34   60:getstatic       #270 <Field Method METHOD_28>
    //   35   63:aload_2         
    //   36   64:iload           4
    //   37   66:aload_3         
    //   38   67:aload           5
    //   39   69:invokeinterface #35  <Method Object MethodInterceptor.afterReturn(Object, Method, Object[], boolean, Object, Throwable)>
    //   40   74:astore          6
    //   41   76:aload           6
    //   42   78:areturn         
    // catch RuntimeException
    //   43   79:astore          7
    //   44   81:aload           7
    //   45   83:athrow          
    // catch Error
    //   46   84:astore          8
    //   47   86:aload           8
    //   48   88:athrow          
    // catch Throwable
    //   49   89:astore          9
    //   50   91:new             #60  <Class UndeclaredThrowableException>
    //   51   94:dup             
    //   52   95:aload           9
    //   53   97:invokespecial   #63  <Method void UndeclaredThrowableException(Throwable)>
    //   54  100:athrow          
    }

    public final void removeAllElements()
    {
    // try 0 61 handler(s) 59 64 69
    //    0    0:iconst_0        
    //    1    1:anewarray       Object[]
    //    2    4:astore_1        
    //    3    5:aconst_null     
    //    4    6:astore_2        
    //    5    7:iconst_0        
    //    6    8:istore_3        
    //    7    9:aconst_null     
    //    8   10:astore          4
    //    9   12:aload_0         
    //   10   13:getfield        #14  <Field MethodInterceptor h>
    //   11   16:aload_0         
    //   12   17:getstatic       #277 <Field Method METHOD_29>
    //   13   20:aload_1         
    //   14   21:invokeinterface #39  <Method boolean MethodInterceptor.invokeSuper(Object, Method, Object[])>
    //   15   26:ifeq            40
    //   16   29:iconst_1        
    // try 30 38 handler(s) 38
    //   17   30:istore_3        
    //   18   31:aload_0         
    //   19   32:invokespecial   #280 <Method void Vector.removeAllElements()>
    //   20   35:goto            40
    // catch Throwable
    //   21   38:astore          4
    //   22   40:aload_0         
    //   23   41:getfield        #14  <Field MethodInterceptor h>
    //   24   44:aload_0         
    //   25   45:getstatic       #277 <Field Method METHOD_29>
    //   26   48:aload_1         
    //   27   49:iload_3         
    //   28   50:aload_2         
    //   29   51:aload           4
    //   30   53:invokeinterface #35  <Method Object MethodInterceptor.afterReturn(Object, Method, Object[], boolean, Object, Throwable)>
    //   31   58:return          
    // catch RuntimeException
    //   32   59:astore          6
    //   33   61:aload           6
    //   34   63:athrow          
    // catch Error
    //   35   64:astore          7
    //   36   66:aload           7
    //   37   68:athrow          
    // catch Throwable
    //   38   69:astore          8
    //   39   71:new             #60  <Class UndeclaredThrowableException>
    //   40   74:dup             
    //   41   75:aload           8
    //   42   77:invokespecial   #63  <Method void UndeclaredThrowableException(Throwable)>
    //   43   80:athrow          
    }

    public final int lastIndexOf(Object obj, int i)
    {
    // try 0 109 handler(s) 107 112 117
    //    0    0:iconst_2        
    //    1    1:anewarray       Object[]
    //    2    4:dup             
    //    3    5:iconst_0        
    //    4    6:aload_1         
    //    5    7:aastore         
    //    6    8:dup             
    //    7    9:iconst_1        
    //    8   10:new             #72  <Class Integer>
    //    9   13:dup             
    //   10   14:iload_2         
    //   11   15:invokespecial   #75  <Method void Integer(int)>
    //   12   18:aastore         
    //   13   19:astore_3        
    //   14   20:aconst_null     
    //   15   21:astore          4
    //   16   23:iconst_0        
    //   17   24:istore          5
    //   18   26:aconst_null     
    //   19   27:astore          6
    //   20   29:aload_0         
    //   21   30:getfield        #14  <Field MethodInterceptor h>
    //   22   33:aload_0         
    //   23   34:getstatic       #283 <Field Method METHOD_30>
    //   24   37:aload_3         
    //   25   38:invokeinterface #39  <Method boolean MethodInterceptor.invokeSuper(Object, Method, Object[])>
    //   26   43:ifeq            69
    //   27   46:iconst_1        
    //   28   47:istore          5
    // try 49 64 handler(s) 67
    //   29   49:new             #72  <Class Integer>
    //   30   52:dup             
    //   31   53:aload_0         
    //   32   54:aload_1         
    //   33   55:iload_2         
    //   34   56:invokespecial   #287 <Method int Vector.lastIndexOf(Object, int)>
    //   35   59:invokespecial   #75  <Method void Integer(int)>
    //   36   62:astore          4
    //   37   64:goto            69
    // catch Throwable
    //   38   67:astore          6
    //   39   69:aload_0         
    //   40   70:getfield        #14  <Field MethodInterceptor h>
    //   41   73:aload_0         
    //   42   74:getstatic       #283 <Field Method METHOD_30>
    //   43   77:aload_3         
    //   44   78:iload           5
    //   45   80:aload           4
    //   46   82:aload           6
    //   47   84:invokeinterface #35  <Method Object MethodInterceptor.afterReturn(Object, Method, Object[], boolean, Object, Throwable)>
    //   48   89:astore          7
    //   49   91:aload           7
    //   50   93:ifnonnull       98
    //   51   96:iconst_0        
    //   52   97:ireturn         
    //   53   98:aload           7
    //   54  100:checkcast       #99  <Class Number>
    //   55  103:invokevirtual   #102 <Method int Number.intValue()>
    //   56  106:ireturn         
    // catch RuntimeException
    //   57  107:astore          8
    //   58  109:aload           8
    //   59  111:athrow          
    // catch Error
    //   60  112:astore          9
    //   61  114:aload           9
    //   62  116:athrow          
    // catch Throwable
    //   63  117:astore          10
    //   64  119:new             #60  <Class UndeclaredThrowableException>
    //   65  122:dup             
    //   66  123:aload           10
    //   67  125:invokespecial   #63  <Method void UndeclaredThrowableException(Throwable)>
    //   68  128:athrow          
    }

    public final int lastIndexOf(Object obj)
    {
    // try 0 94 handler(s) 92 97 102
    //    0    0:iconst_1        
    //    1    1:anewarray       Object[]
    //    2    4:dup             
    //    3    5:iconst_0        
    //    4    6:aload_1         
    //    5    7:aastore         
    //    6    8:astore_2        
    //    7    9:aconst_null     
    //    8   10:astore_3        
    //    9   11:iconst_0        
    //   10   12:istore          4
    //   11   14:aconst_null     
    //   12   15:astore          5
    //   13   17:aload_0         
    //   14   18:getfield        #14  <Field MethodInterceptor h>
    //   15   21:aload_0         
    //   16   22:getstatic       #290 <Field Method METHOD_31>
    //   17   25:aload_2         
    //   18   26:invokeinterface #39  <Method boolean MethodInterceptor.invokeSuper(Object, Method, Object[])>
    //   19   31:ifeq            55
    //   20   34:iconst_1        
    //   21   35:istore          4
    // try 37 50 handler(s) 53
    //   22   37:new             #72  <Class Integer>
    //   23   40:dup             
    //   24   41:aload_0         
    //   25   42:aload_1         
    //   26   43:invokespecial   #293 <Method int Vector.lastIndexOf(Object)>
    //   27   46:invokespecial   #75  <Method void Integer(int)>
    //   28   49:astore_3        
    //   29   50:goto            55
    // catch Throwable
    //   30   53:astore          5
    //   31   55:aload_0         
    //   32   56:getfield        #14  <Field MethodInterceptor h>
    //   33   59:aload_0         
    //   34   60:getstatic       #290 <Field Method METHOD_31>
    //   35   63:aload_2         
    //   36   64:iload           4
    //   37   66:aload_3         
    //   38   67:aload           5
    //   39   69:invokeinterface #35  <Method Object MethodInterceptor.afterReturn(Object, Method, Object[], boolean, Object, Throwable)>
    //   40   74:astore          6
    //   41   76:aload           6
    //   42   78:ifnonnull       83
    //   43   81:iconst_0        
    //   44   82:ireturn         
    //   45   83:aload           6
    //   46   85:checkcast       #99  <Class Number>
    //   47   88:invokevirtual   #102 <Method int Number.intValue()>
    //   48   91:ireturn         
    // catch RuntimeException
    //   49   92:astore          7
    //   50   94:aload           7
    //   51   96:athrow          
    // catch Error
    //   52   97:astore          8
    //   53   99:aload           8
    //   54  101:athrow          
    // catch Throwable
    //   55  102:astore          9
    //   56  104:new             #60  <Class UndeclaredThrowableException>
    //   57  107:dup             
    //   58  108:aload           9
    //   59  110:invokespecial   #63  <Method void UndeclaredThrowableException(Throwable)>
    //   60  113:athrow          
    }

    public final Object elementAt(int i)
    {
    // try 0 81 handler(s) 79 84 89
    //    0    0:iconst_1        
    //    1    1:anewarray       Object[]
    //    2    4:dup             
    //    3    5:iconst_0        
    //    4    6:new             #72  <Class Integer>
    //    5    9:dup             
    //    6   10:iload_1         
    //    7   11:invokespecial   #75  <Method void Integer(int)>
    //    8   14:aastore         
    //    9   15:astore_2        
    //   10   16:aconst_null     
    //   11   17:astore_3        
    //   12   18:iconst_0        
    //   13   19:istore          4
    //   14   21:aconst_null     
    //   15   22:astore          5
    //   16   24:aload_0         
    //   17   25:getfield        #14  <Field MethodInterceptor h>
    //   18   28:aload_0         
    //   19   29:getstatic       #296 <Field Method METHOD_32>
    //   20   32:aload_2         
    //   21   33:invokeinterface #39  <Method boolean MethodInterceptor.invokeSuper(Object, Method, Object[])>
    //   22   38:ifeq            55
    //   23   41:iconst_1        
    // try 42 50 handler(s) 53
    //   24   42:istore          4
    //   25   44:aload_0         
    //   26   45:iload_1         
    //   27   46:invokespecial   #299 <Method Object Vector.elementAt(int)>
    //   28   49:astore_3        
    //   29   50:goto            55
    // catch Throwable
    //   30   53:astore          5
    //   31   55:aload_0         
    //   32   56:getfield        #14  <Field MethodInterceptor h>
    //   33   59:aload_0         
    //   34   60:getstatic       #296 <Field Method METHOD_32>
    //   35   63:aload_2         
    //   36   64:iload           4
    //   37   66:aload_3         
    //   38   67:aload           5
    //   39   69:invokeinterface #35  <Method Object MethodInterceptor.afterReturn(Object, Method, Object[], boolean, Object, Throwable)>
    //   40   74:astore          6
    //   41   76:aload           6
    //   42   78:areturn         
    // catch RuntimeException
    //   43   79:astore          7
    //   44   81:aload           7
    //   45   83:athrow          
    // catch Error
    //   46   84:astore          8
    //   47   86:aload           8
    //   48   88:athrow          
    // catch Throwable
    //   49   89:astore          9
    //   50   91:new             #60  <Class UndeclaredThrowableException>
    //   51   94:dup             
    //   52   95:aload           9
    //   53   97:invokespecial   #63  <Method void UndeclaredThrowableException(Throwable)>
    //   54  100:athrow          
    }

    public final void setElementAt(Object obj, int i)
    {
    // try 0 83 handler(s) 81 86 91
    //    0    0:iconst_2        
    //    1    1:anewarray       Object[]
    //    2    4:dup             
    //    3    5:iconst_0        
    //    4    6:aload_1         
    //    5    7:aastore         
    //    6    8:dup             
    //    7    9:iconst_1        
    //    8   10:new             #72  <Class Integer>
    //    9   13:dup             
    //   10   14:iload_2         
    //   11   15:invokespecial   #75  <Method void Integer(int)>
    //   12   18:aastore         
    //   13   19:astore_3        
    //   14   20:aconst_null     
    //   15   21:astore          4
    //   16   23:iconst_0        
    //   17   24:istore          5
    //   18   26:aconst_null     
    //   19   27:astore          6
    //   20   29:aload_0         
    //   21   30:getfield        #14  <Field MethodInterceptor h>
    //   22   33:aload_0         
    //   23   34:getstatic       #302 <Field Method METHOD_33>
    //   24   37:aload_3         
    //   25   38:invokeinterface #39  <Method boolean MethodInterceptor.invokeSuper(Object, Method, Object[])>
    //   26   43:ifeq            60
    //   27   46:iconst_1        
    // try 47 58 handler(s) 58
    //   28   47:istore          5
    //   29   49:aload_0         
    //   30   50:aload_1         
    //   31   51:iload_2         
    //   32   52:invokespecial   #305 <Method void Vector.setElementAt(Object, int)>
    //   33   55:goto            60
    // catch Throwable
    //   34   58:astore          6
    //   35   60:aload_0         
    //   36   61:getfield        #14  <Field MethodInterceptor h>
    //   37   64:aload_0         
    //   38   65:getstatic       #302 <Field Method METHOD_33>
    //   39   68:aload_3         
    //   40   69:iload           5
    //   41   71:aload           4
    //   42   73:aload           6
    //   43   75:invokeinterface #35  <Method Object MethodInterceptor.afterReturn(Object, Method, Object[], boolean, Object, Throwable)>
    //   44   80:return          
    // catch RuntimeException
    //   45   81:astore          8
    //   46   83:aload           8
    //   47   85:athrow          
    // catch Error
    //   48   86:astore          9
    //   49   88:aload           9
    //   50   90:athrow          
    // catch Throwable
    //   51   91:astore          10
    //   52   93:new             #60  <Class UndeclaredThrowableException>
    //   53   96:dup             
    //   54   97:aload           10
    //   55   99:invokespecial   #63  <Method void UndeclaredThrowableException(Throwable)>
    //   56  102:athrow          
    }

    public final void clear()
    {
    // try 0 61 handler(s) 59 64 69
    //    0    0:iconst_0        
    //    1    1:anewarray       Object[]
    //    2    4:astore_1        
    //    3    5:aconst_null     
    //    4    6:astore_2        
    //    5    7:iconst_0        
    //    6    8:istore_3        
    //    7    9:aconst_null     
    //    8   10:astore          4
    //    9   12:aload_0         
    //   10   13:getfield        #14  <Field MethodInterceptor h>
    //   11   16:aload_0         
    //   12   17:getstatic       #308 <Field Method METHOD_34>
    //   13   20:aload_1         
    //   14   21:invokeinterface #39  <Method boolean MethodInterceptor.invokeSuper(Object, Method, Object[])>
    //   15   26:ifeq            40
    //   16   29:iconst_1        
    // try 30 38 handler(s) 38
    //   17   30:istore_3        
    //   18   31:aload_0         
    //   19   32:invokespecial   #311 <Method void Vector.clear()>
    //   20   35:goto            40
    // catch Throwable
    //   21   38:astore          4
    //   22   40:aload_0         
    //   23   41:getfield        #14  <Field MethodInterceptor h>
    //   24   44:aload_0         
    //   25   45:getstatic       #308 <Field Method METHOD_34>
    //   26   48:aload_1         
    //   27   49:iload_3         
    //   28   50:aload_2         
    //   29   51:aload           4
    //   30   53:invokeinterface #35  <Method Object MethodInterceptor.afterReturn(Object, Method, Object[], boolean, Object, Throwable)>
    //   31   58:return          
    // catch RuntimeException
    //   32   59:astore          6
    //   33   61:aload           6
    //   34   63:athrow          
    // catch Error
    //   35   64:astore          7
    //   36   66:aload           7
    //   37   68:athrow          
    // catch Throwable
    //   38   69:astore          8
    //   39   71:new             #60  <Class UndeclaredThrowableException>
    //   40   74:dup             
    //   41   75:aload           8
    //   42   77:invokespecial   #63  <Method void UndeclaredThrowableException(Throwable)>
    //   43   80:athrow          
    }

    public final int indexOf(Object obj)
    {
    // try 0 94 handler(s) 92 97 102
    //    0    0:iconst_1        
    //    1    1:anewarray       Object[]
    //    2    4:dup             
    //    3    5:iconst_0        
    //    4    6:aload_1         
    //    5    7:aastore         
    //    6    8:astore_2        
    //    7    9:aconst_null     
    //    8   10:astore_3        
    //    9   11:iconst_0        
    //   10   12:istore          4
    //   11   14:aconst_null     
    //   12   15:astore          5
    //   13   17:aload_0         
    //   14   18:getfield        #14  <Field MethodInterceptor h>
    //   15   21:aload_0         
    //   16   22:getstatic       #314 <Field Method METHOD_35>
    //   17   25:aload_2         
    //   18   26:invokeinterface #39  <Method boolean MethodInterceptor.invokeSuper(Object, Method, Object[])>
    //   19   31:ifeq            55
    //   20   34:iconst_1        
    //   21   35:istore          4
    // try 37 50 handler(s) 53
    //   22   37:new             #72  <Class Integer>
    //   23   40:dup             
    //   24   41:aload_0         
    //   25   42:aload_1         
    //   26   43:invokespecial   #317 <Method int Vector.indexOf(Object)>
    //   27   46:invokespecial   #75  <Method void Integer(int)>
    //   28   49:astore_3        
    //   29   50:goto            55
    // catch Throwable
    //   30   53:astore          5
    //   31   55:aload_0         
    //   32   56:getfield        #14  <Field MethodInterceptor h>
    //   33   59:aload_0         
    //   34   60:getstatic       #314 <Field Method METHOD_35>
    //   35   63:aload_2         
    //   36   64:iload           4
    //   37   66:aload_3         
    //   38   67:aload           5
    //   39   69:invokeinterface #35  <Method Object MethodInterceptor.afterReturn(Object, Method, Object[], boolean, Object, Throwable)>
    //   40   74:astore          6
    //   41   76:aload           6
    //   42   78:ifnonnull       83
    //   43   81:iconst_0        
    //   44   82:ireturn         
    //   45   83:aload           6
    //   46   85:checkcast       #99  <Class Number>
    //   47   88:invokevirtual   #102 <Method int Number.intValue()>
    //   48   91:ireturn         
    // catch RuntimeException
    //   49   92:astore          7
    //   50   94:aload           7
    //   51   96:athrow          
    // catch Error
    //   52   97:astore          8
    //   53   99:aload           8
    //   54  101:athrow          
    // catch Throwable
    //   55  102:astore          9
    //   56  104:new             #60  <Class UndeclaredThrowableException>
    //   57  107:dup             
    //   58  108:aload           9
    //   59  110:invokespecial   #63  <Method void UndeclaredThrowableException(Throwable)>
    //   60  113:athrow          
    }

    public final int indexOf(Object obj, int i)
    {
    // try 0 109 handler(s) 107 112 117
    //    0    0:iconst_2        
    //    1    1:anewarray       Object[]
    //    2    4:dup             
    //    3    5:iconst_0        
    //    4    6:aload_1         
    //    5    7:aastore         
    //    6    8:dup             
    //    7    9:iconst_1        
    //    8   10:new             #72  <Class Integer>
    //    9   13:dup             
    //   10   14:iload_2         
    //   11   15:invokespecial   #75  <Method void Integer(int)>
    //   12   18:aastore         
    //   13   19:astore_3        
    //   14   20:aconst_null     
    //   15   21:astore          4
    //   16   23:iconst_0        
    //   17   24:istore          5
    //   18   26:aconst_null     
    //   19   27:astore          6
    //   20   29:aload_0         
    //   21   30:getfield        #14  <Field MethodInterceptor h>
    //   22   33:aload_0         
    //   23   34:getstatic       #320 <Field Method METHOD_36>
    //   24   37:aload_3         
    //   25   38:invokeinterface #39  <Method boolean MethodInterceptor.invokeSuper(Object, Method, Object[])>
    //   26   43:ifeq            69
    //   27   46:iconst_1        
    //   28   47:istore          5
    // try 49 64 handler(s) 67
    //   29   49:new             #72  <Class Integer>
    //   30   52:dup             
    //   31   53:aload_0         
    //   32   54:aload_1         
    //   33   55:iload_2         
    //   34   56:invokespecial   #322 <Method int Vector.indexOf(Object, int)>
    //   35   59:invokespecial   #75  <Method void Integer(int)>
    //   36   62:astore          4
    //   37   64:goto            69
    // catch Throwable
    //   38   67:astore          6
    //   39   69:aload_0         
    //   40   70:getfield        #14  <Field MethodInterceptor h>
    //   41   73:aload_0         
    //   42   74:getstatic       #320 <Field Method METHOD_36>
    //   43   77:aload_3         
    //   44   78:iload           5
    //   45   80:aload           4
    //   46   82:aload           6
    //   47   84:invokeinterface #35  <Method Object MethodInterceptor.afterReturn(Object, Method, Object[], boolean, Object, Throwable)>
    //   48   89:astore          7
    //   49   91:aload           7
    //   50   93:ifnonnull       98
    //   51   96:iconst_0        
    //   52   97:ireturn         
    //   53   98:aload           7
    //   54  100:checkcast       #99  <Class Number>
    //   55  103:invokevirtual   #102 <Method int Number.intValue()>
    //   56  106:ireturn         
    // catch RuntimeException
    //   57  107:astore          8
    //   58  109:aload           8
    //   59  111:athrow          
    // catch Error
    //   60  112:astore          9
    //   61  114:aload           9
    //   62  116:athrow          
    // catch Throwable
    //   63  117:astore          10
    //   64  119:new             #60  <Class UndeclaredThrowableException>
    //   65  122:dup             
    //   66  123:aload           10
    //   67  125:invokespecial   #63  <Method void UndeclaredThrowableException(Throwable)>
    //   68  128:athrow          
    }

    public final Object lastElement()
    {
    // try 0 66 handler(s) 64 69 74
    //    0    0:iconst_0        
    //    1    1:anewarray       Object[]
    //    2    4:astore_1        
    //    3    5:aconst_null     
    //    4    6:astore_2        
    //    5    7:iconst_0        
    //    6    8:istore_3        
    //    7    9:aconst_null     
    //    8   10:astore          4
    //    9   12:aload_0         
    //   10   13:getfield        #14  <Field MethodInterceptor h>
    //   11   16:aload_0         
    //   12   17:getstatic       #325 <Field Method METHOD_37>
    //   13   20:aload_1         
    //   14   21:invokeinterface #39  <Method boolean MethodInterceptor.invokeSuper(Object, Method, Object[])>
    //   15   26:ifeq            41
    //   16   29:iconst_1        
    // try 30 36 handler(s) 39
    //   17   30:istore_3        
    //   18   31:aload_0         
    //   19   32:invokespecial   #328 <Method Object Vector.lastElement()>
    //   20   35:astore_2        
    //   21   36:goto            41
    // catch Throwable
    //   22   39:astore          4
    //   23   41:aload_0         
    //   24   42:getfield        #14  <Field MethodInterceptor h>
    //   25   45:aload_0         
    //   26   46:getstatic       #325 <Field Method METHOD_37>
    //   27   49:aload_1         
    //   28   50:iload_3         
    //   29   51:aload_2         
    //   30   52:aload           4
    //   31   54:invokeinterface #35  <Method Object MethodInterceptor.afterReturn(Object, Method, Object[], boolean, Object, Throwable)>
    //   32   59:astore          5
    //   33   61:aload           5
    //   34   63:areturn         
    // catch RuntimeException
    //   35   64:astore          6
    //   36   66:aload           6
    //   37   68:athrow          
    // catch Error
    //   38   69:astore          7
    //   39   71:aload           7
    //   40   73:athrow          
    // catch Throwable
    //   41   74:astore          8
    //   42   76:new             #60  <Class UndeclaredThrowableException>
    //   43   79:dup             
    //   44   80:aload           8
    //   45   82:invokespecial   #63  <Method void UndeclaredThrowableException(Throwable)>
    //   46   85:athrow          
    }

    public final Object remove(int i)
    {
    // try 0 81 handler(s) 79 84 89
    //    0    0:iconst_1        
    //    1    1:anewarray       Object[]
    //    2    4:dup             
    //    3    5:iconst_0        
    //    4    6:new             #72  <Class Integer>
    //    5    9:dup             
    //    6   10:iload_1         
    //    7   11:invokespecial   #75  <Method void Integer(int)>
    //    8   14:aastore         
    //    9   15:astore_2        
    //   10   16:aconst_null     
    //   11   17:astore_3        
    //   12   18:iconst_0        
    //   13   19:istore          4
    //   14   21:aconst_null     
    //   15   22:astore          5
    //   16   24:aload_0         
    //   17   25:getfield        #14  <Field MethodInterceptor h>
    //   18   28:aload_0         
    //   19   29:getstatic       #331 <Field Method METHOD_38>
    //   20   32:aload_2         
    //   21   33:invokeinterface #39  <Method boolean MethodInterceptor.invokeSuper(Object, Method, Object[])>
    //   22   38:ifeq            55
    //   23   41:iconst_1        
    // try 42 50 handler(s) 53
    //   24   42:istore          4
    //   25   44:aload_0         
    //   26   45:iload_1         
    //   27   46:invokespecial   #334 <Method Object Vector.remove(int)>
    //   28   49:astore_3        
    //   29   50:goto            55
    // catch Throwable
    //   30   53:astore          5
    //   31   55:aload_0         
    //   32   56:getfield        #14  <Field MethodInterceptor h>
    //   33   59:aload_0         
    //   34   60:getstatic       #331 <Field Method METHOD_38>
    //   35   63:aload_2         
    //   36   64:iload           4
    //   37   66:aload_3         
    //   38   67:aload           5
    //   39   69:invokeinterface #35  <Method Object MethodInterceptor.afterReturn(Object, Method, Object[], boolean, Object, Throwable)>
    //   40   74:astore          6
    //   41   76:aload           6
    //   42   78:areturn         
    // catch RuntimeException
    //   43   79:astore          7
    //   44   81:aload           7
    //   45   83:athrow          
    // catch Error
    //   46   84:astore          8
    //   47   86:aload           8
    //   48   88:athrow          
    // catch Throwable
    //   49   89:astore          9
    //   50   91:new             #60  <Class UndeclaredThrowableException>
    //   51   94:dup             
    //   52   95:aload           9
    //   53   97:invokespecial   #63  <Method void UndeclaredThrowableException(Throwable)>
    //   54  100:athrow          
    }

    public final boolean remove(Object obj)
    {
    // try 0 94 handler(s) 92 97 102
    //    0    0:iconst_1        
    //    1    1:anewarray       Object[]
    //    2    4:dup             
    //    3    5:iconst_0        
    //    4    6:aload_1         
    //    5    7:aastore         
    //    6    8:astore_2        
    //    7    9:aconst_null     
    //    8   10:astore_3        
    //    9   11:iconst_0        
    //   10   12:istore          4
    //   11   14:aconst_null     
    //   12   15:astore          5
    //   13   17:aload_0         
    //   14   18:getfield        #14  <Field MethodInterceptor h>
    //   15   21:aload_0         
    //   16   22:getstatic       #337 <Field Method METHOD_39>
    //   17   25:aload_2         
    //   18   26:invokeinterface #39  <Method boolean MethodInterceptor.invokeSuper(Object, Method, Object[])>
    //   19   31:ifeq            55
    //   20   34:iconst_1        
    //   21   35:istore          4
    // try 37 50 handler(s) 53
    //   22   37:new             #47  <Class Boolean>
    //   23   40:dup             
    //   24   41:aload_0         
    //   25   42:aload_1         
    //   26   43:invokespecial   #339 <Method boolean Vector.remove(Object)>
    //   27   46:invokespecial   #54  <Method void Boolean(boolean)>
    //   28   49:astore_3        
    //   29   50:goto            55
    // catch Throwable
    //   30   53:astore          5
    //   31   55:aload_0         
    //   32   56:getfield        #14  <Field MethodInterceptor h>
    //   33   59:aload_0         
    //   34   60:getstatic       #337 <Field Method METHOD_39>
    //   35   63:aload_2         
    //   36   64:iload           4
    //   37   66:aload_3         
    //   38   67:aload           5
    //   39   69:invokeinterface #35  <Method Object MethodInterceptor.afterReturn(Object, Method, Object[], boolean, Object, Throwable)>
    //   40   74:astore          6
    //   41   76:aload           6
    //   42   78:ifnonnull       83
    //   43   81:iconst_0        
    //   44   82:ireturn         
    //   45   83:aload           6
    //   46   85:checkcast       #47  <Class Boolean>
    //   47   88:invokevirtual   #58  <Method boolean Boolean.booleanValue()>
    //   48   91:ireturn         
    // catch RuntimeException
    //   49   92:astore          7
    //   50   94:aload           7
    //   51   96:athrow          
    // catch Error
    //   52   97:astore          8
    //   53   99:aload           8
    //   54  101:athrow          
    // catch Throwable
    //   55  102:astore          9
    //   56  104:new             #60  <Class UndeclaredThrowableException>
    //   57  107:dup             
    //   58  108:aload           9
    //   59  110:invokespecial   #63  <Method void UndeclaredThrowableException(Throwable)>
    //   60  113:athrow          
    }

    public final void setSize(int i)
    {
    // try 0 76 handler(s) 74 79 84
    //    0    0:iconst_1        
    //    1    1:anewarray       Object[]
    //    2    4:dup             
    //    3    5:iconst_0        
    //    4    6:new             #72  <Class Integer>
    //    5    9:dup             
    //    6   10:iload_1         
    //    7   11:invokespecial   #75  <Method void Integer(int)>
    //    8   14:aastore         
    //    9   15:astore_2        
    //   10   16:aconst_null     
    //   11   17:astore_3        
    //   12   18:iconst_0        
    //   13   19:istore          4
    //   14   21:aconst_null     
    //   15   22:astore          5
    //   16   24:aload_0         
    //   17   25:getfield        #14  <Field MethodInterceptor h>
    //   18   28:aload_0         
    //   19   29:getstatic       #342 <Field Method METHOD_40>
    //   20   32:aload_2         
    //   21   33:invokeinterface #39  <Method boolean MethodInterceptor.invokeSuper(Object, Method, Object[])>
    //   22   38:ifeq            54
    //   23   41:iconst_1        
    // try 42 52 handler(s) 52
    //   24   42:istore          4
    //   25   44:aload_0         
    //   26   45:iload_1         
    //   27   46:invokespecial   #345 <Method void Vector.setSize(int)>
    //   28   49:goto            54
    // catch Throwable
    //   29   52:astore          5
    //   30   54:aload_0         
    //   31   55:getfield        #14  <Field MethodInterceptor h>
    //   32   58:aload_0         
    //   33   59:getstatic       #342 <Field Method METHOD_40>
    //   34   62:aload_2         
    //   35   63:iload           4
    //   36   65:aload_3         
    //   37   66:aload           5
    //   38   68:invokeinterface #35  <Method Object MethodInterceptor.afterReturn(Object, Method, Object[], boolean, Object, Throwable)>
    //   39   73:return          
    // catch RuntimeException
    //   40   74:astore          7
    //   41   76:aload           7
    //   42   78:athrow          
    // catch Error
    //   43   79:astore          8
    //   44   81:aload           8
    //   45   83:athrow          
    // catch Throwable
    //   46   84:astore          9
    //   47   86:new             #60  <Class UndeclaredThrowableException>
    //   48   89:dup             
    //   49   90:aload           9
    //   50   92:invokespecial   #63  <Method void UndeclaredThrowableException(Throwable)>
    //   51   95:athrow          
    }

    public final boolean isEmpty()
    {
    // try 0 86 handler(s) 84 89 94
    //    0    0:iconst_0        
    //    1    1:anewarray       Object[]
    //    2    4:astore_1        
    //    3    5:aconst_null     
    //    4    6:astore_2        
    //    5    7:iconst_0        
    //    6    8:istore_3        
    //    7    9:aconst_null     
    //    8   10:astore          4
    //    9   12:aload_0         
    //   10   13:getfield        #14  <Field MethodInterceptor h>
    //   11   16:aload_0         
    //   12   17:getstatic       #348 <Field Method METHOD_41>
    //   13   20:aload_1         
    //   14   21:invokeinterface #39  <Method boolean MethodInterceptor.invokeSuper(Object, Method, Object[])>
    //   15   26:ifeq            48
    //   16   29:iconst_1        
    //   17   30:istore_3        
    // try 31 43 handler(s) 46
    //   18   31:new             #47  <Class Boolean>
    //   19   34:dup             
    //   20   35:aload_0         
    //   21   36:invokespecial   #351 <Method boolean Vector.isEmpty()>
    //   22   39:invokespecial   #54  <Method void Boolean(boolean)>
    //   23   42:astore_2        
    //   24   43:goto            48
    // catch Throwable
    //   25   46:astore          4
    //   26   48:aload_0         
    //   27   49:getfield        #14  <Field MethodInterceptor h>
    //   28   52:aload_0         
    //   29   53:getstatic       #348 <Field Method METHOD_41>
    //   30   56:aload_1         
    //   31   57:iload_3         
    //   32   58:aload_2         
    //   33   59:aload           4
    //   34   61:invokeinterface #35  <Method Object MethodInterceptor.afterReturn(Object, Method, Object[], boolean, Object, Throwable)>
    //   35   66:astore          5
    //   36   68:aload           5
    //   37   70:ifnonnull       75
    //   38   73:iconst_0        
    //   39   74:ireturn         
    //   40   75:aload           5
    //   41   77:checkcast       #47  <Class Boolean>
    //   42   80:invokevirtual   #58  <Method boolean Boolean.booleanValue()>
    //   43   83:ireturn         
    // catch RuntimeException
    //   44   84:astore          6
    //   45   86:aload           6
    //   46   88:athrow          
    // catch Error
    //   47   89:astore          7
    //   48   91:aload           7
    //   49   93:athrow          
    // catch Throwable
    //   50   94:astore          8
    //   51   96:new             #60  <Class UndeclaredThrowableException>
    //   52   99:dup             
    //   53  100:aload           8
    //   54  102:invokespecial   #63  <Method void UndeclaredThrowableException(Throwable)>
    //   55  105:athrow          
    }

    public final boolean containsAll(Collection collection)
    {
    // try 0 94 handler(s) 92 97 102
    //    0    0:iconst_1        
    //    1    1:anewarray       Object[]
    //    2    4:dup             
    //    3    5:iconst_0        
    //    4    6:aload_1         
    //    5    7:aastore         
    //    6    8:astore_2        
    //    7    9:aconst_null     
    //    8   10:astore_3        
    //    9   11:iconst_0        
    //   10   12:istore          4
    //   11   14:aconst_null     
    //   12   15:astore          5
    //   13   17:aload_0         
    //   14   18:getfield        #14  <Field MethodInterceptor h>
    //   15   21:aload_0         
    //   16   22:getstatic       #354 <Field Method METHOD_42>
    //   17   25:aload_2         
    //   18   26:invokeinterface #39  <Method boolean MethodInterceptor.invokeSuper(Object, Method, Object[])>
    //   19   31:ifeq            55
    //   20   34:iconst_1        
    //   21   35:istore          4
    // try 37 50 handler(s) 53
    //   22   37:new             #47  <Class Boolean>
    //   23   40:dup             
    //   24   41:aload_0         
    //   25   42:aload_1         
    //   26   43:invokespecial   #357 <Method boolean Vector.containsAll(Collection)>
    //   27   46:invokespecial   #54  <Method void Boolean(boolean)>
    //   28   49:astore_3        
    //   29   50:goto            55
    // catch Throwable
    //   30   53:astore          5
    //   31   55:aload_0         
    //   32   56:getfield        #14  <Field MethodInterceptor h>
    //   33   59:aload_0         
    //   34   60:getstatic       #354 <Field Method METHOD_42>
    //   35   63:aload_2         
    //   36   64:iload           4
    //   37   66:aload_3         
    //   38   67:aload           5
    //   39   69:invokeinterface #35  <Method Object MethodInterceptor.afterReturn(Object, Method, Object[], boolean, Object, Throwable)>
    //   40   74:astore          6
    //   41   76:aload           6
    //   42   78:ifnonnull       83
    //   43   81:iconst_0        
    //   44   82:ireturn         
    //   45   83:aload           6
    //   46   85:checkcast       #47  <Class Boolean>
    //   47   88:invokevirtual   #58  <Method boolean Boolean.booleanValue()>
    //   48   91:ireturn         
    // catch RuntimeException
    //   49   92:astore          7
    //   50   94:aload           7
    //   51   96:athrow          
    // catch Error
    //   52   97:astore          8
    //   53   99:aload           8
    //   54  101:athrow          
    // catch Throwable
    //   55  102:astore          9
    //   56  104:new             #60  <Class UndeclaredThrowableException>
    //   57  107:dup             
    //   58  108:aload           9
    //   59  110:invokespecial   #63  <Method void UndeclaredThrowableException(Throwable)>
    //   60  113:athrow          
    }

    public final int size()
    {
    // try 0 86 handler(s) 84 89 94
    //    0    0:iconst_0        
    //    1    1:anewarray       Object[]
    //    2    4:astore_1        
    //    3    5:aconst_null     
    //    4    6:astore_2        
    //    5    7:iconst_0        
    //    6    8:istore_3        
    //    7    9:aconst_null     
    //    8   10:astore          4
    //    9   12:aload_0         
    //   10   13:getfield        #14  <Field MethodInterceptor h>
    //   11   16:aload_0         
    //   12   17:getstatic       #360 <Field Method METHOD_43>
    //   13   20:aload_1         
    //   14   21:invokeinterface #39  <Method boolean MethodInterceptor.invokeSuper(Object, Method, Object[])>
    //   15   26:ifeq            48
    //   16   29:iconst_1        
    //   17   30:istore_3        
    // try 31 43 handler(s) 46
    //   18   31:new             #72  <Class Integer>
    //   19   34:dup             
    //   20   35:aload_0         
    //   21   36:invokespecial   #363 <Method int Vector.size()>
    //   22   39:invokespecial   #75  <Method void Integer(int)>
    //   23   42:astore_2        
    //   24   43:goto            48
    // catch Throwable
    //   25   46:astore          4
    //   26   48:aload_0         
    //   27   49:getfield        #14  <Field MethodInterceptor h>
    //   28   52:aload_0         
    //   29   53:getstatic       #360 <Field Method METHOD_43>
    //   30   56:aload_1         
    //   31   57:iload_3         
    //   32   58:aload_2         
    //   33   59:aload           4
    //   34   61:invokeinterface #35  <Method Object MethodInterceptor.afterReturn(Object, Method, Object[], boolean, Object, Throwable)>
    //   35   66:astore          5
    //   36   68:aload           5
    //   37   70:ifnonnull       75
    //   38   73:iconst_0        
    //   39   74:ireturn         
    //   40   75:aload           5
    //   41   77:checkcast       #99  <Class Number>
    //   42   80:invokevirtual   #102 <Method int Number.intValue()>
    //   43   83:ireturn         
    // catch RuntimeException
    //   44   84:astore          6
    //   45   86:aload           6
    //   46   88:athrow          
    // catch Error
    //   47   89:astore          7
    //   48   91:aload           7
    //   49   93:athrow          
    // catch Throwable
    //   50   94:astore          8
    //   51   96:new             #60  <Class UndeclaredThrowableException>
    //   52   99:dup             
    //   53  100:aload           8
    //   54  102:invokespecial   #63  <Method void UndeclaredThrowableException(Throwable)>
    //   55  105:athrow          
    }

    public final boolean addAll(int i, Collection collection)
    {
    // try 0 109 handler(s) 107 112 117
    //    0    0:iconst_2        
    //    1    1:anewarray       Object[]
    //    2    4:dup             
    //    3    5:iconst_0        
    //    4    6:new             #72  <Class Integer>
    //    5    9:dup             
    //    6   10:iload_1         
    //    7   11:invokespecial   #75  <Method void Integer(int)>
    //    8   14:aastore         
    //    9   15:dup             
    //   10   16:iconst_1        
    //   11   17:aload_2         
    //   12   18:aastore         
    //   13   19:astore_3        
    //   14   20:aconst_null     
    //   15   21:astore          4
    //   16   23:iconst_0        
    //   17   24:istore          5
    //   18   26:aconst_null     
    //   19   27:astore          6
    //   20   29:aload_0         
    //   21   30:getfield        #14  <Field MethodInterceptor h>
    //   22   33:aload_0         
    //   23   34:getstatic       #366 <Field Method METHOD_44>
    //   24   37:aload_3         
    //   25   38:invokeinterface #39  <Method boolean MethodInterceptor.invokeSuper(Object, Method, Object[])>
    //   26   43:ifeq            69
    //   27   46:iconst_1        
    //   28   47:istore          5
    // try 49 64 handler(s) 67
    //   29   49:new             #47  <Class Boolean>
    //   30   52:dup             
    //   31   53:aload_0         
    //   32   54:iload_1         
    //   33   55:aload_2         
    //   34   56:invokespecial   #370 <Method boolean Vector.addAll(int, Collection)>
    //   35   59:invokespecial   #54  <Method void Boolean(boolean)>
    //   36   62:astore          4
    //   37   64:goto            69
    // catch Throwable
    //   38   67:astore          6
    //   39   69:aload_0         
    //   40   70:getfield        #14  <Field MethodInterceptor h>
    //   41   73:aload_0         
    //   42   74:getstatic       #366 <Field Method METHOD_44>
    //   43   77:aload_3         
    //   44   78:iload           5
    //   45   80:aload           4
    //   46   82:aload           6
    //   47   84:invokeinterface #35  <Method Object MethodInterceptor.afterReturn(Object, Method, Object[], boolean, Object, Throwable)>
    //   48   89:astore          7
    //   49   91:aload           7
    //   50   93:ifnonnull       98
    //   51   96:iconst_0        
    //   52   97:ireturn         
    //   53   98:aload           7
    //   54  100:checkcast       #47  <Class Boolean>
    //   55  103:invokevirtual   #58  <Method boolean Boolean.booleanValue()>
    //   56  106:ireturn         
    // catch RuntimeException
    //   57  107:astore          8
    //   58  109:aload           8
    //   59  111:athrow          
    // catch Error
    //   60  112:astore          9
    //   61  114:aload           9
    //   62  116:athrow          
    // catch Throwable
    //   63  117:astore          10
    //   64  119:new             #60  <Class UndeclaredThrowableException>
    //   65  122:dup             
    //   66  123:aload           10
    //   67  125:invokespecial   #63  <Method void UndeclaredThrowableException(Throwable)>
    //   68  128:athrow          
    }

    public final boolean addAll(Collection collection)
    {
    // try 0 94 handler(s) 92 97 102
    //    0    0:iconst_1        
    //    1    1:anewarray       Object[]
    //    2    4:dup             
    //    3    5:iconst_0        
    //    4    6:aload_1         
    //    5    7:aastore         
    //    6    8:astore_2        
    //    7    9:aconst_null     
    //    8   10:astore_3        
    //    9   11:iconst_0        
    //   10   12:istore          4
    //   11   14:aconst_null     
    //   12   15:astore          5
    //   13   17:aload_0         
    //   14   18:getfield        #14  <Field MethodInterceptor h>
    //   15   21:aload_0         
    //   16   22:getstatic       #373 <Field Method METHOD_45>
    //   17   25:aload_2         
    //   18   26:invokeinterface #39  <Method boolean MethodInterceptor.invokeSuper(Object, Method, Object[])>
    //   19   31:ifeq            55
    //   20   34:iconst_1        
    //   21   35:istore          4
    // try 37 50 handler(s) 53
    //   22   37:new             #47  <Class Boolean>
    //   23   40:dup             
    //   24   41:aload_0         
    //   25   42:aload_1         
    //   26   43:invokespecial   #375 <Method boolean Vector.addAll(Collection)>
    //   27   46:invokespecial   #54  <Method void Boolean(boolean)>
    //   28   49:astore_3        
    //   29   50:goto            55
    // catch Throwable
    //   30   53:astore          5
    //   31   55:aload_0         
    //   32   56:getfield        #14  <Field MethodInterceptor h>
    //   33   59:aload_0         
    //   34   60:getstatic       #373 <Field Method METHOD_45>
    //   35   63:aload_2         
    //   36   64:iload           4
    //   37   66:aload_3         
    //   38   67:aload           5
    //   39   69:invokeinterface #35  <Method Object MethodInterceptor.afterReturn(Object, Method, Object[], boolean, Object, Throwable)>
    //   40   74:astore          6
    //   41   76:aload           6
    //   42   78:ifnonnull       83
    //   43   81:iconst_0        
    //   44   82:ireturn         
    //   45   83:aload           6
    //   46   85:checkcast       #47  <Class Boolean>
    //   47   88:invokevirtual   #58  <Method boolean Boolean.booleanValue()>
    //   48   91:ireturn         
    // catch RuntimeException
    //   49   92:astore          7
    //   50   94:aload           7
    //   51   96:athrow          
    // catch Error
    //   52   97:astore          8
    //   53   99:aload           8
    //   54  101:athrow          
    // catch Throwable
    //   55  102:astore          9
    //   56  104:new             #60  <Class UndeclaredThrowableException>
    //   57  107:dup             
    //   58  108:aload           9
    //   59  110:invokespecial   #63  <Method void UndeclaredThrowableException(Throwable)>
    //   60  113:athrow          
    }

    private static Class findClass(String s)
    {
    // try 0 5 handler(s) 5
    //    0    0:aload_0         
    //    1    1:invokestatic    #381 <Method Class Class.forName(String)>
    //    2    4:areturn         
    // catch ClassNotFoundException
    //    3    5:astore_1        
    //    4    6:new             #383 <Class NoClassDefFoundError>
    //    5    9:dup             
    //    6   10:aload_1         
    //    7   11:invokevirtual   #388 <Method String ClassNotFoundException.getMessage()>
    //    8   14:invokespecial   #391 <Method void NoClassDefFoundError(String)>
    //    9   17:athrow          
    }

    static 
    {
    //    0    0:ldc2            #394 <String "java.util.Vector">
    //    1    3:invokestatic    #396 <Method Class findClass(String)>
    //    2    6:astore_1        
    //    3    7:iconst_0        
    //    4    8:anewarray       Class[]
    //    5   11:astore_0        
    //    6   12:aload_1         
    //    7   13:ldc2            #398 <String "clear">
    //    8   16:aload_0         
    //    9   17:invokevirtual   #402 <Method Method Class.getDeclaredMethod(String, Class[])>
    //   10   20:putstatic       #308 <Field Method METHOD_34>
    //   11   23:ldc2            #394 <String "java.util.Vector">
    //   12   26:invokestatic    #396 <Method Class findClass(String)>
    //   13   29:astore_1        
    //   14   30:iconst_2        
    //   15   31:anewarray       Class[]
    //   16   34:dup             
    //   17   35:iconst_0        
    //   18   36:getstatic       #406 <Field Class Integer.TYPE>
    //   19   39:aastore         
    //   20   40:dup             
    //   21   41:iconst_1        
    //   22   42:ldc2            #408 <String "java.lang.Object">
    //   23   45:invokestatic    #396 <Method Class findClass(String)>
    //   24   48:aastore         
    //   25   49:astore_0        
    //   26   50:aload_1         
    //   27   51:ldc2            #410 <String "set">
    //   28   54:aload_0         
    //   29   55:invokevirtual   #402 <Method Method Class.getDeclaredMethod(String, Class[])>
    //   30   58:putstatic       #166 <Field Method METHOD_12>
    //   31   61:ldc2            #394 <String "java.util.Vector">
    //   32   64:invokestatic    #396 <Method Class findClass(String)>
    //   33   67:astore_1        
    //   34   68:iconst_2        
    //   35   69:anewarray       Class[]
    //   36   72:dup             
    //   37   73:iconst_0        
    //   38   74:ldc2            #408 <String "java.lang.Object">
    //   39   77:invokestatic    #396 <Method Class findClass(String)>
    //   40   80:aastore         
    //   41   81:dup             
    //   42   82:iconst_1        
    //   43   83:getstatic       #406 <Field Class Integer.TYPE>
    //   44   86:aastore         
    //   45   87:astore_0        
    //   46   88:aload_1         
    //   47   89:ldc2            #412 <String "setElementAt">
    //   48   92:aload_0         
    //   49   93:invokevirtual   #402 <Method Method Class.getDeclaredMethod(String, Class[])>
    //   50   96:putstatic       #302 <Field Method METHOD_33>
    //   51   99:ldc2            #394 <String "java.util.Vector">
    //   52  102:invokestatic    #396 <Method Class findClass(String)>
    //   53  105:astore_1        
    //   54  106:iconst_1        
    //   55  107:anewarray       Class[]
    //   56  110:dup             
    //   57  111:iconst_0        
    //   58  112:getstatic       #406 <Field Class Integer.TYPE>
    //   59  115:aastore         
    //   60  116:astore_0        
    //   61  117:aload_1         
    //   62  118:ldc2            #414 <String "removeElementAt">
    //   63  121:aload_0         
    //   64  122:invokevirtual   #402 <Method Method Class.getDeclaredMethod(String, Class[])>
    //   65  125:putstatic       #160 <Field Method METHOD_11>
    //   66  128:ldc2            #394 <String "java.util.Vector">
    //   67  131:invokestatic    #396 <Method Class findClass(String)>
    //   68  134:astore_1        
    //   69  135:iconst_1        
    //   70  136:anewarray       Class[]
    //   71  139:dup             
    //   72  140:iconst_0        
    //   73  141:getstatic       #406 <Field Class Integer.TYPE>
    //   74  144:aastore         
    //   75  145:astore_0        
    //   76  146:aload_1         
    //   77  147:ldc2            #416 <String "elementAt">
    //   78  150:aload_0         
    //   79  151:invokevirtual   #402 <Method Method Class.getDeclaredMethod(String, Class[])>
    //   80  154:putstatic       #296 <Field Method METHOD_32>
    //   81  157:ldc2            #394 <String "java.util.Vector">
    //   82  160:invokestatic    #396 <Method Class findClass(String)>
    //   83  163:astore_1        
    //   84  164:iconst_0        
    //   85  165:anewarray       Class[]
    //   86  168:astore_0        
    //   87  169:aload_1         
    //   88  170:ldc2            #418 <String "toString">
    //   89  173:aload_0         
    //   90  174:invokevirtual   #402 <Method Method Class.getDeclaredMethod(String, Class[])>
    //   91  177:putstatic       #151 <Field Method METHOD_10>
    //   92  180:ldc2            #394 <String "java.util.Vector">
    //   93  183:invokestatic    #396 <Method Class findClass(String)>
    //   94  186:astore_1        
    //   95  187:iconst_1        
    //   96  188:anewarray       Class[]
    //   97  191:dup             
    //   98  192:iconst_0        
    //   99  193:ldc2            #408 <String "java.lang.Object">
    //  100  196:invokestatic    #396 <Method Class findClass(String)>
    //  101  199:aastore         
    //  102  200:astore_0        
    //  103  201:aload_1         
    //  104  202:ldc2            #420 <String "lastIndexOf">
    //  105  205:aload_0         
    //  106  206:invokevirtual   #402 <Method Method Class.getDeclaredMethod(String, Class[])>
    //  107  209:putstatic       #290 <Field Method METHOD_31>
    //  108  212:ldc2            #394 <String "java.util.Vector">
    //  109  215:invokestatic    #396 <Method Class findClass(String)>
    //  110  218:astore_1        
    //  111  219:iconst_2        
    //  112  220:anewarray       Class[]
    //  113  223:dup             
    //  114  224:iconst_0        
    //  115  225:ldc2            #408 <String "java.lang.Object">
    //  116  228:invokestatic    #396 <Method Class findClass(String)>
    //  117  231:aastore         
    //  118  232:dup             
    //  119  233:iconst_1        
    //  120  234:getstatic       #406 <Field Class Integer.TYPE>
    //  121  237:aastore         
    //  122  238:astore_0        
    //  123  239:aload_1         
    //  124  240:ldc2            #420 <String "lastIndexOf">
    //  125  243:aload_0         
    //  126  244:invokevirtual   #402 <Method Method Class.getDeclaredMethod(String, Class[])>
    //  127  247:putstatic       #283 <Field Method METHOD_30>
    //  128  250:ldc2            #394 <String "java.util.Vector">
    //  129  253:invokestatic    #396 <Method Class findClass(String)>
    //  130  256:astore_1        
    //  131  257:iconst_0        
    //  132  258:anewarray       Class[]
    //  133  261:astore_0        
    //  134  262:aload_1         
    //  135  263:ldc2            #422 <String "removeAllElements">
    //  136  266:aload_0         
    //  137  267:invokevirtual   #402 <Method Method Class.getDeclaredMethod(String, Class[])>
    //  138  270:putstatic       #277 <Field Method METHOD_29>
    //  139  273:ldc2            #394 <String "java.util.Vector">
    //  140  276:invokestatic    #396 <Method Class findClass(String)>
    //  141  279:astore_1        
    //  142  280:iconst_1        
    //  143  281:anewarray       Class[]
    //  144  284:dup             
    //  145  285:iconst_0        
    //  146  286:getstatic       #406 <Field Class Integer.TYPE>
    //  147  289:aastore         
    //  148  290:astore_0        
    //  149  291:aload_1         
    //  150  292:ldc2            #424 <String "get">
    //  151  295:aload_0         
    //  152  296:invokevirtual   #402 <Method Method Class.getDeclaredMethod(String, Class[])>
    //  153  299:putstatic       #270 <Field Method METHOD_28>
    //  154  302:ldc2            #394 <String "java.util.Vector">
    //  155  305:invokestatic    #396 <Method Class findClass(String)>
    //  156  308:astore_1        
    //  157  309:iconst_2        
    //  158  310:anewarray       Class[]
    //  159  313:dup             
    //  160  314:iconst_0        
    //  161  315:ldc2            #408 <String "java.lang.Object">
    //  162  318:invokestatic    #396 <Method Class findClass(String)>
    //  163  321:aastore         
    //  164  322:dup             
    //  165  323:iconst_1        
    //  166  324:getstatic       #406 <Field Class Integer.TYPE>
    //  167  327:aastore         
    //  168  328:astore_0        
    //  169  329:aload_1         
    //  170  330:ldc2            #426 <String "insertElementAt">
    //  171  333:aload_0         
    //  172  334:invokevirtual   #402 <Method Method Class.getDeclaredMethod(String, Class[])>
    //  173  337:putstatic       #263 <Field Method METHOD_27>
    //  174  340:ldc2            #394 <String "java.util.Vector">
    //  175  343:invokestatic    #396 <Method Class findClass(String)>
    //  176  346:astore_1        
    //  177  347:iconst_1        
    //  178  348:anewarray       Class[]
    //  179  351:dup             
    //  180  352:iconst_0        
    //  181  353:getstatic       #406 <Field Class Integer.TYPE>
    //  182  356:aastore         
    //  183  357:astore_0        
    //  184  358:aload_1         
    //  185  359:ldc2            #428 <String "ensureCapacity">
    //  186  362:aload_0         
    //  187  363:invokevirtual   #402 <Method Method Class.getDeclaredMethod(String, Class[])>
    //  188  366:putstatic       #257 <Field Method METHOD_26>
    //  189  369:ldc2            #394 <String "java.util.Vector">
    //  190  372:invokestatic    #396 <Method Class findClass(String)>
    //  191  375:astore_1        
    //  192  376:iconst_1        
    //  193  377:anewarray       Class[]
    //  194  380:dup             
    //  195  381:iconst_0        
    //  196  382:ldc2            #408 <String "java.lang.Object">
    //  197  385:invokestatic    #396 <Method Class findClass(String)>
    //  198  388:aastore         
    //  199  389:astore_0        
    //  200  390:aload_1         
    //  201  391:ldc2            #430 <String "removeElement">
    //  202  394:aload_0         
    //  203  395:invokevirtual   #402 <Method Method Class.getDeclaredMethod(String, Class[])>
    //  204  398:putstatic       #251 <Field Method METHOD_25>
    //  205  401:ldc2            #394 <String "java.util.Vector">
    //  206  404:invokestatic    #396 <Method Class findClass(String)>
    //  207  407:astore_1        
    //  208  408:iconst_2        
    //  209  409:anewarray       Class[]
    //  210  412:dup             
    //  211  413:iconst_0        
    //  212  414:getstatic       #406 <Field Class Integer.TYPE>
    //  213  417:aastore         
    //  214  418:dup             
    //  215  419:iconst_1        
    //  216  420:getstatic       #406 <Field Class Integer.TYPE>
    //  217  423:aastore         
    //  218  424:astore_0        
    //  219  425:aload_1         
    //  220  426:ldc2            #432 <String "removeRange">
    //  221  429:aload_0         
    //  222  430:invokevirtual   #402 <Method Method Class.getDeclaredMethod(String, Class[])>
    //  223  433:putstatic       #244 <Field Method METHOD_24>
    //  224  436:ldc2            #394 <String "java.util.Vector">
    //  225  439:invokestatic    #396 <Method Class findClass(String)>
    //  226  442:astore_1        
    //  227  443:iconst_1        
    //  228  444:anewarray       Class[]
    //  229  447:dup             
    //  230  448:iconst_0        
    //  231  449:ldc2            #434 <String "java.util.Collection">
    //  232  452:invokestatic    #396 <Method Class findClass(String)>
    //  233  455:aastore         
    //  234  456:astore_0        
    //  235  457:aload_1         
    //  236  458:ldc2            #436 <String "addAll">
    //  237  461:aload_0         
    //  238  462:invokevirtual   #402 <Method Method Class.getDeclaredMethod(String, Class[])>
    //  239  465:putstatic       #373 <Field Method METHOD_45>
    //  240  468:ldc2            #394 <String "java.util.Vector">
    //  241  471:invokestatic    #396 <Method Class findClass(String)>
    //  242  474:astore_1        
    //  243  475:iconst_1        
    //  244  476:anewarray       Class[]
    //  245  479:dup             
    //  246  480:iconst_0        
    //  247  481:ldc2            #408 <String "java.lang.Object">
    //  248  484:invokestatic    #396 <Method Class findClass(String)>
    //  249  487:aastore         
    //  250  488:astore_0        
    //  251  489:aload_1         
    //  252  490:ldc2            #438 <String "addElement">
    //  253  493:aload_0         
    //  254  494:invokevirtual   #402 <Method Method Class.getDeclaredMethod(String, Class[])>
    //  255  497:putstatic       #237 <Field Method METHOD_23>
    //  256  500:ldc2            #394 <String "java.util.Vector">
    //  257  503:invokestatic    #396 <Method Class findClass(String)>
    //  258  506:astore_1        
    //  259  507:iconst_2        
    //  260  508:anewarray       Class[]
    //  261  511:dup             
    //  262  512:iconst_0        
    //  263  513:getstatic       #406 <Field Class Integer.TYPE>
    //  264  516:aastore         
    //  265  517:dup             
    //  266  518:iconst_1        
    //  267  519:ldc2            #434 <String "java.util.Collection">
    //  268  522:invokestatic    #396 <Method Class findClass(String)>
    //  269  525:aastore         
    //  270  526:astore_0        
    //  271  527:aload_1         
    //  272  528:ldc2            #436 <String "addAll">
    //  273  531:aload_0         
    //  274  532:invokevirtual   #402 <Method Method Class.getDeclaredMethod(String, Class[])>
    //  275  535:putstatic       #366 <Field Method METHOD_44>
    //  276  538:ldc2            #394 <String "java.util.Vector">
    //  277  541:invokestatic    #396 <Method Class findClass(String)>
    //  278  544:astore_1        
    //  279  545:iconst_0        
    //  280  546:anewarray       Class[]
    //  281  549:astore_0        
    //  282  550:aload_1         
    //  283  551:ldc2            #440 <String "hashCode">
    //  284  554:aload_0         
    //  285  555:invokevirtual   #402 <Method Method Class.getDeclaredMethod(String, Class[])>
    //  286  558:putstatic       #231 <Field Method METHOD_22>
    //  287  561:ldc2            #394 <String "java.util.Vector">
    //  288  564:invokestatic    #396 <Method Class findClass(String)>
    //  289  567:astore_1        
    //  290  568:iconst_0        
    //  291  569:anewarray       Class[]
    //  292  572:astore_0        
    //  293  573:aload_1         
    //  294  574:ldc2            #442 <String "size">
    //  295  577:aload_0         
    //  296  578:invokevirtual   #402 <Method Method Class.getDeclaredMethod(String, Class[])>
    //  297  581:putstatic       #360 <Field Method METHOD_43>
    //  298  584:ldc2            #394 <String "java.util.Vector">
    //  299  587:invokestatic    #396 <Method Class findClass(String)>
    //  300  590:astore_1        
    //  301  591:iconst_1        
    //  302  592:anewarray       Class[]
    //  303  595:dup             
    //  304  596:iconst_0        
    //  305  597:ldc2            #434 <String "java.util.Collection">
    //  306  600:invokestatic    #396 <Method Class findClass(String)>
    //  307  603:aastore         
    //  308  604:astore_0        
    //  309  605:aload_1         
    //  310  606:ldc2            #444 <String "retainAll">
    //  311  609:aload_0         
    //  312  610:invokevirtual   #402 <Method Method Class.getDeclaredMethod(String, Class[])>
    //  313  613:putstatic       #225 <Field Method METHOD_21>
    //  314  616:ldc2            #394 <String "java.util.Vector">
    //  315  619:invokestatic    #396 <Method Class findClass(String)>
    //  316  622:astore_1        
    //  317  623:iconst_1        
    //  318  624:anewarray       Class[]
    //  319  627:dup             
    //  320  628:iconst_0        
    //  321  629:ldc2            #434 <String "java.util.Collection">
    //  322  632:invokestatic    #396 <Method Class findClass(String)>
    //  323  635:aastore         
    //  324  636:astore_0        
    //  325  637:aload_1         
    //  326  638:ldc2            #446 <String "containsAll">
    //  327  641:aload_0         
    //  328  642:invokevirtual   #402 <Method Method Class.getDeclaredMethod(String, Class[])>
    //  329  645:putstatic       #354 <Field Method METHOD_42>
    //  330  648:ldc2            #394 <String "java.util.Vector">
    //  331  651:invokestatic    #396 <Method Class findClass(String)>
    //  332  654:astore_1        
    //  333  655:iconst_0        
    //  334  656:anewarray       Class[]
    //  335  659:astore_0        
    //  336  660:aload_1         
    //  337  661:ldc2            #448 <String "clone">
    //  338  664:aload_0         
    //  339  665:invokevirtual   #402 <Method Method Class.getDeclaredMethod(String, Class[])>
    //  340  668:putstatic       #219 <Field Method METHOD_20>
    //  341  671:ldc2            #394 <String "java.util.Vector">
    //  342  674:invokestatic    #396 <Method Class findClass(String)>
    //  343  677:astore_1        
    //  344  678:iconst_0        
    //  345  679:anewarray       Class[]
    //  346  682:astore_0        
    //  347  683:aload_1         
    //  348  684:ldc2            #450 <String "isEmpty">
    //  349  687:aload_0         
    //  350  688:invokevirtual   #402 <Method Method Class.getDeclaredMethod(String, Class[])>
    //  351  691:putstatic       #348 <Field Method METHOD_41>
    //  352  694:ldc2            #394 <String "java.util.Vector">
    //  353  697:invokestatic    #396 <Method Class findClass(String)>
    //  354  700:astore_1        
    //  355  701:iconst_0        
    //  356  702:anewarray       Class[]
    //  357  705:astore_0        
    //  358  706:aload_1         
    //  359  707:ldc2            #452 <String "firstElement">
    //  360  710:aload_0         
    //  361  711:invokevirtual   #402 <Method Method Class.getDeclaredMethod(String, Class[])>
    //  362  714:putstatic       #145 <Field Method METHOD_9>
    //  363  717:ldc2            #394 <String "java.util.Vector">
    //  364  720:invokestatic    #396 <Method Class findClass(String)>
    //  365  723:astore_1        
    //  366  724:iconst_1        
    //  367  725:anewarray       Class[]
    //  368  728:dup             
    //  369  729:iconst_0        
    //  370  730:getstatic       #406 <Field Class Integer.TYPE>
    //  371  733:aastore         
    //  372  734:astore_0        
    //  373  735:aload_1         
    //  374  736:ldc2            #454 <String "setSize">
    //  375  739:aload_0         
    //  376  740:invokevirtual   #402 <Method Method Class.getDeclaredMethod(String, Class[])>
    //  377  743:putstatic       #342 <Field Method METHOD_40>
    //  378  746:ldc2            #456 <String "java.util.AbstractList">
    //  379  749:invokestatic    #396 <Method Class findClass(String)>
    //  380  752:astore_1        
    //  381  753:iconst_0        
    //  382  754:anewarray       Class[]
    //  383  757:astore_0        
    //  384  758:aload_1         
    //  385  759:ldc2            #458 <String "listIterator">
    //  386  762:aload_0         
    //  387  763:invokevirtual   #402 <Method Method Class.getDeclaredMethod(String, Class[])>
    //  388  766:putstatic       #139 <Field Method METHOD_8>
    //  389  769:ldc2            #456 <String "java.util.AbstractList">
    //  390  772:invokestatic    #396 <Method Class findClass(String)>
    //  391  775:astore_1        
    //  392  776:iconst_1        
    //  393  777:anewarray       Class[]
    //  394  780:dup             
    //  395  781:iconst_0        
    //  396  782:getstatic       #406 <Field Class Integer.TYPE>
    //  397  785:aastore         
    //  398  786:astore_0        
    //  399  787:aload_1         
    //  400  788:ldc2            #458 <String "listIterator">
    //  401  791:aload_0         
    //  402  792:invokevirtual   #402 <Method Method Class.getDeclaredMethod(String, Class[])>
    //  403  795:putstatic       #130 <Field Method METHOD_7>
    //  404  798:ldc2            #456 <String "java.util.AbstractList">
    //  405  801:invokestatic    #396 <Method Class findClass(String)>
    //  406  804:astore_1        
    //  407  805:iconst_0        
    //  408  806:anewarray       Class[]
    //  409  809:astore_0        
    //  410  810:aload_1         
    //  411  811:ldc2            #460 <String "iterator">
    //  412  814:aload_0         
    //  413  815:invokevirtual   #402 <Method Method Class.getDeclaredMethod(String, Class[])>
    //  414  818:putstatic       #121 <Field Method METHOD_6>
    //  415  821:ldc2            #394 <String "java.util.Vector">
    //  416  824:invokestatic    #396 <Method Class findClass(String)>
    //  417  827:astore_1        
    //  418  828:iconst_1        
    //  419  829:anewarray       Class[]
    //  420  832:dup             
    //  421  833:iconst_0        
    //  422  834:ldc2            #462 <String "[Ljava.lang.Object;">
    //  423  837:invokestatic    #396 <Method Class findClass(String)>
    //  424  840:aastore         
    //  425  841:astore_0        
    //  426  842:aload_1         
    //  427  843:ldc2            #464 <String "copyInto">
    //  428  846:aload_0         
    //  429  847:invokevirtual   #402 <Method Method Class.getDeclaredMethod(String, Class[])>
    //  430  850:putstatic       #114 <Field Method METHOD_5>
    //  431  853:ldc2            #394 <String "java.util.Vector">
    //  432  856:invokestatic    #396 <Method Class findClass(String)>
    //  433  859:astore_1        
    //  434  860:iconst_0        
    //  435  861:anewarray       Class[]
    //  436  864:astore_0        
    //  437  865:aload_1         
    //  438  866:ldc2            #466 <String "elements">
    //  439  869:aload_0         
    //  440  870:invokevirtual   #402 <Method Method Class.getDeclaredMethod(String, Class[])>
    //  441  873:putstatic       #105 <Field Method METHOD_4>
    //  442  876:ldc2            #394 <String "java.util.Vector">
    //  443  879:invokestatic    #396 <Method Class findClass(String)>
    //  444  882:astore_1        
    //  445  883:iconst_0        
    //  446  884:anewarray       Class[]
    //  447  887:astore_0        
    //  448  888:aload_1         
    //  449  889:ldc2            #468 <String "capacity">
    //  450  892:aload_0         
    //  451  893:invokevirtual   #402 <Method Method Class.getDeclaredMethod(String, Class[])>
    //  452  896:putstatic       #93  <Field Method METHOD_3>
    //  453  899:ldc2            #394 <String "java.util.Vector">
    //  454  902:invokestatic    #396 <Method Class findClass(String)>
    //  455  905:astore_1        
    //  456  906:iconst_1        
    //  457  907:anewarray       Class[]
    //  458  910:dup             
    //  459  911:iconst_0        
    //  460  912:ldc2            #434 <String "java.util.Collection">
    //  461  915:invokestatic    #396 <Method Class findClass(String)>
    //  462  918:aastore         
    //  463  919:astore_0        
    //  464  920:aload_1         
    //  465  921:ldc2            #470 <String "removeAll">
    //  466  924:aload_0         
    //  467  925:invokevirtual   #402 <Method Method Class.getDeclaredMethod(String, Class[])>
    //  468  928:putstatic       #86  <Field Method METHOD_2>
    //  469  931:ldc2            #394 <String "java.util.Vector">
    //  470  934:invokestatic    #396 <Method Class findClass(String)>
    //  471  937:astore_1        
    //  472  938:iconst_2        
    //  473  939:anewarray       Class[]
    //  474  942:dup             
    //  475  943:iconst_0        
    //  476  944:getstatic       #406 <Field Class Integer.TYPE>
    //  477  947:aastore         
    //  478  948:dup             
    //  479  949:iconst_1        
    //  480  950:getstatic       #406 <Field Class Integer.TYPE>
    //  481  953:aastore         
    //  482  954:astore_0        
    //  483  955:aload_1         
    //  484  956:ldc2            #472 <String "subList">
    //  485  959:aload_0         
    //  486  960:invokevirtual   #402 <Method Method Class.getDeclaredMethod(String, Class[])>
    //  487  963:putstatic       #77  <Field Method METHOD_1>
    //  488  966:ldc2            #394 <String "java.util.Vector">
    //  489  969:invokestatic    #396 <Method Class findClass(String)>
    //  490  972:astore_1        
    //  491  973:iconst_1        
    //  492  974:anewarray       Class[]
    //  493  977:dup             
    //  494  978:iconst_0        
    //  495  979:ldc2            #408 <String "java.lang.Object">
    //  496  982:invokestatic    #396 <Method Class findClass(String)>
    //  497  985:aastore         
    //  498  986:astore_0        
    //  499  987:aload_1         
    //  500  988:ldc2            #474 <String "equals">
    //  501  991:aload_0         
    //  502  992:invokevirtual   #402 <Method Method Class.getDeclaredMethod(String, Class[])>
    //  503  995:putstatic       #45  <Field Method METHOD_0>
    //  504  998:ldc2            #394 <String "java.util.Vector">
    //  505 1001:invokestatic    #396 <Method Class findClass(String)>
    //  506 1004:astore_1        
    //  507 1005:iconst_0        
    //  508 1006:anewarray       Class[]
    //  509 1009:astore_0        
    //  510 1010:aload_1         
    //  511 1011:ldc2            #476 <String "toArray">
    //  512 1014:aload_0         
    //  513 1015:invokevirtual   #402 <Method Method Class.getDeclaredMethod(String, Class[])>
    //  514 1018:putstatic       #213 <Field Method METHOD_19>
    //  515 1021:ldc2            #394 <String "java.util.Vector">
    //  516 1024:invokestatic    #396 <Method Class findClass(String)>
    //  517 1027:astore_1        
    //  518 1028:iconst_1        
    //  519 1029:anewarray       Class[]
    //  520 1032:dup             
    //  521 1033:iconst_0        
    //  522 1034:ldc2            #462 <String "[Ljava.lang.Object;">
    //  523 1037:invokestatic    #396 <Method Class findClass(String)>
    //  524 1040:aastore         
    //  525 1041:astore_0        
    //  526 1042:aload_1         
    //  527 1043:ldc2            #476 <String "toArray">
    //  528 1046:aload_0         
    //  529 1047:invokevirtual   #402 <Method Method Class.getDeclaredMethod(String, Class[])>
    //  530 1050:putstatic       #204 <Field Method METHOD_18>
    //  531 1053:ldc2            #394 <String "java.util.Vector">
    //  532 1056:invokestatic    #396 <Method Class findClass(String)>
    //  533 1059:astore_1        
    //  534 1060:iconst_1        
    //  535 1061:anewarray       Class[]
    //  536 1064:dup             
    //  537 1065:iconst_0        
    //  538 1066:ldc2            #408 <String "java.lang.Object">
    //  539 1069:invokestatic    #396 <Method Class findClass(String)>
    //  540 1072:aastore         
    //  541 1073:astore_0        
    //  542 1074:aload_1         
    //  543 1075:ldc2            #478 <String "remove">
    //  544 1078:aload_0         
    //  545 1079:invokevirtual   #402 <Method Method Class.getDeclaredMethod(String, Class[])>
    //  546 1082:putstatic       #337 <Field Method METHOD_39>
    //  547 1085:ldc2            #394 <String "java.util.Vector">
    //  548 1088:invokestatic    #396 <Method Class findClass(String)>
    //  549 1091:astore_1        
    //  550 1092:iconst_0        
    //  551 1093:anewarray       Class[]
    //  552 1096:astore_0        
    //  553 1097:aload_1         
    //  554 1098:ldc2            #480 <String "trimToSize">
    //  555 1101:aload_0         
    //  556 1102:invokevirtual   #402 <Method Method Class.getDeclaredMethod(String, Class[])>
    //  557 1105:putstatic       #198 <Field Method METHOD_17>
    //  558 1108:ldc2            #394 <String "java.util.Vector">
    //  559 1111:invokestatic    #396 <Method Class findClass(String)>
    //  560 1114:astore_1        
    //  561 1115:iconst_1        
    //  562 1116:anewarray       Class[]
    //  563 1119:dup             
    //  564 1120:iconst_0        
    //  565 1121:getstatic       #406 <Field Class Integer.TYPE>
    //  566 1124:aastore         
    //  567 1125:astore_0        
    //  568 1126:aload_1         
    //  569 1127:ldc2            #478 <String "remove">
    //  570 1130:aload_0         
    //  571 1131:invokevirtual   #402 <Method Method Class.getDeclaredMethod(String, Class[])>
    //  572 1134:putstatic       #331 <Field Method METHOD_38>
    //  573 1137:ldc2            #408 <String "java.lang.Object">
    //  574 1140:invokestatic    #396 <Method Class findClass(String)>
    //  575 1143:astore_1        
    //  576 1144:iconst_0        
    //  577 1145:anewarray       Class[]
    //  578 1148:astore_0        
    //  579 1149:aload_1         
    //  580 1150:ldc2            #482 <String "finalize">
    //  581 1153:aload_0         
    //  582 1154:invokevirtual   #402 <Method Method Class.getDeclaredMethod(String, Class[])>
    //  583 1157:putstatic       #191 <Field Method METHOD_16>
    //  584 1160:ldc2            #394 <String "java.util.Vector">
    //  585 1163:invokestatic    #396 <Method Class findClass(String)>
    //  586 1166:astore_1        
    //  587 1167:iconst_0        
    //  588 1168:anewarray       Class[]
    //  589 1171:astore_0        
    //  590 1172:aload_1         
    //  591 1173:ldc2            #484 <String "lastElement">
    //  592 1176:aload_0         
    //  593 1177:invokevirtual   #402 <Method Method Class.getDeclaredMethod(String, Class[])>
    //  594 1180:putstatic       #325 <Field Method METHOD_37>
    //  595 1183:ldc2            #394 <String "java.util.Vector">
    //  596 1186:invokestatic    #396 <Method Class findClass(String)>
    //  597 1189:astore_1        
    //  598 1190:iconst_1        
    //  599 1191:anewarray       Class[]
    //  600 1194:dup             
    //  601 1195:iconst_0        
    //  602 1196:ldc2            #408 <String "java.lang.Object">
    //  603 1199:invokestatic    #396 <Method Class findClass(String)>
    //  604 1202:aastore         
    //  605 1203:astore_0        
    //  606 1204:aload_1         
    //  607 1205:ldc2            #486 <String "contains">
    //  608 1208:aload_0         
    //  609 1209:invokevirtual   #402 <Method Method Class.getDeclaredMethod(String, Class[])>
    //  610 1212:putstatic       #185 <Field Method METHOD_15>
    //  611 1215:ldc2            #394 <String "java.util.Vector">
    //  612 1218:invokestatic    #396 <Method Class findClass(String)>
    //  613 1221:astore_1        
    //  614 1222:iconst_2        
    //  615 1223:anewarray       Class[]
    //  616 1226:dup             
    //  617 1227:iconst_0        
    //  618 1228:ldc2            #408 <String "java.lang.Object">
    //  619 1231:invokestatic    #396 <Method Class findClass(String)>
    //  620 1234:aastore         
    //  621 1235:dup             
    //  622 1236:iconst_1        
    //  623 1237:getstatic       #406 <Field Class Integer.TYPE>
    //  624 1240:aastore         
    //  625 1241:astore_0        
    //  626 1242:aload_1         
    //  627 1243:ldc2            #488 <String "indexOf">
    //  628 1246:aload_0         
    //  629 1247:invokevirtual   #402 <Method Method Class.getDeclaredMethod(String, Class[])>
    //  630 1250:putstatic       #320 <Field Method METHOD_36>
    //  631 1253:ldc2            #394 <String "java.util.Vector">
    //  632 1256:invokestatic    #396 <Method Class findClass(String)>
    //  633 1259:astore_1        
    //  634 1260:iconst_1        
    //  635 1261:anewarray       Class[]
    //  636 1264:dup             
    //  637 1265:iconst_0        
    //  638 1266:ldc2            #408 <String "java.lang.Object">
    //  639 1269:invokestatic    #396 <Method Class findClass(String)>
    //  640 1272:aastore         
    //  641 1273:astore_0        
    //  642 1274:aload_1         
    //  643 1275:ldc2            #490 <String "add">
    //  644 1278:aload_0         
    //  645 1279:invokevirtual   #402 <Method Method Class.getDeclaredMethod(String, Class[])>
    //  646 1282:putstatic       #180 <Field Method METHOD_14>
    //  647 1285:ldc2            #394 <String "java.util.Vector">
    //  648 1288:invokestatic    #396 <Method Class findClass(String)>
    //  649 1291:astore_1        
    //  650 1292:iconst_1        
    //  651 1293:anewarray       Class[]
    //  652 1296:dup             
    //  653 1297:iconst_0        
    //  654 1298:ldc2            #408 <String "java.lang.Object">
    //  655 1301:invokestatic    #396 <Method Class findClass(String)>
    //  656 1304:aastore         
    //  657 1305:astore_0        
    //  658 1306:aload_1         
    //  659 1307:ldc2            #488 <String "indexOf">
    //  660 1310:aload_0         
    //  661 1311:invokevirtual   #402 <Method Method Class.getDeclaredMethod(String, Class[])>
    //  662 1314:putstatic       #314 <Field Method METHOD_35>
    //  663 1317:ldc2            #394 <String "java.util.Vector">
    //  664 1320:invokestatic    #396 <Method Class findClass(String)>
    //  665 1323:astore_1        
    //  666 1324:iconst_2        
    //  667 1325:anewarray       Class[]
    //  668 1328:dup             
    //  669 1329:iconst_0        
    //  670 1330:getstatic       #406 <Field Class Integer.TYPE>
    //  671 1333:aastore         
    //  672 1334:dup             
    //  673 1335:iconst_1        
    //  674 1336:ldc2            #408 <String "java.lang.Object">
    //  675 1339:invokestatic    #396 <Method Class findClass(String)>
    //  676 1342:aastore         
    //  677 1343:astore_0        
    //  678 1344:aload_1         
    //  679 1345:ldc2            #490 <String "add">
    //  680 1348:aload_0         
    //  681 1349:invokevirtual   #402 <Method Method Class.getDeclaredMethod(String, Class[])>
    //  682 1352:putstatic       #173 <Field Method METHOD_13>
    //  683 1355:return          
    }

    private MethodInterceptor h;
    private static final Method METHOD_0;
    private static final Method METHOD_1;
    private static final Method METHOD_2;
    private static final Method METHOD_3;
    private static final Method METHOD_4;
    private static final Method METHOD_5;
    private static final Method METHOD_6;
    private static final Method METHOD_7;
    private static final Method METHOD_8;
    private static final Method METHOD_9;
    private static final Method METHOD_10;
    private static final Method METHOD_11;
    private static final Method METHOD_12;
    private static final Method METHOD_13;
    private static final Method METHOD_14;
    private static final Method METHOD_15;
    private static final Method METHOD_16;
    private static final Method METHOD_17;
    private static final Method METHOD_18;
    private static final Method METHOD_19;
    private static final Method METHOD_20;
    private static final Method METHOD_21;
    private static final Method METHOD_22;
    private static final Method METHOD_23;
    private static final Method METHOD_24;
    private static final Method METHOD_25;
    private static final Method METHOD_26;
    private static final Method METHOD_27;
    private static final Method METHOD_28;
    private static final Method METHOD_29;
    private static final Method METHOD_30;
    private static final Method METHOD_31;
    private static final Method METHOD_32;
    private static final Method METHOD_33;
    private static final Method METHOD_34;
    private static final Method METHOD_35;
    private static final Method METHOD_36;
    private static final Method METHOD_37;
    private static final Method METHOD_38;
    private static final Method METHOD_39;
    private static final Method METHOD_40;
    private static final Method METHOD_41;
    private static final Method METHOD_42;
    private static final Method METHOD_43;
    private static final Method METHOD_44;
    private static final Method METHOD_45;
}