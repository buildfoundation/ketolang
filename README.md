The goal of Rikochet project is to create a Kotlin runtime environment in which File IO, Network IO and reading of environment variables and System time is prohibited.

List of restricted functionality:

- [x] File IO is not allowed (both reading and writing can be restricted)
- Network IO is not allowed (both reading and writing can be restricted)
- [x] `System.currentTimeMillis()` and other forms of resolving current time and date are not allowed
- Reflection is not allowed
- Static mutable state (companion object, static variables) is not allowed
- Mutable collection as function args and return type are not allowed

## FAQ

Q: Why would you need such restrictive environment?
A: To use Kotlin as embedded configuration language. Ie in build systems.

Q: Why not use Docker or other form of containerization?
A: We'd love to, but containers do not offer cross-platform (Linux, macOS, Windows) support and are not feasible for embedded use-cases like a Build System configuration language, and container doesn't prevent program from reading current time & date.

Q: Why Kotlin? There are languages like Starlark exactly for this purpose!
A: Starlark is great, but Kotlin offers much more — compile-time type safety, convenient collections API, great IDE support and so on.

Q: Why Kotlin JVM?
A: Author of the project has 10+ years of experience with JVM, that's why.

Q: Can other Kotlin Runtimes be used?
A: Yes, in Kotlin JS or Kotlin Native can be used in theory if they can be configured with same level of restrictiveness. 
