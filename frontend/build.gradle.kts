plugins {
	base
	id("com.github.node-gradle.node") version "1.5.1"
}

tasks {
	named("npm_run_build") {
		dependsOn("npm_install")
	}
}
