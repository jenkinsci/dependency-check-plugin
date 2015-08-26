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
import hudson.model.BuildListener;
import org.apache.tools.ant.types.FileSet;
import org.owasp.dependencycheck.Engine;
import org.owasp.dependencycheck.data.nvdcve.CveDB;
import org.owasp.dependencycheck.data.nvdcve.DatabaseException;
import org.owasp.dependencycheck.data.nvdcve.DatabaseProperties;
import org.owasp.dependencycheck.reporting.ReportGenerator;
import org.owasp.dependencycheck.utils.Settings;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.Iterator;
import java.util.logging.Level;

/**
 * This class is called by the DependencyCheckBuilder (the Jenkins build-step plugin) and
 * is responsible for executing a DependencyCheck analysis.
 *
 * @author Steve Springett (steve.springett@owasp.org)
 */
public class DependencyCheckExecutor implements Serializable {

    private static final long serialVersionUID = 4781360460201081295L;

    private Options options;
    private BuildListener listener;
    private ClassLoader classLoader;

    /**
     * Constructs a new DependencyCheckExecutor object.
     *
     * @param options Options to be used for execution
     * @param listener BuildListener object to interact with the current build
     */
    public DependencyCheckExecutor(Options options, BuildListener listener) {
        this(options, listener, null);
    }

    /**
     * Constructs a new DependencyCheckExecutor object.
     *
     * @param options Options to be used for execution
     * @param listener BuildListener object to interact with the current build
     */
    public DependencyCheckExecutor(Options options, BuildListener listener, ClassLoader classLoader) {
        this.options = options;
        this.listener = listener;
        this.classLoader = classLoader;
    }

    /**
     * Performs a DependencyCheck analysis build.
     *
     * @return a boolean value indicating if the build was successful or not. A
     * successful build is not determined by the ability to analyze dependencies,
     * rather, simply to determine if errors were encountered during the execution.
     */
    public boolean performBuild() {
        /* todo: put this in place when Java 1.7 is a requirement for dependency-check-core
        if (getJavaVersion() <= 1.6) {
            log(Messages.Failure_Java_Version());
            return false;
        }
        */

        log(Messages.Executor_Display_Options());
        log(options.toString());

        if (!prepareDirectories()) {
            return false;
        }

        Engine engine = null;
        try {
            engine = executeDependencyCheck();
            return generateExternalReports(engine);
        } catch (DatabaseException ex) {
            log(Messages.Failure_Database_Connect());
        } finally {
            Settings.cleanup(true);
            if (engine != null) {
                engine.cleanup();
            }
        }
        return false;
    }

    /**
     * Executes the Dependency-Check on the dependent libraries.
     *
     * @return the Engine used to scan the dependencies.
     */
    private Engine executeDependencyCheck() throws DatabaseException {
        populateSettings();
        Engine engine = null;
        try {
            if (classLoader != null) {
                engine = new Engine(classLoader);
            } else {
                engine = new Engine();
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
        } finally {
            if (engine != null) {
                engine.cleanup();
            }
        }
        return engine;
    }

    /**
     * Generates the reports for a given dependency-check engine.
     *
     * @param engine a dependency-check engine
     * @return a boolean indicating if the report was generated successfully or not
     */
    private boolean generateExternalReports(Engine engine) {
        DatabaseProperties prop = null;
        CveDB cve = null;
        try {
            cve = new CveDB();
            cve.open();
            prop = cve.getDatabaseProperties();
        } catch (DatabaseException ex) {
            log(Level.SEVERE.getName() + ": " + Messages.Failure_Database_Properties() + ": " + ex);
        } finally {
            if (cve != null) {
                cve.close();
            }
        }
        final ReportGenerator r = new ReportGenerator(options.getName(), engine.getDependencies(), engine.getAnalyzers(), prop);
        try {
            if ("ALL".equalsIgnoreCase(options.getFormat().name())) {
                r.generateReports(options.getOutputDirectory(), ReportGenerator.Format.ALL);
            } else {
                if ("XML".equalsIgnoreCase(options.getFormat().name())) {
                    r.generateReports(options.getOutputDirectory(), ReportGenerator.Format.XML);
                } else {
                    r.generateReports(options.getOutputDirectory(), ReportGenerator.Format.HTML);
                }
            }
            return true; // no errors - return positive response
        } catch (IOException ex) {
            log(Level.SEVERE.getName() + ": " + ex);
        } catch (Exception ex) {
            log(Level.SEVERE.getName() + ": " + ex);
        }
        return false;
    }

    /**
     * Populates DependencyCheck Settings. These may or may not be available as parameters
     * to the engine, and are usually more advanced options.
     */
    private void populateSettings() {
        Settings.initialize();
        Settings.setString(Settings.KEYS.DB_CONNECTION_STRING, "jdbc:h2:file:%s;AUTOCOMMIT=ON;FILE_LOCK=SERIALIZED;");
        Settings.setBoolean(Settings.KEYS.AUTO_UPDATE, options.isAutoUpdate());
        Settings.setString(Settings.KEYS.DATA_DIRECTORY, options.getDataDirectory());

        if (options.getDataMirroringType() != 0) {
            if (options.getCveUrl12Modified() != null) {
                Settings.setString(Settings.KEYS.CVE_MODIFIED_12_URL, options.getCveUrl12Modified().toExternalForm());
            }
            if (options.getCveUrl20Modified() != null) {
                Settings.setString(Settings.KEYS.CVE_MODIFIED_20_URL, options.getCveUrl20Modified().toExternalForm());
            }
            if (options.getCveUrl12Base() != null) {
                Settings.setString(Settings.KEYS.CVE_SCHEMA_1_2, options.getCveUrl12Base().toExternalForm());
            }
            if (options.getCveUrl20Base() != null) {
                Settings.setString(Settings.KEYS.CVE_SCHEMA_2_0, options.getCveUrl20Base().toExternalForm());
            }
        }

        Settings.setBoolean(Settings.KEYS.ANALYZER_JAR_ENABLED, options.isJarAnalyzerEnabled());
        Settings.setBoolean(Settings.KEYS.ANALYZER_JAVASCRIPT_ENABLED, options.isJavascriptAnalyzerEnabled());
        Settings.setBoolean(Settings.KEYS.ANALYZER_NODE_PACKAGE_ENABLED, options.isNodeJsAnalyzerEnabled());
        Settings.setBoolean(Settings.KEYS.ANALYZER_PYTHON_PACKAGE_ENABLED, options.isPythonAnalyzerEnabled());
        Settings.setBoolean(Settings.KEYS.ANALYZER_PYTHON_DISTRIBUTION_ENABLED, options.isPythonAnalyzerEnabled());
        Settings.setBoolean(Settings.KEYS.ANALYZER_RUBY_GEMSPEC_ENABLED, options.isRubyGemAnalyzerEnabled());
        Settings.setBoolean(Settings.KEYS.ANALYZER_ARCHIVE_ENABLED, options.isArchiveAnalyzerEnabled());
        Settings.setBoolean(Settings.KEYS.ANALYZER_ASSEMBLY_ENABLED, options.isAssemblyAnalyzerEnabled());
        Settings.setBoolean(Settings.KEYS.ANALYZER_NUSPEC_ENABLED, options.isNuspecAnalyzerEnabled());
        Settings.setBoolean(Settings.KEYS.ANALYZER_NEXUS_ENABLED, options.isNexusAnalyzerEnabled());
        Settings.setBoolean(Settings.KEYS.ANALYZER_AUTOCONF_ENABLED, options.isAutoconfAnalyzerEnabled());
        Settings.setBoolean(Settings.KEYS.ANALYZER_CMAKE_ENABLED, options.isCmakeAnalyzerEnabled());
        Settings.setBoolean(Settings.KEYS.ANALYZER_OPENSSL_ENABLED, options.isOpensslAnalyzerEnabled());
        if (options.getNexusUrl() != null) {
            Settings.setString(Settings.KEYS.ANALYZER_NEXUS_URL, options.getNexusUrl().toExternalForm());
        }
        Settings.setBoolean(Settings.KEYS.ANALYZER_NEXUS_PROXY, !options.isNexusProxyBypassed());

        Settings.setBoolean(Settings.KEYS.ANALYZER_CENTRAL_ENABLED, options.isCentralAnalyzerEnabled());
        if (options.getCentralUrl() != null) {
            Settings.setString(Settings.KEYS.ANALYZER_CENTRAL_URL, options.getCentralUrl().toExternalForm());
        }

        // Proxy settings
        if (options.getProxyServer() != null) {
            Settings.setString(Settings.KEYS.PROXY_SERVER, options.getProxyServer());
            Settings.setString(Settings.KEYS.PROXY_PORT, String.valueOf(options.getProxyPort()));
        }
        if (options.getProxyUsername() != null) {
            Settings.setString(Settings.KEYS.PROXY_USERNAME, options.getProxyUsername());
        }
        if (options.getProxyPassword() != null) {
            Settings.setString(Settings.KEYS.PROXY_PASSWORD, options.getProxyPassword());
        }

        Settings.setBoolean(Settings.KEYS.DOWNLOADER_QUICK_QUERY_TIMESTAMP, options.isQuickQueryTimestampEnabled());

        // The suppression file can either be a file on the file system or a URL.
        final String supFile = options.getSuppressionFilePath();
        final URL supUrl = options.getSuppressionUrl();
        if (supFile != null) {
            Settings.setString(Settings.KEYS.SUPPRESSION_FILE, supFile);
        } else if (supUrl != null) {
            Settings.setString(Settings.KEYS.SUPPRESSION_FILE, supUrl.toExternalForm());
        }
        if (options.getZipExtensions() != null) {
            Settings.setString(Settings.KEYS.ADDITIONAL_ZIP_EXTENSIONS, options.getZipExtensions());
        }
        if (options.getMonoPath() != null) {
            Settings.setString(Settings.KEYS.ANALYZER_ASSEMBLY_MONO_PATH, options.getMonoPath());
        }
        if (options.getTempPath() != null) {
            Settings.setString(Settings.KEYS.TEMP_DIRECTORY, options.getTempPath());
        }
    }

    /**
     * Makes sure the specified directories exists and/or can be created. Returns true if everything
     * is ok, false otherwise.
     * @return a boolean if the directories exist and/or have been successfully created
     */
    private boolean prepareDirectories() {
        final File outputDirectory = new File(options.getOutputDirectory());
        final File dataDirectory = new File(options.getDataDirectory());

        if (!options.isUpdateOnly()) {
            try {
                if (options.getSuppressionFilePath() != null) {
                    final File suppressionFile = new File(options.getSuppressionFilePath());
                    if (!suppressionFile.exists()) {
                        log(Messages.Warning_Suppression_NonExist());
                        options.setSuppressionFile(null);
                    }
                }
            } catch (Exception e) {
                log(Messages.Error_Suppression_NonExist());
                return false;
            }

            try {
                if (!(outputDirectory.exists() && outputDirectory.isDirectory())) {
                    outputDirectory.mkdirs();
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
                dataDirectory.mkdirs();
            }
        } catch (Exception e) {
            log(Messages.Error_Data_Directory_Create());
            return false;
        }

        return true;
    }

    /**
     * Returns the Java version being used to execute this plugin
     * @return the Java version
     */
    private static double getJavaVersion () {
        String version = System.getProperty("java.version");
        int pos = version.indexOf('.');
        pos = version.indexOf('.', pos+1);
        return Double.parseDouble (version.substring (0, pos));
    }

    /**
     * Log messages to the builds console.
     * @param message The message to log
     */
    private void log(String message) {
        final String outtag = "[" + DependencyCheckPlugin.PLUGIN_NAME + "] ";
        listener.getLogger().println(outtag + message.replaceAll("\\n", "\n" + outtag));
    }
}
