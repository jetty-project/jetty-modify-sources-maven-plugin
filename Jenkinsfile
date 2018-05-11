#!groovy

node("linux") {
  // System Dependent Locations
  def mvntool = tool name: 'maven3', type: 'hudson.tasks.Maven$MavenInstallation'
  def jdktool = tool name: 'jdk8', type: 'hudson.model.JDK'

  // Environment
  List mvnEnv = ["PATH+MVN=${mvntool}/bin", "PATH+JDK=${jdktool}/bin", "JAVA_HOME=${jdktool}/", "MAVEN_HOME=${mvntool}"]
  mvnEnv.add("MAVEN_OPTS=-Xms256m -Xmx1024m -Djava.awt.headless=true")

  try
  {
    stage('Checkout') {
      checkout scm
    }
  } catch (Exception e) {
    //notifyBuild("Checkout Failure")
    throw e
  }

  try
  {
    withMaven(maven:'maven3',jdk:'jdk8') {
      sh "mvn -B -V clean install -P run-its"
    }
  } catch(Exception e) {
    //notifyBuild("Build Failure")
    throw e
  }

  try
  {
    if ( isActiveBranch() )
    {
      withMaven(maven:'maven3',jdk:'jdk8') {
        sh "mvn -B -V deploy"
      }
    }
  } catch(Exception e) {
    throw e;
  }

}

// True if this build is part of the "active" branches
// for Jetty.
def isActiveBranch()
{
  def branchName = "${env.BRANCH_NAME}"
  return ( branchName == "master" );
}

// Test if the Jenkins Pipeline or Step has marked the
// current build as unstable
def isUnstable()
{
  return currentBuild.result == "UNSTABLE"
}

// Send a notification about the build status
def notifyBuild(String buildStatus)
{
  if ( !isActiveBranch() )
  {
    // don't send notifications on transient branches
    return
  }

  // default the value
  buildStatus = buildStatus ?: "UNKNOWN"

  def email = "${env.EMAILADDRESS}"
  def summary = "${env.JOB_NAME}#${env.BUILD_NUMBER} - ${buildStatus}"
  def detail = """<h4>Job: <a href='${env.JOB_URL}'>${env.JOB_NAME}</a> [#${env.BUILD_NUMBER}]</h4>
  <p><b>${buildStatus}</b></p>
  <table>
    <tr><td>Build</td><td><a href='${env.BUILD_URL}'>${env.BUILD_URL}</a></td><tr>
    <tr><td>Console</td><td><a href='${env.BUILD_URL}console'>${env.BUILD_URL}console</a></td><tr>
    <tr><td>Test Report</td><td><a href='${env.BUILD_URL}testReport/'>${env.BUILD_URL}testReport/</a></td><tr>
  </table>
  """

  emailext (
    to: email,
    subject: summary,
    body: detail
  )
}

// vim: et:ts=2:sw=2:ft=groovy
