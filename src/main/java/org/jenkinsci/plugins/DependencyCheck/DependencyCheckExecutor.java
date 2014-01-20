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
import hudson.ProxyConfiguration;
import hudson.model.BuildListener;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;
import org.owasp.dependencycheck.Engine;
import org.owasp.dependencycheck.reporting.ReportGenerator;
import org.owasp.dependencycheck.utils.LogUtils;
import org.owasp.dependencycheck.utils.Settings;

import java.io.*;
import java.util.logging.Level;

/**
 * This class is called by the DependencyCheckBuilder (the Jenkins build-step plugin) and
 * is responsible for executing a DependencyCheck analysis.
 *
 * @author Steve Springett (steve.springett@owasp.org)
 */
public class DependencyCheckExecutor implements Serializable {

    private static final long serialVersionUID = 4781360460201081295L;

    /**
     * Name of the logging properties file.
     */
    private static final String LOG_PROPERTIES_FILE = "log.properties";

    private Options options;
    private BuildListener listener;

    /**
     * Constructs a new DependencyCheckExecutor object
     *
     * @param options Options to be used for execution
     * @param listener BuildListener object to interact with the current build
     */
    public DependencyCheckExecutor(Options options, BuildListener listener) {
        this.options = options;
        this.listener = listener;
    }

    /**
     * Performs a DependencyCheck analysis build
     *
     * @return a boolean value indicating if the build was successful or not. A
     * successful build is not determined by the ability to analyze dependencies,
     * rather, simply to determine if errors were encountered during the execution.
     */
    public boolean performBuild() {
        log(Messages.Executor_Display_Options());
        log(options.toString());

        if (!prepareDirectories())
            return false;

        final Engine engine = executeDependencyCheck();
        return generateExternalReports(engine);
    }

    /**
     * Executes the Dependency-Check on the dependent libraries.
     *
     * @return the Engine used to scan the dependencies.
     */
    private Engine executeDependencyCheck() {
        String log = (options.getVerboseLoggingFile() != null) ? options.getVerboseLoggingFile().getRemote() : null;
        final InputStream in = DependencyCheckExecutor.class.getClassLoader().getResourceAsStream(LOG_PROPERTIES_FILE);
        LogUtils.prepareLogger(in, log);

        populateSettings();
        final Engine engine = new Engine();

        for (FilePath filePath: options.getScanPath()) {
            log(Messages.Executor_Scanning() + " " + filePath.getRemote());
            engine.scan(filePath.getRemote());
        }

        log(Messages.Executor_Analyzing_Dependencies());
        engine.analyzeDependencies();
        return engine;
    }

    /**
     * Generates the reports for a given dependency-check engine.
     *
     * @param engine a dependency-check engine
     * @return a boolean indicating if the report was generated successfully or not
     */
    private boolean generateExternalReports(Engine engine) {
        final ReportGenerator r = new ReportGenerator(options.getName(), engine.getDependencies(), engine.getAnalyzers());

        try {
            if ("ALL".equalsIgnoreCase(options.getFormat().name())) {
                r.generateReports(options.getOutputDirectory().getRemote(), ReportGenerator.Format.ALL);
            } else {
                if ("XML".equalsIgnoreCase(options.getFormat().name())) {
                    r.generateReports(options.getOutputDirectory().getRemote(), ReportGenerator.Format.XML);
                } else {
                    r.generateReports(options.getOutputDirectory().getRemote(), ReportGenerator.Format.HTML);
                }
            }
            return true; // no errors - return positive response
        } catch (IOException ex) {
            log(Level.SEVERE.getName() + ": "+ ex);
        } catch (Exception ex) {
            log(Level.SEVERE.getName() + ": "+ ex);
        }
        return false;
    }

    /**
     * Populates DependencyCheck Settings. These may or may not be available as parameters
     * to the engine, and are usually more advanced options.
     */
    private void populateSettings() {
        Settings.setBoolean(Settings.KEYS.AUTO_UPDATE, options.isAutoUpdate());
        Settings.setString(Settings.KEYS.DATA_DIRECTORY, options.getDataDirectory().getRemote());

        if (options.getDataMirroringType() != 0) {
            if (options.getCveUrl12Modified() != null)
                Settings.setString(Settings.KEYS.CVE_MODIFIED_12_URL, options.getCveUrl12Modified().toExternalForm());
            if (options.getCveUrl20Modified() != null)
                Settings.setString(Settings.KEYS.CVE_MODIFIED_20_URL, options.getCveUrl20Modified().toExternalForm());
            if (options.getCveUrl12Base() != null)
                Settings.setString(Settings.KEYS.CVE_SCHEMA_1_2, options.getCveUrl12Base().toExternalForm());
            if (options.getCveUrl20Base() != null)
                Settings.setString(Settings.KEYS.CVE_SCHEMA_2_0, options.getCveUrl20Base().toExternalForm());
        }

        Settings.setBoolean(Settings.KEYS.ANALYZER_NEXUS_ENABLED, options.isNexusAnalyzerEnabled());
        if (options.getNexusUrl() != null) {
            Settings.setString(Settings.KEYS.ANALYZER_NEXUS_URL, options.getNexusUrl().toExternalForm());
        }

        // Proxy settings.
        ProxyConfiguration proxy = Jenkins.getInstance() != null ? Jenkins.getInstance().proxy : null;
        if (proxy != null) {
            if (!StringUtils.isBlank(proxy.name)) {
                Settings.setString(Settings.KEYS.PROXY_URL, proxy.name);
                Settings.setString(Settings.KEYS.PROXY_PORT, String.valueOf(proxy.port));
            }
            if (!StringUtils.isBlank(proxy.getUserName())) {
                Settings.setString(Settings.KEYS.PROXY_USERNAME, proxy.getUserName());
            }
            if (!StringUtils.isBlank(proxy.getPassword())) {
                Settings.setString(Settings.KEYS.PROXY_PASSWORD, proxy.getPassword());
            }
        }

        if (options.getSuppressionFile() != null) {
            Settings.setString(Settings.KEYS.SUPPRESSION_FILE, options.getSuppressionFile().getRemote());
        }

    }

    /**
     * Makes sure the specified directories exists and/or can be created. Returns true if everything
     * is ok, false otherwise.
     * @return a boolean if the directories exist and/or have been successfully created
     */
    private boolean prepareDirectories() {
        // todo: move this back to dependency-check-core for v1.1.0
        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            log("ERROR: Unable to load default database driver: org.h2.Driver");
            return false;
        }

        try {
            if (options.getSuppressionFile() != null && !options.getSuppressionFile().exists()) {
                log("WARNING: Suppression file does not exist. Omitting.");
                options.setSuppressionFile(null);
                Settings.setString(Settings.KEYS.SUPPRESSION_FILE, null);
            }
        } catch (Exception e) {
            log("ERROR: An error occurred attempting to validate the existence of suppression file.");
            return false;
        }

        try {
            if (! (options.getOutputDirectory().exists() && options.getOutputDirectory().isDirectory()) )
                options.getOutputDirectory().mkdirs();
        } catch (Exception e) {
            log("ERROR: Unable to create output directory");
            return false;
        }

        try {
            if (! (options.getDataDirectory().exists() && options.getDataDirectory().isDirectory()) )
                options.getDataDirectory().mkdirs();
        } catch (Exception e) {
            log("ERROR: Unable to create data directory");
            return false;
        }

        if (options.getScanPath().size() == 0) {
            log("The scan path(s) specified are not valid. Please specify a valid path to scan.");
            return false;
        }

        return true;
    }

    /**
     * Log messages to the builds console
     * @param message The message to log
     */
    private void log(String message) {
        String outtag = "[" + DependencyCheckPlugin.PLUGIN_NAME+"] ";
        message = message.replaceAll("\\n", "\n" + outtag);
        listener.getLogger().println(outtag + message);
    }
}