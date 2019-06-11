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
import java.nio.file.Paths;
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

    private JobOptions jobOptions;
    private GlobalOptions globalOptions;
    private TaskListener listener;
    private transient ClassLoader classLoader;
    private transient ConsoleLogger logger;

    /**
     * The configured settings.
     */
    private transient Settings settings = null;

    /**
     * Constructs a new DependencyCheckExecutor object.
     *
     * @param jobOptions Options to be used for execution
     * @param globalOptions Options to be used for execution
     * @param listener BuildListener object to interact with the current build
     */
    DependencyCheckExecutor(final JobOptions jobOptions, final GlobalOptions globalOptions, final TaskListener listener) {
        this(jobOptions, globalOptions, listener, null);
    }

    /**
     * Constructs a new DependencyCheckExecutor object.
     *
     * @param jobOptions Options to be used for execution
     * @param globalOptions Options to be used for execution
     * @param listener BuildListener object to interact with the current build
     */
    DependencyCheckExecutor(final JobOptions jobOptions, final GlobalOptions globalOptions, final TaskListener listener, final ClassLoader classLoader) {
        this.jobOptions = jobOptions;
        this.globalOptions = globalOptions;
        this.listener = listener;
        this.classLoader = classLoader;
        this.logger = new ConsoleLogger(listener);
    }

    /**
     * Performs a DependencyCheck analysis build.
     *
     * @return a boolean value indicating if the build was successful or not. A
     * successful build is not determined by the ability to analyze
     * dependencies, rather, simply to determine if errors were encountered
     * during the execution.
     */
    public Boolean call() {
        listener.getLogger().println();
        logger.log(Messages.Executor_Deprecated());
        listener.getLogger().println();
        logger.log(Messages.Executor_Display_Options());
        logger.log(jobOptions.toString());
        logger.log(globalOptions.toString());

        if (!prepareDirectories()) {
            return false;
        }

        try (Engine engine = executeDependencyCheck()) {
            if (jobOptions.isUpdateOnly()) {
                return true;
            } else {
                return generateExternalReports(engine);
            }
        } catch (DatabaseException ex) {
            logger.log(Messages.Failure_Database_Connect());
            logger.log(ex.getMessage());
        } catch (UpdateException ex) {
            logger.log(Messages.Failure_Database_Update());
        } catch (ExceptionCollection ec) {
            logger.log(Messages.Failure_Collection());
            for (Throwable t : ec.getExceptions()) {
                logger.log("Exception Caught: " + t.getClass().getCanonicalName());
                if (t.getCause() != null && t.getCause().getMessage() != null) {
                    logger.log("Cause: " + t.getCause().getMessage());
                }
                logger.log("Message: " + t.getMessage());
                logger.log(ExceptionUtils.getStackTrace(t));
            }
        } finally {
            settings.cleanup(true);
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
        if (jobOptions.isUpdateOnly()) {
            logger.log(Messages.Executor_Update_Only());
            engine.doUpdates();
        } else {
            for (String scanPath : jobOptions.getScanPath()) {
                boolean scanPathExists = new File(scanPath).exists();
                if (scanPathExists && !StringUtils.isNotBlank(jobOptions.getExcludes())) {
                    logger.log(Messages.Executor_Scanning() + " " + scanPath);
                    engine.scan(scanPath);
                } else {
                    // Scan path does not exist. Check for Ant style pattern sets.
                    final File baseDir = new File(jobOptions.getWorkspace());

                    // Remove the workspace path from the scan path so FileSet can assume
                    // the specified path is a patternset that defines includes.
                    String includes = scanPath.replace(jobOptions.getWorkspace() + File.separator, "");

                    // If scanpath was empty, there isn't separator on the end.
                    if (includes.equals(jobOptions.getWorkspace())) {
                        includes = "";
                    }
                    // If path is exist, we should add two asterisks to the end.
                    if (scanPathExists) {
                        includes = Paths.get(includes, "**").toString();
                    }
                    final FileSet fileSet = Util.createFileSet(baseDir, includes, jobOptions.getExcludes());
                    final Iterator filePathIter = fileSet.iterator();
                    while (filePathIter.hasNext()) {
                        final FilePath foundFilePath = new FilePath(new FilePath(baseDir), filePathIter.next().toString());
                        logger.log(Messages.Executor_Scanning() + " " + foundFilePath.getRemote());
                        engine.scan(foundFilePath.getRemote());
                    }
                }
            }
            logger.log(Messages.Executor_Analyzing_Dependencies());
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
            for (ReportGenerator.Format format : jobOptions.getFormats()) {
                engine.writeReports(jobOptions.getName(), new File(jobOptions.getOutputDirectory()), format.name());
            }
            return true; // no errors - return positive response
        } catch (ReportException ex) {
            logger.log(Level.SEVERE.getName() + ": " + ex);
        }
        return false;
    }

    /**
     * Populates DependencyCheck Settings. These may or may not be available as
     * parameters to the engine, and are usually more advanced options.
     */
    private void populateSettings() {
        settings = new Settings();
        if (globalOptions.getDbconnstr() == null) {
            settings.setString(Settings.KEYS.DB_CONNECTION_STRING, "jdbc:h2:file:%s;AUTOCOMMIT=ON;LOG=0;CACHE_SIZE=65536;");
        }
        if (StringUtils.isNotBlank(globalOptions.getDbconnstr())) {
            settings.setString(Settings.KEYS.DB_CONNECTION_STRING, globalOptions.getDbconnstr());
            if (StringUtils.isNotBlank(globalOptions.getDbdriver())) {
                settings.setString(Settings.KEYS.DB_DRIVER_NAME, globalOptions.getDbdriver());
            }
            if (StringUtils.isNotBlank(globalOptions.getDbpath())) {
                settings.setString(Settings.KEYS.DB_DRIVER_PATH, globalOptions.getDbpath());
            }
            if (StringUtils.isNotBlank(globalOptions.getDbuser())) {
                settings.setString(Settings.KEYS.DB_USER, globalOptions.getDbuser());
            }
            if (StringUtils.isNotBlank(globalOptions.getDbpassword())) {
                settings.setString(Settings.KEYS.DB_PASSWORD, globalOptions.getDbpassword());
            }
        }
        settings.setBoolean(Settings.KEYS.AUTO_UPDATE, jobOptions.isAutoUpdate());
        settings.setString(Settings.KEYS.DATA_DIRECTORY, jobOptions.getDataDirectory());

        if (globalOptions.getDataMirroringType() == -1 || globalOptions.getDataMirroringType() == 1) {
            if (globalOptions.getCveJsonUrlModified() != null) {
                settings.setString(Settings.KEYS.CVE_MODIFIED_JSON, globalOptions.getCveJsonUrlModified().toExternalForm());
            }
            if (globalOptions.getCveJsonUrlBase() != null) {
                settings.setString(Settings.KEYS.CVE_BASE_JSON, globalOptions.getCveJsonUrlBase().toExternalForm());
            }
            if (globalOptions.getRetireJsRepoJsUrl() != null) {
                settings.setString(Settings.KEYS.ANALYZER_RETIREJS_REPO_JS_URL, globalOptions.getRetireJsRepoJsUrl().toExternalForm());
            }
        }
        if (globalOptions.getDataMirroringType() == -1 || globalOptions.getDataMirroringType() == 2) {
            if (globalOptions.getRetireJsRepoJsUrl() != null) {
                settings.setString(Settings.KEYS.ANALYZER_RETIREJS_REPO_JS_URL, globalOptions.getRetireJsRepoJsUrl().toExternalForm());
            }
        }

        // In order to enable/disable individual analyzers that are annotationed @Experimental,
        // we first need to enable the loading of such analyzers. Then individual enabling/disabling
        // will work like in previous releases (< 1.4.0)
        settings.setBoolean(Settings.KEYS.ANALYZER_EXPERIMENTAL_ENABLED, true);

        settings.setBoolean(Settings.KEYS.ANALYZER_OSSINDEX_ENABLED, false);

        settings.setBoolean(Settings.KEYS.ANALYZER_JAR_ENABLED, globalOptions.isJarAnalyzerEnabled());
        settings.setBoolean(Settings.KEYS.ANALYZER_NODE_PACKAGE_ENABLED, globalOptions.isNodePackageAnalyzerEnabled());
        settings.setBoolean(Settings.KEYS.ANALYZER_NODE_AUDIT_ENABLED, globalOptions.isNodeAuditAnalyzerEnabled());
        settings.setBoolean(Settings.KEYS.ANALYZER_RETIREJS_ENABLED, globalOptions.isRetireJsAnalyzerEnabled());
        settings.setBoolean(Settings.KEYS.ANALYZER_COMPOSER_LOCK_ENABLED, globalOptions.isComposerLockAnalyzerEnabled());
        settings.setBoolean(Settings.KEYS.ANALYZER_PYTHON_DISTRIBUTION_ENABLED, globalOptions.isPythonDistributionAnalyzerEnabled());
        settings.setBoolean(Settings.KEYS.ANALYZER_PYTHON_PACKAGE_ENABLED, globalOptions.isPythonPackageAnalyzerEnabled());
        settings.setBoolean(Settings.KEYS.ANALYZER_BUNDLE_AUDIT_ENABLED, globalOptions.isRubyBundlerAuditAnalyzerEnabled());
        settings.setBoolean(Settings.KEYS.ANALYZER_RUBY_GEMSPEC_ENABLED, globalOptions.isRubyGemAnalyzerEnabled());
        settings.setBoolean(Settings.KEYS.ANALYZER_COCOAPODS_ENABLED, globalOptions.isCocoaPodsAnalyzerEnabled());
        settings.setBoolean(Settings.KEYS.ANALYZER_SWIFT_PACKAGE_MANAGER_ENABLED, globalOptions.isSwiftPackageManagerAnalyzerEnabled());
        settings.setBoolean(Settings.KEYS.ANALYZER_ARCHIVE_ENABLED, globalOptions.isArchiveAnalyzerEnabled());
        settings.setBoolean(Settings.KEYS.ANALYZER_ASSEMBLY_ENABLED, globalOptions.isAssemblyAnalyzerEnabled());
        settings.setBoolean(Settings.KEYS.ANALYZER_NUSPEC_ENABLED, globalOptions.isNuspecAnalyzerEnabled());
        settings.setBoolean(Settings.KEYS.ANALYZER_NEXUS_ENABLED, globalOptions.isNexusAnalyzerEnabled());
        settings.setBoolean(Settings.KEYS.ANALYZER_ARTIFACTORY_ENABLED, globalOptions.isArtifactoryAnalyzerEnabled());
        settings.setBoolean(Settings.KEYS.ANALYZER_MSBUILD_PROJECT_ENABLED, globalOptions.isMsBuildProjectAnalyzerEnabled());
        settings.setBoolean(Settings.KEYS.ANALYZER_NUGETCONF_ENABLED, globalOptions.isNuGetConfigAnalyzerEnabled());
        settings.setBoolean(Settings.KEYS.ANALYZER_AUTOCONF_ENABLED, globalOptions.isAutoconfAnalyzerEnabled());
        settings.setBoolean(Settings.KEYS.ANALYZER_CMAKE_ENABLED, globalOptions.isCmakeAnalyzerEnabled());
        settings.setBoolean(Settings.KEYS.ANALYZER_OPENSSL_ENABLED, globalOptions.isOpensslAnalyzerEnabled());

        // Nexus Analyzer
        if (globalOptions.getNexusUrl() != null) {
            settings.setString(Settings.KEYS.ANALYZER_NEXUS_URL, globalOptions.getNexusUrl().toExternalForm());
        }
        settings.setBoolean(Settings.KEYS.ANALYZER_NEXUS_USES_PROXY, !globalOptions.isNexusProxyBypassed());

        // Central Analyzer
        settings.setBoolean(Settings.KEYS.ANALYZER_CENTRAL_ENABLED, globalOptions.isCentralAnalyzerEnabled());
        if (globalOptions.getCentralUrl() != null) {
            settings.setString(Settings.KEYS.ANALYZER_CENTRAL_URL, globalOptions.getCentralUrl().toExternalForm());
        }

        // Artifactory Analyzer
        if (globalOptions.isArtifactoryAnalyzerEnabled() && globalOptions.getArtifactoryUrl() != null) {
            settings.setString(Settings.KEYS.ANALYZER_ARTIFACTORY_URL, globalOptions.getArtifactoryUrl().toExternalForm());
            settings.setBoolean(Settings.KEYS.ANALYZER_ARTIFACTORY_USES_PROXY, !globalOptions.isArtifactoryProxyBypassed());
            settings.setString(Settings.KEYS.ANALYZER_ARTIFACTORY_API_TOKEN, globalOptions.getArtifactoryApiToken());
            settings.setString(Settings.KEYS.ANALYZER_ARTIFACTORY_API_USERNAME, globalOptions.getArtifactoryApiUsername());
            settings.setString(Settings.KEYS.ANALYZER_ARTIFACTORY_BEARER_TOKEN, globalOptions.getArtifactoryBearerToken());
        }

        // Proxy settings
        if (globalOptions.getProxyServer() != null) {
            settings.setString(Settings.KEYS.PROXY_SERVER, globalOptions.getProxyServer());
            settings.setInt(Settings.KEYS.PROXY_PORT, globalOptions.getProxyPort());
        }
        if (globalOptions.getNonProxyHosts() != null) {
            settings.setString(Settings.KEYS.PROXY_NON_PROXY_HOSTS, globalOptions.getNonProxyHosts());
        }
        if (globalOptions.getProxyUsername() != null) {
            settings.setString(Settings.KEYS.PROXY_USERNAME, globalOptions.getProxyUsername());
        }
        if (globalOptions.getProxyPassword() != null) {
            settings.setString(Settings.KEYS.PROXY_PASSWORD, globalOptions.getProxyPassword());
        }

        settings.setBoolean(Settings.KEYS.DOWNLOADER_QUICK_QUERY_TIMESTAMP, globalOptions.isQuickQueryTimestampEnabled());

        // The suppression file can either be a file on the file system or a URL.
        if (jobOptions.getSuppressionFile() != null) {
            settings.setString(Settings.KEYS.SUPPRESSION_FILE, jobOptions.getSuppressionFile());
        }
        // The hints file can either be a file on the file system or a URL.
        if (jobOptions.getHintsFile() != null) {
            settings.setString(Settings.KEYS.HINTS_FILE, jobOptions.getHintsFile());
        }
        if (jobOptions.getZipExtensions() != null) {
            settings.setString(Settings.KEYS.ADDITIONAL_ZIP_EXTENSIONS, jobOptions.getZipExtensions());
        }
        if (globalOptions.getMonoPath() != null) {
            settings.setString(Settings.KEYS.ANALYZER_ASSEMBLY_DOTNET_PATH, globalOptions.getMonoPath());
        }
        if (globalOptions.getBundleAuditPath() != null) {
            settings.setString(Settings.KEYS.ANALYZER_BUNDLE_AUDIT_PATH, globalOptions.getBundleAuditPath());
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
        final File outputDirectory = new File(jobOptions.getOutputDirectory());
        final File dataDirectory = new File(jobOptions.getDataDirectory());

        if (!jobOptions.isUpdateOnly()) {

            if (jobOptions.getSuppressionFile() != null) {
                try {
                    // Test of the suppressionFile is a URL or not
                    new URL(jobOptions.getSuppressionFile());
                } catch (MalformedURLException e) {
                    // Suppression file was not a URL, so it must be a file path.
                    final File suppressionFile = new File(jobOptions.getSuppressionFile());
                    if (!suppressionFile.exists()) {
                        logger.log(Messages.Warning_Suppression_NonExist());
                        jobOptions.setSuppressionFile(null);
                    }
                }
            }

            if (jobOptions.getHintsFile() != null) {
                try {
                    // Test of the hintsFile is a URL or not
                    new URL(jobOptions.getHintsFile());
                } catch (MalformedURLException e) {
                    // Hints file was not a URL, so it must be a file path.
                    final File hintsFile = new File(jobOptions.getHintsFile());
                    if (!hintsFile.exists()) {
                        logger.log(Messages.Warning_Hints_NonExist());
                        jobOptions.setHintsFile(null);
                    }
                }
            }

            try {
                if (!(outputDirectory.exists() && outputDirectory.isDirectory())) {
                    if (outputDirectory.mkdirs()) {
                        logger.log(Messages.Executor_DirCreated_Output());
                    }
                }
            } catch (Exception e) {
                logger.log(Messages.Error_Output_Directory_Create());
                return false;
            }

            if (jobOptions.getScanPath().size() == 0) {
                logger.log(Messages.Executor_ScanPath_Invalid());
                return false;
            }
        }

        try {
            if (!(dataDirectory.exists() && dataDirectory.isDirectory())) {
                if (dataDirectory.mkdirs()) {
                    logger.log(Messages.Executor_DirCreated_Data());
                }
            }
        } catch (Exception e) {
            logger.log(Messages.Error_Data_Directory_Create());
            return false;
        }

        return true;
    }
}
