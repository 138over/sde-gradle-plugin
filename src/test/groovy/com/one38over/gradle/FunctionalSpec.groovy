package com.one38over.gradle

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import spock.lang.Shared
import static org.gradle.testkit.runner.TaskOutcome.*

class FunctionalSpec extends Specification {
    @Rule final TemporaryFolder testProjectDir = new TemporaryFolder()
    @Shared Map taskConfig
    @Shared File taskConfigFile
    @Shared File buildFile

    // Run before every test method
    def setup() {
        taskConfig = new JsonSlurper().parseText(new File('src/test/resources/sde.json').text)
        taskConfigFile = testProjectDir.newFile('sde.json')
        taskConfigFile.write JsonOutput.prettyPrint(JsonOutput.toJson(taskConfig))

        buildFile = testProjectDir.newFile('build.gradle')
        buildFile << """ 
        import groovy.io.FileType
        import groovy.json.JsonSlurper
        import groovy.json.JsonOutput
        
         plugins {
            id 'de.undercouch.download' version '4.0.2'
            id 'com.one38over.sde'
            // id 'base' provides clean target
         }
         
         """

        /*
        new File(testProjectDir.root.absolutePath).eachFile() { file->
            println file.getAbsolutePath()
            println file.text
        }
        */
    }

    def "external json task configuration"() {
        given:
        println JsonOutput.prettyPrint(JsonOutput.toJson(taskConfig))

        expect:
        taskConfig.containsKey("sde") == true
        taskConfig.sde.foo.task.A.group == "Shell Exec"
    }

    def "gradle runner task configuration"() {
        when:
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments(['tasks', '--all'])
                .withPluginClasspath()
                .build()

        then:
        printf("Output:%s\n", result.output)
        result.output.contains("Shell Exec tasks") == true
        result.output.contains("foo-A") == true
        result.output.contains("foo-B") == true
        result.output.contains("foo-C") == true
        result.output.contains("foo-D") == true
        result.task(":tasks").outcome == SUCCESS
    }

    def "handler CopyFileTask configuration"() {
        when:
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments(['foo-D'])
                .withPluginClasspath()
                .build()

        then:
        new File(testProjectDir.root, "build/proto/build.gradle").exists() == true
        result.task(":foo-D").outcome == SUCCESS
    }

    def "handler DownloadFileTask configuration"() {
        when:
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments(['foo-C'])
                .withPluginClasspath()
                .build()

        then:
        new File(testProjectDir.root, "build/cache/etcd-v3.4.0-linux-amd64.tar.gz").exists() == true
        result.task(":foo-C").outcome == SUCCESS
    }


    /*
     * task foo-A depends on task foo-B
     */
    def "handler ShellExecTask configuration"() {
        when:
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments(['foo-A'])
                .withPluginClasspath()
                .build()

        then:
        result.task(":foo-A").outcome == SUCCESS
        result.task(":foo-B").outcome == SUCCESS
    }

    def "handler SyncFileTask configuration"() {
        given:
        def json = testProjectDir.newFile('foo.json')
        json << """
        { "foo": "\${version}" }
        """

        when:
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments(['foo-E'])
                .withPluginClasspath()
                .build()

        then:
        new JsonSlurper().parseText(new File(testProjectDir.root, "build/proto/foo.json").text).foo == "1.0"
        result.task(":foo-E").outcome == SUCCESS
    }


    /*
     * configure existing tasks
     */
    def "configure task http-get"() {
        given:
        buildFile << """
        task "http-get" {
            doLast {
                // def req = new URL('https://jsonplaceholder.typicode.com/posts/1').openConnection()
                // logger.quiet("Status code: '{}'", req.getResponseCode())
                // def resp = new JsonSlurper().parseText(req.getInputStream().getText())
                // logger.quiet("Response: '{}'", resp)
                                
                String url = "https://jsonplaceholder.typicode.com/posts/1"
                def json = new JsonSlurper().parseText(url.toURL().text)
                println JsonOutput.prettyPrint(JsonOutput.toJson(json))
            }
        }
        """

        when:
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments(['http-get'])
                .withPluginClasspath()
                .build()

        then:
        printf("Output:%s\n", result.output)
        result.task(":http-get").outcome == SUCCESS
    }

    def "configure task http-post"() {
        given:
        buildFile << """
        task "http-post" {
            doLast {
                def body = [title: "foo", body: "bar", userId: 1]
                def req = new URL('https://jsonplaceholder.typicode.com/posts').openConnection()
                req.setRequestMethod("POST")
                req.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                req.setDoOutput(true)
                req.getOutputStream().write(JsonOutput.toJson(body).getBytes("UTF-8"))
                logger.quiet("Status code: '{}'", req.getResponseCode())
                def resp = new JsonSlurper().parseText(req.getInputStream().getText())
                logger.quiet("Response: '{}'", resp)
                println "Last line postA task"
            }            
        }
        """

        when:
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments(['http-post'])
                .withPluginClasspath()
                .build()

        then:
        printf("Output:%s\n", result.output)
        result.task(":http-post").outcome == SUCCESS
    }
}
