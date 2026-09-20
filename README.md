# SKILL BUILDER — NATIVE ANDROID APPLICATION BLUEPRINT

## 1. ROLE

You are a **senior software architect, lead Android engineer (Kotlin & Jetpack Compose), backend engineer, UI/UX designer, database architect, security engineer, QA engineer, and DevOps engineer**.

Your task is to design and develop a production-ready native Android application called:

# SKILL BUILDER

### Tagline

**Learn Skill. Swap Skill. Build Together.**

The application should allow people to:

* Learn skills from mentors.
* Teach skills to learners.
* Swap skills with other users.
* Purchase access to paid skill-learning content when they do not want to exchange skills.
* Upload and consume educational video content.
* Build profiles around their skills.
* Communicate safely through the platform.
* Report users/content/problems.
* Make payments through the official platform payment system.

The application must be designed for **Android Only using Kotlin & Jetpack Compose** and should have a scalable backend architecture.

---

# 2. CORE PRODUCT CONCEPT

Skill Builder is based on a **skill exchange + skill learning marketplace**.

There are two primary concepts.

## A. SKILL SWAP

Example:

User A knows:

> Cake Making

User B knows:

> Guitar

User A wants to learn Guitar.

User B wants to learn Cake Making.

The platform allows A and B to discover each other and exchange their knowledge.

### Flow

A searches for:

> Guitar

B searches for:

> Cake Making

The platform identifies a potential skill-swap match.

Both users can accept the swap.

They can then learn from each other through the platform.

---

# 3. PAID SKILL LEARNING

There will also be situations where a learner wants a skill but cannot or does not want to provide another skill in exchange.

Example:

User A knows:

> Cake Making

User B wants to learn:

> Cake Making

But User B does not want to teach any skill to A.

In this case:

> User B can purchase access to User A's paid learning content.

The mentor provides educational content, particularly video lessons.

The learner pays through the official Skill Builder payment system.

---

# 4. BUSINESS MODEL

Skill Builder will primarily follow a **subscription-based mentor model**.

### Mentor

Mentors can:

* Create a mentor profile.
* List skills.
* Upload learning videos.
* Create courses/learning paths.
* Set learning content availability.
* Participate in skill swaps.
* Receive eligible earnings according to the platform's business rules.
* Track learners.
* View content analytics.

### Learner

Learners should be able to:

* Create an account.
* Search skills.
* Discover mentors.
* Watch available content.
* Find skill-swap opportunities.
* Request/accept skill swaps.
* Purchase eligible learning access.
* Rate/review experiences.
* Report problems.

The exact subscription pricing and revenue-sharing percentages should be implemented as **configurable backend values**, not hard-coded into the application.

---

# 5. USER TYPES

Design the architecture to support role-based access control.

Initial roles:

1. Learner
2. Mentor
3. Admin
4. Support/Admin Moderator

Important:

A user should be able to potentially act as both a learner and a mentor.

Do NOT design the system assuming that one account can only ever have one role.

Use permissions and capabilities rather than scattering role checks throughout the application.

---

# 6. MOBILE APPLICATION (ANDROID ONLY — KOTLIN)

Use:

## Native Android (Kotlin & Jetpack Compose)

The mobile application must support:

* **Android Only** (minSdk 26+, compileSdk/targetSdk 35)

Use a maintainable Modern Android Architecture suitable for an enterprise-grade production application.

Prefer:

* **Kotlin 2.x** (with K2 compiler and strict type safety)
* **Jetpack Compose** (declarative UI with Material 3 design system)
* **Modern Android Architecture** (MVVM / MVI with Unidirectional Data Flow — UDF)
* **Clean Architecture** (Presentation, Domain, and Data separation)
* **Kotlin Coroutines & Flow** (`StateFlow`, `SharedFlow`) for asynchronous execution and reactive state
* **Dagger Hilt** for dependency injection
* **Google Authentication** via modern **Android Credential Manager API** (`androidx.credentials`)
* **AndroidX Media3 (ExoPlayer)** for hardware-accelerated video streaming (HLS/DASH), playback controls, and PiP
* **Retrofit 2 + OkHttp 4** with Kotlinx Serialization for network communication
* **Room Database** for offline-first caching and persistence
* **Jetpack DataStore & EncryptedSharedPreferences** for secure credential and preference storage
* **Coil 3** for asynchronous image loading and caching
* **Jetpack Navigation Compose** for type-safe screen navigation and deep linking
* Form validation & comprehensive error handling (Result/Resource wrappers)

---

# 7. RECOMMENDED ANDROID ARCHITECTURE & PACKAGE STRUCTURE

Use a modular, feature-first Clean Architecture package structure:

```text
app/src/main/java/com/skillbuilder/app/
│
├── core/
│   ├── common/               # Resource wrappers, DispatcherProvider, Constants
│   ├── network/              # Retrofit instance, OkHttp interceptors, AuthAuthenticator
│   ├── database/             # Room Database, DAOs, Entity definitions, TypeConverters
│   ├── datastore/            # Jetpack DataStore / EncryptedSharedPreferences
│   ├── designsystem/         # Compose Theme (Obsidian Dark & Alabaster Light), Tokens, Components
│   └── util/                 # Date formatters, string extensions, input validators
│
├── di/                       # Hilt dependency injection modules (NetworkModule, DatabaseModule, RepositoryModule)
│
├── data/
│   ├── repository/           # Repository implementations (Auth, Skill, Swap, Course, Video, Chat)
│   ├── remote/               # Retrofit API service interfaces and network DTOs
│   └── local/                # Local cache data sources
│
├── domain/
│   ├── model/                # Pure Kotlin domain models (User, Skill, SwapRequest, Course, Lesson)
│   ├── repository/           # Repository interface contracts
│   └── usecase/              # Single-responsibility business use cases (e.g. ExecuteSkillSwapUseCase)
│
├── auth/
│   └── GoogleAuthClient.kt   # Android Credential Manager API wrapper for Google Sign-In
│
└── ui/
    ├── navigation/           # Type-safe Navigation Compose routes & NavGraph
    ├── components/           # Common Compose components (Buttons, Inputs, Cards, VideoPlayer)
    └── features/
        ├── auth/             # LoginScreen, SignUpScreen (Google Auth 1-tap), AuthViewModel
        ├── onboarding/       # Role selection (Learner / Mentor), initial preferences
        ├── profile/          # User profile, portfolio, skill badges, reviews
        ├── skills/           # Skill catalog, taxonomy selector, search & filters
        ├── skillswap/        # Swap matching, request inbox, barter management
        ├── mentors/          # Mentor directory, mentor public profile, booking
        ├── courses/          # Course details, curriculum, video lessons
        ├── video/            # Media3 ExoPlayer surface, scrubber, speed modal, PiP
        ├── chat/             # 1-to-1 swap chat, WebSocket integration, message bubbles
        ├── payments/         # Skill Credits (SC), Google Play Billing, Stripe checkout
        └── settings/         # Theme toggling (Dark/Light), language, notifications
```

The architecture strictly decouples UI components from data fetching logic via ViewModels exposing immutable `StateFlow<UiState>` to Compose screens.

---

# 8. CODING PRINCIPLES

Follow professional software engineering principles throughout the project.

## DRY

Do not duplicate code.

Create reusable:

* Buttons
* Inputs
* Cards
* Modals
* API functions
* Validation schemas
* Loading states
* Error states
* Empty states
* Navigation patterns

---

## SOLID

Apply SOLID principles wherever applicable.

Especially:

* Single Responsibility
* Dependency Inversion
* Interface Segregation

Avoid giant components and giant service files.

---

## MODULAR CODE

Each feature should be independently understandable and maintainable.

For example:

```text
features/skillSwap/
```

should contain the components, hooks, services, types and validation specifically related to skill swapping where practical.

---

## REUSABLE CODE

Never create multiple versions of the same component unnecessarily.

For example:

Instead of:

```text
MentorButton
LearnerButton
CourseButton
PaymentButton
```

create reusable components such as:

```text
Button
PrimaryButton
SecondaryButton
```

with configurable props.

---

## SEPARATION OF CONCERNS

Do not mix:

* UI
* API requests
* business logic
* database logic
* validation
* authentication
* payment logic

Keep these responsibilities separated.

---

# 9. UI/UX DESIGN

The application should feel like a modern professional learning marketplace.

Design principles:

* Clean
* Modern
* Simple
* Accessible
* Responsive
* Beginner friendly
* Professional
* Fast
* Consistent

Create a consistent design system containing:

* Colors
* Typography
* Spacing
* Border radius
* Shadows
* Icons
* Buttons
* Input styles
* Cards
* Bottom navigation
* Headers
* Modal styles

Do not randomly style individual screens.

Use centralized theme tokens.

---

# 10. ONBOARDING

Create a 3-page introduction/onboarding experience.

## PAGE 1

# LEARN SKILL. SWAP SKILL. BUILD TOGETHER.

Explain the Skill Builder concept.

Primary actions:

* Get Started
* Login
* Sign Up

---

## PAGE 2

# SAFETY GUIDELINES

Display:

* Don't share personal information.
* Never share passwords or OTPs.
* Don't share sensitive financial information.
* Use the official Skill Builder payment system.
* Report suspicious users.
* Report inappropriate content.
* Respect other users.
* Don't move transactions outside the official platform unnecessarily.

Include:

> If you have a problem, contact Skill Builder support through the official support channel.

---

## PAGE 3

# TRUST & SAFETY

Show platform trust features:

* Secure account
* User profiles
* Ratings & reviews
* Report/block
* Secure platform payments
* Content moderation
* Support system

Do NOT make unsupported claims such as "100% trusted" or "completely secure".

Only display security/trust claims that are actually implemented.

---

# 11. AUTHENTICATION & GOOGLE SIGN-IN

Implement:

* **Google Authentication (Primary)**:
  * Android Credential Manager API (`androidx.credentials`) integration.
  * Native 1-tap Google Account Chooser bottom sheet (`GetGoogleIdOption`).
  * Google ID Token retrieval and secure backend verification (`/api/v1/auth/google`).
  * Automatic silent sign-in on app launch if authorized Google account exists.
* Email / Password sign up & login (fallback / alternative).
* Logout with credential revocation and local cache clearance.
* Password reset & email verification.
* JWT session management with automated refresh token rotation via OkHttp Authenticator.
* Biometric authentication (Fingerprint / Face Unlock via AndroidX BiometricPrompt) for sensitive transactions.

Authentication must use secure practices:

* Never store plain-text passwords or secret keys.
* Store refresh tokens and session keys strictly in `EncryptedSharedPreferences` / Jetpack DataStore (backed by Android Keystore).
* Never trust client-side claims; all authentication is cryptographically verified on the backend.

---

# 12. USER PROFILE

Each user should have:

* Profile photo
* Name/display name
* Bio
* Location at an appropriate privacy level
* Skills they can teach
* Skills they want to learn
* Experience
* Rating
* Reviews
* Mentor status
* Learning statistics
* Teaching statistics

Do not expose private information unnecessarily.

---

# 13. SKILL SYSTEM

Create a structured skill system.

Each skill should contain:

```text
Skill
├── id
├── name
├── category
├── subcategory
├── description
└── status
```

Examples:

### Food

* Cake Making
* Cooking
* Baking

### Music

* Guitar
* Piano
* Singing

### Technology

* JavaScript
* Python
* React

### Creative

* Photography
* Drawing
* Video Editing

The system must allow administrators to add/edit/remove categories and skills.

Do not hard-code the entire skill catalogue into the mobile application.

---

# 14. SKILL SWAP ENGINE

This is one of the most important features.

A user can specify:

### I CAN TEACH

Example:

```text
Cake Making
```

### I WANT TO LEARN

Example:

```text
Guitar
```

The system searches for users who:

```text
Can teach Guitar
AND
Want to learn Cake Making
```

Create a matching mechanism.

Initial matching can use:

* Skill compatibility
* Skill category
* Availability
* Language
* Experience level
* Location/timezone where appropriate

Later, the architecture can support a more advanced recommendation algorithm.

---

# 15. SWAP REQUEST

A user can send a swap request.

Example:

```text
A wants to learn Guitar
A can teach Cake Making

B wants to learn Cake Making
B can teach Guitar
```

A sends:

> Skill Swap Request

B receives:

* Accept
* Reject
* View profile
* View skills

If both parties accept:

```text
Swap Status = ACTIVE
```

Possible statuses:

```text
PENDING
ACCEPTED
REJECTED
CANCELLED
ACTIVE
COMPLETED
DISPUTED
```

---

# 16. LEARNING CONTENT

Mentors can create learning content.

Content structure:

```text
Course
 ├── Title
 ├── Description
 ├── Skill
 ├── Difficulty
 ├── Thumbnail
 ├── Lessons
 │    ├── Video
 │    ├── Title
 │    ├── Description
 │    └── Duration
 └── Status
```

Support:

* Draft
* Published
* Unpublished
* Archived

---

# 17. VIDEO SYSTEM

Mentors should be able to upload videos.

Do NOT store large video files directly in the application database.

Use appropriate cloud object storage/video infrastructure.

The backend should store:

* Video ID
* Storage URL/reference
* Duration
* Thumbnail
* Course ID
* Lesson ID
* Upload status
* Processing status
* Visibility

Implement secure access to paid/private content.

Consider:

* Video transcoding
* Multiple resolutions
* Streaming
* Signed URLs
* Access control
* Thumbnail generation

Do not expose private storage credentials to the mobile application.

---

# 18. COURSE PLAYER

Create a professional video-learning experience.

Include:

* Video player
* Play/pause
* Seek
* Full screen
* Progress
* Lesson list
* Completed lessons
* Course progress
* Resume watching
* Next lesson
* Previous lesson

Track learning progress on the backend.

---

# 19. PAYMENT SYSTEM

Create a secure payment architecture.

The application should support:

* Paid learning access
* Mentor subscription
* Platform fees
* Payment history
* Refund status
* Transaction status

Use a proper payment provider rather than implementing card processing manually.

Payment flow:

```text
User selects paid skill
        ↓
Checkout
        ↓
Payment provider
        ↓
Payment verification
        ↓
Backend verifies transaction
        ↓
Access granted
```

Never trust payment success based solely on the mobile client.

The backend must verify payment status.

---

# 20. SUBSCRIPTION SYSTEM

Mentor subscriptions should be configurable.

Example:

```text
Free
Basic Mentor
Professional Mentor
Premium Mentor
```

Do not hard-code pricing.

Store plans in the backend.

Each plan can define:

* Price
* Billing period
* Maximum courses
* Maximum videos
* Analytics
* Visibility
* Other platform features

The architecture should allow future plan changes without requiring a new mobile app release.

---

# 21. CHAT

Create an optional secure communication system.

Features:

* One-to-one chat
* Conversation list
* Message timestamps
* Read status
* Block user
* Report user
* Basic moderation

Do not expose personal phone numbers or email addresses automatically.

The system should discourage users from moving payments outside the platform.

---

# 22. REVIEWS & RATINGS

After appropriate learning/swap interactions, users can submit:

* Rating
* Review
* Feedback

Prevent obvious abuse such as:

* Multiple duplicate reviews
* Reviewing without an eligible interaction
* Editing reviews without proper rules

Create moderation capability for admins.

---

# 23. REPORTING SYSTEM

Users must be able to report:

* User
* Course
* Video
* Message
* Payment issue
* Scam
* Harassment
* Inappropriate content
* Other problem

Report structure:

```text
Report
├── id
├── reporterId
├── targetType
├── targetId
├── reason
├── description
├── status
├── createdAt
└── resolvedAt
```

Admin/moderator can:

* View reports
* Investigate
* Change status
* Take moderation action
* Add internal notes

---

# 24. NOTIFICATIONS

Support:

* New swap request
* Swap accepted
* Swap rejected
* New message
* Course update
* Payment confirmation
* Subscription event
* Report update
* Account/security alerts

Use push notifications for mobile.

---

# 25. SEARCH

Users should be able to search:

* Skills
* Mentors
* Courses
* Learning content

Filters:

* Skill category
* Experience
* Rating
* Language
* Price
* Availability
* Learning type
* Mentor/learner preferences

Search architecture should be scalable.

---

# 26. HOME SCREEN

Create a personalized dashboard.

Possible sections:

```text
Hello, Aman 👋

What do you want to learn?

[ Search Skills ]

Recommended Skills

Recommended Mentors

Potential Skill Swaps

Continue Learning

Popular Skills

Your Active Swaps

Your Courses
```

The exact content should be personalized based on user activity.

---

# 27. MENTOR DASHBOARD

Mentor dashboard should contain:

* Total learners
* Active courses
* Published videos
* Subscription status
* Earnings where applicable
* Course performance
* Learner progress
* Reviews
* Skill swap activity

---

# 28. LEARNER DASHBOARD

Learner dashboard:

* Continue learning
* Saved skills
* Purchased learning
* Active swaps
* Learning progress
* Recommended mentors
* Recommended skills
* Reviews

---

# 29. ADMIN PANEL

Build a separate web-based admin dashboard.

Admin capabilities:

### User Management

* View users
* Search users
* Suspend users
* Restore users
* Verify mentors where applicable

### Skill Management

* Create skill
* Edit skill
* Delete/deactivate skill
* Manage categories

### Content Management

* Review courses
* Moderate videos
* Remove inappropriate content

### Reports

* View reports
* Resolve reports
* Track moderation actions

### Payments

* View transactions
* Payment status
* Refund information

### Subscriptions

* Create plans
* Modify plans
* Activate/deactivate plans

### Analytics

* Users
* Mentors
* Learners
* Swaps
* Courses
* Revenue
* Retention
* Reports

---

# 30. BACKEND

Build a clean REST API or another well-structured API architecture.

Recommended backend responsibilities:

* Authentication
* Authorization
* User management
* Skills
* Skill matching
* Courses
* Videos
* Payments
* Subscriptions
* Chat
* Notifications
* Reviews
* Reports
* Analytics

The mobile application should never directly access sensitive database credentials.

---

# 31. DATABASE

Use a scalable relational database for transactional data.

Potential choice:

```text
PostgreSQL
```

Design normalized database schemas.

Important entities:

```text
users
profiles
roles
permissions
skills
skill_categories
user_skills
skill_swap_requests
skill_swaps
courses
course_lessons
videos
learning_progress
subscriptions
subscription_plans
payments
transactions
reviews
reports
conversations
messages
notifications
```

Use:

* Primary keys
* Foreign keys
* Indexes
* Constraints
* Timestamps
* Soft deletion where appropriate

Do not create unnecessary duplicated data.

---

# 32. API DESIGN

Create a centralized API client.

Example:

```text
/api/auth
/api/users
/api/skills
/api/swaps
/api/mentors
/api/courses
/api/videos
/api/payments
/api/subscriptions
/api/reviews
/api/reports
/api/messages
/api/notifications
```

Use consistent:

* HTTP methods
* Status codes
* Error structures
* Validation
* Authentication
* Pagination

---

# 33. SECURITY

Security is a first-class requirement.

Implement:

* Secure authentication
* Password hashing
* Authorization
* Input validation
* Rate limiting
* Secure API endpoints
* Secure file upload validation
* File type validation
* Access control
* Payment verification
* Secure secrets management
* HTTPS in production
* Logging and monitoring

Never:

* Store plaintext passwords.
* Put API secrets in Git.
* Put payment secrets or private API keys in Android client code.
* Trust client-side payment success.
* Trust client-side authorization.
* Expose private storage credentials.

---

# 34. ENVIRONMENT MANAGEMENT

Use environment variables.

Example:

```text
.env.development
.env.staging
.env.production
```

Never commit secrets.

Create:

```text
.env.example
```

with placeholders.

---

# 35. ERROR HANDLING

Implement centralized error handling.

The application should provide:

* Loading states
* Empty states
* Retry states
* Offline states
* API errors
* Authentication errors
* Payment errors
* Upload errors

Do not allow unhandled exceptions to crash important user flows.

---

# 36. OFFLINE / NETWORK HANDLING

Because this is a mobile application, account for:

* Slow internet
* Lost network
* Request timeout
* Retry
* Offline UI
* Cached data where appropriate

Do not cache sensitive data insecurely.

---

# 37. PERFORMANCE

Optimize:

* List rendering
* Images
* Video loading
* API calls
* Database queries
* Pagination
* Caching

Do not load thousands of records into the mobile application at once.

Use pagination/infinite scrolling.

---

# 38. ACCESSIBILITY

Support:

* Proper text scaling
* Accessible labels
* Touch target sizes
* Screen reader compatibility
* Sufficient contrast
* Keyboard navigation in web/admin interfaces

---

# 39. TESTING

Create automated tests.

### Unit Tests

Test:

* Skill matching
* Validation
* Pricing calculations
* Subscription logic
* Permission logic

### Integration Tests

Test:

* Authentication
* API
* Payment verification
* Course access
* Skill swaps

### End-to-End Tests

Test important journeys:

```text
Sign up
→ Create profile
→ Select skills
→ Find mentor
→ Request swap
→ Accept swap
→ Learn
```

and:

```text
Sign up
→ Find paid skill
→ Checkout
→ Payment verification
→ Access course
→ Watch lesson
```

---

# 40. CODE QUALITY

Use:

* TypeScript
* ESLint
* Prettier
* Strict typing
* Clear naming
* Small functions
* Small components
* Reusable services
* Documentation for complex logic

Avoid:

* Giant files
* Giant components
* Copy-paste code
* Magic numbers
* Hard-coded configuration
* Hard-coded payment plans
* Hard-coded user permissions

---

# 41. GIT WORKFLOW

Use Git.

Recommended branches:

```text
main
develop
feature/*
bugfix/*
hotfix/*
```

Use meaningful commit messages.

Example:

```text
feat: add skill swap matching
feat: implement mentor course upload
fix: resolve payment verification issue
refactor: improve course service
```

---

# 42. DOCUMENTATION

Create:

```text
README.md
ARCHITECTURE.md
API.md
DATABASE.md
SECURITY.md
DEPLOYMENT.md
CONTRIBUTING.md
```

README should explain:

* Project
* Features
* Architecture
* Installation
* Environment setup
* Running development environment
* Testing
* Production build

---

# 43. PROJECT DEVELOPMENT PHASES

Do NOT attempt to build everything randomly.

Build in phases.

## PHASE 1 — Foundation

* Android project setup (Gradle Kotlin DSL, Version Catalog)
* Native Android (Kotlin & Jetpack Compose)
* Theme system (Obsidian Dark & Alabaster Light)
* Navigation Compose & DI (Dagger Hilt)
* Google Authentication (Android Credential Manager)
* Backend
* Database
* Environment configuration

## PHASE 2 — Profiles & Skills

* User profile
* Skills
* Categories
* Teach/learn preferences
* Search

## PHASE 3 — Skill Swap

* Matching
* Requests
* Accept/reject
* Active swaps
* Swap progress

## PHASE 4 — Mentor System

* Mentor profiles
* Course creation
* Video upload
* Course publishing

## PHASE 5 — Learning

* Course catalogue
* Video player
* Progress tracking
* Continue learning

## PHASE 6 — Payments

* Payment gateway
* Payment verification
* Subscription plans
* Transaction history
* Access control

## PHASE 7 — Community

* Chat
* Notifications
* Reviews
* Reports
* Blocking

## PHASE 8 — Admin

* Admin dashboard
* Moderation
* Users
* Courses
* Skills
* Payments
* Reports
* Analytics

## PHASE 9 — Testing

* Unit
* Integration
* E2E
* Security testing
* Performance testing

## PHASE 10 — Production

* Production infrastructure
* Monitoring
* Logging
* CI/CD
* App builds
* Store preparation

---

# 44. MVP PRIORITY

Do not overbuild the first version.

The MVP should focus on:

### MUST HAVE

* Registration/login
* User profiles
* Skills
* Teach/learn preferences
* Skill discovery
* Skill swap matching
* Swap requests
* Mentor profiles
* Video learning
* Basic paid content
* Payment integration
* Reviews
* Reporting
* Notifications

### LATER

* AI skill recommendations
* AI mentor matching
* Live classes
* Certificates
* Gamification
* Advanced analytics
* Community groups
* Advanced video tools
* Corporate learning

---

# 45. AI-READY ARCHITECTURE

Do not implement AI immediately unless necessary.

However, design the architecture so AI can later provide:

* Skill recommendations
* Mentor recommendations
* Skill-swap matching
* Personalized learning paths
* Course recommendations
* Skill-gap analysis

Keep recommendation logic behind services/interfaces so the implementation can later be replaced or upgraded.

---

# 46. IMPORTANT PRODUCT RULE

The platform should not force every learner to become a mentor.

A user may:

### Option A

Teach + Learn through skill swapping.

### Option B

Only learn.

### Option C

Only teach.

### Option D

Learn through paid content.

The product architecture must support all of these scenarios.

---

# 47. PRIVACY

Do not unnecessarily expose:

* Phone number
* Email
* Exact location
* Financial information
* Private documents

Users should control what profile information is public.

Implement appropriate privacy settings.

---

# 48. CONTENT OWNERSHIP

Mentors should agree to appropriate content/platform terms before uploading content.

The system should support content moderation and takedown workflows.

Do not assume that uploaded content automatically belongs to the platform.

Create appropriate terms and content ownership rules separately with legal review before production launch.

---

# 49. IMPORTANT DEVELOPMENT RULES

Before writing code:

1. Understand the architecture.
2. Create the database model.
3. Define API contracts.
4. Define navigation.
5. Define authentication.
6. Define feature boundaries.
7. Define reusable components.
8. Define environment configuration.

Then implement feature-by-feature.

Do not create random code without architecture.

---

# 50. AGENT EXECUTION RULE

You are not only a code generator.

You are responsible for producing a **maintainable production-quality software system**.

Before implementing any feature:

### Step 1

Explain what you are going to build.

### Step 2

Identify dependencies.

### Step 3

Define the data model/API changes.

### Step 4

Implement the feature.

### Step 5

Write tests.

### Step 6

Check for security issues.

### Step 7

Check for duplicated code.

### Step 8

Update documentation.

### Step 9

Explain how to run and test the feature.

Do not silently make major architectural decisions.

If an important requirement is ambiguous, identify the ambiguity and propose the safest scalable implementation rather than inventing business rules.

---

# 51. FINAL PRODUCT VISION

Skill Builder should become a platform where:

> **A person's knowledge becomes an opportunity for another person to learn.**

The platform connects:

**People → Skills → Learning → Skill Swaps → Mentors → Courses → Community**

The core philosophy is:

# LEARN SKILL

# SWAP SKILL

# TEACH SKILL

# BUILD TOGETHER

Build the product so that it can start as an MVP but scale into a large cross-platform skill-learning marketplace.

---

# FINAL INSTRUCTION TO THE DEVELOPMENT AGENT

Start by creating the complete technical architecture and project structure.

Do NOT immediately generate the entire application in one giant response.

Work incrementally.

First provide:

1. Final technology stack
2. System architecture
3. Android (Kotlin & Jetpack Compose) architecture
4. Backend architecture
5. Database ERD/schema
6. API specification
7. Authentication architecture
8. Payment architecture
9. Video architecture
10. Admin architecture
11. Folder structure
12. Development phases

Then begin implementation **Phase 1**.

Every subsequent phase must maintain the coding principles defined above:

**Modular • Reusable • Scalable • Secure • Testable • Maintainable • Production-ready**

Never sacrifice architecture quality simply to produce code faster.
