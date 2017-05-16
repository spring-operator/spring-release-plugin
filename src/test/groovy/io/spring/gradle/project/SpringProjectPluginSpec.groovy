/*
 * Copyright 2017 Pivotal Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.spring.gradle.project

import nebula.test.ProjectSpec
import org.ajoberstar.gradle.git.ghpages.GithubPagesPlugin
import org.ajoberstar.grgit.Grgit
import org.asciidoctor.gradle.AsciidoctorPlugin

class SpringProjectPluginSpec extends ProjectSpec {
    Grgit repo

    def setup() {
        repo = Grgit.init(dir: projectDir)
        repo.remote.add(name: 'origin', url: 'git@github.com:spring-gradle-plugins/gradle-project-plugin.git')
        new File(projectDir, '.gitignore') << 'userHome/'

        repo.add(patterns: repo.status().unstaged.getAllChanges())
        repo.commit(message: 'initial commit')
    }

    def 'final task generates releases with .RELEASE suffix'() {
        when:
        project.gradle.startParameter.taskNames = ['final']
        project.plugins.apply(SpringProjectPlugin)

        then:
        project.version.toString() == '0.1.0.RELEASE'
    }

    def 'useLastTag generates releases with .RELEASE suffix'() {
        setup:
        repo.tag.add(name: 'v0.1.0.RELEASE', force: true)

        when:
        project.ext.set('release.useLastTag', 'true')
        project.gradle.startParameter.taskNames = ['final']
        project.plugins.apply(SpringProjectPlugin)

        then:
        project.version.toString() == '0.1.0.RELEASE'
    }

    def 'release.version preserves .RELEASE suffix'() {
        when:
        project.ext.set('release.version', '0.2.0.RELEASE')
        project.gradle.startParameter.taskNames = ['final']
        project.plugins.apply(SpringProjectPlugin)

        then:
        project.version.toString() == '0.2.0.RELEASE'
    }

    def 'asciidoctor and Github pages support enabled if src/docs/asciidoc exists'() {
        when:
        project.file('src/docs/asciidoc').mkdirs()
        project.plugins.apply(SpringProjectPlugin)

        then:
        project.plugins.hasPlugin(AsciidoctorPlugin)
        project.plugins.hasPlugin(GithubPagesPlugin)
    }
}
