package net.sf.cglib;

/**
 * This class is meant to be used as a drop-in replacement for 
 * <code>java.lang.reflect.Proxy</code> under JDK 1.3 or higher. There are some
 * known subtle differences:
 * <ul>
 * <li>The exceptions returned by invoking <code>getExceptionTypes</code>
 * on the <code>Method</code> passed to the <code>invoke</code> method
 * <b>are</b> the exact set that can be thrown without resulting in an
 * <code>UndeclaredThrowableException</code> being thrown.
 * <li>There is no protected constructor which accepts an
 * <code>InvocationHandler</code>. Instead, use the more convenient
 * <code>newProxyInstance</code> static method.
 * <li><code>net.sf.cglib.UndeclaredThrowableException</code> is used instead
 * of <code>java.lang.reflect.UndeclaredThrowableException</code>.
 * </ul> 
 *
 * @author Chris Nokleberg <a href="mailto:chris@nokleberg.com">chris@nokleberg.com</a>
 * @author Neeme Praks <a href="mailto:neeme@apache.org">neeme@apache.org</a>
 * @version $Id: ProxyJdk.java,v 1.1 2003/01/28 11:52:23 nemecec Exp $
 */
public class ProxyJdk extends Proxy {

    protected ProxyJdk(InvocationHandler ih) {
        super(ih);
    }

}
