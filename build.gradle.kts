import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm") version "2.4.20"
    kotlin("plugin.compose") version "2.4.20"
    kotlin("plugin.serialization") version "2.4.20"
    id("org.jetbrains.compose") version "1.12.2"
    id("com.google.devtools.ksp") version "2.3.12"
}

val desktopVersion = providers.gradleProperty("desktopVersion").get()
val androidBase = providers.gradleProperty("androidBase").get()
val androidCommit = providers.gradleProperty("androidCommit").get()

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin.compilerOptions.jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.generateKotlin", "true")
}

// Generates BuildConfig in the app's package, so code copied from Android compiles unchanged.
val generateBuildConfig = tasks.register("generateBuildConfig") {
    val outDir = layout.buildDirectory.dir("generated/buildconfig")
    val version = "$desktopVersion (A$androidBase)"
    inputs.property("version", version)
    inputs.property("commit", androidCommit)
    outputs.dir(outDir)
    doLast {
        val f = outDir.get().asFile.resolve("com/boldblot/backgammonplyground/BuildConfig.kt")
        f.parentFile.mkdirs()
        f.writeText(
            """
            package com.boldblot.backgammonplyground

            object BuildConfig {
                const val VERSION_NAME = "$version"
                const val VERSION_CODE = 1
                const val BUILD_LABEL = "A$androidBase"
                const val ANDROID_COMMIT = "$androidCommit"
                const val DEBUG = false
            }
            """.trimIndent() + "\n"
        )
    }
}
kotlin.sourceSets.main { kotlin.srcDir(generateBuildConfig) }

dependencies {
    implementation(compose.desktop.currentOs)
    implementation("org.jetbrains.compose.material3:material3:1.9.0")
    implementation("org.jetbrains.compose.material:material-icons-extended:1.7.3")
    implementation("org.jetbrains.compose.components:components-resources:1.12.2")
    implementation("org.jetbrains.compose.ui:ui-backhandler:1.12.2")
    implementation("org.jetbrains.androidx.navigation:navigation-compose:2.10.0")
    implementation("org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-compose:2.11.0")
    implementation("org.jetbrains.androidx.lifecycle:lifecycle-runtime-compose:2.11.0")
    implementation("androidx.room:room-runtime:2.8.5")
    ksp("androidx.room:room-compiler:2.8.5")
    implementation("androidx.sqlite:sqlite-bundled:2.7.1")
    implementation("androidx.datastore:datastore-preferences:1.2.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.11.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.11.0")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.11.0")
    testImplementation("androidx.room:room-testing:2.8.5")
}

// The engines: one JNI library per CPU level (engine/CMakeLists.txt), built with CMake and Ninja.
val engineLevels = listOf("x86-64", "x86-64-v3")
val engineBuildTasks = engineLevels.map { level ->
    tasks.register<Exec>("buildEngine-$level") {
        val buildDir = layout.buildDirectory.dir("engine/$level")
        inputs.dir("engine")
        outputs.dir(buildDir)
        // CMake skips bgjni, without an error, unless JAVA_HOME is set: use the JDK that runs Gradle.
        environment("JAVA_HOME", System.getProperty("java.home"))
        commandLine("sh", "-c",
            "cmake -S engine -B ${buildDir.get().asFile} -G Ninja -DCMAKE_BUILD_TYPE=Release -DBG_MARCH=$level > /dev/null && " +
                "cmake --build ${buildDir.get().asFile} > ${buildDir.get().asFile}.log 2>&1 || { tail -30 ${buildDir.get().asFile}.log; exit 1; }")
    }
}

// What the app reads from its bundle: engine data in common/, native libraries per OS (compose.application.resources.dir).
val stageAppResources = tasks.register<Sync>("stageAppResources") {
    into(layout.buildDirectory.dir("app-resources"))
    from("engine-data") { into("common/engine-data") }
    engineLevels.forEachIndexed { i, level ->
        from(layout.buildDirectory.file("engine/$level/libbgjni-$level.so")) { into("linux-x64/native") }
    }
    dependsOn(engineBuildTasks)
}

compose.desktop {
    application {
        nativeDistributions.appResourcesRootDir.set(layout.buildDirectory.dir("app-resources"))
        mainClass = "com.boldblot.backgammonplyground.MainKt"
        jvmArgs("--enable-native-access=ALL-UNNAMED")
        nativeDistributions {
            // AppImage here is only the app folder; scripts/package-appimage.sh makes the single file from it.
            targetFormats(TargetFormat.AppImage, TargetFormat.Deb)
            packageName = "BackgammonPlyGround"
            packageVersion = desktopVersion
            description = "Play a world-class backgammon engine, review every game, learn from every mistake"
            vendor = "Bold Blot"
            licenseFile.set(project.file("LICENSE"))
            modules("java.sql", "jdk.unsupported")
            linux {
                packageName = "backgammon-plyground"
                debMaintainer = "support@boldblot.com"
                menuGroup = "Game"
                appCategory = "games"
                shortcut = true
                iconFile.set(project.file("src/main/composeResources/drawable/plyground_logo.png"))
            }
        }
    }
}

compose.resources {
    packageOfResClass = "com.boldblot.backgammonplyground.generated.resources"
    generateResClass = always
}

tasks.test {
    useJUnit()
    // Engine-backed tests run against a host build of the JNI library (-PbgHostLib=...); skipped without it.
    providers.gradleProperty("bgHostLib").orNull?.let { systemProperty("bg.hostlib", it) }
    systemProperty("bg.datadir", file("engine-data/puretd").absolutePath)
    providers.gradleProperty("bgCalibrateGames").orNull?.let { systemProperty("bg.calibrate.games", it) }
    providers.gradleProperty("bgCalibrateSet").orNull?.let { systemProperty("bg.calibrate.set", it) }
    systemProperty("bg.exportdir", layout.buildDirectory.dir("mat-export").get().asFile.absolutePath)
    jvmArgs("--enable-native-access=ALL-UNNAMED")
}

tasks.matching { it.name == "prepareAppResources" }.configureEach { dependsOn(stageAppResources) }

// For scripts that start the app directly (no Gradle): java -cp "$(./gradlew -q printClasspath)" com.boldblot.backgammonplyground.MainKt
tasks.register("printClasspath") {
    dependsOn("classes", "prepareAppResources")
    doLast { println((sourceSets.main.get().runtimeClasspath).asPath) }
}
