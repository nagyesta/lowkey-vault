import groovy.util.Node
import groovy.util.NodeList

plugins {
    id("java")
    signing
    `maven-publish`
    alias(libs.plugins.licensee.plugin)
}

group = rootProject.group

project.ext {
    set("artifactDisplayName", "Lowkey Vault - Testcontainers")
    set("artifactDescription", "Support for using Lowkey Vault with Testcontainers.")
}

dependencies {
    implementation(platform("org.testcontainers:testcontainers-bom:${libs.versions.testcontainers.get()}")) //import bom
    implementation(libs.testcontainers)
    testImplementation(libs.testcontainers.jupiter)
    testImplementation(project(":lowkey-vault-client"))
    testImplementation(libs.bundles.jackson)
    testImplementation(libs.httpclient)
    testImplementation(libs.commons.codec)
    testImplementation(libs.azure.security.keyvault.keys) {
        exclude(group = "io.netty")
    }
    testImplementation(libs.azure.security.keyvault.secrets) {
        exclude(group = "io.netty")
    }
    testImplementation(libs.mockito.core)
    testImplementation(libs.jupiter)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation(libs.logback.classic)
}

licensee {
    allow("Apache-2.0")
    allow("MIT")
    allow("EPL-1.0")
    allow("BSD-2-Clause")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    toolchain {
        languageVersion = JavaLanguageVersion.of(11)
    }
    withJavadocJar()
    withSourcesJar()
}

val copyLegalDocs = tasks.register<Copy>("copyLegalDocs") {
    from(file("${project.rootProject.projectDir}/LICENSE"))
    from(layout.buildDirectory.file("reports/licensee/artifacts.json").get().asFile)
    from(layout.buildDirectory.file("reports/bom.json").get().asFile)
    into(layout.buildDirectory.dir("resources/main/META-INF").get().asFile)
    rename("artifacts.json", "dependency-licenses.json")
    rename("bom.json", "SBOM.json")
}.get()
copyLegalDocs.dependsOn(tasks.licensee)
copyLegalDocs.dependsOn(tasks.cyclonedxBom)
tasks.javadoc.get().dependsOn(copyLegalDocs)
tasks.compileJava.get().dependsOn(copyLegalDocs)
tasks.processResources.get().finalizedBy(copyLegalDocs)

tasks.test {
    useJUnitPlatform()
    systemProperty("imageVersion", project.version)
    mustRunAfter(":lowkey-vault-docker:build")
    mustRunAfter(":lowkey-vault-docker:test")
    finalizedBy(tasks.getByName("jacocoTestReport"))
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
                            ((depNode as Node).get("scope") as NodeList).forEach { scope ->
                                if (scope is Node && "runtime" == scope.text()) {
                                    scope.setValue("compile")
                                }
                            }
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
