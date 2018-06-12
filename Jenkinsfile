#!groovy

node("linux") {

  def settingsName = 'oss-settings.xml'
  def mvnName = 'maven3.5'
  def jdkName = 'jdk8'

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
    def mvnGoals = "clean install"
    if ( isActiveBranch() )
    {
      mvnGoals = "clean deploy"
    }
    stage('Build') {
      withMaven( maven: mvnName, jdk: jdkName, globalMavenSettingsConfig: settingsName ) {
        sh "mvn -B -V $mvnGoals -P run-its"
      }
    }
  } catch(Exception e) {
    //notifyBuild("Build Failure")
    throw e
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
