package org.eclipse.jetty.toolchain.modifysources;


import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;
import com.github.javaparser.utils.SourceRoot;
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
 * Remove if block starting with if(LOG.isDebugEnabled() from java sources
 */
@Mojo( name = "remove-log-enabled", defaultPhase = LifecyclePhase.GENERATE_SOURCES, threadSafe = true )
public class RemoveLogEnabledStatement
    extends AbstractMojo
{
    /**
     * Location of the sources.
     */
    @Parameter( defaultValue = "${project.basedir}/src/main/java", property = "sourcesLocation", required = true )
    private File sourcesLocation;

    /**
     * Location of the modified sources.
     */
    @Parameter( defaultValue = "${project.build.directory}/modified-sources", property = "outputLocation", required = true )
    private File outputDirectory;

    @Parameter( property = "modifysources.jetty.skip" )
    private boolean skip;

    /**
     * Maven Project.
     */
    @Component
    protected MavenProject project;


    public void execute()
        throws MojoExecutionException
    {
        if(skip)
        {
            getLog().debug( "remove log enabled statement sources skip" );
            return;
        }
        if (project.getPackaging().equals( "pom" ))
        {
            getLog().debug( "remove log enabled statement skip pom packaging" );
            return;
        }
        if(!sourcesLocation.exists())
        {
            getLog().debug( "remove log enabled statement sources location not exists" );
            return;
        }

        try
        {

            SourceRoot sourceRoot = new SourceRoot( sourcesLocation.toPath() );

            // Our sample is in the root of this directory, so no package name.
            List<ParseResult<CompilationUnit>> parseResults = sourceRoot.tryToParse( "" );

            Path out = outputDirectory.toPath();
            if ( Files.exists( out ) )
            {
                Files.walk( out ).sorted( Comparator.reverseOrder() ).map( Path::toFile ).forEach( File::delete );
            }

            parseResults.stream().forEach(
                compilationUnitParseResult -> compilationUnitParseResult.getResult().get().accept(
                    new ModifierVisitor<Void>()
                    {
                        @Override
                        public Visitable visit( IfStmt n, Void arg )
                        {
                            if ( n.getCondition().toString().endsWith( "isDebugEnabled()" ) )
                            {
                                n.remove();
                            }
                            return super.visit( n, arg );
                        }

                        //TODO configurable option
                        @Override
                        public Visitable visit( ExpressionStmt n, Void arg )
                        {
                            if ( n.getExpression().toString().contains( ".debug(" ))
//                            if ( n.getExpression().toString().contains( ".warn(" ) //
//                                || n.getExpression().toString().contains( ".info(" ) //
//                                || n.getExpression().toString().contains( ".ignore(" ) )
                            {
                                n.remove();
                                return null;
                            }
                            else
                            {
                                return super.visit( n, arg );
                            }
                        }
                    }, null ) );

            Files.createDirectories( out );
            sourceRoot.saveAll( out );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "fail to modifyt jetty sources", e );
        }


    }
}
