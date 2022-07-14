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
        assertThat(pom, notNullValue());

        ModifyEE9ToEE8 mojo =
            (ModifyEE9ToEE8) rule.lookupConfiguredMojo( pom, "modify-sources-ee9-to-ee8" );
        assertThat(mojo, notNullValue());
        mojo.setSourceProjectLocation(new File("target/test-classes/project-modify/src/main/java"));
        mojo.setMoveDirectoryStructure(false);
        mojo.execute();

        File outputDirectory = (File) rule.getVariableValueFromObject( mojo, "outputDirectory" );
        assertThat(outputDirectory, notNullValue());
        assertThat(outputDirectory, anExistingDirectory());

        {
            Path modified = Paths.get(outputDirectory.toString(), "org", "jetty", "Scanner.java");
            assertThat(modified.toFile(), anExistingFile());

            String sourceModified = new String(Files.readAllBytes(modified));
            assertThat(sourceModified, containsString("package org.eclipse.jetty.ee8.nested;"));
            assertThat(sourceModified, not(containsString("package org.eclipse.jetty.ee9.nested;")));
            assertThat(sourceModified, not(containsString("jetty-ee9")));
            assertThat(sourceModified, not(containsString("org/eclipse/jetty/ee9")));
            assertThat(sourceModified, not(containsString("webdefault-ee9.xml")));
            assertThat(sourceModified, not(containsString("jakarta/servlet")));
            assertThat(sourceModified, not(containsString("jakarta/websocket")));
            assertThat(sourceModified, not(containsString("jakarta.servlet")));
            assertThat(sourceModified, not(containsString("jakarta.websocket")));
            assertThat(sourceModified, not(containsString("org.eclipse.jetty.ee9")));
            assertThat(sourceModified, containsString("protected void handleOptions(Request request, org.eclipse.jetty.ee8.nested.Response response) throws IOException"));
            assertThat(sourceModified, containsString("final org.eclipse.jetty.ee8.nested.Response response = channel.getResponse();"));
            assertThat(sourceModified, containsString("import javax.servlet.ServletRequestEvent;"));
            assertThat(sourceModified, containsString("import javax.websocket.ContainerProvider;"));
            assertThat(sourceModified, not(containsString("\"ee9\"")));
            assertThat(sourceModified, containsString("\"ee8\""));
            assertThat(sourceModified,
                    containsString("final HttpServletResponse response = org.eclipse.jetty.ee8.nested.Response.unwrap(event.getSuppliedResponse());"));

            assertThat(sourceModified, containsString("if (!javax.servlet.Filter.class.isAssignableFrom(getHeldClass())) {"));
        }

        {
            Path modifiedModuleInfo = Paths.get(outputDirectory.toString(), "module-info.java");
            assertThat(modifiedModuleInfo.toFile(), anExistingFile());
            String sourceModifiedModuleInfo = new String(Files.readAllBytes(modifiedModuleInfo));
            assertThat(sourceModifiedModuleInfo, containsString("requires java.annotation;"));
            assertThat(sourceModifiedModuleInfo, containsString("requires java.transaction;"));
            assertThat(sourceModifiedModuleInfo, containsString("exports org.eclipse.jetty.ee8.annotations;"));
            assertThat(sourceModifiedModuleInfo, not(containsString("org.eclipse.jetty.ee9")));

            assertThat(sourceModifiedModuleInfo, containsString("org.eclipse.jetty.ee8.websocket.common.ExtensionConfigParser;"));
            assertThat(sourceModifiedModuleInfo, containsString("org.eclipse.jetty.ee8.websocket.api.ExtensionConfig.Parser with"));
            assertThat(sourceModifiedModuleInfo,
                    containsString("exports org.eclipse.jetty.ee8.websocket.jakarta.client.internal to org.eclipse.jetty.ee8.websocket.jakarta.server;"));

            assertThat(sourceModifiedModuleInfo, containsString("requires static javax.mail.glassfish;"));
        }
    }

}

