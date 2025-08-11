//
// ========================================================================
// Copyright (c) 1995 Mort Bay Consulting Pty Ltd and others.
//
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v. 2.0 which is available at
// https://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
// which is available at https://www.apache.org/licenses/LICENSE-2.0.
//
// SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
// ========================================================================
//

package org.eclipse.jetty.toolchain.modifysources;

import com.google.inject.Singleton;
import org.apache.commons.text.StringSubstitutor;
import org.apache.commons.text.lookup.StringLookup;
import org.apache.maven.model.Dependency;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.filtering.DefaultMavenFileFilter;
import org.apache.maven.shared.filtering.DefaultMavenResourcesFiltering;
import org.apache.maven.shared.filtering.FilterWrapper;
import org.apache.maven.shared.filtering.MavenFileFilter;
import org.apache.maven.shared.filtering.MavenFilteringException;
import org.apache.maven.shared.filtering.MavenResourcesExecution;
import org.apache.maven.shared.filtering.MavenResourcesFiltering;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.plexus.build.incremental.BuildContext;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Singleton
@Named("mod-files")
public class ModFilesFiltering extends DefaultMavenResourcesFiltering implements MavenResourcesFiltering  {

    private static final Logger LOGGER = LoggerFactory.getLogger( DefaultMavenResourcesFiltering.class );

    private static ThreadLocal<MavenProject> CURRENT_PROJECT = new ThreadLocal<>();

    @Override
    public List<String> getDefaultNonFilteredFileExtensions() {
        return Collections.emptyList();
    }

    @Inject
    public ModFilesFiltering(MavenFileFilter mavenFileFilter, BuildContext buildContext) {
        super(new JettyModFilesFileFilter(buildContext), buildContext);
    }

    @Override
    public void filterResources(MavenResourcesExecution mavenResourcesExecution) throws MavenFilteringException {
        CURRENT_PROJECT.set(mavenResourcesExecution.getMavenProject());
        super.filterResources(mavenResourcesExecution);
    }

    public static class JettyModFilesFileFilter extends DefaultMavenFileFilter implements MavenFileFilter {

        private BuildContext buildContext;

        @Inject
        public JettyModFilesFileFilter(BuildContext buildContext) {
            super(buildContext);
            this.buildContext = buildContext;
        }

        @Override
        public void copyFile(File from, final File to, boolean filtering, List<FilterWrapper> filterWrappers,
                             String encoding, boolean overwrite )
                throws MavenFilteringException {

            MavenProject mavenProject = CURRENT_PROJECT.get();

            try {
                // not looking at non filtered files
                if (filtering && Files.exists(from.toPath())) {
                    // well it is definitely not the best option to read the full content but shouldn't be too big files
                    String content = Files.readString(from.toPath());
                    StringLookup stringLookup = s -> {
                        if(s.contains(":")) {
                            // we have groupId:artifactId so let's get the version
                            String[] parts = s.split(":");
                            String groupId = parts[0];
                            String artifactId = parts[1];
                            Optional<Dependency> dependency = findDependency(groupId, artifactId, mavenProject.getDependencies());
                            if (dependency.isPresent()) {
                                return dependencyToJarName(dependency.get());
                            }
                            dependency = findDependency(groupId, artifactId, mavenProject.getDependencyManagement().getDependencies());
                            return dependencyToJarName(dependency.orElseThrow(() -> new NullPointerException("cannot find dependency " + groupId + ":" + artifactId + " for file " + from)));
                        }
                        return mavenProject.getProperties().getProperty(s);
                    };
                    StringSubstitutor stringSubstitutor = new StringSubstitutor(stringLookup , "@", "@", '\\');
                    Files.writeString(to.toPath(), stringSubstitutor.replace(content), StandardCharsets.UTF_8);
                    buildContext.refresh( to );
                }
            } catch (IOException e) {
                LOGGER.error("error copying file {} to {}", from, to);
                throw new MavenFilteringException(e.getMessage(), e);
            }
        }

    }

    private static Optional<Dependency> findDependency(String groupId, String artifactId, List<Dependency> dependencies) {
        return dependencies.stream()
                .filter(dependency -> groupId.equals(dependency.getGroupId()) && artifactId.equals(dependency.getArtifactId())).findFirst();
    }

    private static String dependencyToJarName(Dependency dependency) {
        return dependency.getArtifactId() + "-" + dependency.getVersion() + ".jar";
    }

}
