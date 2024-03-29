//
// ========================================================================
// Copyright (c) 1995-2022 Mort Bay Consulting Pty Ltd and others.
//
// This program and the accompanying materials are made available under the
// terms of the Eclipse Public License v. 2.0 which is available at
// https://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
// which is available at https://www.apache.org/licenses/LICENSE-2.0.
//
// SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
// ========================================================================
//

module org.eclipse.jetty.ee9.annotations
{
    requires jakarta.annotation;
    requires jakarta.transaction;
    requires java.naming;
    requires org.slf4j;

    requires transitive org.eclipse.jetty.ee9.plus;
    requires transitive org.objectweb.asm;

    exports org.eclipse.jetty.ee9.annotations;

    uses jakarta.servlet.ServletContainerInitializer;

    provides Configuration with AnnotationConfiguration;

    provides org.eclipse.jetty.ee9.websocket.api.ExtensionConfig.Parser with
            org.eclipse.jetty.ee9.websocket.common.ExtensionConfigParser;

    exports org.eclipse.jetty.ee9.websocket.jakarta.client.internal to org.eclipse.jetty.ee9.websocket.jakarta.server;

    provides jakarta.websocket.ContainerProvider with org.eclipse.jetty.ee8.websocket.javax.client.JakartaWebSocketClientContainerProvider;

    requires static jakarta.mail;

}
