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

import org.owasp.dependencycheck.reporting.ReportGenerator;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A container object that holds all of the configurable options to be used by
 * a DependencyCheck analysis.
 *
 * @author Steve Springett (steve.springett@owasp.org)
 */
@SuppressWarnings("unused")
public class JobOptions implements Serializable {

    private static final long serialVersionUID = -7898921930891163161L;

    /**
     * The path to the project workspace.
     */
    private String workspace;

    /**
     * The name of the report to be displayed
     */
    private String name;

    /**
     * Specifies the directory[s] to scan.
     */
    private ArrayList<String> scanPath = new ArrayList<>();

    /**
     * Specifies the path[es] to exclude.
     */
    private String excludes;

    /**
     * Specifies the destination directory for the generated report.
     */
    private String outputDirectory;

    /**
     * Specifies the data directory.
     */
    private String dataDirectory;

    /**
     * The report format to be generated. Default is XML.
     */
    private List<ReportGenerator.Format> formats = new ArrayList<>();

    /**
     * Sets whether auto-updating of the NVD CVE/CPE data is enabled. It is not
     * recommended that this be turned to false. Default is true.
     */
    private boolean autoUpdate = true;

    /**
     * Sets whether an NVD update should be the only thing performed. If true,
     * a scan will not be performed.
     */
    private boolean updateOnly = false;

    /**
     * Specifies the suppression file to use
     */
    private String suppressionFile;

    /**
     * Specifies the hints file to use
     */
    private String hintsFile;

    /**
     * Specifies the file extensions to be treated a ZIP
     */
    private String zipExtensions;

    /**
     * Default Constructor.
     */
    public JobOptions() {
        formats.add(ReportGenerator.Format.XML);
    }

    /**
     * Returns the path to the project workspace.
     */
    public String getWorkspace() {
        return workspace;
    }

    /**
     * Returns the name of the report to be displayed.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the report to be displayed.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the files and/or directory[s] to scan.
     */
    public ArrayList<String> getScanPath() {
        return scanPath;
    }

    /**
     * Sets the file[s] and/or directory[s] to scan.
     */
    public void setScanPath(ArrayList<String> scanPath) {
        this.scanPath = scanPath;
    }

    /**
     * Add a file and/or directory to scan.
     */
    public void addScanPath(String scanPath) {
        if (this.scanPath == null) {
            this.scanPath = new ArrayList<String>();
        }
        this.scanPath.add(scanPath);
    }

    /**
     * Returns the pathes to exclude from scan. Pathes in Ant-style and comma separated.
     */
    public String getExcludes() {
        return excludes;
    }

    /**
     * Sets the pathes to exclude from scan. Pathes in Ant-style and comma separated.
     */
    public void setExcludes(String excludes) {
        this.excludes = excludes;
    }

    /**
     * Returns the destination directory for the generated report.
     */
    public String getOutputDirectory() {
        return outputDirectory;
    }

    /**
     * Sets the destination directory for the generated report.
     */
    public void setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    /**
     * Returns the data directory.
     */
    public String getDataDirectory() {
        return dataDirectory;
    }

    /**
     * Sets the path to the project workspace.
     */
    public void setWorkspace(String workspace) {
        this.workspace = workspace;
    }

    /**
     * Sets the data directory.
     */
    public void setDataDirectory(String dataDirectory) {
        this.dataDirectory = dataDirectory;
    }

    /**
     * Returns the report formats to be generated. Default is XML.
     */
    public List<ReportGenerator.Format> getFormats() {
        return formats;
    }

    /**
     * Adds a report format to be generated.
     */
    public void addFormat(ReportGenerator.Format format) {
        formats.add(format);
    }

    /**
     * Returns whether auto-updating of the NVD CVE/CPE data is enabled. It is not
     * recommended that this be turned to false. Default is true.
     */
    public boolean isAutoUpdate() {
        return autoUpdate;
    }

    /**
     * Sets whether auto-updating of the NVD CVE/CPE data is enabled. It is not
     * recommended that this be turned to false.
     */
    public void setAutoUpdate(boolean autoUpdate) {
        this.autoUpdate = autoUpdate;
    }

    /**
     * Returns whether updates should be the only task performed.
     */
    public boolean isUpdateOnly() {
        return updateOnly;
    }

    /**
     * Sets whether an NVD update should be the only thing performed. If true,
     * a scan will not be performed.
     */
    public void setUpdateOnly(boolean updateOnly) {
        this.updateOnly = updateOnly;
    }

    /**
     * Returns the suppression file.
     */
    public String getSuppressionFile() {
        return suppressionFile;
    }

    /**
     * Sets the suppression file to use.
     */
    public void setSuppressionFile(String file) {
        this.suppressionFile = file;
    }

    /**
     * Returns the hints file.
     */
    public String getHintsFile() {
        return hintsFile;
    }

    /**
     * Sets the hints file to use.
     */
    public void setHintsFile(String file) {
        this.hintsFile = file;
    }

    /**
     * Returns the zip extensions.
     */
    public String getZipExtensions() {
        return zipExtensions;
    }

    /**
     * Sets the zip extensions - must be comma separated.
     */
    public void setZipExtensions(String extensions) {
        this.zipExtensions = extensions;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        if (name == null) {
            sb.append(" -name = ").append("ERROR - NAME NOT SPECIFIED OR INVALID.\n");
        } else {
            sb.append(" -name = ").append(name).append("\n");
        }
        if (!updateOnly && (scanPath == null || scanPath.size() == 0)) {
            sb.append(" -scanPath = ").append("ERROR - PATH NOT SPECIFIED OR INVALID.\n");
        } else {
            for (String filePath: scanPath) {
                sb.append(" -scanPath = ").append(filePath).append("\n");
            }
        }
        if (outputDirectory == null) {
            sb.append(" -outputDirectory = ").append("ERROR - OUTPUT DIRECTORY NOT SPECIFIED OR INVALID.\n");
        } else {
            sb.append(" -outputDirectory = ").append(outputDirectory).append("\n");
        }
        if (dataDirectory == null) {
            sb.append(" -dataDirectory = ").append("ERROR - DATA DIRECTORY NOT SPECIFIED OR INVALID.\n");
        } else {
            sb.append(" -dataDirectory = ").append(dataDirectory).append("\n");
        }
        if (suppressionFile != null) {
            sb.append(" -suppressionFile = ").append(suppressionFile).append("\n");
        }
        if (hintsFile != null) {
            sb.append(" -hintsFile = ").append(hintsFile).append("\n");
        }
        if (zipExtensions != null) {
            sb.append(" -zipExtensions = ").append(zipExtensions).append("\n");
        }
        sb.append(" -formats = ");
        for (ReportGenerator.Format format: formats) {
            sb.append(format.name()).append(" ");
        }
        sb.append("\n");

        sb.append(" -autoUpdate = ").append(autoUpdate).append("\n");
        sb.append(" -updateOnly = ").append(updateOnly);
        return sb.toString();
    }
}
