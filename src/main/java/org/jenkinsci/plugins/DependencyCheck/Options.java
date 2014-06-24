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
     * Specifies the suppression file to use
     */
    private Serializable suppressionFile;

    /**
     * Specifies the file extensions to be treated a ZIP
     */
    private String zipExtensions;

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
     * Specifies the server (hostname/IP) of the proxy server
     */
    private String proxyServer;

    /**
     * Specifies the port number to the proxy server
     */
    private int proxyPort;

    /**
     * Specifies the username to use to authenticate through the proxy server
     */
    private String proxyUsername;

    /**
     * Specifies the password to use to authenticate through the proxy server
     */
    private String proxyPassword;

    /**
     * Specifies if the scan path is solely populated by Maven artifacts
     */
    private boolean useMavenArtifactsScanPath;

    /**
     * Specifies if the Jar analyzer is enabled
     */
    private boolean jarAnalyzerEnabled;

    /**
     * Specifies if the Javascript analyzer is enabled
     */
    private boolean javascriptAnalyzerEnabled;

    /**
     * Specifies if the Archive analyzer is enabled
     */
    private boolean archiveAnalyzerEnabled;

    /**
     * Specifies if the Assembly analyzer is enabled
     */
    private boolean assemblyAnalyzerEnabled;

    /**
     * Specifies if the NuSpec analyzer is enabled
     */
    private boolean nuspecAnalyzerEnabled;

    /**
     * Specifies if the Nexus analyzer is enabled
     */
    private boolean nexusAnalyzerEnabled;

    /**
     * Specifies the Nexus URL to use if enabled
     */
    private URL nexusUrl;

    /**
     * Specifies if the Nexus analyzer should bypass any proxy defined in Jenkins
     */
    private boolean nexusProxyBypass;

    /**
     * Specifies the full path and filename to the Mono binary
     */
    private FilePath monoPath;

    /**
     * Specifies the full path to the temporary directory
     */
    private FilePath tempPath;

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
     * Returns the suppression file
     */
    public Serializable getSuppressionFile() {
        return suppressionFile;
    }

    /**
     * Sets the suppression file to use
     */
    public void setSuppressionFile(Serializable file) {
        this.suppressionFile = file;
    }

    /**
     * Returns the suppression file
     */
    public URL getSuppressionUrl () {
        if (suppressionFile instanceof URL) {
            return (URL)suppressionFile;
        } else {
            return null;
        }
    }

    /**
     * Returns the suppression file
     */
    public FilePath getSuppressionFilePath () {
        if (suppressionFile instanceof FilePath) {
            return (FilePath)suppressionFile;
        } else {
            return null;
        }
    }

    /**
     * Returns the zip extensions
     */
    public String getZipExtensions() {
        return zipExtensions;
    }

    /**
     * Sets the zip extensions - must be comma separated
     */
    public void setZipExtensions(String extensions) {
        this.zipExtensions = extensions;
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

    /**
     * Returns the server (hostname/IP) of the proxy server
     */
    public String getProxyServer() {
        return proxyServer;
    }

    /**
     * Sets the server (hostname/IP) of the proxy server
     */
    public void setProxyServer(String proxyServer) {
        this.proxyServer = proxyServer;
    }

    /**
     * Returns the port number to the proxy server
     */
    public int getProxyPort() {
        return proxyPort;
    }

    /**
     * Sets the port number to the proxy server
     */
    public void setProxyPort(int proxyPort) {
        this.proxyPort = proxyPort;
    }

    /**
     * Returns the username to use to authenticate through the proxy server
     */
    public String getProxyUsername() {
        return proxyUsername;
    }

    /**
     * Sets the username to use to authenticate through the proxy server
     */
    public void setProxyUsername(String proxyUsername) {
        this.proxyUsername = proxyUsername;
    }

    /**
     * Returns the password to use to authenticate through the proxy server
     */
    public String getProxyPassword() {
        return proxyPassword;
    }

    /**
     * Sets the password to use to authenticate through the proxy server
     */
    public void setProxyPassword(String proxyPassword) {
        this.proxyPassword = proxyPassword;
    }

    /**
     * Returns if the scan path is solely populated by Maven artifacts
     */
    public boolean getUseMavenArtifactsScanPath() {
        return useMavenArtifactsScanPath;
    }

    /**
     * Sets if the scan path is solely populated by Maven artifacts
     */
    public void setUseMavenArtifactsScanPath(boolean useMavenArtifactsScanPath) {
        this.useMavenArtifactsScanPath = useMavenArtifactsScanPath;
    }

    /**
     * Returns if the Jar analyzer is enabled or not
     */
    public boolean isJarAnalyzerEnabled() {
        return jarAnalyzerEnabled;
    }

    /**
     * Sets if the Jar analyzer is enabled or not
     */
    public void setJarAnalyzerEnabled(boolean jarAnalyzerEnabled) {
        this.jarAnalyzerEnabled = jarAnalyzerEnabled;
    }

    /**
     * Returns if the Javascript analyzer is enabled or not
     */
    public boolean isJavascriptAnalyzerEnabled() {
        return javascriptAnalyzerEnabled;
    }

    /**
     * Sets if the Javascript analyzer is enabled or not
     */
    public void setJavascriptAnalyzerEnabled(boolean javascriptAnalyzerEnabled) {
        this.javascriptAnalyzerEnabled = javascriptAnalyzerEnabled;
    }

    /**
     * Returns if the Archive analyzer is enabled or not
     */
    public boolean isArchiveAnalyzerEnabled() {
        return archiveAnalyzerEnabled;
    }

    /**
     * Sets if the Archive analyzer is enabled or not
     */
    public void setArchiveAnalyzerEnabled(boolean archiveAnalyzerEnabled) {
        this.archiveAnalyzerEnabled = archiveAnalyzerEnabled;
    }

    /**
     * Returns if the Assembly analyzer is enabled or not
     */
    public boolean isAssemblyAnalyzerEnabled() {
        return assemblyAnalyzerEnabled;
    }

    /**
     * Sets if the Assembly analyzer is enabled or not
     */
    public void setAssemblyAnalyzerEnabled(boolean assemblyAnalyzerEnabled) {
        this.assemblyAnalyzerEnabled = assemblyAnalyzerEnabled;
    }

    /**
     * Returns if the NuSpec analyzer is enabled or not
     */
    public boolean isNuspecAnalyzerEnabled() {
        return nuspecAnalyzerEnabled;
    }

    /**
     * Sets if the NuSpec analyzer is enabled or not
     */
    public void setNuspecAnalyzerEnabled(boolean nuspecAnalyzerEnabled) {
        this.nuspecAnalyzerEnabled = nuspecAnalyzerEnabled;
    }

    /**
     * Returns if the Nexus analyzer is enabled or not
     */
    public boolean isNexusAnalyzerEnabled() {
        return nexusAnalyzerEnabled;
    }

    /**
     * Sets if the Nexus analyzer is enabled or not
     */
    public void setNexusAnalyzerEnabled(boolean nexusAnalyzerEnabled) {
        this.nexusAnalyzerEnabled = nexusAnalyzerEnabled;
    }

    /**
     * Returns the non-default Nexus URL to use
     */
    public URL getNexusUrl() {
        return nexusUrl;
    }

    /**
     * Specifies the non-default Nexus URL to use
     */
    public void setNexusUrl(URL nexusUrl) {
        this.nexusUrl = nexusUrl;
    }

    /**
     * Returns if the Nexus analyzer should bypass any proxy defined in Jenkins
     */
    public boolean isNexusProxyBypassed() {
        return nexusProxyBypass;
    }

    /**
     * Specifies if the Nexus analyzer should bypass any proxy defined in Jenkins
     */
    public void setNexusProxyBypassed(boolean nexusProxyBypass) {
        this.nexusProxyBypass = nexusProxyBypass;
    }

    /**
     * Returns the full path and filename to the Mono binary
     */
    public FilePath getMonoPath() {
        return monoPath;
    }

    /**
     * Specifies the full path and filename to the mono binary
     */
    public void setMonoPath(FilePath monoPath) {
        this.monoPath = monoPath;
    }

    /**
     * Returns the full path of the temporary directory
     */
    public FilePath getTempPath() {
        return tempPath;
    }

    /**
     * Specifies the full path of the temporary directory
     */
    public void setTempPath(FilePath tempPath) {
        this.tempPath = tempPath;
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
        if (getSuppressionFilePath() != null) {
            sb.append(" -suppressionFile = ").append(getSuppressionFilePath().getRemote()).append("\n");
        }
        if (getSuppressionUrl() != null) {
            sb.append(" -suppressionFile = ").append(getSuppressionUrl()).append("\n");
        }
        if (zipExtensions != null) {
            sb.append(" -zipExtensions = ").append(zipExtensions).append("\n");
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

        if (proxyServer != null) {
            sb.append(" -proxyServer = ").append(proxyServer).append("\n");
            sb.append(" -proxyPort = ").append(proxyPort).append("\n");
        }
        if (proxyUsername != null) {
            sb.append(" -proxyUsername = ").append(proxyUsername).append("\n");
        }
        if (proxyPassword != null) {
            sb.append(" -proxyPassword = ").append("********").append("\n");
        }

        sb.append(" -useMavenArtifactsScanPath = ").append(useMavenArtifactsScanPath).append("\n");

        sb.append(" -jarAnalyzerEnabled = ").append(jarAnalyzerEnabled).append("\n");
        sb.append(" -javascriptAnalyzerEnabled = ").append(javascriptAnalyzerEnabled).append("\n");
        sb.append(" -archiveAnalyzerEnabled = ").append(archiveAnalyzerEnabled).append("\n");
        sb.append(" -assemblyAnalyzerEnabled = ").append(assemblyAnalyzerEnabled).append("\n");
        sb.append(" -nuspecAnalyzerEnabled = ").append(nuspecAnalyzerEnabled).append("\n");
        sb.append(" -nexusAnalyzerEnabled = ").append(nexusAnalyzerEnabled).append("\n");
        if (nexusAnalyzerEnabled && nexusUrl != null) {
            sb.append(" -nexusUrl = ").append(nexusUrl.toExternalForm()).append("\n");
        }
        if (nexusAnalyzerEnabled) {
            sb.append(" -nexusProxyBypassed = ").append(nexusProxyBypass).append("\n");
        }
        if (monoPath != null) {
            sb.append(" -monoPath = ").append(monoPath.getRemote()).append("\n");
        }
        if (tempPath != null) {
            sb.append(" -tempPath = ").append(tempPath.getRemote()).append("\n");
        }
        sb.append(" -showEvidence = ").append(showEvidence).append("\n");
        sb.append(" -format = ").append(format.name()).append("\n");
        sb.append(" -autoUpdate = ").append(autoUpdate);
        return sb.toString();
    }

}