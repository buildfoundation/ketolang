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
A: To use it as configuration language. It is suitable for build systems ([Bazel](https://bazel.build/rules/language)), CI systems ([Drone CI](https://docs.drone.io/pipeline/scripting/starlark/), [Cirrus CI](https://cirrus-ci.org/guide/programming-tasks/), [Kraken CI](https://kraken.ci/docs/features/)).

Q: Why not use Docker or other form of containerization?
A: Containers do not offer native cross-platform (Linux, macOS, Windows) runtime, they don't prevent programs from reading current time & date, nor limit program mutability.

Q: Why Kotlin? There are languages like Starlark exactly for this purpose!
A: Starlark is great, but Kotlin offers more — compile-time type safety, convenient collections API, great IDE support and so on.

Q: What Kotlin runtime environments does Ketolang support?
A: JVM, we're working on Kotlin Native too, JS is out of scope right now but may be supported if someone can take a lead.

Q: How does Ketolang implement its restrictiveness?
A: Ketolang restricts Kotlin language and access Kotlin stdlib at compile time via compile plugin.

Q: Is it possible to use libraries with Ketolang?
A: Yes! Libraries should be included as sources to support JVM and Native environments and to verify that libraries don't violate Ketolang restrictions. However, if desired by integrating system — libraries of course can be linked dynamically to provide their API for side-effects in a controlled manner. This is how Starlark is integrated into Bazel, it includes Starlark and provides Bazel Starlark library on top.
