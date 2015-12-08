package com.github.lkishalmi.gradle.gatling;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import org.gradle.api.file.FileCollection;
import org.gradle.api.internal.ConventionTask;
import org.gradle.api.internal.file.FileResolver;
import org.gradle.api.internal.tasks.options.Option;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.internal.DefaultJavaExecAction;
import org.gradle.process.internal.JavaExecAction;

/**
 *
 * @author Laszlo Kishalmi
 */
public class Gatling extends ConventionTask {

    private static final String GATLING_MAIN_CLASS = "io.gatling.app.Gatling";

    private final JavaExecAction javaExecHandleBuilder;

    File classesDir;
    File dataDir;
    File bodiesDir;
    File reportsDir;
    File simulationsDir;
    File confDir;

    String runDescription;
    String simulation;

    boolean mute = true;

    public Gatling() {
        javaExecHandleBuilder = new DefaultJavaExecAction(getFileResolver());
    }

    @Inject
    protected FileResolver getFileResolver() {
        throw new UnsupportedOperationException();
    }

    @TaskAction
    public void exec() {
        javaExecHandleBuilder.setMain(GATLING_MAIN_CLASS);
        setJvmArgs(getJvmArgs());
        setClasspath(getClasspath());
        javaExecHandleBuilder.setArgs(buildArgs());
        if (!isMute()) {
            javaExecHandleBuilder.setStandardInput(System.in);
        }
        javaExecHandleBuilder.execute();
    }

    private List<String> buildArgs() {
        List<String> ret = new LinkedList<>();
        if (isMute()) {
            ret.add("-m");
        }
        if (getClassesDir().isDirectory()) {
            ret.add("-bf");
            ret.add(getClassesDir().getAbsolutePath());
        }
        if (getDataDir().isDirectory()) {
            ret.add("-df");
            ret.add(getDataDir().getAbsolutePath());
        }
        if (getBodiesDir().isDirectory()) {
            ret.add("-bdf");
            ret.add(getBodiesDir().getAbsolutePath());
        }
        ret.add("-rf");
        ret.add(getReportsDir().getAbsolutePath());

        if (getRunDescription() != null) {
            ret.add("-rd");
            ret.add(getRunDescription());
        }

        if (getSimulationsDir() != null) {
            ret.add("-sf");
            ret.add(getSimulationsDir().getAbsolutePath());
        }

        if (getSimulation() != null) {
            ret.add("-s");
            ret.add(getSimulation());
        }

        return ret;
    }

    public List<String> getJvmArgs() {
        return javaExecHandleBuilder.getJvmArgs();
    }

    public void setJvmArgs(Iterable<?> arguments) {
        javaExecHandleBuilder.setJvmArgs(arguments);
    }

    public Gatling jvmArgs(Iterable<?> arguments) {
        javaExecHandleBuilder.jvmArgs(arguments);
        return this;
    }

    public Gatling jvmArgs(Object... arguments) {
        javaExecHandleBuilder.jvmArgs(arguments);
        return this;
    }

    public Map<String, Object> getSystemProperties() {
        return javaExecHandleBuilder.getSystemProperties();
    }

    public void setSystemProperties(Map<String, ?> properties) {
        javaExecHandleBuilder.setSystemProperties(properties);
    }

    public Gatling systemProperties(Map<String, ?> properties) {
        javaExecHandleBuilder.systemProperties(properties);
        return this;
    }

    public Gatling systemProperty(String name, Object value) {
        javaExecHandleBuilder.systemProperty(name, value);
        return this;
    }

    public boolean getDebug() {
        return javaExecHandleBuilder.getDebug();
    }

    @Option(option = "debug-jvm", description = "Enable debugging for the process. The process is started suspended and listening on port 5005. [INCUBATING]")
    public void setDebug(boolean enabled) {
        javaExecHandleBuilder.setDebug(enabled);
    }

    public Gatling setClasspath(FileCollection classpath) {
        FileCollection cp = getConfDir().isDirectory()
                ? cp = getProject().files(getConfDir()).plus(classpath)
                : classpath;

        javaExecHandleBuilder.setClasspath(cp);
        return this;
    }

    public Gatling classpath(Object... paths) {
        javaExecHandleBuilder.classpath(paths);
        return this;
    }

    @InputFiles
    public FileCollection getClasspath() {
        return javaExecHandleBuilder.getClasspath();
    }

    public File getClassesDir() {
        return classesDir;
    }

    public void setClassesDir(File classesDir) {
        this.classesDir = classesDir;
    }

    public File getDataDir() {
        return dataDir;
    }

    public void setDataDir(File dataDir) {
        this.dataDir = dataDir;
    }

    public File getBodiesDir() {
        return bodiesDir;
    }

    public void setBodiesDir(File bodiesDir) {
        this.bodiesDir = bodiesDir;
    }

    public File getReportsDir() {
        return reportsDir;
    }

    public void setReportsDir(File reportsDir) {
        this.reportsDir = reportsDir;
    }

    public File getSimulationsDir() {
        return simulationsDir;
    }

    public void setSimulationsDir(File simulationsDir) {
        this.simulationsDir = simulationsDir;
    }

    public String getRunDescription() {
        return runDescription;
    }

    public void setRunDescription(String runDescription) {
        this.runDescription = runDescription;
    }

    public String getSimulation() {
        return simulation;
    }

    public void setSimulation(String simulation) {
        this.simulation = simulation;
    }

    public boolean isMute() {
        return mute;
    }

    public void setMute(boolean mute) {
        this.mute = mute;
    }

    public File getConfDir() {
        return confDir;
    }

    public void setConfDir(File confDir) {
        this.confDir = confDir;
    }

}
