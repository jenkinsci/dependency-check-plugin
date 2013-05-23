/*
 * This file is part of DependencyCheck Jenkins plugin.
 *
 * DependencyCheck is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * DependencyCheck is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * DependencyCheck. If not, see http://www.gnu.org/licenses/.
 */
package org.jenkinsci.plugins.DependencyCheck;

import hudson.model.BuildListener;
import hudson.model.Hudson;
import org.owasp.dependencycheck.App;
import org.owasp.dependencycheck.reporting.ReportGenerator;
import org.owasp.dependencycheck.utils.CliParser;
import org.owasp.dependencycheck.utils.Settings;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * This class is called by the DependencyCheckBuilder (the Jenkins build-step plugin) and
 * is responsible for executing a DependencyCheck analysis.
 *
 * @author Steve Springett (steve.springett@owasp.org)
 */
public class DependencyCheckExecutor {

    /**
     * Name of the logging properties file.
     */
    private static final String LOG_PROPERTIES_FILE = "configuration/log.properties";

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
        prepareLogger();

        Thread thread = Thread.currentThread();
        ClassLoader loader = Hudson.getInstance().getPluginManager().uberClassLoader;
        thread.setContextClassLoader(loader);

        Settings.setBoolean(Settings.KEYS.AUTO_UPDATE, options.isAutoUpdate());
        Settings.setBoolean(Settings.KEYS.PERFORM_DEEP_SCAN, options.isDeepScan());

        //todo: temporary
        String[] args = new String[] {
                "-" + CliParser.ArgumentName.APPNAME, options.getName(),
                "-" + CliParser.ArgumentName.OUT, options.getOutputDirectory().getAbsolutePath(),
                "-" + CliParser.ArgumentName.SCAN, options.getScanPath().get(0).getAbsolutePath(),
                "-" + CliParser.ArgumentName.OUTPUT_FORMAT, String.valueOf(ReportGenerator.Format.XML)
        };

        listener.getLogger().println(Messages.Executor_Display_Options());
        listener.getLogger().println(options.toString());

        //todo: change to use Engine directly
        App app = new App();
        app.run(args);

        return true;
    }

    /**
     * Configures the logger for use by the application.
     */
    private static void prepareLogger() {
        InputStream in = null;
        try {
            in = App.class.getClassLoader().getResourceAsStream(LOG_PROPERTIES_FILE);
            LogManager.getLogManager().reset();
            LogManager.getLogManager().readConfiguration(in);
        } catch (IOException ex) {
            System.err.println(ex.toString());
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception ex) {
                //ignore
            }
        }
    }

}