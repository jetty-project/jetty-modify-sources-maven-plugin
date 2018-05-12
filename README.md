# jetty-modify-sources-maven-plugin

The goal of this plugin is to simply remove logger debug statement from source code.
This include
* if(LOG.isDebugEnabled())
* LOG.debug("foo bar")

I guess it can be used outside of Eclipse Jetty sources.