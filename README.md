The goal of Rikochet project is to create a Kotlin runtime environment in which File IO, Network IO and reading of environment variables and System time is prohibited.

List of restricted Kotlin functionality:

- [x] File IO is not allowed (both reading and writing can be restricted)
- Network IO is not allowed (both reading and writing can be restricted)
- [x] `System.currentTimeMillis()` and other forms of resolving current time and date are not allowed
- Reflection is not allowed
- [x] Static mutable state (companion object, static variables) is not allowed
- [x] Mutable collections as function args, return type or properties are not allowed
- [x] Delegated properties are not allowed
- [x] Type aliases are not allowed
- Type casting is not allowed (ie casting List to MutableList to get to mutable state)
- Classes and interfaces are not allowed, however data classes are allowed

## FAQ

Q: Why would you need such restrictive environment?
A: To create a version of Kotlin that is side-effectless. To then use it as embedded configuration language and so on. Ie in build systems.

Q: Why not use Docker or other form of containerization?
A: We'd love to, but containers do not offer cross-platform (Linux, macOS, Windows) support and are not feasible for embedded use-cases like a Build System configuration language, and container doesn't prevent program from reading current time & date.

Q: Why Kotlin? There are languages like Starlark exactly for this purpose!
A: Starlark is great, but Kotlin offers much more — compile-time type safety, convenient collections API, great IDE support and so on.

Q: Why Kotlin JVM?
A: Author of the project has 10+ years of experience with JVM, that's why.

Q: Can other Kotlin Runtimes be used?
A: Yes, in Kotlin JS or Kotlin Native can be used in theory if they can be configured with same level of restrictiveness. 
