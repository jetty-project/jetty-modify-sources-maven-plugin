package org.eclipse.jetty.toolchain.modifysources;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.eclipse.jetty.toolchain.modifysources.ModifyEE9ToEE8.changeEE9TypeToEE8;

/**
 * Modify services files from EE9 project to be compiled with EE8 dependencies
 */
@Mojo( name = "modify-service-loader-files-ee9-to-ee8", defaultPhase = LifecyclePhase.PROCESS_RESOURCES, threadSafe = true )
public class ModifyEE9ToEE8ServiceLoaderFiles
    extends AbstractMojo
{

    @Parameter( property = "jetty.modifysources.servicesloader.files.skip" )
    private boolean skip;

    @Parameter( defaultValue = "${project.build.outputDirectory}")
    private File outputDirectory;

    /**
     * Maven Project.
     */
    @Parameter( defaultValue = "${project}", readonly = true )
    protected MavenProject project;

    /**
     * extra file names to change content
     */
    @Parameter
    private List<String> extraFileNames = new ArrayList<>();

    public void execute()
        throws MojoExecutionException {
        if (skip) {
            getLog().debug("EE9toEE8 skip");
            return;
        }
        if (project.getPackaging().equals("pom")) {
            getLog().debug("EE9toEE8 skip pom packaging");
            return;
        }
        try {
            // project.getBuild().getOutputDirectory()
            File metaInfDirectory = new File(outputDirectory, "META-INF/services");
            // file starting with org.eclipse.jetty.ee9 or jakarta.servlet
            {
                String[] files = metaInfDirectory.list((dir, name) -> StringUtils.startsWith(name, "org.eclipse.jetty.ee9") ||
                        StringUtils.startsWith(name, "jakarta.") || extraFileNames.contains(name));
                if (files==null) {
                    return;
                }
                for (String fileName : files) {
                    File file = new File(metaInfDirectory, fileName);
                    String newFileName = changeEE9TypeToEE8(fileName);
                    File newFile = new File(metaInfDirectory, newFileName == null ? fileName : newFileName);
                    List<String> newContent = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8).
                            stream().map(ModifyEE9ToEE8::changeEE9TypeToEE8).collect(Collectors.toList());
                    Files.write(newFile.toPath(), newContent, StandardCharsets.UTF_8);
                    if (newFileName != null) {
                        Files.delete(file.toPath());
                    }
                }
            }



        } catch (IOException e) {
            throw new MojoExecutionException("fail to modify jetty sources", e);
        }

    }

    public void setOutputDirectory(File outputDirectory) {
        this.outputDirectory = outputDirectory;
    }
}
