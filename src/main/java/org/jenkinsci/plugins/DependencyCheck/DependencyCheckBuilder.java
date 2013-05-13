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

import hudson.Extension;
import hudson.Launcher;
import hudson.Proc;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.JDK;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.owasp.dependencycheck.utils.CliParser;

import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;

/**
 * The DependencyCheck builder class provides the ability to invoke a DependencyCheck build as
 * a Jenkins build step. This class then performs the necessary wrapping around the invoking of
 * the DependencyCheck binary jar by taking all the configuration options defined in the UI and
 * creating command line arguments for them when executing.
 *
 * @author Steve Springett
 */
public class DependencyCheckBuilder extends Builder {

    private final String scanpath;
    private final String outdir;
    private final boolean isDeepscanEnabled;
    private final boolean isAutoupdateDisabled;


    @DataBoundConstructor // Fields in config.jelly must match the parameter names
    public DependencyCheckBuilder(String scanpath, String outdir,
                                  Boolean isDeepscanEnabled, Boolean isAutoupdateDisabled) {
        this.scanpath = scanpath;
        this.outdir = outdir;
        this.isDeepscanEnabled = (isDeepscanEnabled != null) && isDeepscanEnabled;
        this.isAutoupdateDisabled = (isAutoupdateDisabled != null) && isAutoupdateDisabled;
    }

    /**
     * Retrieves the path to scan. This is a per-build config item.
     * This method must match the value in <tt>config.jelly</tt>.
     */
    public String getScanpath() {
        return scanpath;
    }

    /**
     * Retrieves the output directory to write the report. This is a per-build config item.
     * This method must match the value in <tt>config.jelly</tt>.
     */
    public String getOutdir() {
        return outdir;
    }

    /**
     * Retrieves whether a deep scan should be enabled or not. This is a per-build config item.
     * This method must match the value in <tt>config.jelly</tt>.
     */
    public boolean isDeepscanEnabled() {
        return isDeepscanEnabled;
    }

    /**
     * Retrieves whether auto update should be disabled or not. This is a per-build config item.
     * This method must match the value in <tt>config.jelly</tt>.
     */
    public boolean isAutoupdateDisabled() {
        return isAutoupdateDisabled;
    }

    /**
     * This method is called whenever the DependencyCheck build step is executed.
     *
     * @param build    A Build object
     * @param launcher A Launcher object
     * @param listener A BuildListener object
     * @return A true or false value indicating if the build was successful or if it failed
     */
    @Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
        String command = generateCommand(build, getDescriptor().jarpath);
        try {
            Proc proc = launcher.launch(command, build.getEnvVars(), listener.getLogger(), build.getProject().getWorkspace());
            int exitCode = proc.join();
            return exitCode == 0;
        } catch (IOException e) {
            e.printStackTrace();
            listener.getLogger().println("IOException !");
            return false;
        } catch (InterruptedException e) {
            e.printStackTrace();
            listener.getLogger().println("InterruptedException!");
            return false;
        }

        /*
        String[] args = {
                "-" + CliParser.ArgumentName.APPNAME, build.getProject().getDisplayName(),
                "-" + CliParser.ArgumentName.OUT, outdir,
                "-" + CliParser.ArgumentName.SCAN, scanpath,
                "-" + CliParser.ArgumentName.PERFORM_DEEP_SCAN, isDeepscanEnabled,
                "-" + CliParser.ArgumentName.DISABLE_AUTO_UPDATE, isAutoupdateDisabled,
                "-" + CliParser.ArgumentName.OUTPUT_FORMAT, "XML"
        };

        App app = new App();
        app.run(args);
        return true;
        */
    }

    /**
     * A Descriptor Implementation
     */
    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    /**
     * Generates the necessary command line arguments from the build steps' configuration
     *
     * @return A String representation of the entire command line with arguments to execute
     */
    private String generateCommand(AbstractBuild build, String jarpath) {
        /*
            todo: need to evaluate if this is the proper way to perform this type of action
            or if there's a better Jenkins supported way. Additionally, it may be better to
            call run(args[]) in org.owasp.dependencycheck.App directly.
         */

        /* todo: support for multiple scan paths separated by space or comma. DependencyCheck already supports this. */

        StringBuilder sb = new StringBuilder();

        JDK jdk = build.getProject().getJDK();
        // Determine if a JDK has been defined for this project or not. If so, use it.
        if (jdk != null && !StringUtils.isBlank(jdk.getHome()))
            sb.append(build.getProject().getJDK().getHome()).append(File.separator);
            // JDK was not defined for this project. Check to see if JAVA_HOME is defined and if so, use it.
        else if (!StringUtils.isBlank(System.getenv("JAVA_HOME")))
            sb.append(System.getenv("JAVA_HOME")).append(File.separator).append("bin").append(File.separator);
        // If neither one were true, then 'java' is expected to be in the path.

        // Sets the DependencyCheck application name to the Jenkins display name. If a display name
        // was not defined, it will simply return the name of the build.
        String appname = build.getProject().getDisplayName();

        String tmpoutdir = outdir;
        // If the configured output directory is empty, set this builds output dir to the root of the projects workspace
        // This is necessary as changing the value of outdir, changes the configuration for the build, so use temp var
        if (StringUtils.isEmpty(tmpoutdir))
            tmpoutdir = build.getWorkspace().getRemote();

        sb.append("java");
        sb.append(" -jar ").append("\"").append(jarpath).append("\" ");
        sb.append("-").append(CliParser.ArgumentName.APPNAME).append(" \"").append(appname).append("\" ");
        sb.append("-").append(CliParser.ArgumentName.OUT).append(" \"").append(tmpoutdir).append("\" ");
        sb.append("-").append(CliParser.ArgumentName.SCAN).append(" \"").append(scanpath).append("\" ");
        sb.append("-").append(CliParser.ArgumentName.PERFORM_DEEP_SCAN).append(" ").append(isDeepscanEnabled).append(" ");
        sb.append("-").append(CliParser.ArgumentName.DISABLE_AUTO_UPDATE).append(" ").append(isAutoupdateDisabled).append(" ");
        sb.append("-").append(CliParser.ArgumentName.OUTPUT_FORMAT).append(" XML");
        return sb.toString();
    }

    /**
     * Descriptor for {@link DependencyCheckBuilder}. Used as a singleton.
     * The class is marked as public so that it can be accessed from views.
     * <p/>
     * <p/>
     * See <tt>src/main/resources/org/jenkinsci/plugins/DependencyCheck/DependencyCheckBuilder/*.jelly</tt>
     * for the actual HTML fragment for the configuration screen.
     */
    @Extension // This indicates to Jenkins that this is an implementation of an extension point.
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        // The jarpath is the absolute path and filename to the DependencyCheck jar file.
        private String jarpath;

        public DescriptorImpl() {
            super(DependencyCheckBuilder.class);
            load();
        }

        /**
         * Performs on-the-fly validation of the form field 'jarpath'
         *
         * @param value This parameter receives the value that the user has typed.
         * @return Indicates the outcome of the validation. This is sent to the browser.
         */
        public FormValidation doCheckJarpath(@QueryParameter String value) throws IOException, ServletException {
            if (StringUtils.isBlank(value))
                return FormValidation.warning(Messages.Form_Error_setupJar());

            File file = new File(value);
            if (!(file.exists() && file.isFile() && value.endsWith(".jar")))
                return FormValidation.error(Messages.Form_Error_jarNotFound());

            return FormValidation.ok();
        }

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            // Indicates that this builder can be used with all kinds of project types 
            return true;
        }

        /**
         * This name is used on the build configuration screen
         */
        public String getDisplayName() {
            return Messages.Builder_Name();
        }

        /**
         * This method is called when saving the global configuration
         *
         * @param req      a StaplerRequest object
         * @param formData a JSONObject
         * @return a boolean if the configuration was successfully saved
         * @throws FormException
         */
        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            // Retrieve the jarpath from the global configuration form
            jarpath = formData.getString("jarpath");

            save(); // Persist the global configuration

            return super.configure(req, formData);
        }

        /**
         * Returns the globally configured jarpath. The jarpath is the absolute path and filename
         * to the DependencyCheck jar file.
         *
         * @return the String of the path
         */
        public String getJarpath() {
            return jarpath;
        }

    }
}