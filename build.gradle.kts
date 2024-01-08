plugins {
    kotlin("jvm") version "1.9.0"
    `maven-publish`
}

group = "com.github.WantToBeeMe"
version = "0.0.2"

repositories {
    mavenCentral()
    jcenter() //needed for joml and imgui
}

val lwjglVersion = "3.3.2"
val jomlVersion = "1.10.5"
val imguiVersion = "1.75-0.7.2"
val lwjglNativesWindows = "natives-windows"
//val lwjglNativesWindowsX = "natives-windows-x86"
//val lwjglNativesLinux = "natives-linux"

val imguiNativesWindows = "natives-windows"
//val imguiNativesWindowsX = "natives-windows-x86"
//val imguiNativesLinux = "natives-linux"
//val imguiNativesLinuxX = "natives-linux-x86"


dependencies {
    implementation(kotlin("stdlib"))
    testImplementation(kotlin("test"))
    implementation("org.yaml:snakeyaml:2.0")

    //imGUI
    implementation("io.imgui.java:binding:$imguiVersion")
    implementation("io.imgui.java:lwjgl3:$imguiVersion")
    runtimeOnly("io.imgui.java:$imguiNativesWindows:$imguiVersion")
    //runtimeOnly("io.imgui.java:$imguiNativesWindowsX:1.75-0.7.2")
    //runtimeOnly("io.imgui.java:$imguiNativesLinux:1.75-0.7.2")
    //runtimeOnly("io.imgui.java:$imguiNativesLinuxX:1.75-0.7.2")

    //LWJGL
    implementation(platform("org.lwjgl:lwjgl-bom:$lwjglVersion"))
    implementation("org.lwjgl", "lwjgl")
    implementation("org.lwjgl", "lwjgl-glfw")
    implementation("org.lwjgl", "lwjgl-opengl")
    implementation("org.lwjgl", "lwjgl-stb")
    //implementation("org.lwjgl", "lwjgl-assimp")
    //implementation("org.lwjgl", "lwjgl-nfd")
    //implementation("org.lwjgl", "lwjgl-openal")
    runtimeOnly("org.lwjgl", "lwjgl", classifier = lwjglNativesWindows)
    runtimeOnly("org.lwjgl", "lwjgl-glfw", classifier = lwjglNativesWindows)
    runtimeOnly("org.lwjgl", "lwjgl-opengl", classifier = lwjglNativesWindows)
    runtimeOnly("org.lwjgl", "lwjgl-stb", classifier = lwjglNativesWindows)
    //runtimeOnly("org.lwjgl", "lwjgl-assimp", classifier = lwjglNativesWindows)
    //runtimeOnly("org.lwjgl", "lwjgl-nfd", classifier = lwjglNativesWindows)
    //runtimeOnly("org.lwjgl", "lwjgl-openal", classifier = lwjglNativesWindows)
    implementation("org.joml", "joml", jomlVersion)
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(8)
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }

    repositories {
        mavenLocal()
    }
}
