package net.sf.cglib.osgi;

import static java.lang.String.format;
import static org.ops4j.pax.exam.CoreOptions.bundle;
import static org.ops4j.pax.exam.CoreOptions.composite;
import static org.ops4j.pax.exam.CoreOptions.provision;
import static org.ops4j.pax.tinybundles.core.TinyBundles.bundle;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.Properties;

import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.options.UrlProvisionOption;

import net.sf.cglib.samples.Bean;
import net.sf.cglib.samples.Beans;
import net.sf.cglib.samples.Trace;

public class BundleHelper {

	private static final String CGLIB;
	private static final String CGLIB_NODEP;

	static {
		InputStream in = BundleHelper.class.getResourceAsStream("/version.properties");
		try {
			Properties props = new Properties();
			props.load(in);
			final String version = props.getProperty("version");
			CGLIB = toUrl("cglib", version);
			CGLIB_NODEP = toUrl("cglib-nodep", version);
		} catch (final Exception e) {
			throw new Error(e);
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private static String toUrl(String bundle, String version) throws Exception {
		return new File(format("../%s/target/%s-%s.jar", bundle, bundle, version)).getCanonicalFile().toURI().toURL()
				.toString();
	}

	private static Option itCaseBundle() {
		return provision(
				bundle().add(Trace.class).add(Bean.class).add(Beans.class).set("Export-Package", "net.sf.cglib.samples")
						.set("Import-Package", "net.sf.cglib.core,net.sf.cglib.proxy,net.sf.cglib.reflect").build());
	}

	public static Option cglibBundle() {
		return composite(itCaseBundle(), bundle(CGLIB));
	}

	public static Option cglibNodepBundle() {
		return composite(itCaseBundle(), bundle(CGLIB_NODEP));
	}
}
