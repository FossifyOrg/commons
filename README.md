# Commons
Some helper functions, dialogs, etc. used by multiple Fossify apps.</br>
For reporting bugs/features that affect multiple apps, please use the <a href="https://github.com/FossifyOrg/General-Discussion">General Discussion</a> repository.

## Local development

### Publishing to local Maven repository

To publish this library to your local Maven repository (`~/.m2/repository`), use:

```bash
# Publish with a custom version
./gradlew -PVERSION=1.2.3 publishToMavenLocal

# Publish with git commit hash as version
./gradlew -PVERSION=$(git rev-parse --short HEAD) publishToMavenLocal
```

The version can also be set via `VERSION` environment variable.

### Using locally published build in other apps

1. **Ensure `mavenLocal()` is in the app's repositories** (`settings.gradle.kts`):

   ```kotlin
   dependencyResolutionManagement {
       repositories {
           mavenCentral()
           google()
           maven { setUrl("https://jitpack.io") }
           mavenLocal() // already exists in Fossify app repos
       }
   }
   ```

2. **Update the commons version** in the app's `gradle/libs.versions.toml`:

   ```toml
   [versions]
   commons = "1.2.3"  # Use the version you published
   ```

3. **Sync and build.**

> [!TIP]
> Gradle resolves dependencies in repository order. Since `mavenLocal()` is listed last, it will only be used if the version isn't found in other repositories. To force using local, use a unique version like `1.2.3-local` or a git commit hash.
