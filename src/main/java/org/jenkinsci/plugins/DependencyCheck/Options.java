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

import hudson.FilePath;
import org.owasp.dependencycheck.reporting.ReportGenerator;

import java.util.ArrayList;

/**
 * A container object that holds all of the configurable options to be used by
 * a DependencyCheck analysis.
 *
 * @author Steve Springett (steve.springett@owasp.org)
 */
@SuppressWarnings("unused")
public class Options {

    /**
     * The name of the report to be displayed
     */
    private String name;

    /**
     * Specifies the directory[s] to scan.
     */
    private ArrayList<FilePath> scanPath = new ArrayList<FilePath>();

    /**
     * Specifies the destination directory for the generated report.
     */
    private FilePath outputDirectory;

    /**
     * Specifies the data directory.
     */
    private FilePath dataDirectory;

    /**
     * Specifies the CPE data directory.
     */
    private FilePath cpeDataDirectory;

    /**
     * Specifies the CVE data directory.
     */
    private FilePath cveDataDirectory;

    /**
     * Boolean value (true/false) whether or not the evidence collected
     * about a dependency is displayed in the report. Default is false.
     */
    private boolean showEvidence = false;

    /**
     * Indicates that a deepScan should be performed. This may cause more
     * false positives. Default is false.
     */
    private boolean deepScan = false;

    /**
     * The report format to be generated. Default is XML.
     */
    private ReportGenerator.Format format = ReportGenerator.Format.XML;

    /**
     * Sets whether auto-updating of the NVD CVE/CPE data is enabled. It is not
     * recommended that this be turned to false. Default is true.
     */
    private boolean autoUpdate = true;

    /**
     * Returns the name of the report to be displayed
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the report to be displayed
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the files and/or directory[s] to scan.
     */
    public ArrayList<FilePath> getScanPath() {
        return scanPath;
    }

    /**
     * Sets the file[s] and/or directory[s] to scan
     */
    public void setScanPath(ArrayList<FilePath> scanPath) {
        this.scanPath = scanPath;
    }

    /**
     * Add a file and/or directory to scan
     */
    public void addScanPath(FilePath scanPath) {
        if (this.scanPath == null) {
            this.scanPath = new ArrayList<FilePath>();
        }
        this.scanPath.add(scanPath);
    }

    /**
     * Returns the destination directory for the generated report.
     */
    public FilePath getOutputDirectory() {
        return outputDirectory;
    }

    /**
     * Sets the destination directory for the generated report.
     */
    public void setOutputDirectory(FilePath outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    /**
     * Returns the data directory.
     */
    public FilePath getDataDirectory() {
        return dataDirectory;
    }

    /**
     * Sets the data directory.
     */
    public void setDataDirectory(FilePath dataDirectory) {
        this.dataDirectory = dataDirectory;
    }

    /**
     * Returns the CPE data directory.
     */
    public FilePath getCpeDataDirectory() {
        return cpeDataDirectory;
    }

    /**
     * Sets the CPE data directory.
     */
    public void setCpeDataDirectory(FilePath cpeDataDirectory) {
        this.cpeDataDirectory = cpeDataDirectory;
    }

    /**
     * Returns the CVE data directory.
     */
    public FilePath getCveDataDirectory() {
        return cveDataDirectory;
    }

    /**
     * Sets the CVE data directory.
     */
    public void setCveDataDirectory(FilePath cveDataDirectory) {
        this.cveDataDirectory = cveDataDirectory;
    }

    /**
     * Returns a boolean value (true/false) whether or not the evidence collected
     * about a dependency is displayed in the report. Default is false.
     */
    public boolean isShowEvidence() {
        return showEvidence;
    }

    /**
     * Sets a boolean value (true/false) whether or not the evidence collected
     * about a dependency is displayed in the report.
     */
    public void setShowEvidence(boolean showEvidence) {
        this.showEvidence = showEvidence;
    }

    /**
     * Returns a boolean that indicates if a deepScan should be performed. This
     * may cause more false positives. Default is false.
     */
    public boolean isDeepScan() {
        return deepScan;
    }

    /**
     * Sets if a deepScan should be performed. This may cause more
     * false positives.
     */
    public void setDeepScan(boolean deepScan) {
        this.deepScan = deepScan;
    }

    /**
     * Returns the report format to be generated. Default is XML.
     */
    public ReportGenerator.Format getFormat() {
        return format;
    }

    /**
     * Sets the report format to be generated.
     */
    public void setFormat(ReportGenerator.Format format) {
        this.format = format;
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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (name == null) {
            sb.append("name = ").append("ERROR - NAME NOT SPECIFIED OR INVALID.\n");
        } else {
            sb.append("name = ").append(name).append("\n");
        }
        if (scanPath == null || scanPath.size() == 0) {
            sb.append("scanPath = ").append("ERROR - PATH NOT SPECIFIED OR INVALID.\n");
        } else {
            for (FilePath filePath: scanPath) {
                sb.append("scanPath = ").append(filePath.getRemote()).append("\n");
            }
        }
        if (outputDirectory == null) {
            sb.append("outputDirectory = ").append("ERROR - OUTPUT DIRECTORY NOT SPECIFIED OR INVALID.\n");
        } else {
            sb.append("outputDirectory = ").append(outputDirectory.getRemote()).append("\n");
        }
        if (dataDirectory == null) {
            sb.append("dataDirectory = ").append("ERROR - DATA DIRECTORY NOT SPECIFIED OR INVALID.\n");
        } else {
            sb.append("dataDirectory = ").append(dataDirectory.getRemote()).append("\n");
        }
        sb.append("showEvidence = ").append(showEvidence).append("\n");
        sb.append("deepScan = ").append(deepScan).append("\n");
        sb.append("format = ").append(format.name()).append("\n");
        sb.append("autoUpdate = ").append(autoUpdate).append("\n");
        return sb.toString();
    }

}