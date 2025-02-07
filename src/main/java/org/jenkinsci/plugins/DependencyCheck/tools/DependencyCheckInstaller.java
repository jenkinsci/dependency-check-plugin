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

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPOutputStream;

import org.jenkinsci.plugins.DependencyCheck.Messages;
import org.kohsuke.stapler.DataBoundConstructor;

import edu.umd.cs.findbugs.annotations.NonNull;
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
            installable = (Installable) ((NodeSpecific<?>) installable).forNode(node, log);
        }

        if (!isUpToDate(expected, installable)) {
            FilePath cache = getLocalCacheFile(node);
            boolean skipInstall = false;
            if (!DISABLE_CACHE && cache.exists()) {
                if (isCacheValid(cache)) {
                    log.getLogger().println(Messages.Installer_installFromCache(cache, expected, node.getDisplayName()));
                    try {
                        cache.untar(expected, TarCompression.GZIP);
                        skipInstall = true;
                    } catch (IOException e) {
                        log.error("Use of caches failed: " + e.getMessage());
                    }
                } else {
                    log.getLogger().println(Messages.Installer_invalidMD5Cache(cache.getRemote()));
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

    private void buildCache(FilePath expected, FilePath cache) throws IOException, InterruptedException {
        // update the local cache on master
        // download to a temporary file and rename it in to handle concurrency (between slave nodes) and failure correctly
        FilePath cacheParent = cache.getParent();
        if (cacheParent == null) {
            return;
        }
        cacheParent.mkdirs(); // ensure cache folder exists
        FilePath tmp = cacheParent.createTempFile("cache-", null);
        try {
            try (OutputStream out = new GZIPOutputStream(tmp.write())) {
                // workaround to not store current folder as root folder in the archive
                // this prevent issue when tool name is renamed
                expected.tar(out, "**");
            }
            cache.delete();
            tmp.renameTo(cache);
            cacheParent.child(cache.getBaseName() + ".md5").write(cache.digest(), StandardCharsets.UTF_8.name());
        } finally {
            tmp.delete();
        }
    }

    @NonNull
    private FilePath getLocalCacheFile(@NonNull Node node) throws IOException {
        Platform platform = Platform.of(node);
        // we store cache as tar.gz to preserve symlink
        return Jenkins.get().getRootPath() //
                .child("caches") //
                .child("dependency-check") //
                .child(platform.toString()) //
                .child(id + ".tar.gz");
    }

    // check cached file using MD5 due JENKINS-71916
    private boolean isCacheValid(@NonNull FilePath cache) throws IOException, InterruptedException {
        FilePath cacheParent = cache.getParent();
        if (cacheParent == null) {
            return false;
        }
        FilePath md5Cache = cacheParent.child(cache.getBaseName() + ".md5");
        if (md5Cache.exists()) {
            String md5 = cache.digest();
            return md5.equals(md5Cache.readToString());
        }
        return false;
    }

    @Extension
    public static final class DescriptorImpl extends DownloadFromUrlInstaller.DescriptorImpl<DependencyCheckInstaller> {

        @Override
        public String getDisplayName() {
            return Messages.Installer_displayName();
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
