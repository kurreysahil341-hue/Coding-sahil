package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.model.ProjectData
import com.example.ui.screens.getFileContent
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object ProjectZipExporter {

    /**
     * Packages the project into a real .zip archive containing all source files,
     * Gradle wrapper configs, GitHub Actions APK workflows, and manifests.
     */
    fun packageProjectToZip(context: Context, project: ProjectData): Pair<File, Long> {
        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val safeName = project.title.lowercase().replace("[^a-z0-9]".toRegex(), "_")
        val zipFile = File(exportDir, "${safeName}_export.zip")

        // Map of relative file path -> content string
        val filesToZip = mapOf(
            "app/src/main/java/com/example/MainActivity.kt" to getFileContent("MainActivity.kt", project.code),
            "app/src/main/java/com/example/model/Models.kt" to getFileContent("Models.kt", ""),
            "app/src/main/java/com/example/data/SavedAppEntity.kt" to getFileContent("SavedAppEntity.kt", ""),
            "app/src/main/AndroidManifest.xml" to """
                <?xml version="1.0" encoding="utf-8"?>
                <manifest xmlns:android="http://schemas.android.com/apk/res/android">
                    <uses-permission android:name="android.permission.INTERNET" />
                    <application
                        android:allowBackup="true"
                        android:label="${project.title}"
                        android:theme="@style/Theme.Material3.DayNight.NoActionBar">
                        <activity
                            android:name=".MainActivity"
                            android:exported="true">
                            <intent-filter>
                                <action android:name="android.intent.action.MAIN" />
                                <category android:name="android.intent.category.LAUNCHER" />
                            </intent-filter>
                        </activity>
                    </application>
                </manifest>
            """.trimIndent(),
            "app/build.gradle.kts" to getFileContent("build.gradle.kts", ""),
            "settings.gradle.kts" to """
                pluginManagement {
                    repositories {
                        google()
                        mavenCentral()
                        gradlePluginPortal()
                    }
                }
                dependencyResolutionManagement {
                    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
                    repositories {
                        google()
                        mavenCentral()
                    }
                }
                rootProject.name = "${project.title}"
                include(":app")
            """.trimIndent(),
            "gradle.properties" to """
                org.gradle.jvmargs=-Xmx4g -Dfile.encoding=UTF-8
                org.gradle.parallel=true
                kotlin.code.style=official
                android.nonTransitiveRClass=true
                org.gradle.caching=true
                org.gradle.configuration-cache=true
                googleServices.missing.passthrough=true
            """.trimIndent(),
            "gradle/wrapper/gradle-wrapper.properties" to """
                distributionBase=GRADLE_USER_HOME
                distributionPath=wrapper/dists
                distributionUrl=https\://services.gradle.org/distributions/gradle-9.3.1-bin.zip
                networkTimeout=10000
                validateDistributionUrl=true
                zipStoreBase=GRADLE_USER_HOME
                zipStorePath=wrapper/dists
            """.trimIndent(),
            ".github/workflows/build-apk.yml" to """
                name: Build Android Debug APK

                on:
                  push:
                    branches: [ main, master ]
                  pull_request:
                    branches: [ main, master ]
                  workflow_dispatch:

                permissions:
                  contents: write

                jobs:
                  build:
                    name: Build Debug APK
                    runs-on: ubuntu-latest

                    steps:
                      - name: Checkout Code
                        uses: actions/checkout@v4
                        with:
                          fetch-depth: 0

                      - name: Set up JDK 21
                        uses: actions/setup-java@v4
                        with:
                          distribution: 'temurin'
                          java-version: '21'

                      - name: Setup Gradle
                        uses: gradle/actions/setup-gradle@v4
                        with:
                          gradle-home-cache-cleanup: true

                      - name: Make Gradle Wrapper Executable
                        run: chmod +x ./gradlew

                      - name: Prepare Debug Keystore
                        run: |
                          if [ -f debug.keystore.base64 ]; then
                            base64 -d debug.keystore.base64 > debug.keystore
                          elif [ ! -f debug.keystore ]; then
                            keytool -genkeypair -alias androiddebugkey -keypass android -keystore debug.keystore -storepass android -dname "CN=Android Debug,O=Android,C=US" -keyalg RSA -keysize 2048 -validity 10000
                          fi

                      - name: Build Debug APK
                        run: ./gradlew assembleDebug --stacktrace

                      - name: Upload Debug APK Artifact
                        uses: actions/upload-artifact@v4
                        with:
                          name: AI-Studio-Debug-APK
                          path: app/build/outputs/apk/debug/*.apk
                          retention-days: 14
            """.trimIndent(),
            "gradlew" to """#!/bin/sh
# Gradle wrapper launcher script
APP_HOME="`pwd -P`"
CLASSPATH="`echo "${'$'}APP_HOME"/gradle/wrapper/gradle-wrapper.jar`"
java -jar "${'$'}CLASSPATH" "${'$'}@"
""".trimIndent(),
            "gradlew.bat" to """@rem Gradle wrapper script for Windows
@if "%DEBUG%" == "" @echo off
set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%
set CLASSPATH=%APP_HOME%\gradle\wrapper\gradle-wrapper.jar
java.exe -jar "%CLASSPATH%" %*
""".trimIndent(),
            "README.md" to """
                # ${project.title}
                Generated via Google AI Studio Build.
                
                ## Automatic GitHub Actions APK Build
                This repository includes `.github/workflows/build-apk.yml`.
                When pushed to GitHub, GitHub Actions will automatically compile and produce `app-debug.apk` under the **Actions** tab as a downloadable artifact!
                
                ## Local Build Commands
                ```bash
                chmod +x gradlew
                ./gradlew assembleDebug
                ```
                The output APK is generated at `app/build/outputs/apk/debug/app-debug.apk`.
            """.trimIndent(),
            ".gitignore" to """
                *.iml
                .gradle
                /local.properties
                /.idea/caches
                /.idea/libraries
                /.idea/modules.xml
                /.idea/workspace.xml
                .DS_Store
                /build
                /captures
                .externalNativeBuild
                .cxx
                local.properties
                .env
                debug.keystore

                !gradle/wrapper/gradle-wrapper.jar
                !gradlew
                !gradlew.bat
            """.trimIndent()
        )

        ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFile))).use { zos ->
            for ((relativePath, content) in filesToZip) {
                val entry = ZipEntry(relativePath)
                zos.putNextEntry(entry)
                zos.write(content.toByteArray(Charsets.UTF_8))
                zos.closeEntry()
            }
        }

        return Pair(zipFile, zipFile.length())
    }

    /**
     * Triggers the Android system share/download intent for the exported ZIP file.
     */
    fun shareOrDownloadZip(context: Context, zipFile: File) {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                zipFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/zip"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Exported Project: ${zipFile.name}")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Save or Share Project ZIP").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } catch (_: Exception) {
            // Fallback for environments without FileProvider declaration
        }
    }
}
