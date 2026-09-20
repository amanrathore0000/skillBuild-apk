<!--
=============================================================================
SKILL BUILDER — SYSTEM ARCHITECTURE & ANDROID UI/UX DESIGN SPECIFICATION
Document Version: 3.0.0 (Native Android Jetpack Compose Edition)
Status: Approved / Baseline Specification
Platform: Android Native Only (Kotlin & Jetpack Compose)
Authentication: Google Authentication (Credential Manager)
=============================================================================
-->

# SKILL BUILDER — System Architecture & Design Document

> **Document Version:** 3.0.0 (Native Android Compose Edition)  
> **Status:** Approved / Active Baseline  
> **Target Platform:** Android Native Only (`minSdk = 26`, `targetSdk = 35`)  
> **Primary Authentication:** Google Authentication via Android Credential Manager API  
> **UI Framework:** Jetpack Compose + Material Design 3 (Material You)  
> **Themes Supported:** Obsidian Crimson (Dark Theme) & Alabaster Crimson (Light Theme)

---

## 1. Executive Summary & Core Design Philosophy

**Skill Builder** is an end-to-end reciprocal knowledge exchange marketplace and structured video learning platform built natively for Android. The product operates on a dual learning model:
1. **Reciprocal Skill Swap (Barter):** Bilateral matching connecting users who offer complementary skills.
2. **Paid Marketplace & Skill Credits (SC) Economy:** Structured video courses accessible via payments or platform **Skill Credits (SC)** earned by teaching.

### 1.1 Dual-Theme Jetpack Compose Design System
The visual identity combines modern typography, disciplined whitespace, and an electric crimson signature accent:
- **Obsidian Crimson (Dark Theme):** True pitch black canvas (`#000000`), deep zinc elevated cards (`#121214`), subtle borders (`#232326`), and electric crimson accent (`#EF4444` / `#FF3B30`). Engineered for OLED power conservation and immersive focus.
- **Alabaster Crimson (Light Theme):** Pure crisp white canvas (`#FFFFFF`), light stone borders (`#E5E7EB`), cool gray surfaces (`#F8F9FA`), and deep crimson accent (`#E53935`). Engineered for daytime legibility and clean contrast.

---

## 2. Jetpack Compose Design Token Specifications

```kotlin
// Android Design Tokens in Kotlin
object SkillBuilderTokens {
    // Dark Theme (Obsidian Crimson)
    object Dark {
        val CanvasBg = Color(0xFF000000)
        val Surface = Color(0xFF121214)
        val SurfaceSecondary = Color(0xFF1C1C1F)
        val Border = Color(0xFF232326)
        val BrandPrimary = Color(0xFFEF4444)
        val TextPrimary = Color(0xFFFFFFFF)
        val TextSecondary = Color(0xFF9CA3AF)
        val TextMuted = Color(0xFF6B7280)
    }

    // Light Theme (Alabaster Crimson)
    object Light {
        val CanvasBg = Color(0xFFFFFFFF)
        val Surface = Color(0xFFF8F9FA)
        val SurfaceSecondary = Color(0xFFF1F3F5)
        val Border = Color(0xFFE5E7EB)
        val BrandPrimary = Color(0xFFE53935)
        val TextPrimary = Color(0xFF111827)
        val TextSecondary = Color(0xFF4B5563)
        val TextMuted = Color(0xFF9CA3AF)
    }
}
```

---

## 3. Google Authentication User Experience (Credential Manager)

The Android app features a modern Google Sign-In experience powered by `androidx.credentials`:

1. **One-Tap Bottom Sheet:** When the user taps **"Continue with Google"**, Android displays the native Google Identity bottom sheet populated with the user's on-device Google Accounts.
2. **Instant Profile Population:** Upon account selection, the app retrieves the user's verified name, email address, and Google profile picture directly from the cryptographic Google ID Token.
3. **Seamless Transition:** The token is posted to `/api/v1/auth/google`, and the user is instantly transitioned into the app dashboard without entering passwords or receiving OTP delays.

---

## 4. Modern Android Architecture (MVVM + UDF + Compose)

```text
               ┌────────────────────────────────────────────────────────┐
               │              Jetpack Compose UI Screen                 │
               │  (Observes StateFlow<UiState> / Emits UiEvents)        │
               └───────────────────────┬────────────────────────────────┘
                                       │ UiEvent (e.g. OnGoogleSignInClicked)
                                       ▼
               ┌────────────────────────────────────────────────────────┐
               │                   Hilt ViewModel                       │
               │  (Manages UiState, launches Coroutines in viewModelScope)│
               └───────────────────────┬────────────────────────────────┘
                                       │ Domain UseCase Execution
                                       ▼
               ┌────────────────────────────────────────────────────────┐
               │                  Domain Layer (UseCases)               │
               │  (AuthenticateWithGoogleUseCase, ExecuteSwapUseCase)   │
               └───────────────────────┬────────────────────────────────┘
                                       │ Repository Interface
                                       ▼
               ┌────────────────────────────────────────────────────────┐
               │                   Data Repository                      │
               │  (Orchestrates Network Service & Local Room Database)  │
               └───────────────┬────────────────────────┬───────────────┘
                               │                        │
                               ▼                        ▼
               ┌────────────────────────┐      ┌────────────────────────┐
               │    Retrofit 2 API      │      │    Room Database       │
               │   (Remote REST API)    │      │    (Local SQLite DB)   │
               └────────────────────────┘      └────────────────────────┘
```

---

## 5. Media3 Video Player Architecture

- **Engine:** AndroidX Media3 `ExoPlayer` instance bound to lifecycle.
- **Surface:** `AndroidView` wrapping `androidx.media3.ui.PlayerView` inside Compose, with custom overlay controls:
  - Play/Pause toggle with large center pulse animation.
  - Interactive scrubbing timeline with buffered progress.
  - $\pm 10\text{s}$ quick jump buttons.
  - Speed selector modal ($0.5\times, 0.75\times, 1.0\times, 1.25\times, 1.5\times, 2.0\times$).
  - Fullscreen toggle & Picture-in-Picture (`Activity.enterPictureInPictureMode()`).
