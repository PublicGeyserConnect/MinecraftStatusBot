plugins {
    java
}

group = "com.github.jensco"
version = "1.0"

repositories {
    mavenCentral()

    maven("https://jitpack.io")
    maven("https://repo.opencollab.dev/main")
    maven("https://m2.chew.pro/snapshots/")
    maven("https://repo.rtm516.co.uk/main/")
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    implementation("net.dv8tion:JDA:5.0.0-beta.10")
    implementation("pw.chew:jda-chewtils:2.0-SNAPSHOT")

    implementation("ch.qos.logback", "logback-classic", "1.2.10")

    implementation("nl.vv32.rcon:rcon:1.2.0")

    // Web requests and json
    implementation("org.json:json:20230227")

    implementation("org.reflections:reflections:0.10.2")

    // Database implementations
    implementation("com.mysql:mysql-connector-j:8.0.33")
    implementation("org.xerial:sqlite-jdbc:3.42.0.0")
    implementation("com.zaxxer:HikariCP:5.0.1")

    // Pinging java and bedrock servers
    implementation("com.github.rtm516:minecraft-server-ping:c61c496104")
    implementation("com.nukkitx.protocol:bedrock-common:2.9.17-SNAPSHOT")
}

tasks {
    jar {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        archiveFileName.set("${project.name}.jar")

        from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })

        manifest {
            attributes(
                    "Class-Path" to configurations.runtimeClasspath.get().files.joinToString(" ") { it.name },
                    "Main-Class" to "com.github.jensco.Bot"
            )
        }
    }

    test {
        useJUnitPlatform()
    }
}

tasks.test {
    useJUnitPlatform()
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}