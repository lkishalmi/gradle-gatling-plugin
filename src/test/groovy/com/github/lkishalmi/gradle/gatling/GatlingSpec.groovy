package com.github.lkishalmi.gradle.gatling

import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import static org.apache.commons.io.FileUtils.copyDirectory

abstract class GatlingSpec extends Specification {
    @Rule
    public final TemporaryFolder testProjectDir = new TemporaryFolder()

    File testProjectBuildDir

    def createBuildFolder(String layout) {
        copyDirectory(new File(this.class.getResource("/$layout-layout").file), testProjectDir.root)
        testProjectBuildDir = new File(testProjectDir.root, "build")
    }

    def generateBuildScript() {
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

    def generateBuildScriptWithResultFolder() {
        testProjectDir.newFile("build.gradle") << """
plugins {
    id 'com.github.lkishalmi.gatling'
}
gatling {
    simulationLogFolder = new File("$testProjectDir.root/reports/generateReport/")
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
