package com.one38over.gradle

import groovy.io.FileType
import groovy.json.JsonOutput
import org.gradle.api.GradleException
import org.gradle.api.Project

class TaskHandler  {
    private Project project

    TaskHandler(Project project) {
        this.project = project
    }

    boolean isClass(String clazz) {
        try {
            Class.forName(clazz)
            return true
        } catch(ClassNotFoundException e) {
            return false
        }
    }

    List getHandlerList() {
        List handlers = []
        this.metaClass.methods.findAll { MetaMethod method ->
            if (method.declaringClass.name == this.class.name) {
                if (method.name =~ /^[A-Z]/) {
                    handlers << method.name
                }
            }
        }
        return handlers
    }

    String assembleTaskName(String prefix, String name) {
        return prefix + "-" + name
    }

    void assign(String componentName, String taskName, Map taskConfiguration) {
        Expando t = new Expando(taskConfiguration)
        t.taskName = assembleTaskName(componentName, taskName)

        if (t.getProperty("config").getProperties().get("handler")) {
            throw new GradleException("error task handler not defined for ${t.taskName}")
        }


        if (! this.metaClass.respondsTo(this, t.config.handler)) {
            throw new GradleException("error task handler not implemented: ${t.config.handler} for ${t.taskName}")
        }

        this.invokeMethod(t.config.handler, t)
    }

    /*
     * If a task handler is implemented to have doFirst and/or doLast actions, we
     * need to specify project.buildDir not buildDir or $buildDir like we normally
     * would in the build.gradle script. This enables functional test to be written
     * and run from the IDE and ensure that the buildDir is resolved temporary
     * project root
     *
     * Also note that if doFirst and/or doLast actions are added to a task, the
     * task can no longer run in parallel
     *
     *  import groovy.io.FileType
     *
     *  doLast {
     *      def dir = new File(project.buildDir, "proto")
     *      println(dir.absolutePath)
     *      dir.eachFileRecurse (FileType.FILES) { file ->
     *          println file
     *      }
     *  }
     *
     */

    void CopyFileTask(Expando t) {
        try {
            Map environment = t.config.enviroment ?: [:]
            project.tasks.register(t.taskName as String, Class.forName("org.gradle.api.tasks.Copy")) {
                group = t.group as String
                description = t.description as String
                dependsOn = t.unorderedDeps ?: []
                from(t.config.src as String)
                into(t.config.destdir as String)
                expand(environment)
            }
        } catch(Exception e) {
            throw new Exception(e)
        }
    }

    /**
     *
     * @param t Expando task configuration
     */
    void DownloadFileTask(Expando t) {
        try {
            project.tasks.register(t.taskName as String, Class.forName("de.undercouch.gradle.tasks.download.Download")) {
                group = t.group as String
                description = t.description as String
                dependsOn = t.unorderedDeps ?: []
                src t.config.src
                dest new File(project.buildDir, t.config.dest)
                overwrite true
            }
        } catch(Exception e) {
            throw new Exception(e)
        }

    }

    void ShellExecTask(Expando t) {
        project.tasks.register(t.taskName as String, Class.forName('com.one38over.gradle.ShellExecTask')) {
            group           = t.group as String
            description     = t.description as String
            dependsOn       = t.unorderedDeps ?: []
            workdir         = new File(t.config.workdir as String) as File
            environment     = t.config.environment ?: [:]
            command         = t.config.command
            sudo            = t.config.sudo as boolean ?: false
        }
    }


    void SyncFileTask(Expando t) {
        Map environment = t.config.environment ?: [:]
        project.tasks.register(t.taskName as String, Class.forName('org.gradle.api.tasks.Sync')) {
            group = t.group as String
            description = t.description as String
            dependsOn = t.unorderedDeps ?: []
            from(t.config.src as String)
            into(t.config.destdir as String)
            expand(environment)
        }
    }


    /*

    void SyncFilesTask(Expando config) {
        project.tasks.register(config.taskName as String, Class.forName(config.handler as String)) {
            group           = config.group as String
            description     = config.description as String
            dependsOn       = config.unorderedDeps ?: []
            from            = new File(config.srcdir as String)
            include         = { config.files as List }
            into            = new File(config.destdir as String)
            expand          = config.environment ?: [:]
        }
    }

    void VerifyFile(Expando config) {
        project.tasks.register(config.taskName as String, Class.forName(config.handler as String)) {
            group           = config.group as String
            description     = config.description as String
            dependsOn       = config.unorderedDeps ?: []
            src             = new File(config.src as String)
            algorithm       = config.algorithm as String
            checksum        = config.checksum as String
        }
    }
    */
}