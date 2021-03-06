package biz.aQute.bnd.diagnostics.gogo;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.osgi.resource.Capability;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.log.LogService;

import aQute.bnd.junit.ConsoleLogger;
import aQute.bnd.junit.JUnitFramework;
import aQute.bnd.junit.JUnitFramework.BundleBuilder;
import aQute.libg.glob.Glob;
import biz.aQute.bnd.diagnostics.gogo.foo.Foo;
import biz.aQute.bnd.diagnostics.gogo.impl.Diagnostics;
import biz.aQute.bnd.diagnostics.gogo.impl.Search;

public class DiagnosticsTest {
	static JUnitFramework	fw;
	static ConsoleLogger	log;

	@BeforeClass
	public static void load() throws Exception {
		try {
			fw = new JUnitFramework();
			log = new ConsoleLogger(fw.context);
			fw.addBundle("	org.apache.felix.gogo.command," + "org.apache.felix.gogo.runtime,"
				+ "org.apache.felix.scr;version=2.0.12" + "");
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}

	}

	@AfterClass
	public static void close() throws Exception {
		fw.close();
	}

	@Test
	public void testSimple() {
		Bundle[] bundles = fw.context.getBundles();
		assertThat(bundles).hasSize(4);

		ServiceReference<LogService> serviceReference = fw.context.getServiceReference(LogService.class);
		assertThat(serviceReference).isNotNull();
		assertThat(serviceReference.getBundle()
			.getBundleId()).isEqualTo(0L);
		LogService service = fw.context.getService(serviceReference);
		assertThat(service).isInstanceOf(ConsoleLogger.Facade.class);

		service.log(LogService.LOG_DEBUG, "Hello World");
	}

	@Test
	public void testBasicDiagnostics() {
		Diagnostics d = new Diagnostics(fw.context);
		List<Capability> caps = d.caps(-1, "*");
		System.out.println("Caps" + caps);
	}

	@Component
	public static class TestSearchingComponent {

		@Reference
		Foo foo;

		@Activate
		void activate() {
			System.out.println("activate TestSearchingComponent");
		}

		@Deactivate
		void deactivate() {
			System.out.println("deactivate TestSearchingComponent");
		}
	}

	@Component
	public static class TestFooComponent implements Foo {

		@Activate
		void activate() {
			System.out.println("activate TestFooComponent");
		}

		@Deactivate
		void deactivate() {
			System.out.println("deactivate TestFooComponent");
		}
	}

	@Test
	public void testWantedWithPrivateGetAndExportedRegister() throws Exception {

		Diagnostics diagnostic = new Diagnostics(fw.context);

		//
		// get service Foo, has private package with Foo
		//
		BundleBuilder aBuilder = fw.bundle();
		aBuilder.addResource(TestSearchingComponent.class);
		aBuilder.setPrivatePackage(Foo.class.getPackage()
			.getName());
		Bundle a = aBuilder.install();
		a.start();

		List<Search> wanted = diagnostic.wanted(a.getBundleId(), Glob.ALL);

		boolean foundFooClassDiagnostic = false;
		for (Search s : wanted) {
			if (s.serviceName.equals(Foo.class.getName())) {
				assertThat(s.mismatched).isEmpty();
				assertThat(s.matched).isEmpty();
				foundFooClassDiagnostic = true;
			}
		}

		assertThat(foundFooClassDiagnostic).isEqualTo(true);

		//
		// register service Foo, exported package with Foo
		//

		BundleBuilder bBuilder = fw.bundle();
		bBuilder.addResource(TestFooComponent.class);
		bBuilder.setExportPackage(Foo.class.getPackage()
			.getName());
		Bundle b = bBuilder.install();
		b.start();

		wanted = diagnostic.wanted(a.getBundleId(), Glob.ALL);

		foundFooClassDiagnostic = false;
		for (Search s : wanted) {
			if (s.serviceName.equals(Foo.class.getName())) {
				assertThat(s.mismatched).contains(b.getBundleId());
				assertThat(s.matched).isEmpty();
				foundFooClassDiagnostic = true;
			}
		}
		assertThat(foundFooClassDiagnostic).isEqualTo(true);

		a.uninstall();
		b.uninstall();
	}
}
