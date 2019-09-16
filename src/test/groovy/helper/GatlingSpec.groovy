package helper

import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import static org.apache.commons.io.FileUtils.copyDirectory

abstract class GatlingSpec extends Specification {
    @Rule
    public final TemporaryFolder projectDir = new TemporaryFolder()

    File srcDir

    File buildDir

    File buildFile

    def createBuildFolder(String fixtureDir) {
        if (fixtureDir) {
            copyDirectory(new File(this.class.getResource(fixtureDir).file), projectDir.root)
        }
        srcDir = new File(projectDir.root, "src/gatling/simulations")
        buildDir = new File(projectDir.root, "build")
    }

    def generateBuildScripts() {
        buildFile = projectDir.newFile("build.gradle")
        buildFile.text = """
plugins { id 'com.github.lkishalmi.gatling' }
repositories { jcenter() }
dependencies { gatling group: 'commons-lang', name: 'commons-lang', version: '2.6' }
"""
    }
}
