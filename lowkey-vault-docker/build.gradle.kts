plugins {
    id("java")
    alias(libs.plugins.abort.mission)
    alias(libs.plugins.docker)
    alias(libs.plugins.docker.run)
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

tasks.register<Copy>("copyAppJar") {
    inputs.file(rootProject.project(":lowkey-vault-app").tasks.named("bootJar").get().outputs.files.singleFile)
    outputs.file(layout.buildDirectory.file("app/lowkey-vault.jar").get().asFile)
    from(rootProject.project(":lowkey-vault-app").tasks.named("bootJar").get().outputs.files.singleFile)
    into(layout.buildDirectory.dir("app/").get().asFile)
    rename {
        "lowkey-vault.jar"
    }
    dependsOn(":lowkey-vault-app:bootJar")
    dependsOn(":lowkey-vault-app:test")
}

docker {
    name = "lowkey-vault:${rootProject.version}"
    tag("dockerNagyesta", "nagyesta/lowkey-vault:${rootProject.version}")
    setDockerfile(file("src/docker/Dockerfile"))
    files(layout.buildDirectory.file("app/lowkey-vault.jar").get().asFile)
    buildArgs(mapOf("BUILDPLATFORM" to "linux/amd64"))
    pull(true)
    noCache(true)
}
tasks.getByName("dockerPrepare").inputs.file(layout.buildDirectory.file("app/lowkey-vault.jar").get().asFile)
tasks.getByName("dockerPrepare").dependsOn(tasks.getByName("copyAppJar"))
tasks.getByName("clean").mustRunAfter(tasks.getByName("dockerClean"))

dockerRun {
    name = "lowkey-vault"
    image = "lowkey-vault:${rootProject.version}"
    ports("8444:8443")
    daemonize = true
    arguments("--rm", "--platform", "linux/amd64")
    env(mapOf("LOWKEY_ARGS" to "--LOWKEY_DEBUG_REQUEST_LOG=false " +
            "--LOWKEY_VAULT_NAMES=certs-generic,keys-generic,keys-delete,secrets-generic,secrets-delete " +
            "--LOWKEY_VAULT_ALIASES=keys-delete.localhost=keys-alias-delete.localhost:<port>,secrets-delete.localhost=secrets-alias-delete.localhost:<port>"))
}
tasks.getByName("dockerRun").dependsOn(tasks.getByName("docker"))

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
    dependsOn(tasks.getByName("dockerRun"))
    finalizedBy(tasks.getByName("dockerStop"))
}

abortMission {
    toolVersion = libs.versions.abortMission.get()
}

tasks.register("publish") {
    dependsOn("build")
    dependsOn("dockerPushDockerNagyesta")
}
