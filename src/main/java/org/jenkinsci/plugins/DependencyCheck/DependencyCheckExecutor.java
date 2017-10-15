/*
 * This file is part of Dependency-Check Jenkins plugin.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jenkinsci.plugins.DependencyCheck;

import hudson.FilePath;
import hudson.Util;
import hudson.model.TaskListener;
import jenkins.security.MasterToSlaveCallable;
import org.apache.tools.ant.types.FileSet;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang.StringUtils;
import org.owasp.dependencycheck.Engine;
import org.owasp.dependencycheck.data.nvdcve.DatabaseException;
import org.owasp.dependencycheck.data.nvdcve.DriverLoadException;
import org.owasp.dependencycheck.data.nvdcve.DriverLoader;
import org.owasp.dependencycheck.data.update.exception.UpdateException;
import org.owasp.dependencycheck.exception.ExceptionCollection;
import org.owasp.dependencycheck.exception.ReportException;
import org.owasp.dependencycheck.reporting.ReportGenerator;
import org.owasp.dependencycheck.utils.Settings;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.logging.Level;

/**
 * This class is called by the DependencyCheckBuilder (the Jenkins build-step
 * plugin) and is responsible for executing a DependencyCheck analysis.
 *
 * @author Steve Springett (steve.springett@owasp.org)
 */
class DependencyCheckExecutor extends MasterToSlaveCallable<Boolean, IOException> implements Serializable {

    private static final long serialVersionUID = 4781360460201081295L;

    private Options options;
    private TaskListener listener;
    private transient ClassLoader classLoader;
    /**
     * The configured settings.
     */
    private Settings settings = null;

    /**
     * Constructs a new DependencyCheckExecutor object.
     *
     * @param options Options to be used for execution
     * @param listener BuildListener object to interact with the current build
     */
    DependencyCheckExecutor(final Options options, final TaskListener listener) {
        this(options, listener, null);
    }

    /**
     * Constructs a new DependencyCheckExecutor object.
     *
     * @param options Options to be used for execution
     * @param listener BuildListener object to interact with the current build
     */
    DependencyCheckExecutor(final Options options, final TaskListener listener, final ClassLoader classLoader) {
        this.options = options;
        this.listener = listener;
        this.classLoader = classLoader;
    }

    /**
     * Performs a DependencyCheck analysis build.
     *
     * @return a boolean value indicating if the build was successful or not. A
     * successful build is not determined by the ability to analyze
     * dependencies, rather, simply to determine if errors were encountered
     * during the execution.
     */
    public Boolean call() throws IOException {
        if (getJavaVersion() <= 1.6) {
            log(Messages.Failure_Java_Version());
            return false;
        }

        log(Messages.Executor_Display_Options());
        log(options.toString());

        if (!prepareDirectories()) {
            return false;
        }

        Engine engine = null;
        try {
            engine = executeDependencyCheck();
            if (options.isUpdateOnly()) {
                return true;
            } else {
                return generateExternalReports(engine);
            }
        } catch (DatabaseException ex) {
            log(Messages.Failure_Database_Connect());
            log(ex.getMessage());
        } catch (UpdateException ex) {
            log(Messages.Failure_Database_Update());
        } catch (ExceptionCollection ec) {
            log(Messages.Failure_Collection());
            for (Throwable t : ec.getExceptions()) {
                log("Exception Caught: " + t.getClass().getCanonicalName());
                if (t.getCause() != null && t.getCause().getMessage() != null) {
                    log("Cause: " + t.getCause().getMessage());
                }
                log("Message: " + t.getMessage());
                log(ExceptionUtils.getStackTrace(t));
            }
        } finally {
            settings.cleanup(true);
            if (engine != null) {
                engine.close();
            }
        }
        return false;
    }

    /**
     * Executes the Dependency-Check on the dependent libraries.
     *
     * @return the Engine used to scan the dependencies.
     */
    private Engine executeDependencyCheck() throws DatabaseException, UpdateException, ExceptionCollection {
        populateSettings();
        Engine engine;

        if (classLoader != null) {
            engine = new Engine(classLoader, settings);
        } else {
            engine = new Engine(settings);
        }
        if (options.isUpdateOnly()) {
            log(Messages.Executor_Update_Only());
            engine.doUpdates();
        } else {
            for (String scanPath : options.getScanPath()) {
                if (new File(scanPath).exists()) {
                    log(Messages.Executor_Scanning() + " " + scanPath);
                    engine.scan(scanPath);
                } else {
                    // Scan path does not exist. Check for Ant style pattern sets.
                    final File baseDir = new File(options.getWorkspace());

                    // Remove the workspace path from the scan path so FileSet can assume
                    // the specified path is a patternset that defines includes.
                    final String includes = scanPath.replace(options.getWorkspace() + File.separator, "");
                    final FileSet fileSet = Util.createFileSet(baseDir, includes, null);
                    final Iterator filePathIter = fileSet.iterator();
                    while (filePathIter.hasNext()) {
                        final FilePath foundFilePath = new FilePath(new FilePath(baseDir), filePathIter.next().toString());
                        log(Messages.Executor_Scanning() + " " + foundFilePath.getRemote());
                        engine.scan(foundFilePath.getRemote());
                    }
                }
            }
            log(Messages.Executor_Analyzing_Dependencies());
            engine.analyzeDependencies();
        }

        return engine;
    }

    /**
     * Generates the reports for a given dependency-check engine.
     *
     * @param engine a dependency-check engine
     * @return a boolean indicating if the report was generated successfully or
     * not
     */
    private boolean generateExternalReports(Engine engine) {
        try {
            for (ReportGenerator.Format format : options.getFormats()) {
                engine.writeReports(options.getName(), new File(options.getOutputDirectory()), format.name());
            }
            return true; // no errors - return positive response
        } catch (ReportException ex) {
            log(Level.SEVERE.getName() + ": " + ex);
        }
        return false;
    }

    /**
     * Populates DependencyCheck Settings. These may or may not be available as
     * parameters to the engine, and are usually more advanced options.
     */
    private void populateSettings() {
        settings = new Settings();
        if (options.getDbconnstr() == null) {
            settings.setString(Settings.KEYS.DB_CONNECTION_STRING, "jdbc:h2:file:%s;MV_STORE=FALSE;AUTOCOMMIT=ON;");

            // Hack for force loading of H2 database driver
            // https://github.com/jeremylong/DependencyCheck/issues/930
            try {
                DriverLoader.load("org.h2.Driver");
            } catch (DriverLoadException e) {
                System.out.println(e);
            }

        }
        if (StringUtils.isNotBlank(options.getDbconnstr())) {
            settings.setString(Settings.KEYS.DB_CONNECTION_STRING, options.getDbconnstr());
            if (StringUtils.isNotBlank(options.getDbdriver())) {
                settings.setString(Settings.KEYS.DB_DRIVER_NAME, options.getDbdriver());
            }
            if (StringUtils.isNotBlank(options.getDbpath())) {
                settings.setString(Settings.KEYS.DB_DRIVER_PATH, options.getDbpath());
            }
            if (StringUtils.isNotBlank(options.getDbuser())) {
                settings.setString(Settings.KEYS.DB_USER, options.getDbuser());
            }
            if (StringUtils.isNotBlank(options.getDbpassword())) {
                settings.setString(Settings.KEYS.DB_PASSWORD, options.getDbpassword());
            }
        }
        settings.setBoolean(Settings.KEYS.AUTO_UPDATE, options.isAutoUpdate());
        settings.setString(Settings.KEYS.DATA_DIRECTORY, options.getDataDirectory());

        if (options.getDataMirroringType() != 0) {
            if (options.getCveUrl12Modified() != null) {
                settings.setString(Settings.KEYS.CVE_MODIFIED_12_URL, options.getCveUrl12Modified().toExternalForm());
            }
            if (options.getCveUrl20Modified() != null) {
                settings.setString(Settings.KEYS.CVE_MODIFIED_20_URL, options.getCveUrl20Modified().toExternalForm());
            }
            if (options.getCveUrl12Base() != null) {
                settings.setString(Settings.KEYS.CVE_SCHEMA_1_2, options.getCveUrl12Base().toExternalForm());
            }
            if (options.getCveUrl20Base() != null) {
                settings.setString(Settings.KEYS.CVE_SCHEMA_2_0, options.getCveUrl20Base().toExternalForm());
            }
        }

        // In order to enable/disable individual analyzers that are annotationed @Experimental,
        // we first need to enable the loading of such analyzers. Then individual enabling/disabling
        // will work like in previous releases (< 1.4.0)
        settings.setBoolean(Settings.KEYS.ANALYZER_EXPERIMENTAL_ENABLED, true);

        settings.setBoolean(Settings.KEYS.ANALYZER_JAR_ENABLED, options.isJarAnalyzerEnabled());
        settings.setBoolean(Settings.KEYS.ANALYZER_NSP_PACKAGE_ENABLED, options.isNspAnalyzerEnabled());
        settings.setBoolean(Settings.KEYS.ANALYZER_COMPOSER_LOCK_ENABLED, options.isComposerLockAnalyzerEnabled());
        settings.setBoolean(Settings.KEYS.ANALYZER_PYTHON_DISTRIBUTION_ENABLED, options.isPythonDistributionAnalyzerEnabled());
        settings.setBoolean(Settings.KEYS.ANALYZER_PYTHON_PACKAGE_ENABLED, options.isPythonPackageAnalyzerEnabled());
        settings.setBoolean(Settings.KEYS.ANALYZER_BUNDLE_AUDIT_ENABLED, options.isRubyBundlerAuditAnalyzerEnabled());
        settings.setBoolean(Settings.KEYS.ANALYZER_RUBY_GEMSPEC_ENABLED, options.isRubyGemAnalyzerEnabled());
        settings.setBoolean(Settings.KEYS.ANALYZER_COCOAPODS_ENABLED, options.isCocoaPodsAnalyzerEnabled());
        settings.setBoolean(Settings.KEYS.ANALYZER_SWIFT_PACKAGE_MANAGER_ENABLED, options.isSwiftPackageManagerAnalyzerEnabled());
        settings.setBoolean(Settings.KEYS.ANALYZER_ARCHIVE_ENABLED, options.isArchiveAnalyzerEnabled());
        settings.setBoolean(Settings.KEYS.ANALYZER_ASSEMBLY_ENABLED, options.isAssemblyAnalyzerEnabled());
        settings.setBoolean(Settings.KEYS.ANALYZER_NUSPEC_ENABLED, options.isNuspecAnalyzerEnabled());
        settings.setBoolean(Settings.KEYS.ANALYZER_NEXUS_ENABLED, options.isNexusAnalyzerEnabled());
        settings.setBoolean(Settings.KEYS.ANALYZER_AUTOCONF_ENABLED, options.isAutoconfAnalyzerEnabled());
        settings.setBoolean(Settings.KEYS.ANALYZER_CMAKE_ENABLED, options.isCmakeAnalyzerEnabled());
        settings.setBoolean(Settings.KEYS.ANALYZER_OPENSSL_ENABLED, options.isOpensslAnalyzerEnabled());
        if (options.getNexusUrl() != null) {
            settings.setString(Settings.KEYS.ANALYZER_NEXUS_URL, options.getNexusUrl().toExternalForm());
        }
        settings.setBoolean(Settings.KEYS.ANALYZER_NEXUS_USES_PROXY, !options.isNexusProxyBypassed());

        settings.setBoolean(Settings.KEYS.ANALYZER_CENTRAL_ENABLED, options.isCentralAnalyzerEnabled());
        if (options.getCentralUrl() != null) {
            settings.setString(Settings.KEYS.ANALYZER_CENTRAL_URL, options.getCentralUrl().toExternalForm());
        }

        // Proxy settings
        if (options.getProxyServer() != null) {
            settings.setString(Settings.KEYS.PROXY_SERVER, options.getProxyServer());
            settings.setString(Settings.KEYS.PROXY_PORT, String.valueOf(options.getProxyPort()));
        }
        if (options.getProxyUsername() != null) {
            settings.setString(Settings.KEYS.PROXY_USERNAME, options.getProxyUsername());
        }
        if (options.getProxyPassword() != null) {
            settings.setString(Settings.KEYS.PROXY_PASSWORD, options.getProxyPassword());
        }

        settings.setBoolean(Settings.KEYS.DOWNLOADER_QUICK_QUERY_TIMESTAMP, options.isQuickQueryTimestampEnabled());

        // The suppression file can either be a file on the file system or a URL.
        if (options.getSuppressionFile() != null) {
            settings.setString(Settings.KEYS.SUPPRESSION_FILE, options.getSuppressionFile());
        }
        // The hints file can either be a file on the file system or a URL.
        if (options.getHintsFile() != null) {
            settings.setString(Settings.KEYS.HINTS_FILE, options.getHintsFile());
        }
        if (options.getZipExtensions() != null) {
            settings.setString(Settings.KEYS.ADDITIONAL_ZIP_EXTENSIONS, options.getZipExtensions());
        }
        if (options.getMonoPath() != null) {
            settings.setString(Settings.KEYS.ANALYZER_ASSEMBLY_MONO_PATH, options.getMonoPath());
        }
        if (options.getBundleAuditPath() != null) {
            settings.setString(Settings.KEYS.ANALYZER_BUNDLE_AUDIT_PATH, options.getBundleAuditPath());
        }
        if (options.getTempPath() != null) {
            settings.setString(Settings.KEYS.TEMP_DIRECTORY, options.getTempPath());
        }
    }

    /**
     * Makes sure the specified directories exists and/or can be created.
     * Returns true if everything is ok, false otherwise.
     *
     * @return a boolean if the directories exist and/or have been successfully
     * created
     */
    private boolean prepareDirectories() {
        final File outputDirectory = new File(options.getOutputDirectory());
        final File dataDirectory = new File(options.getDataDirectory());

        if (!options.isUpdateOnly()) {

            if (options.getSuppressionFile() != null) {
                try {
                    // Test of the suppressionFile is a URL or not
                    new URL(options.getSuppressionFile());
                } catch (MalformedURLException e) {
                    // Suppression file was not a URL, so it must be a file path.
                    final File suppressionFile = new File(options.getSuppressionFile());
                    if (!suppressionFile.exists()) {
                        log(Messages.Warning_Suppression_NonExist());
                        options.setSuppressionFile(null);
                    }
                }
            }

            if (options.getHintsFile() != null) {
                try {
                    // Test of the hintsFile is a URL or not
                    new URL(options.getHintsFile());
                } catch (MalformedURLException e) {
                    // Hints file was not a URL, so it must be a file path.
                    final File hintsFile = new File(options.getHintsFile());
                    if (!hintsFile.exists()) {
                        log(Messages.Warning_Hints_NonExist());
                        options.setHintsFile(null);
                    }
                }
            }

            try {
                if (!(outputDirectory.exists() && outputDirectory.isDirectory())) {
                    if (outputDirectory.mkdirs()) {
                        log(Messages.Executor_DirCreated_Output());
                    }
                }
            } catch (Exception e) {
                log(Messages.Error_Output_Directory_Create());
                return false;
            }

            if (options.getScanPath().size() == 0) {
                log(Messages.Executor_ScanPath_Invalid());
                return false;
            }
        }

        try {
            if (!(dataDirectory.exists() && dataDirectory.isDirectory())) {
                if (dataDirectory.mkdirs()) {
                    log(Messages.Executor_DirCreated_Data());
                }
            }
        } catch (Exception e) {
            log(Messages.Error_Data_Directory_Create());
            return false;
        }

        return true;
    }

    /**
     * Returns the Java version being used to execute this plugin
     *
     * @return the Java version
     */
    private static double getJavaVersion() {
        String version = System.getProperty("java.version");
        int pos = version.indexOf('.');
        pos = version.indexOf('.', pos + 1);
        return Double.parseDouble(version.substring(0, pos));
    }

    /**
     * Log messages to the builds console.
     *
     * @param message The message to log
     */
    private void log(String message) {
        if (message == null) {
            return;
        }
        final String outtag = "[" + DependencyCheckPlugin.PLUGIN_NAME + "] ";
        listener.getLogger().println(outtag + message.replaceAll("\\n", "\n" + outtag));
    }
}
