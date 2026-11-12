plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "com.pokke.music"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.pokke.music"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }
    // phone = mobile APK (default); tv = Android TV / leanback APK (pokke-tv)
    flavorDimensions += "device"
    productFlavors {
        create("phone") {
            dimension = "device"
            applicationId = "com.pokke.music"
        }
        create("tv") {
            dimension = "device"
            applicationId = "com.pokke.music.tv"
            versionNameSuffix = "-tv"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    // TV sources live under src/main/tv (not the default src/tv).
    // Without this mapping the tv flavor never picks up the TV manifest,
    // so the launcher stays on MainActivity and the class is missing / wrong.
    sourceSets {
        getByName("tv") {
            manifest.srcFile("src/main/tv/AndroidManifest.xml")
            java.srcDirs("src/main/tv/java")
            res.srcDirs("src/main/tv/res")
        }
        // Keep phone on the default main sources only (TV code stays available
        // as library classes but is not the launcher).
        getByName("phone") {
            // default main is enough
        }
    }
}

val newPipeExtractorRaw: Configuration by configurations.creating {
    isTransitive = false
    isCanBeConsumed = false
}
val newPipeExtractorStripped = tasks.register<org.gradle.api.tasks.bundling.Jar>("stripNewPipeExtractorUtils") {
    archiveFileName.set("NewPipeExtractor-v0.26.3-noutils.jar")
    destinationDirectory.set(layout.buildDirectory.dir("stripped-libs"))
    from(provider { newPipeExtractorRaw.map { zipTree(it) } }) {
        exclude("org/schabi/newpipe/extractor/utils/Utils.class")
        exclude("org/schabi/newpipe/extractor/utils/Utils\$*.class")
    }
}

dependencies {
    implementation(project(":bitchord-sharedui"))
    newPipeExtractorRaw("com.github.TeamNewPipe:NewPipeExtractor:v0.26.3")
    val composeBom = platform("androidx.compose:compose-bom:2024.10.01")
    implementation(composeBom)
    androidTestImplementation(composeBom)
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    // Kyant Liquid Glass (Backdrop) — iOS-like refraction
    // vendored backdrop: com.pokke.music.ui.backdrop
    // local Capsule: com.pokke.music.ui.shapes
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.navigation:navigation-compose:2.8.3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.compose.animation:animation")
    implementation("androidx.compose.foundation:foundation")
    // Image loading
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("io.coil-kt:coil-gif:2.7.0")

    // Palette for dynamic color extraction from album art
    implementation("androidx.palette:palette-ktx:1.0.0")

    // Media
    implementation("androidx.media3:media3-exoplayer:1.4.1")
    implementation("androidx.media3:media3-session:1.4.1")
    implementation("androidx.media3:media3-ui:1.4.1")
    implementation("androidx.media3:media3-datasource-okhttp:1.4.1")
    // NewPipe Extractor: pinned to BitChord's tested v0.26.3. Strip its Utils
    // class before dexing to avoid duplicate utility classes in Android builds.
    implementation(files(newPipeExtractorStripped))
    implementation("com.github.TeamNewPipe:nanojson:e9d656ddb49a412a5a0a5d5ef20ca7ef09549996")
    implementation("org.jsoup:jsoup:1.22.2")
    implementation("com.squareup.okhttp3:okhttp:5.3.2")
    implementation("com.google.code.findbugs:jsr305:3.0.2")
    implementation("com.google.protobuf:protobuf-javalite:4.35.0")
    implementation("org.mozilla:rhino:1.8.1")
    implementation("org.mozilla:rhino-engine:1.8.1")
    // Preferences (Liquid Glass toggle, animated covers, auth state)
    implementation("androidx.datastore:datastore-preferences:1.1.1")
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
