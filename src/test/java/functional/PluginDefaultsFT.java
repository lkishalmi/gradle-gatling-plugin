package functional;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import static org.gradle.testkit.runner.TaskOutcome.*;
import org.junit.BeforeClass;

/**
 *
 * @author Laszlo Kishalmi
 */
public class PluginDefaultsFT {

    @Rule
    public final TemporaryFolder testProjectDir = new TemporaryFolder();

    private static List<File> pluginClasspath;
    private File buildFile;

    @BeforeClass
    public static void initPluginClasspath() {
        String[] pcp = System.getProperty("pluginClasspath", "").split(":");

        pluginClasspath = new LinkedList<>();
        for (String entry : pcp) {
            pluginClasspath.add(new File(entry));
        }
    }

    @Before
    public void setup() throws IOException {
        buildFile = testProjectDir.newFile("build.gradle");
    }

    @Test
    public void testLoadPlugin() throws IOException {
        String buildFileContent = "plugins {\n"
                + "            id 'com.github.lkishalmi.gatling'\n"
                + "        }";
        writeFile(buildFile, buildFileContent);

        BuildResult result = GradleRunner.create()
                .withProjectDir(testProjectDir.getRoot())
                .withPluginClasspath(pluginClasspath)
                .withArguments("tasks")
                .build();

    }

    private void writeFile(File destination, String content) throws IOException {
        BufferedWriter output = null;
        try {
            output = new BufferedWriter(new FileWriter(destination));
            output.write(content);
        } finally {
            if (output != null) {
                output.close();
            }
        }
    }
}
