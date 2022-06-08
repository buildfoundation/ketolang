>The future of general purpose _configuration_ languages.

Ketolang is a dialect of Kotlin without side-effects, it allows only data-classes and pure functions. 

Ketolang aims to cover use-cases of Starlark language but in compile-time, type-safe environment of Kotlin, with benefit of running in more performant environment than an interpreter (JVM, Native).

List of restricted Kotlin functionality (each option will have feature flag to enable/disable it):

- [x] File IO is not allowed (both reading and writing)
- [x] Network IO is not allowed (both reading and writing can be restricted)
- [x] `System.currentTimeMillis()` and other forms of resolving current time and date are not allowed
- [x] Reading environment variables is not allowed
- [x] Reflection is not allowed
- [x] Global mutable state is not allowed
- [x] Mutable collections as function args, return type or properties are not allowed
- [x] Delegated properties are not allowed
- [x] Type aliases are not allowed
- [x] Type casting to mutable types is not allowed (ie casting List to MutableList to get to mutable state)
- [x] Regular classes and interfaces are not allowed, however data classes, enums and objects are allowed
- [x] Code must be placed in named packages

## FAQ

Q: Why would you need such restrictive environment?
A: Ketolang is configuration language, that tries to guarantee reproducibility. It is suitable for build systems ([Bazel](https://bazel.build/rules/language)), CI systems ([Drone CI](https://docs.drone.io/pipeline/scripting/starlark/), [Cirrus CI](https://cirrus-ci.org/guide/programming-tasks/), [Kraken CI](https://kraken.ci/docs/features/)) or anything else when you want to have reproducible output for given input!

Q: Isn't Docker or other form of containerization suitable for this?
A: Containers do not offer native cross-platform (Linux, macOS, Windows) runtime, they don't prevent programs from reading current time, IO (although they limit mounted volumes and networking) & date, nor limit program reproducibility.

Q: Why Kotlin? There are languages like [Starlark](https://github.com/bazelbuild/starlark/blob/master/spec.md) exactly for this purpose!
A: Starlark is great, but Kotlin offers more — **compile-time type safety**, convenient collections/stream API, great IDE support and so on!

Q: What Kotlin runtime environments does Ketolang support?
A: JVM and Native, JS is out of scope right now, it should work but someone needs to take a lead on that as I am not proficient with JS runtime.

Q: How does Ketolang implement its restrictiveness?
A: Ketolang restricts Kotlin language and access Kotlin stdlib at compile time via compile plugin, it is also compiled with `-no-reflect -no-jdk`.

Q: Is it possible to use libraries with Ketolang?
A: Yes! Libraries should be included as sources to support both JVM and Native environments and to verify that libraries themselves don't violate Ketolang restrictions. However, if desired by integrating system — libraries of course can be linked dynamically to provide their API for side-effects in a controlled manner. This is how Starlark is integrated into Bazel, it includes Starlark and provides Bazel Starlark library on top.

Q: How to try Ketolang?
A: Easiest way right now is to clone the repo, open the project in IntelliJ and write tests (see [kotlinc/compiler-plugin-logic/src/test](kotlinc/compiler-plugin-logic/src/test) for reference)!

Q: How is Ketolang intended to be integrated into other systems such as Bazel?
A: Ketolang will be published as compiler plugin to Maven Central, then `.kt` Ketolang files need to be compiled with `kotlinc` targeting either JVM or Native platform with following options respectively `-no-reflect -no-jdk`, see [samples/cli/cli.sh](samples/cli/cli.sh) for reference!
