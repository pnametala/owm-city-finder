plugins {
    id("com.google.cloud.tools.jib") version "1.1.1"
}

dependencies {
    compile(kotlin("stdlib-jdk8"))
    compile("io.javalin:javalin:${properties["javalin_version"]}")
    compile("org.slf4j:slf4j-simple:${properties["slf4j_version"]}")
    compile("org.apache.lucene:lucene-core:3.6.2")
    compile(project(":owm-city-finder-client"))
    testCompile("com.github.mvysny.dynatest:dynatest-engine:${properties["dynatest_version"]}")
}

val configureBintray = ext["configureBintray"] as (artifactId: String) -> Unit
configureBintray("owm-city-finder-server")

val zip by tasks.creating(Zip::class) {
    from("src/main/scripts")
    into("lib") {
        from(configurations.runtime.allArtifacts.files)
        from(configurations.runtime)
    }
}

artifacts {
    add("archives", zip)
}

jib {
    from {
        image = "openjdk:11"
    }
}