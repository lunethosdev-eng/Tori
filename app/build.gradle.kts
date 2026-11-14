plugins {
    id("com.android.application")
    // CRITICAL: android.builtInKotlin=false requires this or .kt files never reach the DEX
    // (ClassNotFoundException: MainActivity / TvMainActivity).
    id("org.jetbrains.kotlin.android")
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
        multiDexEnabled = true

        // Public Supabase project settings for the community plugin catalog.
        // Configure with Gradle properties or TORI_PLUGINS_SUPABASE_* environment variables.
        val pluginsSupabaseUrl = (project.findProperty("pluginsSupabaseUrl") as String?)
            ?: System.getenv("TORI_PLUGINS_SUPABASE_URL").orEmpty()
        val pluginsSupabaseAnonKey = (project.findProperty("pluginsSupabaseAnonKey") as String?)
            ?: System.getenv("TORI_PLUGINS_SUPABASE_ANON_KEY").orEmpty()
        buildConfigField("String", "PLUGINS_SUPABASE_URL", "\\"${pluginsSupabaseUrl.replace("\\", "\\\\").replace("\\\"","\\\\\\\"")}\\"")
        buildConfigField("String", "PLUGINS_SUPABASE_ANON_KEY", "\\"${pluginsSupabaseAnonKey.replace("\\", "\\\\").replace("\\\"","\\\\\\\"")}\\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }
    // phone = mobile APK; tv = Android TV / leanback APK
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
    androidResources {
        // MediaPlayer uses AssetFileDescriptor for bundled Canvas clips.
        noCompress += listOf("mp4", "webm")
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    // TV code lives under src/main/tv (not the default src/tv).
    sourceSets {
        named("tv") {
            manifest.srcFile("src/main/tv/AndroidManifest.xml")
            java.srcDir("src/main/tv/java")
            kotlin.srcDir("src/main/tv/java")
            res.srcDir("src/main/tv/res")
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
    implementation("androidx.multidex:multidex:2.0.1")
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
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.navigation:navigation-compose:2.8.3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.compose.animation:animation")
    implementation("androidx.compose.foundation:foundation")
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("io.coil-kt:coil-gif:2.7.0")
    implementation("androidx.palette:palette-ktx:1.0.0")
    implementation("androidx.media3:media3-exoplayer:1.4.1")
    implementation("androidx.media3:media3-session:1.4.1")
    implementation("androidx.media3:media3-ui:1.4.1")
    implementation("androidx.media3:media3-datasource-okhttp:1.4.1")
    implementation(files(newPipeExtractorStripped))
    implementation("com.github.TeamNewPipe:nanojson:e9d656ddb49a412a5a0a5d5ef20ca7ef09549996")
    implementation("org.jsoup:jsoup:1.22.2")
    implementation("com.squareup.okhttp3:okhttp:5.3.2")
    implementation("com.google.code.findbugs:jsr305:3.0.2")
    implementation("com.google.protobuf:protobuf-javalite:4.35.0")
    implementation("org.mozilla:rhino:1.8.1")
    implementation("org.mozilla:rhino-engine:1.8.1")
    implementation("androidx.datastore:datastore-preferences:1.1.1")
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
