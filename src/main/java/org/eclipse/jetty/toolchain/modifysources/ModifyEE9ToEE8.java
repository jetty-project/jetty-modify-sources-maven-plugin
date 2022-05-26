package org.eclipse.jetty.toolchain.modifysources;


import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.CastExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.MethodReferenceExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.TypeExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.modules.ModuleDeclaration;
import com.github.javaparser.ast.modules.ModuleExportsDirective;
import com.github.javaparser.ast.modules.ModuleOpensDirective;
import com.github.javaparser.ast.modules.ModuleProvidesDirective;
import com.github.javaparser.ast.modules.ModuleRequiresDirective;
import com.github.javaparser.ast.modules.ModuleUsesDirective;
import com.github.javaparser.ast.nodeTypes.NodeWithName;
import com.github.javaparser.ast.nodeTypes.NodeWithType;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;
import com.github.javaparser.utils.SourceRoot;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
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
@Mojo( name = "modify-sources-ee9-to-ee8", defaultPhase = LifecyclePhase.GENERATE_SOURCES, threadSafe = true )
public class ModifyEE9ToEE8
    extends AbstractMojo
{
    /**
     * Location of the project to convert.
     */
    @Parameter( property = "sourceProjectLocation", required = true )
    private File sourceProjectLocation;

    /**
     * Location of the modified sources.
     */
    @Parameter( defaultValue = "${project.build.directory}/generated-sources/ee8", property = "outputLocation", required = true )
    private File outputDirectory;

    @Parameter( property = "jetty.modifysources.EE9toEE8.skip" )
    private boolean skip;
    /**
     * move generated sources org/eclipse/jetty/ee9 to org/eclipse/jetty/ee8
     */
    @Parameter( property = "jetty.modifysources.EE9toEE8.moveDirectoryStructure" )
    private boolean moveDirectoryStructure = true;

    @Parameter( property = "jetty.modifysources.testSources" )
    private boolean testSources = true;

    @Parameter( property = "jetty.modifysources.addToCompileSourceRoot" )
    private boolean addToCompileSourceRoot = false;

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
        if(!sourceProjectLocation.exists())
        {
            getLog().info( "EE9toEE8 sourceProjectLocation not exists" );
            return;
        }

        try
        {

            SourceRoot sourceRoot = new SourceRoot(sourceProjectLocation.toPath());
            sourceRoot.getParserConfiguration().setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_17);
            //sourceRoot.

            // Our sample is in the root of this directory, so no package name.
            List<ParseResult<CompilationUnit>> parseResults = sourceRoot.tryToParse( "" );

            Path out = outputDirectory.toPath();
            if ( Files.exists( out ) )
            {
                Files.walk(out).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
            }

            for (ParseResult<CompilationUnit> parseResult : parseResults) {
                CompilationUnit cu = parseResult.getResult().get();

                cu.accept(
                    new ModifierVisitor<Void>()
                    {

                        @Override
                        public Node visit(ImportDeclaration n, Void arg) {
                            String currentName = n.getNameAsString();
                            changeEE9NameToEE8(n);
                            return super.visit(n, arg);
                        }

                        @Override
                        public Visitable visit(PackageDeclaration n, Void arg) {
                            changeEE9NameToEE8(n);
                            return super.visit(n, arg);
                        }

                        @Override
                        public Visitable visit(com.github.javaparser.ast.body.Parameter n, Void arg) {
                            changeEE9TypeToEE8(n);
                            return super.visit(n, arg);
                        }

                        @Override
                        public Visitable visit(VariableDeclarator n, Void arg) {
                            changeEE9TypeToEE8(n);
                            return super.visit(n, arg);
                        }

                        @Override
                        public Visitable visit(VariableDeclarationExpr n, Void arg) {
                            List <VariableDeclarator> variables = n.getVariables();
                            for (VariableDeclarator variable: variables) {
                                changeEE9TypeToEE8(variable);
                            }
                            return super.visit(n, arg);
                        }

                        @Override
                        public Visitable visit(NameExpr n, Void arg) {

                            return super.visit(n, arg);
                        }

                        @Override
                        public Visitable visit(ExpressionStmt n, Void arg) {
                            return super.visit(n, arg);
                        }

                        @Override
                        public Visitable visit(FieldAccessExpr n, Void arg) {
                            return super.visit(n, arg);
                        }

                        @Override
                        public Visitable visit(MethodCallExpr n, Void arg) {
                            if(StringUtils.startsWith(n.toString(), "org.eclipse.jetty.ee9") && n.getScope().isPresent()) {
                                // org.eclipse.jetty.ee9.nested.Response.unwrap(event.getSuppliedResponse())
                                String fullString = n.toString();
                                Expression expression = n.getScope().get();
                                if(expression.isFieldAccessExpr()) {
                                    FieldAccessExpr fieldAccessExpr = expression.asFieldAccessExpr();
                                    // Response
                                    String classSimpleName = fieldAccessExpr.getName().asString();
                                    // org.eclipse.jetty.ee9.nested
                                    String ee9PackageName = StringUtils.substringBefore(fullString, "." + classSimpleName);
                                    // org.eclipse.jetty.ee8.nested
                                    String ee8PackageName = "org.eclipse.jetty.ee8." + ee9PackageName.substring("org.eclipse.jetty.ee9.".length());
                                    NameExpr nameExpr = new NameExpr(ee8PackageName + "." + classSimpleName);
                                    n.setScope(nameExpr);
                                }

                            }
                            return super.visit(n, arg);
                        }

                        @Override
                        public Visitable visit(CastExpr n, Void arg) {
                            changeEE9TypeToEE8(n);
                            return super.visit(n, arg);
                        }



                        @Override
                        public Visitable visit(ModuleDeclaration n, Void arg) {
                            changeEE9NameToEE8(n);
                            return super.visit(n, arg);
                        }

                        @Override
                        public Visitable visit(ModuleExportsDirective n, Void arg) {
                            changeEE9NameToEE8(n);
                            return super.visit(n, arg);
                        }

                        @Override
                        public Visitable visit(ModuleRequiresDirective n, Void arg) {
                            changeEE9NameToEE8(n);
                            return super.visit(n, arg);
                        }

                        @Override
                        public Visitable visit(ModuleProvidesDirective n, Void arg) {
                            changeEE9NameToEE8(n);
                            return super.visit(n, arg);
                        }

                        @Override
                        public Visitable visit(ModuleUsesDirective n, Void arg) {
                            changeEE9NameToEE8(n);
                            return super.visit(n, arg);
                        }

                        @Override
                        public Visitable visit(ModuleOpensDirective n, Void arg) {
                            changeEE9NameToEE8(n);
                            return super.visit(n, arg);
                        }

                        @Override
                        public Visitable visit(CompilationUnit cu, Void arg) {
                            return super.visit(cu, arg);
                        }
                    }, null );

            }

            Files.createDirectories(out);
            sourceRoot.saveAll(out);

            if (moveDirectoryStructure) {
                FileUtils.copyDirectory(new File(outputDirectory, "org/eclipse/jetty/ee9"),
                        new File(outputDirectory, "org/eclipse/jetty/ee8"));
                FileUtils.deleteDirectory(new File(outputDirectory, "org/eclipse/jetty/ee9"));
            }

            if (addToCompileSourceRoot) {
                if (testSources) {
                    project.getTestCompileSourceRoots().add(outputDirectory.getAbsolutePath());
                } else {
                    project.getCompileSourceRoots().add(outputDirectory.getAbsolutePath());
                }
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
            String newName = "org.eclipse.jetty.ee8." + currentName.substring("org.eclipse.jetty.ee9.".length());
            n.setName(newName);
        }
        if (currentName.startsWith("jakarta.servlet.")) {
            String newName = "javax.servlet." + currentName.substring("jakarta.servlet.".length());
            n.setName(newName);
        }
    }

    private static void changeEE9TypeToEE8(NodeWithType n) {
        //org.eclipse.jetty.ee9.nested to org.eclipse.jetty.ee8.nested
        String currentType =n.getTypeAsString();
        if (currentType.startsWith("org.eclipse.jetty.ee9.")) {
            String newType = "org.eclipse.jetty.ee8." + currentType.substring("org.eclipse.jetty.ee9.".length());
            n.setType(newType);
        }
        if (currentType.startsWith("jakarta.servlet.")) {
            String newType = "javax.servlet." + currentType.substring("jakarta.servlet.".length());
            n.setType(newType);
        }
    }


    protected void setSourceProjectLocation(File sourceProjectLocation) {
        this.sourceProjectLocation = sourceProjectLocation;
    }

    protected void setMoveDirectoryStructure(boolean moveDirectoryStructure) {
        this.moveDirectoryStructure = moveDirectoryStructure;
    }
}
