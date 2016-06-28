package net.sf.cglib.osgi;

import static java.lang.String.format;
import static org.ops4j.pax.exam.CoreOptions.bundle;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.Properties;

import org.ops4j.pax.exam.options.UrlProvisionOption;

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
		} catch (final IOException e) {
			throw new Error(e);
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private static String toUrl(String bundle, String version) throws MalformedURLException {
		return new File(format("../%s/target/%s-%s.jar", bundle, bundle, version)).toURI().toURL().toString();
	}

	public static UrlProvisionOption cglibBundle() {
		return bundle(CGLIB);
	}
	
	public static UrlProvisionOption cglibNodepBundle() {
		return bundle(CGLIB_NODEP);
	}
}
