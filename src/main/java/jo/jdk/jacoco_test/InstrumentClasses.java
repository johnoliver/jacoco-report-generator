package jo.jdk.jacoco_test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.List;

import org.jacoco.core.instr.Instrumenter;
import org.jacoco.core.runtime.OfflineInstrumentationAccessGenerator;

/**
 * Task for offline instrumentation of class files.
 */
public class InstrumentClasses {
	private List<String> exclude = Arrays.asList("NoClassDefFoundError",
			"Throwable", "Thread", "Object", "Long", "Number",
			"NullPointerException", "RuntimeException", "Exception",
			"LinkageError", "NoClassDefFoundError", "Error",
			"StackTraceElement", "String", "Serializable", "Collections",
			"List", "ArrayList", "Class", "System", "Properties", "Hashtable", "Math", "Map",
			"Objects", "InetAddress");

	private List<String> regexexclude = Arrays
			.asList(
//					".*",
					".*java/util.*",
					".*java/lang.*",
////					".*java/rmi.*",
					".*java/security.*", 

					".*java/io.*",
					".*java/nio.*",
					".*sun/misc.*"
					
////					".*java/applet.*", 
////					".*java/awt.*", 
////					".*java/beans.*"
////					".*java/math.*",
////					".*java/net.*", 
////					".*java/sql.*", 
////					".*java/text.*",
////					".*java/time.*", 
					);
	
	private List<String> regexinclude = Arrays
			.asList(
					".*java/beans.*"
					);
	private static int count = 0;

	public static void main(String[] args) throws IOException {
		new InstrumentClasses().execute(new File(args[0]));
	}

	public void execute(File destdir) throws IOException {
		if (destdir == null) {
			throw new RuntimeException("Destdir not set");
		}

		final Instrumenter instrumenter = new Instrumenter(
				new OfflineInstrumentationAccessGenerator());

		Files.walkFileTree(destdir.toPath(), new Instrumentator(instrumenter));

	}

	private class Instrumentator extends SimpleFileVisitor<Path> {
		private Instrumenter instrumenter;

		public Instrumentator(Instrumenter instrumenter) {
			this.instrumenter = instrumenter;
		}

		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
				throws IOException {

			if (exclude.contains(file.getFileName().toString().split("\\.")[0])) {
				return FileVisitResult.CONTINUE;
			}
			
			boolean include = false;
			for (String regex : regexinclude) {
				if (file.toAbsolutePath().toString().matches(regex)) {
					include = true;
					break;
				}
			}
			
			if(include == false) {
				for (String regex : regexexclude) {
					if (file.toAbsolutePath().toString().matches(regex)) {
						return FileVisitResult.CONTINUE;
					}
				}
			}

			if (!file.getFileName().toString().endsWith(".class")) {
				return FileVisitResult.CONTINUE;
			}

			System.out.println("Instrumenting: "+file.getFileName());
			instrument(instrumenter, file);
			return FileVisitResult.CONTINUE;
		}

	}

	private void instrument(final Instrumenter instrumenter,
			final Path inputFile) throws IOException {
		final File file = inputFile.toFile();
		File tempFile = File.createTempFile(file.getName(), "tmp");
		try (InputStream input = new FileInputStream(file);
				OutputStream output = new FileOutputStream(tempFile)) {
			instrumenter.instrument(input, output, file.getName());

		} catch (final Exception e) {
			tempFile.delete();
			return;
		}
		Files.move(tempFile.toPath(), inputFile,
				StandardCopyOption.REPLACE_EXISTING);
	}
}
