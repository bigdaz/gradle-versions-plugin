package com.github.benmanes.gradle.versions

import org.gradle.testkit.runner.GradleRunner

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

import java.io.File
import java.nio.file.Files
import spock.lang.Specification

final class JavaLibrarySpec extends Specification {
  private File testProjectDir = Files.createTempDirectory('test').toFile()

  private File buildFile
  private String mavenRepoUrl

  def 'setup'() {
    mavenRepoUrl = getClass().getResource('/maven/').toURI()
  }

  def "Show updates for an api dependency in a java-library project"() {
    given:
    buildFile = new File(testProjectDir, 'build.gradle')
    buildFile <<
      """
        plugins {
          id 'java-library'
          id 'com.github.ben-manes.versions'
        }

        repositories {
          maven {
            url '${mavenRepoUrl}'
          }
        }

        dependencies {
          api 'com.google.inject:guice:2.0'
        }
      """.stripIndent()

    when:
    def result = GradleRunner.create()
      .withProjectDir(testProjectDir)
      .withArguments('dependencyUpdates')
      .withPluginClasspath()
      .build()

    then:
    result.output.contains('com.google.inject:guice [2.0 -> 3.1]')
    result.task(':dependencyUpdates').outcome == SUCCESS
  }
}
