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
        mojo.setMoveDirectoryStructure(true);
        mojo.execute();

        File outputDirectory = (File) rule.getVariableValueFromObject( mojo, "outputDirectory" );
        assertThat(outputDirectory, notNullValue());
        assertThat(outputDirectory, anExistingDirectory());

        {
            Path modified = Paths.get(outputDirectory.toString(), "org", "eclipse", "jetty", "ee8", "javax", "nested", "JavaxScanner.java");
            assertThat(modified.toFile(), anExistingFile());

            String sourceModified = new String(Files.readAllBytes(modified));
            assertThat(sourceModified, containsString("package org.eclipse.jetty.ee8.javax.nested;"));
            assertThat(sourceModified, not(containsString("package org.eclipse.jetty.ee9.nested;")));
            assertThat(sourceModified, not(containsString("jetty-ee9")));
            assertThat(sourceModified, not(containsString("org/eclipse/jetty/ee9")));
            assertThat(sourceModified, not(containsString("webdefault-ee9.xml")));
            assertThat(sourceModified, not(containsString("jakarta/servlet")));
            assertThat(sourceModified, not(containsString("jakarta/websocket")));
            assertThat(sourceModified, not(containsString("jakarta.servlet")));
            assertThat(sourceModified, not(containsString("jakarta.websocket")));
            assertThat(sourceModified, not(containsString("org.eclipse.jetty.ee9")));
            assertThat(sourceModified, not(containsString("@ManagedObject(\"EE9 Context\")")));
            assertThat(sourceModified, containsString("@ManagedObject(\"EE8 Context\")"));
            assertThat(sourceModified, containsString("protected void handleOptions(Request request, org.eclipse.jetty.ee8.nested.Response response) throws IOException"));
            assertThat(sourceModified, containsString("final org.eclipse.jetty.ee8.nested.Response response = channel.getResponse();"));
            assertThat(sourceModified, containsString("import javax.servlet.ServletRequestEvent;"));
            assertThat(sourceModified, containsString("import javax.websocket.ContainerProvider;"));
            assertThat(sourceModified, not(containsString("\"ee9\"")));
            assertThat(sourceModified, containsString("\"ee8\""));
            assertThat(sourceModified, containsString("public static final int SERVLET_MAJOR_VERSION = 4;"));
            assertThat(sourceModified,
                    containsString("final HttpServletResponse response = org.eclipse.jetty.ee8.nested.Response.unwrap(event.getSuppliedResponse());"));


            assertThat(sourceModified,
                    containsString("redirectEntity(\"https://jakarta.ee/xml/ns/jakartaee/javaee_9.xsd\", jakartaee10);"));

            assertThat(sourceModified,
                    containsString("redirectEntity(\"http://jakarta.ee/xml/ns/jakartaee/javaee_9.xsd\", jakartaee10);"));

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
                    containsString("exports org.eclipse.jetty.ee8.websocket.javax.client.internal to org.eclipse.jetty.ee8.websocket.javax.server;"));

            assertThat(sourceModifiedModuleInfo, containsString("requires static javax.mail.glassfish;"));

            assertThat(sourceModifiedModuleInfo,
                    containsString("provides javax.websocket.ContainerProvider with org.eclipse.jetty.ee8.websocket.javax.client.JavaxWebSocketClientContainerProvider;"));
        }

        {
            Path modified = Paths.get(outputDirectory.toString(), "org", "eclipse", "jetty", "ee8", "websocket", "javax", "common", "JavaxWebSocketFrameHandler.java");
            assertThat(modified.toFile(), anExistingFile());
            String sourceModified = new String(Files.readAllBytes(modified));
            assertThat(sourceModified, containsString("package org.eclipse.jetty.ee8.websocket.javax.common;"));
            assertThat(sourceModified, containsString("private static final Logger LOG = LoggerFactory.getLogger(JavaxWebSocketFrameHandler.class);"));
            assertThat(sourceModified, containsString("private final List<JavaxWebSocketSessionListener> sessionListeners = new ArrayList<>();"));
            assertThat(sourceModified, containsString("private JavaxWebSocketMessageMetadata textMetadata;"));
            assertThat(sourceModified, containsString("private JavaxWebSocketMessageMetadata textMetadata;"));

            assertThat(sourceModified, containsString("int permits = _passes == null ? 0 : _passes.availablePermits();"));
            assertThat(sourceModified, containsString("_passes = new Semaphore((value - _throttledRequests + permits), true);"));

            assertThat(sourceModified,
                    containsString("JavaxWebSocketMessageMetadata actualTextMetadata = JavaxWebSocketMessageMetadata.copyOf(textMetadata);"));
            assertThat(sourceModified,
                    containsString("HttpClient httpClient = (HttpClient) servletContext.getAttribute(JavaxWebSocketServletContainerInitializer.HTTPCLIENT_ATTRIBUTE);"));
        }
    }

    @Test
    public void testChangeToEE8WithNoChangeComment()
            throws Exception
    {

        File pom = new File( "target/test-classes/project-modify/" );
        assertThat(pom, notNullValue());

        ModifyEE9ToEE8 mojo =
                (ModifyEE9ToEE8) rule.lookupConfiguredMojo( pom, "modify-sources-ee9-to-ee8" );
        assertThat(mojo, notNullValue());
        mojo.setSourceProjectLocation(new File("target/test-classes/project-modify/src/main/java"));
        mojo.setMoveDirectoryStructure(true);
        mojo.execute();

        File outputDirectory = (File) rule.getVariableValueFromObject( mojo, "outputDirectory" );

        {
            Path modified = Paths.get(outputDirectory.toString(), "org", "eclipse", "jetty", "ee8", "CrossContextDispatcher.java");
            assertThat(modified.toFile(), anExistingFile());
            String sourceModified = new String(Files.readAllBytes(modified));
            assertThat(sourceModified, containsString("package org.eclipse.jetty.ee8"));
            assertThat(sourceModified, containsString("class CrossContextDispatcher"));
            assertThat(sourceModified, containsString("name = \"jakarta.servlet.\" + name.substring(14);"));
            assertThat(sourceModified, not(containsString("name = \"javax.servlet.\" + name.substring(14);")));
        }
    }


}

