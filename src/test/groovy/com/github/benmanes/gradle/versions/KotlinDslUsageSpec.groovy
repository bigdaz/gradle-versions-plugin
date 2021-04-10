package com.github.benmanes.gradle.versions

import java.io.File
import java.nio.file.Files
import org.gradle.testkit.runner.GradleRunner
import spock.lang.Specification
import spock.lang.Unroll

final class KotlinDslUsageSpec extends Specification {
  private File testProjectDir = Files.createTempDirectory('test').toFile()

  private File buildFile

  def 'setup'() {
    def mavenRepoUrl = getClass().getResource('/maven/').toURI()

    buildFile = new File(testProjectDir, 'build.gradle.kts')
    buildFile <<
      """
        import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

        plugins {
          java
          id("com.github.ben-manes.versions")
        }

        apply(plugin = "com.github.ben-manes.versions")

        repositories {
          maven(url = "${mavenRepoUrl}")
        }

        dependencies {
          implementation("com.google.inject:guice:2.0")
        }
        """.stripIndent()
  }

  @Unroll
  def "user friendly kotlin-dsl"() {
    given:
    def srdErrWriter = new StringWriter()
    buildFile << '''
      tasks.named<DependencyUpdatesTask>("dependencyUpdates") {
          checkForGradleUpdate = true
          outputFormatter = "json"
          outputDir = "build/dependencyUpdates"
          reportfileName = "report"
          resolutionStrategy {
            componentSelection {
              all {
                if (candidate.version == "3.1" && currentVersion != "") {
                  reject("Guice 3.1 not allowed")
                }
              }
            }
          }
        }
    '''

    when:
    def result = GradleRunner.create()
      .withGradleVersion(gradleVersion)
      .withPluginClasspath()
      .withProjectDir(testProjectDir)
      .withArguments('dependencyUpdates')
      .forwardStdError(srdErrWriter)
      .build()

    then:
    result.output.contains('''com.google.inject:guice [2.0 -> 3.0]''')
    srdErrWriter.toString().empty

    where:
    gradleVersion << ['5.6']
  }
}
