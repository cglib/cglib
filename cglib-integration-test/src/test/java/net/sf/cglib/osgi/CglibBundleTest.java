package net.sf.cglib.osgi;

import static net.sf.cglib.osgi.BundleHelper.cglibBundle;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.provision;
import static org.ops4j.pax.exam.CoreOptions.wrappedBundle;
import static org.ops4j.pax.tinybundles.core.TinyBundles.bundle;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

import net.sf.cglib.beans.BeanCopier;
import net.sf.cglib.core.ClassGenerator;
import net.sf.cglib.core.internal.Function;
import net.sf.cglib.proxy.Callback;
import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.samples.Beans;
import net.sf.cglib.samples.Trace;
import net.sf.cglib.transform.ClassFilter;
import net.sf.cglib.transform.impl.FieldProvider;
import net.sf.cglib.util.ParallelSorter;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class CglibBundleTest {
	
	@Configuration
	public Option[] options() {
		return new Option[] { 
				cglibBundle(),
				provision(bundle().add(Trace.class).add(Beans.class).build()),
				mavenBundle("org.ow2.asm", "asm").versionAsInProject(),
				wrappedBundle(mavenBundle("org.apache.ant", "ant").versionAsInProject())
		};
	}
	
    @Test
    public void samples() throws Throwable{
        Trace.main(new String[]{});
        Beans.main(new String[]{});
    }

	/**
	 * Loads a class from any exported package. No ClassNotFoundException should
	 * be caused to be thrown.
	 */
	@Test
	public void verifyExports() {		
		BeanCopier.class.getName();
		ClassGenerator.class.getName();
		Function.class.getName();
		Callback.class.getName();
		FastClass.class.getName();
		ClassFilter.class.getName();
		FieldProvider.class.getName();
		ParallelSorter.class.getName();
	}
}
