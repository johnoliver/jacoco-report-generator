/*******************************************************************************
 * Copyright (c) 2009, 2013 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *    
 *******************************************************************************/
package jo.jdk.jacoco_test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.data.ExecutionDataReader;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.SessionInfoStore;
import org.jacoco.report.DirectorySourceFileLocator;
import org.jacoco.report.FileMultiReportOutput;
import org.jacoco.report.IReportVisitor;
import org.jacoco.report.html.HTMLFormatter;

/**
 * This example creates a HTML report for eclipse like projects based on a
 * single execution data store called jacoco.exec. The report contains no
 * grouping information.
 * 
 * The class files under test must be compiled with debug information, otherwise
 * source highlighting will not work.
 */
public class ReportGenerator {

	private final String title;

	private final File executionDataFile;
	private final File classesDirectory;
	private final File sourceDirectory;
	private final File reportDirectory;

	private ExecutionDataStore executionDataStore;
	private SessionInfoStore sessionInfoStore;

	public ReportGenerator(File executionDataFile, File classesDirectory, File sourceDirectory, File reportDirectory) {
		this.executionDataFile = executionDataFile;
		this.classesDirectory = classesDirectory;
		this.sourceDirectory = sourceDirectory;
		this.reportDirectory = reportDirectory;
		this.title = "Foo";
	}

	/**
	 * Create the report.
	 * 
	 * @throws IOException
	 */
	public void create() throws IOException {

		// Read the jacoco.exec file. Multiple data stores could be merged
		// at this point
		loadExecutionData();

		// Run the structure analyzer on a single class folder to build up
		// the coverage model. The process would be similar if your classes
		// were in a jar file. Typically you would create a bundle for each
		// class folder and each jar you want in your report. If you have
		// more than one bundle you will need to add a grouping node to your
		// report
		final IBundleCoverage bundleCoverage = analyzeStructure();

		createReport(bundleCoverage);

	}

	private void createReport(final IBundleCoverage bundleCoverage)
			throws IOException {

		// Create a concrete report visitor based on some supplied
		// configuration. In this case we use the defaults
		final HTMLFormatter htmlFormatter = new HTMLFormatter();
		final IReportVisitor visitor = htmlFormatter
				.createVisitor(new FileMultiReportOutput(reportDirectory));

		// Initialize the report with all of the execution and session
		// information. At this point the report doesn't know about the
		// structure of the report being created
		visitor.visitInfo(sessionInfoStore.getInfos(),
				executionDataStore.getContents());

		// Populate the report structure with the bundle coverage information.
		// Call visitGroup if you need groups in your report.
		visitor.visitBundle(bundleCoverage, new DirectorySourceFileLocator(
				sourceDirectory, "utf-8", 4));

		// Signal end of structure information to allow report to write all
		// information out
		visitor.visitEnd();

	}

	private void loadExecutionData() throws IOException {
		final FileInputStream fis = new FileInputStream(executionDataFile);
		final ExecutionDataReader executionDataReader = new ExecutionDataReader(
				fis);
		executionDataStore = new ExecutionDataStore();
		sessionInfoStore = new SessionInfoStore();

		executionDataReader.setExecutionDataVisitor(executionDataStore);
		executionDataReader.setSessionInfoVisitor(sessionInfoStore);

		while (executionDataReader.read()) {
		}

		fis.close();
	}

	private IBundleCoverage analyzeStructure() throws IOException {
		final CoverageBuilder coverageBuilder = new CoverageBuilder();
		final Analyzer analyzer = new Analyzer(executionDataStore,
				coverageBuilder);

		analyzer.analyzeAll(classesDirectory);

		return coverageBuilder.getBundle(title);
	}

	public static void main(final String[] args) throws IOException, ParseException {

		Options options = new Options().addOption(createOption("d","dataFile","Location of the Jacoco.exec file", "dir"))
				                       .addOption(createOption("c","classes", "Location of the class files", "dir"))
				                       .addOption(createOption("s","src",     "Source directory", "dir"))
				                       .addOption(createOption("o","outdir",  "Resport output location", "dir"));

		CommandLineParser parser = new GnuParser();
		CommandLine cmd;
		try {
			cmd = parser.parse(options, args);
		} catch (Exception e) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("gnu", options);
			return;
		}

		File executionDataFile = new File(cmd.getOptionValue("d"));
		File classesDirectory = new File(cmd.getOptionValue("c"));
		File sourceDirectory = new File(cmd.getOptionValue("s"));
		File reportDirectory = new File(cmd.getOptionValue("o"));
		
		final ReportGenerator generator = new ReportGenerator(executionDataFile, classesDirectory, sourceDirectory, reportDirectory);
		generator.create();
	}

	private static Option createOption(String arg, String longOpt, String description, String argName) {
		return OptionBuilder
					.withLongOpt(longOpt)
                    .withDescription(description)
                    .hasArg(true)
					.withArgName(argName)
					.isRequired(true)
					.create(arg);
	}

}
