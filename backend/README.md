# SkillBuilder Backend API (Node.js + TypeScript + MySQL)

Production-grade, scalable backend for the **SkillBuilder** Android application, designed with **Node.js**, **Express**, **TypeScript**, and **MySQL 8.0** using **Prisma ORM** and **Socket.io**.

---

## 🌟 Core Features

- **Google Credential Manager & JWT Auth**: Direct 1-tap Google ID token exchange (`/api/v1/auth/google`) and standard email/password authentication.
- **Reciprocal Skill-Swap Engine**: Bilateral matching algorithm evaluating complementary taught vs. wanted skills with compatibility scoring.
- **Video Courses & Streaming**: Course management, Media3 ExoPlayer HLS streaming, lesson progress tracking (auto-completion at $\ge 90\%$), and Cloudflare R2 / S3 pre-signed upload URLs.
- **Real-Time Encrypted Chat & Doubts Engine**: Socket.io real-time messaging with live typing indicators, Doubt Queries (`isResolved`), and Video Demands with community upvoting.
- **Mentor Wallet & Payouts**: Balance tracking, transaction ledgers, and payout requests.

---

## 🏗️ Tech Stack

| Component | Technology |
|---|---|
| **Runtime & Language** | Node.js v20+ with TypeScript 5.x |
| **HTTP Framework** | Express.js (Clean Layered Architecture) |
| **Relational Database** | MySQL 8.0 |
| **ORM / Data Access** | Prisma ORM 6.x |
| **Real-Time Communication** | Socket.io 4.x (WebSockets) |
| **Identity & Security** | Google Auth Library, JWT (`jsonwebtoken`), `bcryptjs`, `zod` |
| **Media Storage** | Cloudflare R2 / AWS S3 Pre-signed URLs |

---

## 🚀 Quick Start Guide

### 1. Prerequisites
- **Node.js** v20+ and **npm** installed.
- **MySQL 8.0** server running locally or on a cloud provider (e.g., AWS RDS, PlanetScale, Aiven).

### 2. Install Dependencies
```bash
cd backend
npm install
```

### 3. Configure Environment Variables
Copy `.env.example` to `.env` and configure your database credentials:
```bash
cp .env.example .env
```
Ensure `DATABASE_URL` matches your local MySQL server:
```env
DATABASE_URL="mysql://root:password@localhost:3306/skillbuilder"
PORT=5000
JWT_SECRET="your-jwt-secret"
JWT_REFRESH_SECRET="your-jwt-refresh-secret"
GOOGLE_CLIENT_ID="your-google-oauth-client-id.apps.googleusercontent.com"
```

### 4. Database Setup & Migrations
```bash
# Generate Prisma Client
npm run prisma:generate

# Push database schema to MySQL (creates tables & indexes)
npm run prisma:push

# Seed database with sample categories, skills, demo users, course, and swaps
npm run prisma:seed
```

### 5. Start Development Server
```bash
npm run dev
```
The server will start on `http://localhost:5000`.

---

## 📡 API Endpoints Reference

### 🔐 Authentication (`/api/v1/auth`)
| Method | Endpoint | Description | Auth Required |
|---|---|---|---|
| `POST` | `/api/v1/auth/google` | Verify Google ID token from Android Credential Manager | No |
| `POST` | `/api/v1/auth/signup` | Register with email and password | No |
| `POST` | `/api/v1/auth/login` | Login with email and password | No |
| `POST` | `/api/v1/auth/refresh` | Issue fresh access token using refresh token | No |
| `GET` | `/api/v1/auth/me` | Fetch authenticated user profile | Yes (Bearer) |

### 👤 Users & Mentors (`/api/v1/users`)
| Method | Endpoint | Description | Auth Required |
|---|---|---|---|
| `GET` | `/api/v1/users/:id` | Fetch public user profile and skills | No |
| `PATCH` | `/api/v1/users/me` | Update bio, location, avatar, mentor status | Yes (Bearer) |
| `PUT` | `/api/v1/users/me/skills` | Update taught and wanted skill lists | Yes (Bearer) |
| `GET` | `/api/v1/users/me/mentor-stats` | Fetch mentor revenue, active students, rating | Yes (Bearer) |

### 🎯 Skill Taxonomy (`/api/v1/skills`)
| Method | Endpoint | Description | Auth Required |
|---|---|---|---|
| `GET` | `/api/v1/skills/categories` | List skill categories with icons and counts | No |
| `GET` | `/api/v1/skills` | Search & filter skills (categoryId, search, level) | No |
| `POST` | `/api/v1/skills` | Suggest/add a new skill | Yes (Bearer) |

### 🔄 Reciprocal Skill Swaps (`/api/v1/swaps`)
| Method | Endpoint | Description | Auth Required |
|---|---|---|---|
| `GET` | `/api/v1/swaps/matches` | Execute reciprocal matching algorithm | Yes (Bearer) |
| `POST` | `/api/v1/swaps` | Create new swap proposal | Yes (Bearer) |
| `GET` | `/api/v1/swaps` | List user's swap proposals (sent & received) | Yes (Bearer) |
| `PATCH` | `/api/v1/swaps/:id/status` | Update proposal status (`ACCEPTED`, `ACTIVE`, `COMPLETED`, `REJECTED`) | Yes (Bearer) |

### 🎬 Courses & Video Lessons (`/api/v1/courses`)
| Method | Endpoint | Description | Auth Required |
|---|---|---|---|
| `GET` | `/api/v1/courses` | Browse public course catalog | Optional |
| `GET` | `/api/v1/courses/:id` | Get course details with curriculum & progress | Optional |
| `POST` | `/api/v1/courses` | Create new video course | Yes (Bearer) |
| `POST` | `/api/v1/courses/:id/lessons` | Add video lesson to course | Yes (Bearer) |
| `POST` | `/api/v1/courses/lessons/:lessonId/progress` | Sync watch progress (`>= 90%` completes lesson) | Yes (Bearer) |
| `POST` | `/api/v1/courses/upload-url` | Generate Cloudflare R2 / S3 pre-signed upload URL | Yes (Bearer) |

### 💬 Real-Time Chat & Doubts (`/api/v1/chat`)
| Method | Endpoint | Description | Auth Required |
|---|---|---|---|
| `GET` | `/api/v1/chat/conversations` | List user conversations with unread/doubt badges | Yes (Bearer) |
| `GET` | `/api/v1/chat/conversations/:id/messages` | Fetch message history for conversation | Yes (Bearer) |
| `POST` | `/api/v1/chat/messages` | Send message (Standard, Doubt Query, Video Demand) | Yes (Bearer) |
| `PATCH` | `/api/v1/chat/messages/:id/resolve` | Mark doubt query as resolved | Yes (Bearer) |
| `POST` | `/api/v1/chat/messages/:id/vote` | Upvote a video demand | Yes (Bearer) |

### 💳 Mentor Wallet (`/api/v1/wallet`)
| Method | Endpoint | Description | Auth Required |
|---|---|---|---|
| `GET` | `/api/v1/wallet` | Get wallet balance and transaction ledger | Yes (Bearer) |
| `POST` | `/api/v1/wallet/payout` | Request payout transfer | Yes (Bearer) |

---

## ⚡ WebSocket Real-Time Events (Socket.io)

Connect to the WebSocket server using JWT handshake:
```javascript
import { io } from "socket.io-client";

const socket = io("http://localhost:5000", {
  auth: { token: "<USER_JWT_ACCESS_TOKEN>" }
});

// Join conversation
socket.emit("join_conversation", "conv-alex-sophia");

// Send message
socket.emit("send_message", {
  conversationId: "conv-alex-sophia",
  text: "Can you explain the thumb independence technique?",
  type: "DOUBT_QUERY",
  referenceTopic: "Lesson 2: Travis Picking"
});

// Receive live message
socket.on("new_message", (message) => {
  console.log("New message:", message);
});

// Resolve doubt in real-time
socket.emit("resolve_doubt", {
  messageId: "<MESSAGE_ID>",
  conversationId: "conv-alex-sophia"
});
```
