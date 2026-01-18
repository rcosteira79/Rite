# kotlin-inject Assessment for HabitLock

**Date:** January 18, 2026  
**Context:** Evaluating kotlin-inject as an alternative to Metro DI for Kotlin Multiplatform

---

## Overview of kotlin-inject

**Repository:** https://github.com/evant/kotlin-inject  
**Current Version:** ~0.7.2  
**Maintainer:** Eva Tatarka (Google Engineer)

### What is kotlin-inject?

kotlin-inject is a compile-time dependency injection library for Kotlin Multiplatform that uses KSP (Kotlin Symbol Processing) to generate dependency injection code. It's designed to be:
- Fast (compile-time, no reflection)
- Type-safe
- Minimal runtime overhead
- Fully Kotlin Multiplatform compatible

---

## Key Features

### ✅ Pros

1. **Full KMP Support**
   - Works seamlessly across all KMP targets (Android, iOS, JVM, JS, Native)
   - No platform-specific code generation issues
   - Battle-tested in production KMP apps

2. **Compile-Time Safety**
   - All dependencies resolved at compile time
   - Type-safe dependency graph
   - Circular dependency detection
   - Missing dependency detection

3. **Lightweight**
   - Minimal runtime library (~10KB)
   - No reflection
   - No runtime performance overhead
   - Generated code is readable and debuggable

4. **Simple API**
   - Uses standard Kotlin annotations
   - `@Inject` for constructor injection
   - `@Component` for dependency graphs
   - `@Provides` for factory methods
   - `@Scope` for scoped instances

5. **KSP-Based**
   - Fast code generation
   - Incremental compilation support
   - Better than kapt performance

6. **Active Development**
   - Regular updates
   - Good community support
   - Used by major projects (Cash App, etc.)

### ⚠️ Cons

1. **Learning Curve**
   - Requires understanding of DI concepts
   - Different from manual DI patterns
   - Generated code needs to be understood for debugging

2. **Build Configuration**
   - Requires KSP setup
   - Additional build complexity
   - Potential build time increase (though faster than kapt)

3. **Limited Documentation**
   - Fewer examples compared to Dagger/Hilt
   - Community smaller than Dagger ecosystem
   - Need to read source/tests for advanced features

4. **KSP Dependency**
   - Requires KSP plugin
   - Gradle sync may be slower
   - Generated code needs to be checked into VCS for some CI setups

---

## Comparison: Manual DI vs kotlin-inject vs Metro

| Feature | Manual DI (Current) | kotlin-inject | Metro |
|---------|---------------------|---------------|-------|
| **KMP Support** | ✅ Full | ✅ Full | ⚠️ Limited |
| **Type Safety** | ✅ Compile-time | ✅ Compile-time | ✅ Compile-time |
| **Build Time** | ✅ Fast | ⚠️ Moderate (KSP) | ⚠️ Moderate (KSP) |
| **Runtime Overhead** | ✅ None | ✅ None | ✅ None |
| **Code Generation** | ❌ No | ✅ Yes (KSP) | ✅ Yes (KSP) |
| **Boilerplate** | ⚠️ Manual wiring | ✅ Minimal | ✅ Minimal |
| **Debugging** | ✅ Easy | ⚠️ Moderate | ⚠️ Moderate |
| **Circular Deps** | ⚠️ Manual check | ✅ Auto-detected | ✅ Auto-detected |
| **Scoping** | ⚠️ Manual | ✅ Built-in | ✅ Built-in |
| **Testing** | ✅ Easy mocking | ✅ Easy mocking | ✅ Easy mocking |
| **Maintainability** | ✅ Good | ✅ Excellent | ✅ Excellent |
| **Community** | N/A | ⚠️ Small | ⚠️ Very small |
| **Documentation** | N/A | ⚠️ Limited | ⚠️ Very limited |
| **Maturity** | N/A | ✅ Stable | ⚠️ Early stage |

---

## kotlin-inject for HabitLock

### Would it be a good fit?

**YES, kotlin-inject would be an excellent fit for HabitLock.** Here's why:

#### ✅ Perfect Match Reasons

1. **KMP Compatibility**
   - HabitLock targets Android, iOS, and JVM (Desktop)
   - kotlin-inject has first-class KMP support
   - No platform-specific workarounds needed

2. **Project Complexity**
   - HabitLock has ~15 repositories, ~10 use cases, ~6 ViewModels
   - Medium complexity - perfect for DI framework
   - Not too simple (where manual is better) nor too complex (where more powerful DI needed)

3. **Clean Architecture**
   - HabitLock follows clean architecture with clear layers
   - kotlin-inject's component model maps well to layers
   - Easy to create app-scoped and screen-scoped components

4. **Type Safety**
   - Current manual DI already provides type safety
   - kotlin-inject maintains this with added benefits
   - Compile-time circular dependency detection would catch issues early

5. **Testing**
   - Easy to create test components with mocks
   - Can override dependencies for tests
   - Better than current manual approach

#### ⚠️ Considerations

1. **Build Setup**
   - Need to add KSP plugin and dependencies
   - Slightly longer build times (acceptable tradeoff)

2. **Migration Effort**
   - Would need to refactor current manual DI
   - ~2-3 hours of work
   - Good learning opportunity

3. **Team Familiarity**
   - If team is not familiar with DI frameworks, there's a learning curve
   - But kotlin-inject's API is quite intuitive

---

## Implementation Example for HabitLock

### Current Manual DI (AppModule)
```kotlin
class AppModule(private val driverFactory: DatabaseDriverFactory) {
    private val database by lazy { HabitLockDatabase(driverFactory.createDriver()) }
    private val userRepository by lazy { UserRepositoryImpl(database) }
    // ... 20+ more lazy properties
    
    fun provideTodayViewModel(): TodayViewModel {
        return TodayViewModel(
            userRepository, habitRepository, habitInstanceRepository,
            generateDailyHabitsUseCase, processEndOfDayUseCase,
            completeHabitUseCase, skipHabitUseCase, undoHabitUseCase
        )
    }
}
```

### With kotlin-inject
```kotlin
@Component
@AppScope
abstract class AppComponent(
    @Component val databaseDriverFactory: DatabaseDriverFactory
) {
    // Provided instances
    abstract val database: HabitLockDatabase
    abstract val userRepository: UserRepository
    
    // ViewModels
    abstract val todayViewModel: TodayViewModel
    abstract val onboardingViewModel: OnboardingViewModel
    // ... other ViewModels
    
    companion object
}

// In repositories/data classes - just add @Inject to constructor
@AppScope
@Inject
class UserRepositoryImpl(
    database: HabitLockDatabase
) : UserRepository {
    // implementation
}

// For ViewModels
@Inject
class TodayViewModel(
    userRepository: UserRepository,
    habitRepository: HabitRepository,
    // ... all dependencies injected automatically
) : ViewModel() {
    // implementation
}

// Usage in App.kt
val appComponent = remember { AppComponent::class.create(driverFactory) }
val todayViewModel = remember { appComponent.todayViewModel }
```

**Benefits:**
- No manual lazy properties
- No manual wiring of dependencies
- Compiler ensures all dependencies are satisfied
- Circular dependencies caught at compile time

---

## Migration Path

If we decide to adopt kotlin-inject, here's the migration path:

### Step 1: Add Dependencies
```toml
# gradle/libs.versions.toml
[versions]
kotlinInject = "0.7.2"

[libraries]
kotlin-inject-runtime = { module = "me.tatarka.inject:kotlin-inject-runtime", version.ref = "kotlinInject" }

[plugins]
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
```

### Step 2: Update build.gradle.kts
```kotlin
plugins {
    alias(libs.plugins.ksp)
}

dependencies {
    implementation(libs.kotlin.inject.runtime)
    ksp(libs.kotlin.inject.compiler)
}
```

### Step 3: Add @Inject to Classes
- Add `@Inject` to repository constructors
- Add `@Inject` to use case constructors
- Add `@Inject` to ViewModel constructors

### Step 4: Create Component
- Replace `HabitLockAppComponent` with `@Component` annotated abstract class
- Define scope with `@AppScope`
- Remove manual wiring from `AppModule`

### Step 5: Update App.kt
- Use generated component factory
- Remove manual lazy initialization

**Estimated Time:** 2-3 hours

---

## Recommendation

### For HabitLock: ⚠️ STICK WITH MANUAL DI (for now)

**Reasoning:**

1. **Current Solution Works Well**
   - Manual DI is clean, simple, and working perfectly
   - 45% code reduction already achieved
   - No compilation or runtime issues
   - Team understands it completely

2. **Marginal Benefits**
   - kotlin-inject would add ~20-30% less boilerplate
   - But current manual DI is already quite clean
   - No circular dependency issues currently
   - Scoping is handled well with lazy

3. **Not Worth Migration Now**
   - Project is in Phase 1, just completed foundation
   - Focus should be on Phase 2 (business logic)
   - Migration would distract from feature development
   - Can revisit when project grows significantly

4. **Future Consideration**
   - When project has 30+ dependencies → consider kotlin-inject
   - When team grows → standardized DI framework helps
   - When adding feature modules → scoped components valuable

### When to Reconsider

**Revisit kotlin-inject if:**
- Number of dependencies doubles (30+ repositories/use cases)
- Circular dependency issues arise
- Team expands beyond 2-3 developers
- Feature modules/dynamic features needed
- Build performance becomes issue (ironically, DI could help with lazy loading)

---

## Conclusion

**kotlin-inject is an excellent DI framework for Kotlin Multiplatform** and would work beautifully with HabitLock. However, **the current manual DI solution is already very good** and switching now would provide marginal benefits while adding complexity and delaying feature development.

### Current State: ✅ Good Enough
- Clean, maintainable manual DI
- Type-safe, performant
- Team understands it
- No issues in practice

### kotlin-inject: ✅ Great, But Not Urgent
- Would work excellently
- Better for larger projects
- Keep in mind for future

### Recommendation: 
**Continue with manual DI, document kotlin-inject as a future option when project scales.**

---

## Resources

- **kotlin-inject GitHub:** https://github.com/evant/kotlin-inject
- **Documentation:** https://github.com/evant/kotlin-inject/blob/main/docs/index.md
- **KMP Sample:** https://github.com/evant/kotlin-inject/tree/main/samples/multiplatform
- **Comparison with Dagger:** https://github.com/evant/kotlin-inject/blob/main/docs/comparison.md

---

## Alternative: Keep Manual DI, Add Validation

If we want some benefits of DI frameworks without full migration, we could:

1. **Add Dependency Graph Visualization**
   - Simple script to parse AppModule and generate graph
   - Helps spot circular dependencies

2. **Add Compile-Time Checks**
   - Custom KSP processor to validate dependencies
   - Lighter weight than full DI framework

3. **Document Conventions**
   - Clear guidelines for adding dependencies
   - Code review checklist

**This gives us 80% of the benefits with 20% of the effort.**
