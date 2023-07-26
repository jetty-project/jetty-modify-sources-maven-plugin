File source = new File( basedir, "target/test-classes/web.xml" );
assert source.isFile()
assert !source.text.contains( 'ee9' )
assert !source.text.contains( 'jakarta')
assert !source.text.contains( 'version="5.0"')
assert !source.text.contains( 'web-app_5_0.xsd')

assert source.text.contains( 'org.eclipse.jetty.ee8.plus.webapp.PlusDescriptorProcessorTest$TestInjections');
assert source.text.contains( 'javax.servlet.Foo');

File jsp = new File( basedir, "target/test-classes/logout.jsp" );
assert jsp.isFile()
assert !jsp.text.contains( 'jakarta')



