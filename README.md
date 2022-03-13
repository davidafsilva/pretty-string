# pretty-string

[![Master Build Status](https://img.shields.io/github/workflow/status/davidafsilva/pretty-string/Master%20Build?label=Build&style=flat-square)](https://github.com/davidafsilva/pretty-string/actions?query=workflow%3A%22Master+Build%22+branch%3Amain)
[![Latest Release](https://img.shields.io/maven-central/v/pt.davidafsilva.jvm/pretty-string?color=brightgreen&label=Latest%20Release&style=flat-square)](https://repo1.maven.org/maven2/pt/davidafsilva/jvm/kotlin/pretty-string/)
[![License](https://img.shields.io/github/license/davidafsilva/pretty-string?color=brightgreen&label=License&logo=License&style=flat-square)](https://opensource.org/licenses/BSD-3-Clause)

This small Kotlin library provides an alternative to the regular `toString` implementation that is generated for data
classes, which does not support prettifying its output right out-of-the-box.

## Table of Contents
* [Usage](#usage)
  + [Import](#import)
    - [Gradle](#gradle)
    - [Maven](#maven)
  + [API](#api)
    - [Example](#example)
* [Building](#building)

## Usage

### Import
1. Add maven central repository to your configuration
2. Import the library

#### Gradle
Groovy:
```kotlin
repositories {
    mavenCentral()
}
dependencies {
    implementation("pt.davidafsilva.jvm.kotlin:pretty-string:0.1.0")
}
```

#### Maven
```xml
<dependencies>
  <dependency>
    <groupId>pt.davidafsilva.jvm.kotlin</groupId>
    <artifactId>pretty-string</artifactId>
    <version>0.1.0</version>
  </dependency>
</dependencies>
```

### API

This public API is very small. In fact, it only exposes single `toPrettyString()` top-level function. It is defined 
as an extension of `Any?`, thus you should be able to call it with any receiver.

#### Example

Let's say you have the following data classes definitions:

```kotlin

data class Person(
    val fullName: String,
    val birthDate: LocalDate,
    val children: List<Person> = emptyList(),
    val socialProfiles: List<SocialProfile> = emptyList(),
)

data class SocialProfile(
    val name: String,
    val url: String
)
```

And you have the following instance of it:
```kotlin
val person = Person(
    fullName = "John Doe",
    birthDate = LocalDate.of(2000, Month.JANUARY, 31),
    children = listOf(
        Person(fullName = "John Doe Jr.", birthDate = LocalDate.of(2022, Month.MARCH, 13))
    )
)
```

Calling `person.toPrettyString()` should return something along the lines of:
```text
Person@3e3abc88(
  fullName = "John Doe",
  birthDate = "2000-01-31",
  children = [
    Person@7857fe2(
      fullName = "John Doe Jr.",
      birthDate = "2022-03-13",
      children = [],
      socialProfiles = [],
    ),
  ],
  socialProfiles = [],
)
```

## Building
At the project root, run the following command:
```shell
./gradlew clean build
```

The above command will run both the tests and verification checks.

## Disclaimer

The function has support for what I consider to be the bare minimum to properly prettify most of the data class
definitions we (developers) usually create. If there's something missing and you'd like to see it supported, feel free
to file an issue or just go ahead an open the PR with its support :)
