package org.eclipse.jetty.toolchain.modifysources;


import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.expr.CastExpr;
import com.github.javaparser.ast.expr.ClassExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.modules.ModuleDeclaration;
import com.github.javaparser.ast.modules.ModuleExportsDirective;
import com.github.javaparser.ast.modules.ModuleOpensDirective;
import com.github.javaparser.ast.modules.ModuleProvidesDirective;
import com.github.javaparser.ast.modules.ModuleRequiresDirective;
import com.github.javaparser.ast.modules.ModuleUsesDirective;
import com.github.javaparser.ast.nodeTypes.NodeWithName;
import com.github.javaparser.ast.nodeTypes.NodeWithType;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;
import com.github.javaparser.utils.SourceRoot;
import org.apache.commons.io.FileUtils;
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
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.eclipse.jetty.toolchain.modifysources.ModifyEE9ToEE8.changeEE9TypeToEE8;

/**
 * Modify sources from EE9 project to be compiled with EE8 dependencies
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
                        StringUtils.startsWith(name, "jakarta.servlet"));
                for (String fileName : files) {
                    File file = new File(metaInfDirectory, fileName);
                    String newFileName = changeEE9TypeToEE8(fileName);
                    File newFile = new File(metaInfDirectory, newFileName);
                    List<String> newContent = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8).
                            stream().map(s -> changeEE9TypeToEE8(s)).collect(Collectors.toList());
                    Files.write(newFile.toPath(), newContent, StandardCharsets.UTF_8);
                    Files.delete(file.toPath());
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
