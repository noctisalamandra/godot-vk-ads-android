import com.android.build.gradle.internal.tasks.factory.dependsOn

plugins {
    id("com.android.library")
}

val pluginName = "GodotAndroidVkAds"
val pluginPackageName = "com.darkmoonight.godotandroidvkads"


android {
    namespace = pluginPackageName
    compileSdk = 34

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        minSdk = 24

        manifestPlaceholders["godotPluginName"] = pluginName
        manifestPlaceholders["godotPluginPackageName"] = pluginPackageName
        buildConfigField("String", "GODOT_PLUGIN_NAME", "\"${pluginName}\"")
        setProperty("archivesBaseName", pluginName)
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation("com.my.target:mytarget-sdk:5.27.1")         // Vk mobile ads
    implementation("org.godotengine:godot:4.3.0.stable")        // Godot
    implementation("androidx.test.ext:junit:1.2.1")
}

val copyDebugAARToPluginAddons by tasks.registering(Copy::class) {
    from("build/outputs/aar")
    include("$pluginName-debug.aar")
    into("plugin/addons/$pluginName/bin/debug")
}

val copyReleaseAARToPluginAddons by tasks.registering(Copy::class) {
    from("build/outputs/aar")
    include("$pluginName-release.aar")
    into("plugin/addons/$pluginName/bin/release")
}

val cleanPluginAddons by tasks.registering(Delete::class) {
    delete("plugin/addons/$pluginName")
}

val copyAddonsToPlugin by tasks.registering(Copy::class) {
    dependsOn(cleanPluginAddons)
    finalizedBy(copyDebugAARToPluginAddons)
    finalizedBy(copyReleaseAARToPluginAddons)

    from("export_scripts")
    into("plugin/addons/$pluginName")
}

tasks.named("assemble").configure {
    finalizedBy(copyAddonsToPlugin)
}

tasks.named<Delete>("clean").apply {
    dependsOn(cleanPluginAddons)
}