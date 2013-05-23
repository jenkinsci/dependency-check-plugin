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
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.File;

/**
 * The DependencyCheck builder class provides the ability to invoke a DependencyCheck build as
 * a Jenkins build step. This class then performs the necessary wrapping around the invoking of
 * the DependencyCheck binary jar by taking all the configuration options defined in the UI and
 * creating command line arguments for them when executing.
 *
 * @author Steve Springett
 */
@SuppressWarnings("unused")
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
        Options options = generateOptions(build);
        DependencyCheckExecutor executor = new DependencyCheckExecutor(options, listener);
        return executor.performBuild();
    }

    /**
     * Generate Options from build configuration preferences that will be passed to
     * the build step in DependencyCheck
     * @param build an AbstractBuild object
     * @return DependencyCheck Options
     */
    private Options generateOptions(AbstractBuild build) {
        Options options = new Options();

        // Sets the DependencyCheck application name to the Jenkins display name. If a display name
        // was not defined, it will simply return the name of the build.
       options.setName(build.getProject().getDisplayName());

        // If the configured output directory is empty, set this builds output dir to the root of the projects workspace
        File outdirFile;
        if (StringUtils.isEmpty(outdir)) {
            outdirFile = new File(build.getWorkspace().getRemote());
        } else {
            outdirFile = new File (outdir.trim());
        }
        if (outdirFile.exists()) {
            options.setOutputDirectory(outdirFile);
        }

        // Support for multiple scan paths in a single analysis
        for (String tmpscanpath : scanpath.split(",")) {
            File file = new File(tmpscanpath.trim());
            if (file.exists()) {
                options.addScanPath(file);
            }
        }

        options.setDeepScan(isDeepscanEnabled);
        options.setAutoUpdate(!isAutoupdateDisabled);

        //todo: add proxy support
        return options;
    }

    /**
     * A Descriptor Implementation
     */
    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
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

        public DescriptorImpl() {
            super(DependencyCheckBuilder.class);
            load();
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
    }
}