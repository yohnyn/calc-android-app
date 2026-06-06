# Project Agent Instructions

## Build And Compilation Policy

- Never run online, remote, or local compilation/build commands for this project.
- Do not run Gradle build, assemble, compile, install, or test tasks, including commands such as `./gradlew build`, `./gradlew assembleDebug`, and Android Studio builds.
- The user will execute all compilation, build, and test commands locally.
- Code review, source inspection, static reasoning, and non-compiling checks are allowed.
- After making changes, clearly tell the user which local build or test commands they may run, but do not execute those commands.
