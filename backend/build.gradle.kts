import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("idea")
    id("eclipse")
    id("org.springframework.boot") version "2.2.6.RELEASE"
    id("io.spring.dependency-management") version "1.0.9.RELEASE"
    id("com.google.cloud.tools.jib") version "3.1.4"
    id("org.jlleitschuh.gradle.ktlint") version "9.2.1"
    id("org.openapi.generator") version "4.2.1"
    kotlin("jvm") version "1.3.71"
    kotlin("plugin.spring") version "1.3.71"
    kotlin("plugin.jpa") version "1.3.71"
}

java.sourceCompatibility = JavaVersion.VERSION_11

val postgresVersion = "42.2.8"
val alpineJreImage = "adoptopenjdk/openjdk11:alpine-jre"
val testContainersVersion = "1.15.3"
val openApiDocumentation = "${project.rootDir}/backend/documents/learn-tool.yaml"
val skipWebApp = "skipWebApp"

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.security:spring-security-oauth2-client")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.apache.commons:commons-csv:1.6")
    implementation("org.hibernate.search:hibernate-search-mapper-orm:6.0.7.Final")
    implementation("org.hibernate.search:hibernate-search-backend-lucene:6.0.7.Final")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("io.jsonwebtoken:jjwt-api:0.11.2")
    compileOnly("org.projectlombok:lombok")
    runtimeOnly("org.postgresql:postgresql:$postgresVersion")
    implementation("io.jsonwebtoken:jjwt-impl:0.11.2")
    implementation("io.jsonwebtoken:jjwt-jackson:0.11.2")
    annotationProcessor("org.projectlombok:lombok")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:2.1.0")
    testImplementation("pl.pragmatists:JUnitParams:1.1.1")
    testImplementation("org.testcontainers:testcontainers:$testContainersVersion")
    testImplementation("org.testcontainers:postgresql:$testContainersVersion")
}

tasks.withType<KotlinCompile> {
    if (!project.hasProperty(skipWebApp)) {
        dependsOn(":frontend:npm_run_build")
    }
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "1.8"
    }
}

tasks.test {
    useJUnit {
        includeCategories("de.mcella.spring.learntool.UnitTest")
    }
}

task<Test>("integrationTest") {
    useJUnit {
        includeCategories("de.mcella.spring.learntool.IntegrationTest")
    }
    maxParallelForks = 1
}

tasks.create<org.openapitools.generator.gradle.plugin.tasks.ValidateTask>("validateLearnToolOpenApi") {
    inputSpec.set(openApiDocumentation)
}

task("validateOpenApi") {
    dependsOn.add(listOf("validateLearnToolOpenApi"))
}

jib {
    from {
        image = "$alpineJreImage"
    }
    to {
        image = "learntool/backend"
    }
    //noinspection GroovyAssignabilityCheck
    container {
        jvmFlags = listOf("-Xmx1g", "-XX:+ExitOnOutOfMemoryError")
    }
}
