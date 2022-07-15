File source = new File( basedir, "target/test-classes/web.xml" );
assert source.isFile()
assert !source.text.contains( 'ee9' )
assert !source.text.contains( 'jakarta')

assert source.text.contains( 'org.eclipse.jetty.ee8.plus.webapp.PlusDescriptorProcessorTest$TestInjections');
assert source.text.contains( 'javax.servlet.Foo');
