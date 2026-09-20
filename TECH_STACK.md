
=============================================================================
DOCUMENT STATUS: ACTIVE ARCHITECTURAL SPECIFICATION
PLATFORM: NATIVE ANDROID (KOTLIN)
AUTHENTICATION: GOOGLE AUTHENTICATION (CREDENTIAL MANAGER API)
=============================================================================
-->

# SKILL BUILDER — Technology Stack Specification

> **Document Version:** 2.0.0 (Native Android Edition)  
> **Target Platform:** Android Native Only (`minSdk 26`, `compileSdk 35`, `targetSdk 35`)  
> **Core Language:** Kotlin 2.x  
> **UI Framework:** Jetpack Compose + Material Design 3  
> **Primary Authentication:** Google Authentication (Android Credential Manager API)  
> **Scope:** Native Android Client, Backend API, Database, Video Infrastructure, Real-time & Payments

---

## 1. Architectural Principles & Technology Selection Matrix

| Tier | Chosen Technology | Rationale & Trade-off Analysis |
|---|---|---|
| **Platform Target** | **Android Only** (`minSdk = 26` Android 8.0, `targetSdk = 35` Android 15) | Optimized strictly for the Android operating system; eliminates cross-platform abstraction overhead, guarantees 120Hz smooth animations, native memory management, and complete hardware integration. |
| **Language & Toolchain** | **Kotlin 2.x** with **K2 Compiler** & Kotlin Coroutines | Modern concise type safety, null-safety, inline value classes, functional idioms, and asynchronous concurrency via Coroutines and reactive `StateFlow`. |
| **UI Toolkit** | **Jetpack Compose** + **Material Design 3 (Material You)** | Android's modern declarative UI toolkit. Eliminates XML view inflation overhead and imperative DOM-style mutations. Features Obsidian Dark & Alabaster Light theme tokens with dynamic color support. |
| **Architecture Pattern** | **Modern Android Architecture (MVVM / MVI)** + **Clean Architecture** | Unidirectional Data Flow (UDF: Events $\rightarrow$ ViewModel $\rightarrow$ Immutable StateFlow $\rightarrow$ Compose UI). Strict layering: Presentation $\rightarrow$ Domain (UseCases & Models) $\rightarrow$ Data (Repositories, Local DB, Remote API). |
| **Authentication Engine** | **Google Authentication** via **Android Credential Manager API** (`androidx.credentials`) | Modern Google-recommended identity standard replacing legacy Google Sign-In. Native 1-tap Account Chooser bottom sheet (`GetGoogleIdOption`), passkeys, biometric fallback, and secure backend Google ID token exchange (`/api/v1/auth/google`). |
| **Dependency Injection** | **Dagger Hilt** (`com.google.dagger:hilt-android`) | Standard Android compile-time DI container; seamless integration with Android Jetpack components (`@HiltViewModel`, `@AndroidEntryPoint`, `hiltViewModel()` in Compose). Zero runtime reflection penalty. |
| **Networking & HTTP** | **Retrofit 2** + **OkHttp 4** + **Kotlinx Serialization** | Type-safe REST client with OkHttp interceptors (logging, auth headers, rate-limiting) and automated token refresh authenticator. Kotlinx Serialization provides high-performance reflectionless JSON parsing. |
| **Video Playback & Streaming** | **AndroidX Media3 (ExoPlayer)** (`androidx.media3:media3-exoplayer`) | Enterprise-grade native media playback engine; hardware-accelerated HLS/DASH adaptive bitrate streaming, customizable Compose player surface, playback speed controls, and native Picture-in-Picture (PiP). |
| **Local Persistence** | **Room Database** (`androidx.room`) with Coroutines Flow queries | SQLite abstraction with compile-time SQL verification, reactive Flow observable queries, and support for SQLCipher encrypted database files. |
| **Key-Value & Credential Storage** | **Jetpack DataStore** (Preferences) + **EncryptedSharedPreferences** | Transactional, coroutine-based key-value storage. Hardware-backed Android Keystore cryptography for auth tokens and user session data. |
| **Image Loading** | **Coil 3 Compose** (`io.coil-kt.coil3`) | Lightweight, Kotlin-first, coroutine-based image loading engine designed natively for Jetpack Compose with memory and disk caching. |
| **Navigation** | **Jetpack Navigation Compose** (`androidx.navigation:navigation-compose`) | Type-safe Kotlin Serialization route definitions, deep link handling for swap invites and course links, and seamless screen transition animations. |
| **Real-time Engine** | **OkHttp WebSocket** wrapped in Kotlin Coroutines Flow | Bi-directional real-time messaging for 1-to-1 swap chat, live typing status, and swap state transition updates with automatic reconnection and heartbeat. |
| **Payments & Monetization** | **Google Play Billing Library 7.x** + **Stripe Android SDK** | Native Google Play Billing for in-app subscriptions (Mentor membership tiers) and digital Skill Credits (SC); Stripe Android SDK for direct web checkout and card payments. |
| **Push Notifications** | **Firebase Cloud Messaging (FCM)** via Android Play Services | Native Android notification channels (`NotificationChannelCompat`), foreground/background message receivers, and deep-link intent handling. |
| **Build System** | **Gradle 8.x** with **Kotlin DSL (`build.gradle.kts`)** & **Version Catalog (`libs.versions.toml`)** | Type-safe build scripts, centralized dependency and plugin versions, and incremental compilation with Kotlin Symbol Processing (KSP). |

---

## 2. Detailed Android Application Architecture (`/app`)

```text
app/
├── build.gradle.kts                  # App-level build script (plugins, dependencies, compose options)
├── proguard-rules.pro                # R8 / ProGuard rules for release shrinking and obfuscation
└── src/
    └── main/
        ├── AndroidManifest.xml       # App permissions, activities, services, intent filters
        ├── res/                      # Android resources (drawables, mipmaps, strings, xml)
        └── java/com/skillbuilder/app/
            │
            ├── SkillBuilderApp.kt    # Application class (@HiltAndroidApp)
            ├── MainActivity.kt       # Single activity host with setContent { SkillBuilderTheme { ... } }
            │
            ├── core/                 # Shared infrastructure & utilities
            │   ├── common/           # Result/Resource sealed class, AppDispatchers, Constants
            │   ├── network/          # Retrofit builder, AuthInterceptor, TokenAuthenticator
            │   ├── database/         # SkillBuilderDatabase, DAOs, Room Entities, Converters
            │   ├── datastore/        # UserPreferencesDataStore, SecureTokenStorage
            │   └── designsystem/     # Theme.kt (Obsidian/Alabaster), Color.kt, Typography.kt, Shape.kt
            │
            ├── di/                   # Hilt Modules
            │   ├── AppModule.kt      # Application-scoped singletons
            │   ├── NetworkModule.kt  # OkHttpClient, Retrofit, ApiService
            │   ├── DatabaseModule.kt # Room DB & DAO providers
            │   └── RepositoryModule.kt# Repository interface bindings
            │
            ├── auth/                 # Google Authentication
            │   ├── GoogleAuthClient.kt   # Credential Manager API wrapper (1-tap Google Sign-In)
            │   └── GoogleSignInResult.kt # Sign-in state data models (User details, ID token)
            │
            ├── data/                 # Data Layer
            │   ├── remote/           # API endpoints, Request/Response DTOs
            │   ├── local/            # Local data sources, database cache
            │   └── repository/       # AuthRepositoryImpl, SkillRepositoryImpl, SwapRepositoryImpl...
            │
            ├── domain/               # Domain Layer (Pure Kotlin, zero Android framework dependencies)
            │   ├── model/            # User, Skill, SwapRequest, Course, Lesson, Review
            │   ├── repository/       # Repository interfaces
            │   └── usecase/          # AuthenticateWithGoogleUseCase, RequestSkillSwapUseCase...
            │
            └── ui/                   # Presentation Layer (Jetpack Compose)
                ├── navigation/       # NavGraph.kt, Screen.kt (Type-Safe Routes)
                ├── components/       # SkillCard, Avatar, VideoPlayerView, BottomNav, TopAppBar
                └── screens/
                    ├── auth/         # LoginScreen.kt, SignUpScreen.kt, AuthViewModel.kt
                    ├── learner/      # LearnerHomeScreen.kt, BrowseSkillsScreen.kt, LearnScreen.kt
                    ├── mentor/       # MentorStudioScreen.kt, UploadVideoScreen.kt, MentorEarningsScreen.kt
                    ├── swap/         # SwapMatchingScreen.kt, ActiveSwapScreen.kt, SwapDetailScreen.kt
                    ├── course/       # CourseDetailScreen.kt, LessonPlayerScreen.kt
                    ├── chat/         # ChatListScreen.kt, ConversationScreen.kt
                    └── profile/      # ProfileScreen.kt, EditProfileScreen.kt, SettingsScreen.kt
```

---

## 3. Google Authentication User Flow (Credential Manager)

```
+------------------+         +----------------------------+         +--------------------------+         +---------------------+
|  Compose UI      |         |  GoogleAuthClient          |         |  Android Credential      |         |  SkillBuilder       |
|  (AuthScreen)    |         |  (Credential Manager)      |         |  Manager / Google Play   |         |  Backend API        |
+------------------+         +----------------------------+         +--------------------------+         +---------------------+
         |                                 |                                      |                             |
         | --- Tap "Continue with Google"->|                                      |                             |
         |                                 | --- Build GetGoogleIdOption -------->|                             |
         |                                 | --- getCredential(context, req) ---->|                             |
         |                                 |                                      | --- Display 1-tap popup --->|
         |                                 |                                      |     (Device Google Accounts)|
         |                                 |                                      | <--- User selects account --|
         |                                 | <--- CustomCredential (ID Token) ---|                             |
         |                                 |                                                                    |
         |                                 | --- POST /api/v1/auth/google { idToken } ------------------------->|
         |                                 |                                                                    | --- Verify token with
         |                                 |                                                                    |     Google Auth Server
         |                                 |                                                                    | --- Issue JWT access &
         |                                 |                                                                    |     refresh tokens
         |                                 | <--- 200 OK { user, accessToken, refreshToken } -------------------|
         | <--- AuthSuccess(user) ---------|                                                                    |
         |                                 |                                                                    |
         | --- Navigate to Home Screen --->|                                                                    |
```

---

## 4. Third-Party Services & Infrastructure Endpoints

```
                                  ┌──────────────────────────┐
                                  │        Cloudflare        │
                                  │   (DNS / WAF / DDoS)     │
                                  └─────────────┬────────────┘
                                                │
                     ┌──────────────────────────┴──────────────────────────┐
                     ▼                                                     ▼
        ┌────────────────────────┐                            ┌────────────────────────┐
        │  NestJS Backend API    │                            │     Cloudflare R2      │
        │    (Docker / ECS)      │                            │    (Video Storage)     │
        └───────────┬────────────┘                            └───────────┬────────────┘
                    │                                                     │
         ┌──────────┴──────────┬──────────────────────┐                   │
         ▼                     ▼                      ▼                   ▼
   ┌───────────┐         ┌───────────┐         ┌──────────────┐    ┌───────────────────┐
   │PostgreSQL │         │   Redis   │         │Google /      │    │  Video Streaming  │
   │ (RDS 16+) │         │  (Cache)  │         │Stripe APIs   │    │     (HLS CDN)     │
   └───────────┘         └───────────┘         └──────────────┘    └───────────────────┘
```

---

## 5. Security & Secret Management

1. **Client Isolation:** The Android client contains **no** API secret keys, Stripe secret keys, or database credentials. It only holds the public Google Web Client ID and backend base URL.
2. **Keystore-Backed Encryption:** User tokens (JWT access & refresh tokens) are saved exclusively using Android `EncryptedSharedPreferences` backed by the hardware-level Android Keystore (`MasterKey.DEFAULT_MASTER_KEY_ALIAS`).
3. **Biometric Guard:** Sensitive operations (withdrawing mentor earnings, initiating payment payouts) are guarded with AndroidX BiometricPrompt.
4. **Certificate Pinning:** OkHttpClient incorporates network security config and optional certificate pinning for production TLS endpoints.
