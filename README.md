# Package a Kotlin/JS-based NodeJS script/GitHub Action

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
  nccVersion.set("latest")
  target.set("")
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
