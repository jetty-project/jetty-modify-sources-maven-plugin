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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

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
        throws MojoExecutionException
    {
        if(skip)
        {
            getLog().debug( "EE9toEE8 skip" );
            return;
        }
        if (project.getPackaging().equals( "pom" ))
        {
            getLog().debug( "EE9toEE8 skip pom packaging" );
            return;
        }
        try {
            // project.getBuild().getOutputDirectory()
            File metaInfDirectory = new File(outputDirectory, "META-INF/services");
            // file starting with org.eclipse.jetty.ee9

            String[] files = metaInfDirectory.list((dir, name) -> StringUtils.startsWith(name, "org.eclipse.jetty.ee9"));
            for (String fileName : files) {
                File file -
            }

        }
        catch (IOException e)
        {
            throw new MojoExecutionException("fail to modify jetty sources", e);
        }

    }


    private static void changeEE9NameToEE8(NodeWithName n) {
        //org.eclipse.jetty.ee9.nested to org.eclipse.jetty.ee8.nested
        String currentName = n.getName().asString();
        if (currentName.startsWith("org.eclipse.jetty.ee9.")) {
            String newName = StringUtils.replace(currentName, "org.eclipse.jetty.ee9.", "org.eclipse.jetty.ee8.");
            n.setName(newName);
        }
        if (currentName.startsWith("jakarta.servlet.")) {
            String newName = StringUtils.replace(currentName, "jakarta.servlet.", "javax.servlet.");
            n.setName(newName);
        }
    }

    private String changeEE9TypeToEE8(String currentType) {
        //org.eclipse.jetty.ee9.nested to org.eclipse.jetty.ee8.nested

        if (currentType.startsWith("org.eclipse.jetty.ee9.")) {
            String newType = StringUtils.replace(currentType, "org.eclipse.jetty.ee9.", "org.eclipse.jetty.ee8.");
            return newType;
        }
        if (currentType.startsWith("jakarta.servlet.")) {
            String newType = StringUtils.replace(currentType, "jakarta.servlet.", "javax.servlet.");
            return newType;
        }

        return currentType;
    }

    public void setOutputDirectory(File outputDirectory) {
        this.outputDirectory = outputDirectory;
    }
}
