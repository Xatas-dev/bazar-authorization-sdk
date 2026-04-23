plugins {
    id ("java-library")
    id("com.google.protobuf") version "0.9.4"
    id("maven-publish")
}

java {
    withSourcesJar()
    withJavadocJar()
}

group = "org.bazar"
version = "1.0.0"

repositories {
    mavenCentral()
}

val grpcVersion = "1.59.0"
val protobufVersion = "3.24.4"
val grpcKotlinVersion = "1.4.0"

dependencies {
    // gRPC dependencies
    implementation("io.grpc:grpc-netty-shaded:$grpcVersion")
    implementation("io.grpc:grpc-protobuf:$grpcVersion")
    implementation("io.grpc:grpc-stub:$grpcVersion")
    implementation("com.google.protobuf:protobuf-java:$protobufVersion")
    implementation("javax.annotation:javax.annotation-api:1.3.2")

    // Logging
    implementation("org.slf4j:slf4j-api:2.0.9")
    implementation("ch.qos.logback:logback-classic:1.4.11")

    // Test dependencies
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("io.grpc:grpc-testing:$grpcVersion")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            groupId = group.toString()
            artifactId = "bazar-authorization-sdk"
            version = project.version.toString()
        }
    }

    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/Xatas-dev/bazar-authorization-sdk")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:$protobufVersion"
    }
    plugins {
        create("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:$grpcVersion"
        }
    }
    generateProtoTasks {
        all().forEach { task ->
            task.plugins {
                create("grpc")
            }
        }
    }
}

tasks.test {
    useJUnitPlatform()
}