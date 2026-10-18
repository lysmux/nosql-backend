import org.springframework.boot.gradle.tasks.run.BootRun

plugins {
    kotlin("jvm") version "2.4.20"
    kotlin("plugin.spring") version "2.4.20"
    id("org.springframework.boot") version "4.1.1"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.openapi.generator") version "7.14.0"
    id("dev.detekt") version "2.0.0-alpha.6"
}

group = "ru.itmo"
version = "1.0.0"

kotlin {
    jvmToolchain(21)
    compilerOptions {
        freeCompilerArgs.add("-Xjsr305=strict")
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.typesafe:config:1.4.3")

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.security:spring-security-crypto")
    implementation("tools.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
    implementation(platform("io.mongock:mongock-bom:5.5.1"))
    implementation("io.mongock:mongock-springboot-v3")
    implementation("io.mongock:mongodb-springdata-v4-driver")

    implementation("org.springframework.boot:spring-boot-starter-actuator")
    runtimeOnly("io.micrometer:micrometer-registry-prometheus")

    implementation(files("libs/riak-client-73d87d1.jar"))
    runtimeOnly("com.google.protobuf:protobuf-java:3.25.9")
    runtimeOnly("io.netty:netty-all")
    runtimeOnly("com.fasterxml.jackson.core:jackson-databind")
    runtimeOnly("com.fasterxml.jackson.datatype:jackson-datatype-joda")
    runtimeOnly("commons-codec:commons-codec")
    runtimeOnly("org.erlang.otp:jinterface:1.6.1")
    runtimeOnly("javax.xml.bind:jaxb-api:2.3.1")

    detektPlugins("dev.detekt:detekt-rules-ktlint-wrapper:2.0.0-alpha.6")
}

detekt {
    config.setFrom("config/detekt/detekt.yml")
    buildUponDefaultConfig = true
    basePath = projectDir
}

configurations.matching { it.name == "detekt" }.all {
    resolutionStrategy.eachDependency {
        if (requested.group == "org.jetbrains.kotlin") useVersion("2.4.10")
    }
}

val openApiSpec = layout.projectDirectory.file("openapi/generated/openapi.yaml")
val openApiOutput = layout.buildDirectory.dir("generated/openapi")

openApiGenerate {
    generatorName = "kotlin-spring"
    inputSpec = openApiSpec.asFile.invariantSeparatorsPath
    outputDir = openApiOutput.map { it.asFile.invariantSeparatorsPath }

    apiPackage = "ru.itmo.delivery.entrypoint.api"
    modelPackage = "ru.itmo.delivery.entrypoint.api.model"
    modelNameSuffix = "Dto"

    configOptions = mapOf(
        "interfaceOnly" to "true",
        "useSpringBoot3" to "true",
        "useBeanValidation" to "false",
        "documentationProvider" to "none",
        "annotationLibrary" to "none",
        "enumPropertyNaming" to "UPPERCASE",
    )

    typeMappings = mapOf("DateTime" to "Instant")
    importMappings = mapOf("Instant" to "java.time.Instant")
    schemaMappings = mapOf("NotificationEventStream" to "org.springframework.web.servlet.mvc.method.annotation.SseEmitter")

    globalProperties = mapOf("apis" to "", "models" to "")
    cleanupOutput = true
}

sourceSets.main {
    kotlin.srcDir(openApiOutput.map { it.dir("src/main/kotlin") })
}

tasks.compileKotlin {
    dependsOn(tasks.openApiGenerate)
}

tasks.register<Exec>("tspCompile") {
    workingDir = layout.projectDirectory.dir("openapi").asFile
    val npm = if (System.getProperty("os.name").startsWith("Windows")) "npm.cmd" else "npm"
    commandLine(npm, "run", "build")
}

tasks.named<BootRun>("bootRun") {
    systemProperty("config.resource", "local.conf")
}