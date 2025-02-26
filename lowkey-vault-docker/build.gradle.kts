plugins {
    id("java")
    alias(libs.plugins.abort.mission)
}

version = rootProject.version
group = rootProject.group

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(project(":lowkey-vault-client"))
    testImplementation(libs.bouncycastle.bcpkix)
    testImplementation(libs.httpclient)
    testImplementation(libs.commons.codec)
    testImplementation(libs.azure.security.keyvault.keys) {
        exclude(group = "io.netty")
    }
    testImplementation(libs.azure.security.keyvault.secrets) {
        exclude(group = "io.netty")
    }
    testImplementation(libs.azure.security.keyvault.certificates) {
        exclude(group = "io.netty")
    }
    testImplementation(libs.bundles.cucumber)
    testImplementation(libs.abort.mission.cucumber)
    testImplementation(libs.findbugs.jsr305)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
    withJavadocJar()
    withSourcesJar()
}

var copyAppJar = tasks.register<Copy>("copyAppJar") {
    inputs.file(rootProject.project(":lowkey-vault-app").tasks.named("bootJar").get().outputs.files.singleFile)
    outputs.file(layout.buildDirectory.file("docker/lowkey-vault.jar").get().asFile)
    from(rootProject.project(":lowkey-vault-app").tasks.named("bootJar").get().outputs.files.singleFile)
    into(layout.buildDirectory.dir("docker/").get().asFile)
    rename {
        "lowkey-vault.jar"
    }
    dependsOn(":lowkey-vault-app:bootJar")
    dependsOn(":lowkey-vault-app:test")
}

var copyDockerFile = tasks.register<Copy>("copyDockerFile") {
    inputs.file(file("src/docker/Dockerfile"))
    outputs.file(layout.buildDirectory.file("docker/Dockerfile").get().asFile)
    from(file("src/docker/Dockerfile"))
    into(layout.buildDirectory.dir("docker/").get().asFile)
    dependsOn(copyAppJar)
}

var dockerBuild = tasks.register<Exec>("dockerBuild") {
    group = "Docker"
    inputs.file(layout.buildDirectory.file("docker/lowkey-vault.jar").get().asFile)
    inputs.dir(layout.buildDirectory.dir("docker"))
    workingDir = layout.buildDirectory.dir("docker").get().asFile
    commandLine = listOf(
            "docker", "build",
            "-t", "lowkey-vault:${rootProject.version}",
            "-t", "nagyesta/lowkey-vault:${rootProject.version}",
            "--build-arg", "BUILDPLATFORM=linux/amd64",
            "."
    )
    dependsOn(copyAppJar, copyDockerFile)
}

var dockerRun = tasks.register<Exec>("dockerRun") {
    group = "Docker"
    inputs.dir(layout.buildDirectory.dir("docker"))
    workingDir = layout.buildDirectory.dir("docker").get().asFile
    environment(mapOf("LOWKEY_ARGS" to "--LOWKEY_DEBUG_REQUEST_LOG=false " +
            "--LOWKEY_VAULT_NAMES=certs-generic,keys-generic,keys-delete,secrets-generic,secrets-delete " +
            "--LOWKEY_VAULT_ALIASES=keys-delete.localhost=keys-alias-delete.localhost:<port>,secrets-delete.localhost=secrets-alias-delete.localhost:<port>"))
    commandLine = listOf(
            "docker", "run", "--rm",
            "--platform", "linux/amd64",
            "--name", "lowkey-vault",
            "-e", "LOWKEY_ARGS",
            "-d",
            "-p", "8444:8443",
            "lowkey-vault:${rootProject.version}"
    )
    dependsOn(dockerBuild)
}

var dockerStop = tasks.register<Exec>("dockerStop") {
    group = "Docker"
    inputs.dir(layout.buildDirectory.dir("docker"))
    workingDir = layout.buildDirectory.dir("docker").get().asFile
    commandLine = listOf("docker", "stop", "lowkey-vault")
    dependsOn(dockerRun)
}

var dockerPush = tasks.register<Exec>("dockerPush") {
    group = "Docker"
    inputs.dir(layout.buildDirectory.dir("docker"))
    workingDir = layout.buildDirectory.dir("docker").get().asFile
    commandLine = listOf("docker", "push", "nagyesta/lowkey-vault:${rootProject.version}")
    dependsOn(dockerBuild, dockerStop)
}

tasks.test {
    inputs.file(file("src/docker/Dockerfile"))
    inputs.file(rootProject.project(":lowkey-vault-app").tasks.named("bootJar").get().outputs.files.singleFile)
    outputs.file(layout.buildDirectory.file("reports/abort-mission/abort-mission-report.json").get().asFile)
    outputs.dir(layout.buildDirectory.dir("reports/cucumber").get().asFile)
    useTestNG {
        systemProperty("cucumber.execution.parallel.enabled", System.getProperty("test.parallel"))
        systemProperty("cucumber.filter.tags", "not @ignore")
        systemProperty("abort-mission.report.directory", layout.buildDirectory.dir("reports/abort-mission/").get().asFile)
        systemProperty("abort-mission.force.abort.evaluators", rootProject.extra.get("dockerAbortGroups") as String)
        systemProperty("abort-mission.suppress.abort.evaluators", rootProject.extra.get("dockerSuppressGroups") as String)
    }
    dependsOn(dockerRun)
    finalizedBy(dockerStop)
}

abortMission {
    toolVersion = libs.versions.abortMission.get()
}

tasks.register("publish") {
    dependsOn("build")
    dependsOn(dockerPush)
}
