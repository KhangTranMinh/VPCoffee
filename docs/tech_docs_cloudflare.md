# Cloudflare Worker — Technical Documentation

> FCM Push Notification Proxy for VPCoffee.
> Last updated: 2026-08-26

---

## Table of Contents

1. [Overview](#1-overview)
2. [API Endpoints](#2-api-endpoints)
3. [Authentication Flow](#3-authentication-flow)
4. [Environment Variables & Secrets](#4-environment-variables--secrets)
5. [Error Handling](#5-error-handling)
6. [CORS Configuration](#6-cors-configuration)
7. [Deployment](#7-deployment)
8. [Security Considerations](#8-security-considerations)

---

## 1. Overview

The Cloudflare Worker is an **FCM Push Notification Proxy**. It sits between the VPCoffee Android app and Firebase, ensuring that service account credentials never leave the server side.

**Why a proxy?**
- Service account credentials must not be embedded in client apps (Google flags this as a security issue)
- The worker authenticates with Firebase using a service account JWT
- The Android app only needs to send a simple HTTPS POST

**Worker URL:** `https://send-push-notification.kloverahn.workers.dev`

**Source:** `cloudflare-worker/src/index.js`

**Runtime:** Cloudflare Workers (V8 isolates, Web Crypto API)

**Dependencies:** Zero npm packages — all crypto uses built-in Web Crypto API

---

## 2. API Endpoints

| Method | Path | Purpose | Request Body | Success Response |
|--------|------|---------|-------------|-----------------|
| `GET` | `/test` | Health check | — | `{ status: "ok", message: "VPCoffee Push Worker is running", timestamp: "..." }` |
| `POST` | `/send-push` | Send FCM push notification | `{ token, title, body, data? }` | `{ success: true, messageId: "..." }` |
| `OPTIONS` | `*` | CORS preflight | — | Empty 204 with CORS headers |
| Any | `*` (unmatched) | Fallback | — | `{ error: "Not found" }` (404) |

### POST /send-push

**Required fields:**
| Field | Type | Description |
|-------|------|-------------|
| `token` | string | FCM device registration token |
| `title` | string | Notification title |
| `body` | string | Notification body text |

**Optional fields:**
| Field | Type | Description |
|-------|------|-------------|
| `data` | object | Arbitrary key-value data payload (defaults to `{}`) |

**Example request:**
```bash
curl -X POST https://send-push-notification.kloverahn.workers.dev/send-push \
  -H "Content-Type: application/json" \
  -d '{"token":"FCM_TOKEN","title":"Hello","body":"World"}'
```

**Example response:**
```json
{
  "success": true,
  "messageId": "projects/vpcoffee-791be/messages/0:1234567890"
}
```

---

## 3. Authentication Flow

The worker authenticates with Firebase using the **Google OAuth2 Service Account JWT flow**, implemented entirely with the Web Crypto API (no external libraries).

### Flow Diagram

```
┌─────────────┐    POST /send-push    ┌─────────────────────┐
│  Android App │ ────────────────────► │  Cloudflare Worker  │
└─────────────┘                        └──────────┬──────────┘
                                                  │
                                    1. Parse service account JSON
                                    2. Build JWT (header + payload)
                                    3. Sign with RSA private key
                                    4. Exchange JWT for access token
                                                  │
                                                  ▼
                                       ┌─────────────────────┐
                                       │  Google OAuth2       │
                                       │  Token Endpoint      │
                                       └──────────┬──────────┘
                                                  │
                                    5. Receive access_token
                                    6. Call FCM API with Bearer token
                                                  │
                                                  ▼
                                       ┌─────────────────────┐
                                       │  FCM HTTP v1 API     │
                                       └─────────────────────┘
```

### Step-by-Step

1. **Parse service account JSON** from the `SERVICE_ACCOUNT_JSON` secret
2. **Build JWT header:** `{ alg: "RS256", typ: "JWT" }`
3. **Build JWT payload:**
   - `iss` — service account `client_email`
   - `scope` — `https://www.googleapis.com/auth/firebase.messaging`
   - `aud` — `https://oauth2.googleapis.com/token`
   - `iat` — current Unix timestamp
   - `exp` — current time + 3600 seconds (1 hour)
4. **Base64url-encode** header and payload (RFC 4648 §5, no padding)
5. **Parse PEM private key** — strip headers/whitespace, decode from base64 to binary
6. **Import key** via `crypto.subtle.importKey` using `pkcs8` format with `RSASSA-PKCS1-v1_5` and `SHA-256`
7. **Sign** the `header.payload` string with `crypto.subtle.sign`
8. **Exchange signed JWT** for access token by POSTing to token URL with `grant_type=urn:ietf:params:oauth:grant-type:jwt-bearer`
9. **Use access token** as `Bearer` token in FCM API call

**Note:** A new access token is minted on every `/send-push` request (no caching). Google tokens last 1 hour.

---

## 4. Environment Variables & Secrets

### Plain Variables (in `wrangler.toml`)

| Variable | Value | Used In |
|----------|-------|---------|
| `FCM_API_URL` | `https://fcm.googleapis.com/v1/projects/vpcoffee-791be/messages:send` | FCM API call |
| `OAUTH_TOKEN_URL` | `https://oauth2.googleapis.com/token` | JWT exchange |
| `OAUTH_SCOPE` | `https://www.googleapis.com/auth/firebase.messaging` | JWT payload |

### Encrypted Secret (via `wrangler secret put`)

| Secret | Purpose |
|--------|---------|
| `SERVICE_ACCOUNT_JSON` | Full JSON contents of Firebase service account key. Contains `client_email` and `private_key` used to mint JWT. |

**Security:** Secrets are never stored in source code. The `.gitignore` excludes `.dev.vars` (local development secrets) and `.wrangler/` (cache).

---

## 5. Error Handling

The worker has four layers of error handling:

### Input Validation
- **Trigger:** Missing `token`, `title`, or `body`
- **Response:** `400` — `{ error: "Missing required fields: token, title, body" }`

### FCM API Errors
- **Trigger:** FCM response status is not OK
- **Response:** `500` — `{ error: "<FCM error message>" }`
- **Fallback:** `"FCM request failed"` if no message in response

### Access Token Failure
- **Trigger:** Token exchange response lacks `access_token`
- **Response:** `500` — `{ error: "Failed to get access token: <response body>" }`

### Catch-All
- **Trigger:** Any uncaught exception (JSON parse, crypto errors, network errors)
- **Response:** `500` — `{ error: "<error message>" }`

All errors are logged to Cloudflare console via `console.error`.

---

## 6. CORS Configuration

### Preflight Handler
Responds to `OPTIONS` requests with:
```
Access-Control-Allow-Origin: *
Access-Control-Allow-Methods: GET, POST, OPTIONS
Access-Control-Allow-Headers: Content-Type
```

### All Responses
Every JSON response (success or error) includes:
```
Access-Control-Allow-Origin: *
```

### Security Implications
- Policy is fully permissive (`*` origin)
- Any client that knows the URL can call the worker
- Security relies on:
  - FCM device token being required (can't push to unknown devices)
  - Service account secret being server-side only

---

## 7. Deployment

### Prerequisites
- Cloudflare account (free, no credit card)
- Wrangler CLI installed (`npm install -g wrangler`)
- Authenticated (`wrangler login`)

### Deploy
```bash
cd cloudflare-worker
wrangler deploy
```

### Set Secret (first time only)
```bash
cat credentials/service-account.json | wrangler secret put SERVICE_ACCOUNT_JSON
```

### Update Environment Variables
Edit `wrangler.toml` and run `wrangler deploy`.

### View Logs
```bash
wrangler tail
```

---

## 8. Security Considerations

| Aspect | Status | Notes |
|--------|--------|-------|
| Service account in app | ✅ Removed | Credentials stored as encrypted Cloudflare secret |
| Service account in git | ✅ Not committed | `credentials/` is in `.gitignore` |
| Worker authentication | ⚠️ None | Any caller with the URL can invoke `/send-push` |
| CORS policy | ⚠️ Permissive | `*` origin allows any client |
| Token caching | ❌ None | New access token minted on every request |

### Recommendations
- Consider adding API key authentication to the worker
- Consider caching access tokens (they last 1 hour)
- Monitor Cloudflare analytics for unexpected traffic
