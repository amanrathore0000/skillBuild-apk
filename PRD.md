<!--
=============================================================================
DOCUMENT STATUS: ACTIVE PRODUCT REQUIREMENTS
PLATFORM: NATIVE ANDROID ONLY (KOTLIN)
AUTHENTICATION: GOOGLE AUTHENTICATION (CREDENTIAL MANAGER API)
=============================================================================
-->

# SKILL BUILDER — Product Requirements Document (PRD)

> **Document Version:** 2.0.0 (Native Android Edition)  
> **Status:** Approved / Active Baseline  
> **Target Platforms:** Mobile (Android Native Only via Kotlin & Jetpack Compose), Web (Admin & Moderator Dashboard)  
> **Primary Authentication:** Google Authentication (Android Credential Manager API)  
> **Tagline:** *Learn Skill. Swap Skill. Build Together.*

---

## 1. Executive Summary & Vision

### 1.1 Product Vision
**Skill Builder** is an end-to-end reciprocal knowledge exchange marketplace and structured educational platform designed natively for Android. The platform connects learners and mentors through two complementary learning pathways:
1. **Skill Swap (Barter / Reciprocal Exchange):** Bilateral matching connecting users who possess complementary skills (e.g., User A teaches Guitar and learns Cake Making; User B teaches Cake Making and learns Guitar).
2. **Paid Skill Learning (Direct Marketplace):** Monetized on-demand courses and structured video learning paths for learners who want to acquire skills without offering a return skill exchange.

### 1.2 Core Philosophy
```
LEARN SKILL ────► SWAP SKILL ────► TEACH SKILL ────► BUILD TOGETHER
```

---

## 2. Target Personas & User Roles

The system uses **Role-Based Access Control (RBAC) with dynamic capabilities**. A single account can act as both Learner and Mentor simultaneously without needing separate accounts or destructive profile resets.

| Persona / Role | Description | Primary Goals & Capabilities |
|---|---|---|
| **Learner** | Individuals seeking to acquire new practical, creative, or technical skills. | One-tap Google sign-in, browse catalog, discover mentors, request skill swaps, enroll in courses, stream video lessons with Media3 ExoPlayer, track progress, review mentors. |
| **Mentor** | Individuals or professionals looking to teach skills and/or monetize content. | Manage mentor profile, list skills taught, upload course curriculum and videos directly, manage swap requests, track earnings and analytics. |
| **Hybrid User** | Users who both learn and teach. | Seamlessly access both Learner and Mentor capabilities within the single Android application. |
| **Admin** | Platform operators and executive moderators. | Full platform oversight: user verification, category/skill management, content moderation, subscription pricing management, financial reporting. |

---

## 3. Revenue & Monetization Model

1. **Mentor Subscription Tiers:**
   - Mentors subscribe to access advanced creator features (unlimited course uploads, featured search placement, detailed analytics).
2. **Paid Course Access & Platform Commission:**
   - One-time pay-per-course / learning path model for learners opting out of skill swapping.
   - Platform collects a configurable transaction commission prior to mentor payout.
3. **Escrow & Verified Payouts:**
   - In-app purchases via Google Play Billing for subscriptions and digital credits; Stripe for web checkout.
   - Anti-circumvention safety measures prevent off-platform settlements.

---

## 4. User Experience & Onboarding

### 4.1 3-Stage Introductory Onboarding Flow
- **Screen 1: Concept Introduction**
  - Highlighting *"Learn Skill. Swap Skill. Build Together."*
  - Overview of reciprocal skill swapping and video marketplace.
  - CTAs: `Continue with Google` (1-tap), `Log In`, `Sign Up with Email`.
- **Screen 2: Safety & Community Guidelines**
  - Safety rules: Never share personal passwords, OTPs, or off-platform payment handles. Keep all transactions strictly within the official Skill Builder ecosystem.
- **Screen 3: Trust & Platform Verification**
  - Highlight verified mentor badges, authenticated reviews, report triage, and secure media delivery.

---

## 5. Functional Requirements

### 5.1 Authentication & Profile Management
- **Google Authentication (Primary):**
  - Native integration with Android's **Credential Manager API** (`androidx.credentials`).
  - Single-tap Google Account Chooser bottom sheet (`GetGoogleIdOption`).
  - Google ID Token retrieval and secure backend verification (`/api/v1/auth/google`).
  - Automatic silent sign-in on subsequent app launches.
- **Email/Password & Biometric Auth (Alternative):**
  - Email verification, forgot/reset password flows, and AndroidX BiometricPrompt for sensitive financial actions.
- **Dual-Sided Profile:**
  - Public profile: Avatar (synced from Google Account or custom), display name, verified badges, bio, city/state location, average rating, teaching portfolio.
  - Private settings: Email, billing credentials, active subscriptions, privacy controls.
  - Skill preferences: "Skills I Can Teach" and "Skills I Want to Learn".

### 5.2 Dynamic Skill Taxonomy Catalog
- Hierarchical structure: `Category` $\rightarrow$ `Subcategory` $\rightarrow$ `Skill`.
- Managed strictly via backend API with offline caching via Room Database.
- Admin ability to seed, approve, edit, and merge user-suggested skills.

### 5.3 Skill Swap Engine
- **Matching Algorithm:**
  - Evaluates reciprocal matches where:
    $$\text{User}_A(\text{Teaches}) \cap \text{User}_B(\text{Wants}) \neq \emptyset \quad \land \quad \text{User}_B(\text{Teaches}) \cap \text{User}_A(\text{Wants}) \neq \emptyset$$
- **Swap Lifecycle State Machine:**
  - `PENDING` $\rightarrow$ `ACCEPTED` $\rightarrow$ `ACTIVE` $\rightarrow$ `COMPLETED` (or `REJECTED`, `CANCELLED`, `DISPUTED`).
  - Active swaps unlock private 1-on-1 collaboration chat.

### 5.4 Course Management & Video Ingestion (Mentor)
- Direct secure uploads via pre-signed URLs directly to cloud object storage (Cloudflare R2).
- Video transcoding to adaptive HLS bitrates (1080p, 720p, 480p, 360p).

### 5.5 Video Player & Learning Experience (Learner)
- **Engine:** AndroidX Media3 (ExoPlayer).
- **Controls:** Play/pause, seek $(\pm 10\text{s})$, playback speed selector $(0.5\times - 2.0\times)$, resolution switcher, Picture-in-Picture (PiP), background audio support.
- **Progress Tracking:** Automatic progress syncing every 15 seconds to server; lesson auto-completion threshold $(\ge 90\%)$.

### 5.6 Real-Time Communication & Chat
- 1-on-1 text messaging strictly between paired swap partners or enrolled students and mentors using OkHttp WebSocket client.
- In-app push notifications via Firebase Cloud Messaging (FCM).

---

## 6. Non-Functional Requirements

| Dimension | Specification & Metric |
|---|---|
| **Performance** | API response time $P_{95} < 200\text{ms}$; video start latency $< 1.5\text{s}$ over 4G; Compose UI rendering at smooth 60–120 FPS without jank. |
| **Android Target** | Fully compatible with Android 8.0 Oreo (`API 26`) through Android 15 (`API 35`). |
| **Security** | Hardware-backed Android Keystore for token storage (`EncryptedSharedPreferences`), zero client secrets, TLS 1.3 with certificate pinning support. |
| **Accessibility** | Android accessibility standards with Compose semantics (`contentDescription`, `Modifier.semantics`, 48x48dp minimum touch targets). |
