@file:Suppress("SpellCheckingInspection")

import groovy.util.Node
import groovy.util.NodeList
import org.apache.tools.ant.filters.ReplaceTokens
import org.springframework.boot.gradle.tasks.bundling.BootJar


plugins {
    id("java")
    signing
    `maven-publish`
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.lombok)
    alias(libs.plugins.abort.mission)
    alias(libs.plugins.licensee.plugin)
}

group = rootProject.group

extra.apply {
    set("artifactDisplayName", "Lowkey Vault - App")
    set("artifactDescription", "Assembled application of Lowkey Vault.")
}

dependencies {
    implementation(libs.bundles.spring.boot.app)
    implementation(libs.bundles.logback)
    implementation(libs.bundles.tomcat)
    implementation(libs.bundles.jjwt)
    implementation(libs.bouncycastle.bcpkix)
    implementation(libs.hibernate.validator)
    implementation(libs.handlebars)
    implementation(libs.findbugs.jsr305)
    implementation(libs.springdoc.openapi.ui)
    annotationProcessor(libs.lombok)
    annotationProcessor(libs.spring.boot.configuration.processor)
    testImplementation(libs.bundles.spring.test)
    testImplementation(libs.jupiter)
    testImplementation(libs.abort.mission.jupiter)
}

licensee {
    allow("Apache-2.0")
    allow("EPL-1.0")
    allow("EPL-2.0")
    allowUrl("https://repository.jboss.org/licenses/apache-2.0.txt")
    allowUrl("https://www.bouncycastle.org/licence.html")
    allowUrl("https://github.com/openjdk/nashorn/blob/main/LICENSE")
    allowUrl("http://www.eclipse.org/legal/epl-2.0")
    allowUrl("http://www.eclipse.org/org/documents/edl-v10.php")
    allowUrl("https://asm.ow2.io/license.html")
    allowUrl("https://opensource.org/license/mit")
    allowUrl("https://github.com/webjars/webjars-locator-lite/blob/main/LICENSE.md")
    ignoreDependencies("org.apache.tomcat", "tomcat-servlet-api")
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
    withJavadocJar()
    withSourcesJar()
}

val copyLegalDocs = tasks.register<Copy>("copyLegalDocs") {
    group = "documentation"
    description = "Copies legal files and reports."
    from(file("${project.rootProject.projectDir}/LICENSE"))
    from(layout.buildDirectory.file("reports/licensee/artifacts.json").get().asFile)
    from(layout.buildDirectory.file("reports/bom.json").get().asFile)
    into(layout.buildDirectory.dir("resources/main/META-INF").get().asFile)
    rename("artifacts.json", "dependency-licenses.json")
    rename("bom.json", "SBOM.json")
}.get()
copyLegalDocs.dependsOn(tasks.licensee)
copyLegalDocs.dependsOn(tasks.cyclonedxDirectBom)
tasks.javadoc.get().dependsOn(copyLegalDocs)
tasks.compileJava.get().dependsOn(copyLegalDocs)
tasks.processResources.get().finalizedBy(copyLegalDocs)

tasks.test {
    outputs.file(layout.buildDirectory.file("reports/abort-mission/abort-mission-report.json").get().asFile)
    useJUnitPlatform {
        systemProperty("junit.jupiter.extensions.autodetection.enabled", true)
        systemProperty("junit.jupiter.execution.parallel.enabled", true)
        systemProperty("junit.jupiter.execution.parallel.mode.default", "same_thread")
        systemProperty("junit.jupiter.execution.parallel.mode.classes.default", "concurrent")
    }
    finalizedBy(tasks.getByName("jacocoTestReport"))
}

project.tasks.processResources {
    val tokens = mapOf("version" to project.version)
    filesMatching("**/application.properties") {
        filter<ReplaceTokens>("tokens" to tokens)
    }
}

abortMission {
    toolVersion = libs.versions.abortMission.get()
}

tasks.getByName<Jar>("jar") {
    enabled = false
}

tasks.getByName<BootJar>("bootJar") {
    inputs.property("version", project.version)
    archiveVersion.value(project.version as String)
}
tasks.getByName<Task>("resolveMainClassName") {
    dependsOn(tasks.named("copyLegalDocs"))
}

tasks.register<Exec>("regenerateCertJks") {
    group = "other"
    description = "Regenerates the JKS certificate store used by Lowkey Vault."
    outputs.file("${project.projectDir}/src/main/resources/cert/keystore.jks")
    workingDir(file("${project.projectDir}/src/main/resources/cert"))
    outputs.upToDateWhen { false }

    //generate key in JKS with JDK15+ keytool to allow wildcard SAN
    commandLine = mutableListOf("${project.property("keyToolDir") ?: ""}/keytool",
            "-genkeypair",
            "-alias", "lowkey-vault.local",
            "-keyalg", "RSA",
            "-keysize", "4096",
            "-validity", "3650",
            "-dname", "CN=lowkey-vault.local",
            "-keypass", "changeit",
            "-keystore", "keystore.jks",
            "-storeType", "JKS",
            "-storepass", "changeit",
            "-ext", "SAN=dns:lowkey-vault.local,dns:lowkey-vault,dns:*.localhost,dns:*.lowkey-vault,dns:*.lowkey-vault.local,dns:*.default.svc.cluster.local,dns:localhost,ip:127.0.0.1")

    doFirst {
        file("${project.projectDir}/src/main/resources/cert/keystore.jks").delete()
    }

    logging.captureStandardOutput(LogLevel.INFO)
    logging.captureStandardError(LogLevel.ERROR)
}
tasks.register<Exec>("regenerateCert") {
    group = "other"
    description = "Regenerates the P12 certificate store used by Lowkey Vault."
    outputs.file("${project.projectDir}/src/main/resources/cert/keystore.p12")
    outputs.file("${project.projectDir}/src/main/resources/cert/keystore.jks")
    inputs.file("${project.projectDir}/src/main/resources/cert/keystore.jks")
    workingDir(file("${project.projectDir}/src/main/resources/cert"))
    outputs.upToDateWhen { false }
    dependsOn(tasks.getByName("regenerateCertJks"))
    //convert to P12 using the old keytool to fix algorithm issues when used with old JDK (and still use P12)
    commandLine = mutableListOf("keytool",
            "-importkeystore",
            "-srckeystore", "keystore.jks",
            "-srcstorepass", "changeit",
            "-srcstoretype", "JKS",
            "-destkeystore", "keystore.p12",
            "-deststorepass", "changeit",
            "-deststoretype", "pkcs12")

    doFirst {
        file("${project.projectDir}/src/main/resources/cert/keystore.p12").delete()
    }
    doLast {
        file("${project.projectDir}/src/main/resources/cert/keystore.jks").delete()
    }

    logging.captureStandardOutput(LogLevel.INFO)
    logging.captureStandardError(LogLevel.ERROR)
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri(rootProject.extra.get("githubMavenRepoUrl").toString())
            credentials {
                username = rootProject.extra.get("gitUser").toString()
                password = rootProject.extra.get("gitToken").toString()
            }
        }
    }
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifact(tasks["bootJar"])
            artifactId = project.name
            pom {
                name.set(project.extra.get("artifactDisplayName").toString())
                description.set(project.extra.get("artifactDescription").toString())
                url.set(rootProject.extra.get("repoUrl").toString())
                packaging = "jar"
                licenses {
                    license {
                        name.set(rootProject.extra.get("licenseName").toString())
                        url.set(rootProject.extra.get("licenseUrl").toString())
                    }
                }
                developers {
                    developer {
                        id.set(rootProject.extra.get("maintainerId").toString())
                        name.set(rootProject.extra.get("maintainerName").toString())
                        email.set(rootProject.extra.get("maintainerUrl").toString())
                    }
                }
                scm {
                    connection.set(rootProject.extra.get("scmConnection").toString())
                    developerConnection.set(rootProject.extra.get("scmConnection").toString())
                    url.set(rootProject.extra.get("scmProjectUrl").toString())
                }
            }
            pom.withXml {
                asNode().apply {
                    (get("dependencies") as NodeList).forEach { depsNode ->
                        ((depsNode as Node).get("dependency") as NodeList).forEach { depNode ->
                            depsNode.remove(depNode as Node)
                        }
                    }
                }
            }
        }
    }
}

signing {
    sign(publishing.publications["mavenJava"])
}
