package org.eclipse.jetty.toolchain.modifysources;


import org.apache.maven.plugin.testing.MojoRule;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.io.FileMatchers.anExistingDirectory;
import static org.hamcrest.io.FileMatchers.anExistingFile;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

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
        assertThat(pom, notNullValue());

        ModifyEE9ToEE8ServiceLoaderFiles mojo =
            (ModifyEE9ToEE8ServiceLoaderFiles) rule.lookupConfiguredMojo( pom, "modify-service-loader-files-ee9-to-ee8" );
        assertThat(mojo, notNullValue());
        mojo.setOutputDirectory(new File("target/test-classes/project-modify/src/main/resources"));

        mojo.execute();

        File outputDirectory = (File) rule.getVariableValueFromObject( mojo, "outputDirectory" );
        assertThat(outputDirectory, notNullValue());
        assertThat(outputDirectory, anExistingDirectory());

        Path modified = Paths.get(outputDirectory.toString(), "META-INF", "services", "org.eclipse.jetty.ee9.webapp.Configuration");
        assertThat(modified.toFile(), not(anExistingFile()));

        modified = Paths.get(outputDirectory.toString(), "META-INF", "services", "org.eclipse.jetty.ee8.webapp.Configuration");
        assertThat(modified.toFile(), anExistingFile());

        String modifiedContent = new String(Files.readAllBytes(modified));
        assertThat(modifiedContent, not(containsString("org.eclipse.jetty.ee9")));

        assertThat(modifiedContent, containsString("org.eclipse.jetty.ee8.webapp.FragmentConfiguration"));
        assertThat(modifiedContent, containsString("org.eclipse.jetty.ee8.webapp.JettyWebXmlConfiguration"));
        assertThat(modifiedContent, containsString("org.eclipse.jetty.ee8.webapp.JaasConfiguration"));
        assertThat(modifiedContent, containsString("org.eclipse.jetty.ee8.webapp.JaspiConfiguration"));
        assertThat(modifiedContent, containsString("org.eclipse.jetty.ee8.webapp.JmxConfiguration"));
        assertThat(modifiedContent, containsString("org.eclipse.jetty.ee8.webapp.JndiConfiguration"));
        assertThat(modifiedContent, containsString("org.eclipse.jetty.ee8.webapp.JspConfiguration"));
        assertThat(modifiedContent, containsString("org.eclipse.jetty.ee8.webapp.MetaInfConfiguration"));
        assertThat(modifiedContent, containsString("org.eclipse.jetty.ee8.webapp.ServletsConfiguration"));
        assertThat(modifiedContent, containsString("org.eclipse.jetty.ee8.webapp.WebAppConfiguration"));
        assertThat(modifiedContent, containsString("org.eclipse.jetty.ee8.webapp.WebInfConfiguration"));
        assertThat(modifiedContent, containsString("org.eclipse.jetty.ee8.webapp.WebXmlConfiguration"));


        modified = Paths.get(outputDirectory.toString(), "META-INF", "services", "jakarta.servlet.ServletContainerInitializer");
        assertThat(modified.toFile(), not(anExistingFile()));

        modified = Paths.get(outputDirectory.toString(), "META-INF", "services", "javax.servlet.ServletContainerInitializer");
        assertThat(modified.toFile(), anExistingFile());

        modifiedContent = new String(Files.readAllBytes(modified));
        assertThat(modifiedContent, not(containsString("org.eclipse.jetty.ee9")));

        assertThat(modifiedContent,
                not(containsString("org.eclipse.jetty.ee9.websocket.jakarta.server.config.JakartaWebSocketServletContainerInitializer")));
        assertThat(modifiedContent,
                containsString("org.eclipse.jetty.ee8.websocket.javax.server.config.JavaxWebSocketServletContainerInitializer"));

    }

}

