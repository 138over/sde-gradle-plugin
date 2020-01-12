package com.one38over.gradle

import groovy.json.JsonOutput
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option

class DownloadFileTask extends DefaultTask {
    @Input
    String src

    @InputFile

    File dest

    @Input
    boolean overwrite

    @TaskAction
    void exec() {
        getLogger().quiet("src '{}' dest '{}' overwrite '{}'", src, dest.absolutePath, overwrite)
    }
}
