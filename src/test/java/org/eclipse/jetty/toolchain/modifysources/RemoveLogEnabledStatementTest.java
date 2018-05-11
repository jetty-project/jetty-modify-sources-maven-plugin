package org.eclipse.jetty.toolchain.modifysources;


import org.apache.maven.plugin.testing.MojoRule;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class RemoveLogEnabledStatementTest
{
    @Rule
    public MojoRule rule = new MojoRule()
    {
        @Override
        protected void before()
            throws Throwable
        {
        }

        @Override
        protected void after()
        {
        }
    };

    /**
     * @throws Exception if any
     */
    @Test
    public void test_remove()
        throws Exception
    {
        File pom = new File( "target/test-classes/project-to-test/" );
        assertNotNull( pom );
        assertTrue( pom.exists() );

        RemoveLogEnabledStatement removeLogEnabledStatement =
            (RemoveLogEnabledStatement) rule.lookupConfiguredMojo( pom, "remove-log-enabled" );
        assertNotNull( removeLogEnabledStatement );
        removeLogEnabledStatement.execute();

        File outputDirectory = (File) rule.getVariableValueFromObject( removeLogEnabledStatement, "outputDirectory" );
        assertNotNull( outputDirectory );
        assertTrue( outputDirectory.exists() );

        Path modified = Paths.get( outputDirectory.toString(), "org", "jetty", "Scanner.java" );

        assertTrue( Files.exists(modified) );

        String sourceModified = new String( Files.readAllBytes( modified ) );
        assertTrue( sourceModified.contains( "package org.eclipse.jetty.util;" ) );
    }

}

