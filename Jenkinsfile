#!groovy

pipeline {
  agent none
  options {
    disableConcurrentBuilds()
    durabilityHint('PERFORMANCE_OPTIMIZED')
    buildDiscarder(logRotator(numToKeepStr: '10', artifactNumToKeepStr: '5'))
    timeout(time: 120, unit: 'MINUTES')
  }
  stages {
    stage( "Parallel Stage" ) {
      parallel {
        stage( "Build / Test - JDK11" ) {
          agent { node { label 'linux-light' } }
          options { timeout( time: 120, unit: 'MINUTES' ) }
          steps {
            mavenBuild( "jdk11", "clean install javadoc:jar" )
            warnings consoleParsers: [[parserName: 'Maven'], [parserName: 'Java']]
            script {
              if (env.BRANCH_NAME == 'main') {
                mavenBuild( "jdk11", "deploy" )
              }
            }
          }
        }
        stage( "Build / Test - JDK17" ) {
          agent { node { label 'linux-light' } }
          options { timeout( time: 120, unit: 'MINUTES' ) }
          steps {
            mavenBuild( "jdk17", "clean install javadoc:jar" )
          }
        }
        stage( "Build / Test - JDK21" ) {
          agent { node { label 'linux-light' } }
          options { timeout( time: 120, unit: 'MINUTES' ) }
          steps {
            mavenBuild( "jdk21", "clean install javadoc:jar" )
          }
        }
      }
    }
  }
}

/**
 * To other developers, if you are using this method above, please use the following syntax.
 *
 * mavenBuild("<jdk>", "<profiles> <goals> <plugins> <properties>"
 *
 * @param jdk the jdk tool name (in jenkins) to use for this build
 * @param cmdline the command line in "<profiles> <goals> <properties>"`format.
 * @return the Jenkinsfile step representing a maven build
 */
def mavenBuild(jdk, cmdline) {
  def mvnName = 'maven3'
  def localRepo = "${env.JENKINS_HOME}/${env.EXECUTOR_NUMBER}" // ".repository" //
  def settingsName = 'oss-settings.xml'
  def mavenOpts = '-Xms2g -Xmx2g -Djava.awt.headless=true'

  withMaven(
          maven: mvnName,
          jdk: "$jdk",
          publisherStrategy: 'EXPLICIT',
          globalMavenSettingsConfig: settingsName,
          options: [junitPublisher(disabled: false)],
          mavenOpts: mavenOpts,
          mavenLocalRepo: localRepo) {
    // Some common Maven command line + provided command line
    sh "mvn -V -B -DfailIfNoTests=false -Djetty.testtracker.log=true -e $cmdline"
  }
}

// vim: et:ts=2:sw=2:ft=groovy
