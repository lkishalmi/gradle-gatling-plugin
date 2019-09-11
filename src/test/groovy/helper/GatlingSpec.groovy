package helper

import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import static org.apache.commons.io.FileUtils.copyDirectory

abstract class GatlingSpec extends Specification {
    @Rule
    public final TemporaryFolder testProjectDir = new TemporaryFolder()

    File testProjectSrcDir

    File testProjectBuildDir

    File buildFile

    def createBuildFolder(String fixtureDir) {
        if (fixtureDir) {
            copyDirectory(new File(this.class.getResource(fixtureDir).file), testProjectDir.root)
        }
        testProjectSrcDir = new File(testProjectDir.root, "src/gatling/simulations")
        testProjectBuildDir = new File(testProjectDir.root, "build")
    }

    def generateBuildScripts() {
        buildFile = testProjectDir.newFile("build.gradle")
        buildFile.text = """
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
