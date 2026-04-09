# Network SDK - Publishing and Deployment Guide

This guide covers how to configure, publish, and deploy the Network SDK library to Maven repositories.

## Overview

The Network SDK library is configured as a Kotlin Multiplatform library and can be published to:
1. Local Maven repository (for local development and testing)
2. Maven Central Repository (official public releases)
3. JFrog Artifactory (private/enterprise repositories)
4. GitHub Packages

## Prerequisites

- Kotlin 2.3.20+
- Gradle 8.0+
- Maven publishing plugin
- Credentials for the target repository

## Configuration

### 1. Library Configuration (build.gradle.kts)

The network library's `build.gradle.kts` is configured with publishing support:

```kotlin
publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = "com.example"
            artifactId = "network-sdk"
            version = "1.0.0"
            
            afterEvaluate {
                from(components["release"])
            }
        }
    }
    
    repositories {
        maven {
            name = "LocalMaven"
            url = uri("${layout.buildDirectory}/repo")
        }
    }
}
```

### 2. Update Version and Metadata

Edit `network/build.gradle.kts`:

```kotlin
groupId = "com.example"          // Organization/company domain
artifactId = "network-sdk"       // Library name
version = "1.0.0"                // Semantic version
```

## Publishing Workflows

### Option 1: Publish to Local Maven Repository

Use this for local development and testing:

```bash
./gradlew :network:publishToMavenLocal
```

**Output location:**
- macOS/Linux: `~/.m2/repository/com/example/network-sdk/1.0.0/`
- Windows: `C:\Users\<USERNAME>\.m2\repository\com\example\network-sdk\1.0.0\`

**Use in another project:**

```kotlin
repositories {
    mavenLocal()
}

dependencies {
    implementation("com.example:network-sdk:1.0.0")
}
```

### Option 2: Publish to JFrog Artifactory

#### Step 1: Configure Credentials

Create or edit `~/.gradle/gradle.properties`:

```properties
artifactoryUsername=your_username
artifactoryPassword=your_api_key
```

Or set environment variables:

```bash
# macOS/Linux
export ARTIFACTORY_USERNAME=your_username
export ARTIFACTORY_PASSWORD=your_api_key

# Windows (PowerShell)
$env:ARTIFACTORY_USERNAME="your_username"
$env:ARTIFACTORY_PASSWORD="your_api_key"
```

#### Step 2: Update Repository URL

Edit `network/build.gradle.kts`:

```kotlin
repositories {
    maven {
        name = "Artifactory"
        url = uri("https://your-company.jfrog.io/artifactory/libs-release/")
        credentials {
            username = project.findProperty("artifactoryUsername") as String? ?: ""
            password = project.findProperty("artifactoryPassword") as String? ?: ""
        }
    }
}
```

#### Step 3: Publish

```bash
./gradlew :network:publish
```

### Option 3: Publish to Maven Central

Maven Central requires more steps:

#### Step 1: Create Sonatype Account

1. Sign up at https://issues.sonatype.org
2. Create a JIRA ticket requesting repository access
3. Verify domain ownership

#### Step 2: Configure Signing

Install GPG and create a key pair:

```bash
gpg --gen-key
gpg --list-keys  # Get your key ID
```

Add to `gradle.properties`:

```properties
signing.keyId=LAST_8_CHARS_OF_KEY_ID
signing.password=your_key_password
signing.secretKeyRingFile=/path/to/.gnupg/secring.gpg
```

#### Step 3: Configure Maven Central Repository

Edit `network/build.gradle.kts`:

```kotlin
publishing {
    repositories {
        maven {
            name = "MavenCentral"
            url = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = project.findProperty("ossUsername") as String? ?: ""
                password = project.findProperty("ossPassword") as String? ?: ""
            }
        }
    }
}
```

#### Step 4: Publish

```bash
./gradlew :network:publish
```

### Option 4: Publish to GitHub Packages

#### Step 1: Create GitHub Personal Access Token

1. Go to https://github.com/settings/tokens
2. Create a new token with `write:packages` scope

#### Step 2: Configure Credentials

Add to `~/.gradle/gradle.properties`:

```properties
gpr.user=your_github_username
gpr.key=your_personal_access_token
```

#### Step 3: Update Repository

Edit `network/build.gradle.kts`:

```kotlin
repositories {
    maven {
        name = "GitHubPackages"
        url = uri("https://maven.pkg.github.com/your-org/network-sdk")
        credentials {
            username = project.findProperty("gpr.user") as String?
            password = project.findProperty("gpr.key") as String?
        }
    }
}
```

#### Step 4: Publish

```bash
./gradlew :network:publish
```

## Versioning Strategy

Use Semantic Versioning (MAJOR.MINOR.PATCH):

```
1.0.0  = Initial release
1.1.0  = New features (backward compatible)
1.0.1  = Bug fixes (backward compatible)
2.0.0  = Breaking changes
```

Update version in `network/build.gradle.kts`:

```kotlin
version = "1.1.0"
```

## Complete Publishing Configuration

Here's a complete `network/build.gradle.kts` configuration:

```kotlin
plugins {
    // ... existing plugins ...
    id("maven-publish")
}

// ... kotlin and android blocks ...

publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = "com.example"
            artifactId = "network-sdk"
            version = "1.1.0"

            afterEvaluate {
                from(components["release"])
            }

            pom {
                name.set("Network SDK")
                description.set("Kotlin Multiplatform HTTP Network Library")
                url.set("https://github.com/your-org/network-sdk")

                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }

                developers {
                    developer {
                        id.set("your-id")
                        name.set("Your Name")
                        email.set("your-email@example.com")
                    }
                }

                scm {
                    connection.set("scm:git:github.com/your-org/network-sdk.git")
                    developerConnection.set("scm:git:ssh://github.com/your-org/network-sdk.git")
                    url.set("https://github.com/your-org/network-sdk")
                }
            }
        }
    }

    repositories {
        maven {
            name = "LocalMaven"
            url = uri("${layout.buildDirectory}/repo")
        }

        maven {
            name = "Artifactory"
            url = uri("https://your-company.jfrog.io/artifactory/libs-release/")
            credentials {
                username = project.findProperty("artifactoryUsername") as String? ?: ""
                password = project.findProperty("artifactoryPassword") as String? ?: ""
            }
        }

        maven {
            name = "MavenCentral"
            url = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = project.findProperty("ossUsername") as String? ?: ""
                password = project.findProperty("ossPassword") as String? ?: ""
            }
        }

        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/your-org/network-sdk")
            credentials {
                username = project.findProperty("gpr.user") as String?
                password = project.findProperty("gpr.key") as String?
            }
        }
    }
}
```

## Using Published Library

### From Maven Local

```kotlin
repositories {
    mavenLocal()
}

dependencies {
    implementation("com.example:network-sdk:1.0.0")
}
```

### From Artifactory

```kotlin
repositories {
    maven {
        url = uri("https://your-company.jfrog.io/artifactory/libs-release/")
    }
}

dependencies {
    implementation("com.example:network-sdk:1.0.0")
}
```

### From Maven Central

```kotlin
repositories {
    mavenCentral()
}

dependencies {
    implementation("com.example:network-sdk:1.0.0")
}
```

## Troubleshooting

### "Credentials not found" error

Ensure credentials are in:
- `~/.gradle/gradle.properties`
- Environment variables
- Or passed as Gradle properties: `./gradlew publish -PartifactoryUsername=user -PartifactoryPassword=pass`

### "Repository not found" error

Verify:
- Repository URL is correct
- You have write permissions
- Repository exists or auto-creation is enabled

### "Signing failed" error (Maven Central)

Ensure:
- GPG is installed and configured
- Key ID is correct in gradle.properties
- GPG agent is running

## Continuous Integration Setup

### GitHub Actions Example

Create `.github/workflows/publish.yml`:

```yaml
name: Publish to Maven Central

on:
  release:
    types: [published]

jobs:
  publish:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Set up JDK 11
        uses: actions/setup-java@v3
        with:
          java-version: '11'
      
      - name: Publish to Maven Central
        run: ./gradlew :network:publish
        env:
          SIGNING_KEY_ID: ${{ secrets.SIGNING_KEY_ID }}
          SIGNING_PASSWORD: ${{ secrets.SIGNING_PASSWORD }}
          OSSRH_USERNAME: ${{ secrets.OSSRH_USERNAME }}
          OSSRH_PASSWORD: ${{ secrets.OSSRH_PASSWORD }}
```

## Next Steps

1. Choose your target repository
2. Configure credentials
3. Update library metadata (version, description)
4. Run publish command
5. Verify in repository UI

For more information, see the main [README.md](./README.md)

