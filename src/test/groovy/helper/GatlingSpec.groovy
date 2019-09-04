package helper

import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import static org.apache.commons.io.FileUtils.copyDirectory

abstract class GatlingSpec extends Specification {
    @Rule
    public final TemporaryFolder testProjectDir = new TemporaryFolder()

    File testProjectBuildDir

    def createBuildFolder(String fixtureDir) {
        if (fixtureDir) {
            copyDirectory(new File(this.class.getResource(fixtureDir).file), testProjectDir.root)
        }
        testProjectBuildDir = new File(testProjectDir.root, "build")
    }

    def generateBuildScripts() {
        testProjectDir.newFile("build.gradle") << """
plugins {
    id 'com.github.lkishalmi.gatling'
}
repositories {
    jcenter()
}
dependencies {
    gatling group: 'commons-lang', name: 'commons-lang', version: '2.6'
}
"""
    }
}
