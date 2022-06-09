>The future of general purpose _configuration_ languages.
Imagine hundreds of side-effect-free libraries, used together for complex configuration logic w/o worrying about a surprise from a line of code.
Imagine cloud lambda computing with reproducible functions written in a type-safe language.

Ketolang is a dialect of Kotlin without side-effects, it allows only immutable data and pure functions. 

Ketolang aims to cover use-cases of Starlark language but in compile-time, type-safe environment of Kotlin, with benefit of running in a more performant environment than an interpreter (JVM, Native and maybe JS).

List of restricted Kotlin functionality (feature toggles coming):

- [x] File IO is not allowed
- [x] Network IO is not allowed
- [x] Resolving current time and date is not allowed
- [x] Reading environment variables is not allowed
- [x] Reflection is not allowed
- [x] Global mutable state is not allowed
- [x] Mutable collections as function args, return type or properties are not allowed, however mutability is allowed within a function body
- [x] Delegated properties are not allowed
- [x] Type aliases are not allowed
- [x] Type casting to mutable types is not allowed (ie casting List to MutableList to access mutable state)
- [x] Regular classes and interfaces are not allowed, however data classes, enums, sealed classes and objects are allowed
- [x] Code must be placed in named packages
 
Cornerstone idea is that a function in Ketolang must produce same output for a given input no matter which phase of the Moon it is right now, while function parameters and return values must be immutable. 

Design of Ketolang allows an integration environment to apply optimizations like:

- Execute functions in parallel
- Cache function invocations

(That's what Bazel ~does with Starlark)

## FAQ

##### Q: Why would you need such restrictive environment?
A: To guarantee reproducibility of a program, when combined with other Ketolang libraries this means that complicated logic can be written w/o risk of unexpected execution result. It is suitable as configuration language for build systems like [Bazel](https://bazel.build/rules/language), [Buck](https://buck.build/) and many others, CI systems like [Drone CI](https://docs.drone.io/pipeline/scripting/starlark/), [Cirrus CI](https://cirrus-ci.org/guide/programming-tasks/), [Kraken CI](https://kraken.ci/docs/features/) or anything else when you want to have reproducible output for given input! Ketolang program can generate complicated static configurations (JSON, XML, YAML) w/ benefit of being written in a type-safe programming language.

##### Q: Isn't Docker or other form of containerization suitable for this?
A: Containers do not offer native cross-platform (Linux, macOS, Windows) runtime, they don't prevent programs from reading current time, IO (although they limit mounted volumes and networking) & date, nor limit program reproducibility, nor standardize reusable libraries.

##### Q: Why Kotlin? There are languages like [Starlark](https://github.com/bazelbuild/starlark/blob/master/spec.md) exactly for this purpose!
A: Starlark is great, but Kotlin offers more — **compile-time type safety**, convenient collections/stream API, great IDE support and so on!

##### Q: What Kotlin runtime environments does Ketolang support?
A: JVM and Native. JS is out of scope right now, it should work but someone needs to take a lead on that as I am not proficient with JS runtimes.

##### Q: How does Ketolang implement its restrictiveness?
A: Ketolang restricts Kotlin language and access to Kotlin stdlib at compile time via compile plugin, it is also compiled with `-no-reflect -no-jdk` to exclude reflection and JDK from linking.

##### Q: Is it possible to use libraries with Ketolang?
A: Yes! Ketolang libraries should be distributed as source archives to support both JVM and Native environments and to verify that libraries themselves don't violate Ketolang restrictions. Distributing libraries as sources means easy publishing — a GitHub repo with standardised (TODO) structure will be enough! However, if desired by integrating system — libraries of course can be linked dynamically. This is how Starlark is integrated into Bazel, it includes Starlark and provides Bazel Starlark library on top.

##### Q: How to try Ketolang?
A: Easiest way right now is to clone the repo, open the project in IntelliJ CE and write tests (see [kotlinc/compiler-plugin-logic/src/test](kotlinc/compiler-plugin-logic/src/test) for reference)!

##### Q: How will IDE integration work?
A: Ketolang will target IntelliJ CE, it will use existing Kotlin Common (MultiPlatform) support, meaning JDK functions will not be available, allowing only generic subset of Kotlin. Ketolang libraries will be dependent on as sources, thus be fully available to compiler, IDE and debugger!

##### Q: How Ketolang should be compiled?
A: Ketolang libraries should be distributed as source archives. Then a library should be compiled with Ketolang compiler plugin into a JVM or Native distributable w/ respective flags `-no-reflect -no-jdk`, this is to optimize code recompilation instead of compiling entire source set every time. Resulting set of JVM or Native libraries then can be combined into an executable. See [samples/cli/cli.sh](samples/cli/cli.sh) for reference!  

##### Q: Project Status?
A: Accumulating feedback from people in the industry: original Kotlin developers from JetBrains, Bazel and Starlark maintainers and such. Collecting bugs. Try it out, submit an example of broken code and/or PR to fix it! Once somewhat stabilized, there will be Maven Central publications and integration instructions.
