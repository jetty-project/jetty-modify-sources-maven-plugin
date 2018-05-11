File source = new File( basedir, "target/modified-sources/org/eclipse/jetty/util/Scanner.java" );

assert source.isFile()


assert !source.text.contains( 'isDebugEnabled' )