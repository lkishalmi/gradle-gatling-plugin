package com.github.lkishalmi.gradle.gatling

trait JvmConfigurable {

    static final List<String> DEFAULT_JVM_ARGS = [
        '-Xmx1G',
        '-XX:+HeapDumpOnOutOfMemoryError',
        '-XX:+UseG1GC',
        '-XX:MaxGCPauseMillis=30',
        '-XX:G1HeapRegionSize=16m',
        '-XX:InitiatingHeapOccupancyPercent=75',
        '-XX:+ParallelRefProcEnabled',
        '-XX:+PerfDisableSharedMem',
        '-XX:+OptimizeStringConcat'
    ]

    static final Map DEFAULT_SYSTEM_PROPS = ["java.net.preferIPv4Stack": true, "java.net.preferIPv6Addresses": false]

    List<String> jvmArgs

    Map systemProperties

}
