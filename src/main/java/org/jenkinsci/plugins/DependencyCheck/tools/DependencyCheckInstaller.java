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
package org.jenkinsci.plugins.DependencyCheck.tools;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.io.input.CountingInputStream;
import org.jenkinsci.plugins.DependencyCheck.Messages;
import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Extension;
import hudson.FilePath;
import hudson.FilePath.TarCompression;
import hudson.model.Node;
import hudson.model.TaskListener;
import hudson.slaves.NodeSpecific;
import hudson.tools.DownloadFromUrlInstaller;
import hudson.tools.ToolInstallation;
import jenkins.model.Jenkins;

/**
 * Download and installs Dependency-Check CLI.
 *
 * @author Steve Springett (steve.springett@owasp.org)
 * @since 5.0.0
 */
public class DependencyCheckInstaller extends DownloadFromUrlInstaller {

    private static final boolean DISABLE_CACHE = Boolean.getBoolean(DependencyCheckInstaller.class.getName() + ".cache.disable");

    @DataBoundConstructor
    public DependencyCheckInstaller(String id) {
        super(id);
    }

    @Override
    public FilePath performInstallation(ToolInstallation tool, Node node, TaskListener log) throws IOException, InterruptedException {
        FilePath expected = preferredLocation(tool, node);

        Installable installable = getInstallable();
        if (installable == null) {
            log.getLogger().println("Invalid tool ID " + id);
            return expected;
        }

        if (installable instanceof NodeSpecific) {
            installable = (Installable) ((NodeSpecific) installable).forNode(node, log);
        }

        if (!isUpToDate(expected, installable)) {
            File cache = getLocalCacheFile(installable, node);
            boolean skipInstall = false;
            if (!DISABLE_CACHE && cache.exists()) {
                log.getLogger().println(Messages.Installer_installFromCache(cache, expected, node.getDisplayName()));
                try {
                    restoreCache(expected, cache);
                    skipInstall = true;
                } catch (IOException e) {
                    log.error("Use of caches failed: " + e.getMessage());
                }
            }
            if (!skipInstall) {
                // perform the default logic operations
                expected = super.performInstallation(tool, node, log);
                if (!DISABLE_CACHE) {
                    buildCache(expected, cache);
                }
            }
        }
        return expected;
    }

    private void buildCache(FilePath expected, File cache) throws IOException, InterruptedException {
        // update the local cache on master
        // download to a temporary file and rename it in to handle concurrency and failure correctly,
        Path tmp = new File(cache.getPath() + ".tmp").toPath();
        try {
            Path tmpParent = tmp.getParent();
            if (tmpParent != null) {
                Files.createDirectories(tmpParent);
            }
            try (OutputStream out = new GzipCompressorOutputStream(Files.newOutputStream(tmp))) {
                // workaround to not store current folder as root folder in the archive
                // this prevent issue when tool name is renamed
                expected.tar(out, "**");
            }
            Files.move(tmp, cache.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(tmp);
        }
    }

    private File getLocalCacheFile(Installable installable, Node node) throws DetectionFailedException {
        Platform platform = Platform.of(node);
        // we store cache as tar.gz to preserve symlink
        return new File(Jenkins.get().getRootPath() //
                .child("caches") //
                .child("dependency-check") //
                .child(platform.toString()) //
                .child(id + ".tar.gz") //
                .getRemote());
    }

    private void restoreCache(FilePath expected, File cache) throws IOException, InterruptedException {
        try (InputStream in = cache.toURI().toURL().openStream()) {
            CountingInputStream cis = new CountingInputStream(in);
            try {
                Objects.requireNonNull(expected).untarFrom(cis, TarCompression.GZIP);
            } catch (IOException e) {
                throw new IOException(Messages.Installer_failedToUnpack(cache.toURI().toURL(), cis.getByteCount()), e);
            }
        }
    }

    @Extension
    public static final class DescriptorImpl extends DownloadFromUrlInstaller.DescriptorImpl<DependencyCheckInstaller> {

        @Override
        public String getDisplayName() {
            return Messages.Installation_displayName();
        }

        @Override
        public String getId() {
            // For backward compatibility when I will rename package to respect Java naming convention
            return "org.jenkinsci.plugins.DependencyCheck.tools.DependencyCheckInstaller";
        }

        @Override
        public boolean isApplicable(Class<? extends ToolInstallation> toolType) {
            return toolType == DependencyCheckInstallation.class;
        }
    }
}
