package com.github.lkishalmi.gradle.gatling

import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Shared
import spock.lang.Specification

import static org.apache.commons.io.FileUtils.copyDirectory

abstract class GatlingSpec extends Specification {
    @Rule
    public final TemporaryFolder testProjectDir = new TemporaryFolder()

    File testProjectBuildDir

    @Shared
    def pluginVersion = System.getProperty("com.github.lkishalmi.gatling.version")

    def setupSpec() {
        assert pluginVersion != null : "Provide plugin version via `-Dcom.github.lkishalmi.gatling.version=`"
    }

    def createBuildFolder(String layout) {
        copyDirectory(new File(this.class.getResource("/$layout-layout").file), testProjectDir.root)
        testProjectBuildDir = new File(testProjectDir.root, "build")
    }

    def generateBuildScripts() {
        testProjectDir.newFile("build.gradle") << """
plugins {
    id 'com.github.lkishalmi.gatling' version '$pluginVersion'
}
repositories {
    jcenter()
}
dependencies {
    gatling group: 'commons-lang', name: 'commons-lang', version: '2.6'
}
"""

        testProjectDir.newFile("settings.gradle") << """
pluginManagement {
    resolutionStrategy {
        eachPlugin {
            if (requested.id.namespace == 'com.github.lkishalmi') {
                useModule('com.github.lkishalmi.gatling:gradle-gatling-plugin:$pluginVersion')
            }
        }
    }
    repositories {
        maven {
            url "${new File(System.getProperty("user.home"), ".m2/repository").toURI()}"
        }
    }
}
"""
    }
}
