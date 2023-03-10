# Package a Kotlin/JS-based NodeJS script/GitHub Action

[![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/org.araqnid.kotlin-nodejs-application?logo=gradle)](https://plugins.gradle.org/plugin/org.araqnid.kotlin-nodejs-application)
[![Kotlin](https://img.shields.io/badge/kotlin-1.8.10-blue.svg)](http://kotlinlang.org)
[![Gradle Build](https://github.com/araqnid/gradle-kotlin-nodejs-application/actions/workflows/gradle-build.yml/badge.svg)](https://github.com/araqnid/gradle-kotlin-nodejs-application/actions/workflows/gradle-build.yml)

Uses Vercel's [NCC](https://github.com/vercel/ncc) tool to bundle the produced NodeJS
executable with all its dependencies. There are two plugins provided:

- `org.araqnid.kotlin-nodejs-application` will build a ZIP archive of the bundled
  executable. This will contain an `index.js` and the contents can just be run by `node`
  after unpacking.
- `org.araqnid.kotlin-github-action` will write the bundled executable to the `dist`
  directory. This can then be checked in to the repo, which is how GitHub Actions expects
  to fetch executable scripts.

When using `org.araqnid.kotlin-nodejs-application`, a `.nvmrc` file will be inserted into the produced archive to
indicate which version of Node the bundle was built with.

The bundling will be added as a dependency of the `assemble` task.

## Example usage

### NodeJS application in ZIP file

In `build.gradle.kts`:

```kotlin
plugins {
  kotlin("js")
  id("org.araqnid.kotlin-nodejs-application") version "0.0.4"
}

kotlin {
  js(IR) {
    nodejs {}
    binaries.executable()
    useCommonJs()
  }
}

dependencies {

}

nodeJsApplication {
  // defaults
  nccVersion.set("latest")
  minify.set(true)
  v8cache.set(false)
  target.set("")
  sourceMap.set(true)
  // moduleName.set("project-name") // shouldn't be necessary
  // externalModules.add("aws-sdk") // would expect aws-sdk to be installed globally when executed
}
```

### GitHub action in `dist`

In `build.gradle.kts`:

```kotlin
plugins {
  kotlin("js")
  id("org.araqnid.kotlin-github-action") version "0.0.4"
}

kotlin {
  js(IR) {
    nodejs {}
    binaries.executable()
    useCommonJs()
  }
}

dependencies {

}

actionPackaging {
  // defaults
  nccVersion.set("latest")
  minify.set(true)
  target.set("")
  sourceMap.set(false)
  // moduleName.set("action-name") // shouldn't be necessary
  // externalModules.add("@actions/core") // would expect to use @actions/core installed on the runner
}
```


## Configuration

The plugins define either a `nodeJsApplication` or `actionPackaging` extension respectively to receive settings. Most of
the settings correspond to options to pass to NCC.

### v8cache

Produce a V8 cache file (save some startup time) (`nodeJsApplication` only).

### minify

Minify bundled Javascript. Licenses will be extracted to a `LICENSE.txt` file in the Zip file.

### target

Specify target ES variant (see https://webpack.js.org/configuration/target)

### sourceMap

Produce a source map alongside the runnable script.

### externalModules

Configure external modules that will not be included in the bundle.

### moduleName

Must match the root name of the JavaScript produced by the Kotlin compiler. The plugin will try to glean this from
the compilation task.

### nccVersion

Version of NCC to use (defaults to "latest").
