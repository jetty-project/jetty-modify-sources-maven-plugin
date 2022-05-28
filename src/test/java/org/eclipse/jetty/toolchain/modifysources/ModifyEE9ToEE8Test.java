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

public class ModifyEE9ToEE8Test
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

        ModifyEE9ToEE8 mojo =
            (ModifyEE9ToEE8) rule.lookupConfiguredMojo( pom, "modify-sources-ee9-to-ee8" );
        assertNotNull( mojo );
        mojo.setSourceProjectLocation(new File("target/test-classes/project-modify/src/main/java"));
        mojo.setMoveDirectoryStructure(false);
        mojo.execute();

        File outputDirectory = (File) rule.getVariableValueFromObject( mojo, "outputDirectory" );
        assertNotNull( outputDirectory );
        assertTrue( outputDirectory.exists() );

        Path modified = Paths.get(outputDirectory.toString(), "org", "jetty", "Scanner.java");

        assertTrue(Files.exists(modified));

        String sourceModified = new String(Files.readAllBytes(modified));
        assertTrue(sourceModified.contains("package org.eclipse.jetty.ee8.nested;"));
        assertFalse(sourceModified.contains("package org.eclipse.jetty.ee9.nested;"));
        assertFalse(sourceModified.contains("org/eclipse/jetty/ee9"));
        assertFalse(sourceModified.contains("jakarta/servlet"));
        assertFalse(sourceModified.contains("jakarta.servlet"));
        assertFalse(sourceModified.contains("org.eclipse.jetty.ee9"));
        assertTrue(
                sourceModified.contains("protected void handleOptions(Request request, org.eclipse.jetty.ee8.nested.Response response) throws IOException"));
        assertTrue(
                sourceModified.contains("final org.eclipse.jetty.ee8.nested.Response response = channel.getResponse();"));
        assertTrue(
                sourceModified.contains("import javax.servlet.ServletRequestEvent;"));
        assertTrue(
                sourceModified.contains("final HttpServletResponse response = org.eclipse.jetty.ee8.nested.Response.unwrap(event.getSuppliedResponse());"));

        assertTrue(
                sourceModified.contains("if (!javax.servlet.Filter.class.isAssignableFrom(getHeldClass())) {"));

    }

}

