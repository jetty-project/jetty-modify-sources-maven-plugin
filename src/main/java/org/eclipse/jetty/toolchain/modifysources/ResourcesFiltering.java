package org.eclipse.jetty.toolchain.modifysources;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
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
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

@Singleton
@Named("ee9-to-ee8")
public class ResourcesFiltering extends DefaultMavenResourcesFiltering implements MavenResourcesFiltering  {

    private static final Logger LOGGER = LoggerFactory.getLogger( DefaultMavenResourcesFiltering.class );

    @Override
    public List<String> getDefaultNonFilteredFileExtensions() {
        return Collections.emptyList();
    }

    @Inject
    public ResourcesFiltering(MavenFileFilter mavenFileFilter, BuildContext buildContext) {
        super(new JettyMavenFileFilter(buildContext), buildContext);
    }

    @Override
    public boolean filteredFileExtension(String fileName, List<String> userNonFilteredFileExtensions) {
        String ext = getExtension(fileName);
        return ext == null? false : Arrays.asList("xml", "properties", "txt").contains(ext);
    }

    private static String getExtension( String fileName )
    {
        String rawExt = FilenameUtils.getExtension(fileName);
        return rawExt == null ? null : rawExt.toLowerCase(Locale.ROOT);
    }

    @Override
    public void filterResources(MavenResourcesExecution mavenResourcesExecution) throws MavenFilteringException {
        super.filterResources(mavenResourcesExecution);
    }

    public static class JettyMavenFileFilter extends DefaultMavenFileFilter implements MavenFileFilter {

        private BuildContext buildContext;

        @Inject
        public JettyMavenFileFilter(BuildContext buildContext) {
            super(buildContext);
            this.buildContext = buildContext;
        }

        @Override
        public void copyFile(File from, final File to, boolean filtering, List<FilterWrapper> filterWrappers,
                             String encoding, boolean overwrite )
                throws MavenFilteringException {
            try {
                // not looking at non filtered files
                if (filtering && Files.exists(from.toPath())) {
                    // well it is definitely not the best option to read the full content but shouldn't be too big files
                    String content = Files.readString(from.toPath());
                    if(StringUtils.contains(content, "jakarta.")) {
                        content = StringUtils.replace(content, "jakarta.", "javax.");
                    }
                    if(StringUtils.contains(content, "jakarta/")) {
                        content = StringUtils.replace(content, "jakarta/", "javax/");
                    }
                    if(StringUtils.contains(content, "ee9")) {
                        content = StringUtils.replace(content, "ee9", "ee8");
                    }
                    Files.writeString(to.toPath(), content, StandardCharsets.UTF_8);
                    buildContext.refresh( to );
                }
            } catch (IOException e) {
                LOGGER.error("error copying file {} to {}", from, to);
                throw new MavenFilteringException(e.getMessage(), e);
            }
        }

    }

}
