/*
 * This file is part of Dependency-Check Jenkins plugin.
 *
 * Dependency-Check is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * Dependency-Check is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Dependency-Check. If not, see http://www.gnu.org/licenses/.
 */
package org.jenkinsci.plugins.DependencyCheck;

import hudson.FilePath;
import org.owasp.dependencycheck.reporting.ReportGenerator;

import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;

/**
 * A container object that holds all of the configurable options to be used by
 * a DependencyCheck analysis.
 *
 * @author Steve Springett (steve.springett@owasp.org)
 */
@SuppressWarnings("unused")
public class Options implements Serializable {

    private static final long serialVersionUID = 4571161829818421072L;

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
     * Boolean value (true/false) whether or not the evidence collected
     * about a dependency is displayed in the report. Default is false.
     */
    private boolean showEvidence = false;

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
     * Specifies the verbose logging file to use
     */
    private FilePath verboseLoggingFile;

    /**
     * Specifies the data mirroring type (scheme) to use
     */
    private int dataMirroringType;

    /**
     * Specifies the CVE 1.2 modified URL
     */
    private URL cveUrl12Modified;

    /**
     * Specifies the CVE 2.0 modified URL
     */
    private URL cveUrl20Modified;

    /**
     * Specifies the CVE 1.2 base URL
     */
    private URL cveUrl12Base;

    /**
     * Specifies the CVE 2.0 base URL
     */
    private URL cveUrl20Base;

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

    /**
     * Returns the verbose logging file
     */
    public FilePath getVerboseLoggingFile() {
        return verboseLoggingFile;
    }

    /**
     * Sets whether verbose logging of the Dependency-Check engine and analyzers
     * is enabled.
     */
    public void setVerboseLoggingFile(FilePath file) {
        this.verboseLoggingFile = file;
    }

    /**
     * Returns the data mirroring type (scheme) to use where 0 = none and 1 = NIST CPE/CVE
     */
    public int getDataMirroringType() {
        return dataMirroringType;
    }

    /**
     * Sets the data mirroring type
     */
    public void setDataMirroringType(int dataMirroringType) {
        this.dataMirroringType = dataMirroringType;
    }

    /**
     * Returns the CVE 1.2 modified URL
     */
    public URL getCveUrl12Modified() {
        return cveUrl12Modified;
    }

    /**
     * Sets the CVE 1.2 modified URL
     */
    public void setCveUrl12Modified(URL url) {
        this.cveUrl12Modified = url;
    }

    /**
     * Returns the CVE 2.0 modified URL
     */
    public URL getCveUrl20Modified() {
        return cveUrl20Modified;
    }

    /**
     * Sets the CVE 2.0 modified URL
     */
    public void setCveUrl20Modified(URL url) {
        this.cveUrl20Modified = url;
    }

    /**
     * Returns the CVE 1.2 base URL
     */
    public URL getCveUrl12Base() {
        return cveUrl12Base;
    }

    /**
     * Sets the CVE 1.2 base URL
     */
    public void setCveUrl12Base(URL url) {
        this.cveUrl12Base = url;
    }

    /**
     * Returns the CVE 2.0 base URL
     */
    public URL getCveUrl20Base() {
        return cveUrl20Base;
    }

    /**
     * Sets the CVE 2.0 base URL
     */
    public void setCveUrl20Base(URL url) {
        this.cveUrl20Base = url;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (name == null) {
            sb.append(" -name = ").append("ERROR - NAME NOT SPECIFIED OR INVALID.\n");
        } else {
            sb.append(" -name = ").append(name).append("\n");
        }
        if (scanPath == null || scanPath.size() == 0) {
            sb.append(" -scanPath = ").append("ERROR - PATH NOT SPECIFIED OR INVALID.\n");
        } else {
            for (FilePath filePath: scanPath) {
                sb.append(" -scanPath = ").append(filePath.getRemote()).append("\n");
            }
        }
        if (outputDirectory == null) {
            sb.append(" -outputDirectory = ").append("ERROR - OUTPUT DIRECTORY NOT SPECIFIED OR INVALID.\n");
        } else {
            sb.append(" -outputDirectory = ").append(outputDirectory.getRemote()).append("\n");
        }
        if (dataDirectory == null) {
            sb.append(" -dataDirectory = ").append("ERROR - DATA DIRECTORY NOT SPECIFIED OR INVALID.\n");
        } else {
            sb.append(" -dataDirectory = ").append(dataDirectory.getRemote()).append("\n");
        }
        if (verboseLoggingFile != null) {
            sb.append(" -verboseLogFile = ").append(verboseLoggingFile.getRemote()).append("\n");
        }

        sb.append(" -dataMirroringType = ").append(dataMirroringType==0 ? "none" : "NIST CPE/CVE").append("\n");
        if (dataMirroringType != 0) {
            if (cveUrl12Modified == null) {
                sb.append(" -cveUrl12Modified = ").append("ERROR - CVE 1.2 MODIFIED URL NOT SPECIFIED OR INVALID.\n");
            } else {
                sb.append(" -cveUrl12Modified = ").append(cveUrl12Modified.toExternalForm()).append("\n");
            }
            if (cveUrl20Modified == null) {
                sb.append(" -cveUrl20Modified = ").append("ERROR - CVE 2.0 MODIFIED URL NOT SPECIFIED OR INVALID.\n");
            } else {
                sb.append(" -cveUrl20Modified = ").append(cveUrl20Modified.toExternalForm()).append("\n");
            }
            if (cveUrl12Base == null) {
                sb.append(" -cveUrl12Base = ").append("ERROR - CVE 1.2 BASE URL NOT SPECIFIED OR INVALID.\n");
            } else {
                sb.append(" -cveUrl12Base = ").append(cveUrl12Base.toExternalForm()).append("\n");
            }
            if (cveUrl20Base == null) {
                sb.append(" -cveUrl20Base = ").append("ERROR - CVE 2.0 BASE URL NOT SPECIFIED OR INVALID.\n");
            } else {
                sb.append(" -cveUrl20Base = ").append(cveUrl20Base.toExternalForm()).append("\n");
            }
        }

        sb.append(" -showEvidence = ").append(showEvidence).append("\n");
        sb.append(" -format = ").append(format.name()).append("\n");
        sb.append(" -autoUpdate = ").append(autoUpdate);
        return sb.toString();
    }

}