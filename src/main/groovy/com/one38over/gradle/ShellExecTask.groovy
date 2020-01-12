package com.one38over.gradle

import groovy.json.JsonOutput
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option

class ShellExecTask extends DefaultTask {
    /*
    private String foo

    @Option(option = "foo", description = "configuration file")
    void setConfig(String config) {
        this.foo = foo
    }
    */

    @Input
    String command

    @Input
    Map environment

    @Input
    boolean sudo

    @Input
    String workdir

    @TaskAction
    void exec() {
        def foo = "wtf"
        getLogger().quiet("command '{}' workspace '{}' environment '{}'", command, workdir, environment)
    }
}
