package org.eclipse.jetty.toolchain.modifysources;


import org.apache.maven.plugin.testing.MojoRule;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ModifyEE9ToEE8ServiceLoaderFileTest
{
    @Rule
    public MojoRule rule = new MojoRule()
    {
        @Override
        protected void before()
            throws Throwable
        {
//            FileUtils.copyDirectory(new File("src/test/resources/project-modify/"),
//                    new File());
        }

        @Override
        protected void after()
        {
        }
    };


    @Test
    public void testChangeToEE8()
        throws Exception
    {

        File pom = new File( "target/test-classes/project-modify/" );
        assertNotNull( pom );
        assertTrue( pom.exists() );

        ModifyEE9ToEE8ServiceLoaderFiles mojo =
            (ModifyEE9ToEE8ServiceLoaderFiles) rule.lookupConfiguredMojo( pom, "modify-service-loader-files-ee9-to-ee8" );
        assertNotNull( mojo );
        mojo.setOutputDirectory(new File("target/test-classes/project-modify/src/main/resources"));

        mojo.execute();

        File outputDirectory = (File) rule.getVariableValueFromObject( mojo, "outputDirectory" );
        assertNotNull( outputDirectory );
        assertTrue( outputDirectory.exists() );

        Path modified = Paths.get(outputDirectory.toString(), "META-INF", "services", "org.eclipse.jetty.ee9.webapp.Configuration");
        assertTrue(Files.exists(modified));

        String modifiedContent = new String(Files.readAllBytes(modified));
        assertFalse(modifiedContent.contains("org.eclipse.jetty.ee9"));


    }

}

