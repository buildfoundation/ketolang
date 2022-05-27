Rikochet is a dialect of Kotlin without side-effects.

List of restricted Kotlin functionality (each option has feature flag to enable/disable it):

- File IO is not allowed (both reading and writing can be restricted)
- Network IO is not allowed (both reading and writing can be restricted)
- `System.currentTimeMillis()` and other forms of resolving current time and date are not allowed
- Reading environment variables is not allowed
- Reflection is not allowed
- [x] Static mutable state (companion object, top level static variables) is not allowed
- [x] Mutable collections as function args, return type or properties are not allowed
- [x] Delegated properties are not allowed
- [x] Type aliases are not allowed
- Type casting is not allowed (ie casting List to MutableList to get to mutable state)
- Classes and interfaces are not allowed, however data classes are allowed

## FAQ

Q: Why would you need such restrictive environment?
A: To use it as embedded configuration language and so on. Ie in build systems such as Bazel.

Q: Why not use Docker or other form of containerization?
A: We'd love to, but containers do not offer cross-platform (Linux, macOS, Windows) support and are not feasible for embedded use-cases like a Build System configuration language, and container doesn't prevent program from reading current time & date.

Q: Why Kotlin? There are languages like Starlark exactly for this purpose!
A: Starlark is great, but Kotlin offers much more — compile-time type safety, convenient collections API, great IDE support and so on.

Q: What Kotlin runtime environments does Rikochet support?
A: JVM, we're working on Kotlin Native too, JS is out of scope right now.

Q: How does Rikochet implement its restrictiveness?
A: Rikochet restricts Kotlin at compile time via compile plugin (KSP).

Q: Is it possible to use libraries with Rikochet?
A: Yes! But with a caveat: most libraries should be included as sources to support JVM and Native environments and to verify that libraries don't violate Rikochet restrictions. However, if desired by integration logic — libraries of course can provide their API for side-effects in a controlled manner. This is how Starlark is integrated into Bazel.
