# Package Kotlin/JS codebase as NodeJS application

Uses Vercel's [NCC](https://github.com/vercel/ncc) tool to bundle the produced NodeJS
executable with all its dependencies into a ZIP file. Typically, the ZIP file will
contain `index.js`: the intent is that if unpacked into a temporary directory, you can
then simply run `node` on that unpacked directory.

This plugin will also try to add a `.nvmrc` file to the produced archive to indicate
which version of Node the bundle was built with.

The ZIP archive will automatically be built as a dependency of the `assemble` task.
Depending on the configuration, it will add a dependency on one of the following tasks:

- `packageNodeJsDistributableWithNCC`: use NCC to bundle the app and dependencies into a single file
- `packageNodeJsDistributableExploded`: simply copy the produced output and entire node_modules tree
  (rarely a good idea)

## Example usage

In `build.gradle.kts`:

```kotlin
plugins {
    kotlin("js")
    id("org.araqnid.kotlin-nodejs-application") version "0.0.1"
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
  minify.set(true)
  v8cache.set(false)
  sourceMap.set(false)
  useNcc.set(true)
}
```

## Configuration

This plugin defines a `nodeJsApplication` extension to receive settings. Most of the settings
correspond to options to pass to NCC.

### v8cache

Produce a V8 cache file (save some startup time).

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

### useNcc

Use NCC to bundle files (the default) as opposed to just copying the produced files and `node_modules`.
