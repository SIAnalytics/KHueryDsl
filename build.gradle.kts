import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.20"
    `maven-publish`
}

group = "com.sia"
version = "1.0-SNAPSHOT"

repositories {
    jcenter()
    mavenCentral()
}

// dependency injection
val koinVersion = "2.2.+"

// hibernate
val hibernateVersion = "5.4.17.Final"
val hibernateTypeVersion = "2.9.+"

// database
val pgsqlDriverVersion = "42.2.+"

// time
val jodaTimeVersion = "2.10.+"
val kodaTimeVersion = "2.0.+"

// common
val config4kVersion = "0.4.+"

// test
val kotestVersion = "4.2.0"
val testContainersVersion = "1.15.+"
val mockkVersion = "1.10.4"

dependencies {
    api("org.jetbrains.kotlin:kotlin-reflect")

    api("org.koin:koin-core:$koinVersion")

    api("org.hibernate:hibernate-entitymanager:$hibernateVersion")
    api("org.hibernate:hibernate-spatial:$hibernateVersion")
    api("com.vladmihalcea:hibernate-types-52:$hibernateTypeVersion")

    api("io.github.config4k:config4k:$config4kVersion")

    api("joda-time:joda-time:$jodaTimeVersion")
    api("com.github.debop:koda-time:$kodaTimeVersion")

    testImplementation("io.kotest:kotest-runner-junit5-jvm:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core-jvm:$kotestVersion")

    testImplementation("io.mockk:mockk:$mockkVersion")
    testImplementation("org.testcontainers:postgresql:$testContainersVersion")
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "11"
}