File source = new File( basedir, "target/modified-sources/org/eclipse/jetty/util/Scanner.java" );
assert source.isFile()
assert !source.text.contains( 'isDebugEnabled' )
assert !source.text.contains( '.debug')
assert !source.text.contains( 'afile = new File("if")');
assert source.text.contains( 'afile = new File("else");');

File buildLog = new File( basedir, 'build.log' )
assert buildLog.text.contains( 'Compiling 2 source files' )