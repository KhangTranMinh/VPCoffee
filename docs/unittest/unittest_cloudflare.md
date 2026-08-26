# Cloudflare Worker — Unit Tests

> Unit test documentation for the Cloudflare Worker FCM proxy.
> Last updated: 2026-08-26

---

## Test Infrastructure

### Dependencies
| Library | Version | Purpose |
|---------|---------|---------|
| Vitest | ^1.6.0 | Test runner |
| Miniflare | ^3.20240806.0 | Cloudflare Workers runtime for testing |

### Run Tests
```bash
cd cloudflare-worker
npm test
```

### Watch Mode
```bash
npm run test:watch
```

---

## Test Files

### `src/index.test.js`
Tests for the Cloudflare Worker endpoints and routing.

#### GET /test (Health Check)
| Test Case | Description |
|-----------|-------------|
| `returns ok status` | Verifies health check returns 200 with status, message, and timestamp |

#### POST /send-push (Send Notification)
| Test Case | Description |
|-----------|-------------|
| `returns 400 when token is missing` | Input validation |
| `returns 400 when title is missing` | Input validation |
| `returns 400 when body is missing` | Input validation |
| `returns 400 when body is empty` | Empty body validation |

#### Unknown Routes
| Test Case | Description |
|-----------|-------------|
| `returns 404 for unknown path` | Route not found |
| `returns 404 for root path` | Root path handling |

#### CORS
| Test Case | Description |
|-----------|-------------|
| `handles OPTIONS preflight request` | CORS preflight |
| `includes CORS headers in responses` | CORS headers |

#### HTTP Methods
| Test Case | Description |
|-----------|-------------|
| `returns 404 for PUT on /send-push` | Method not matched |
| `returns 404 for DELETE on /send-push` | Method not matched |

---

## Test Coverage Summary

| Category | Tests | Status |
|----------|-------|--------|
| Health check endpoint | 1 test | ✅ |
| Send push validation | 4 tests | ✅ |
| Route handling | 2 tests | ✅ |
| CORS | 2 tests | ✅ |
| HTTP methods | 2 tests | ✅ |
| **Total** | **11 tests** | ✅ |

---

## What's NOT Tested

| Component | Reason |
|-----------|--------|
| `getAccessToken()` JWT flow | Requires mocking `crypto.subtle` and `fetch` for Google OAuth |
| `handleSendPush()` FCM call | Requires mocking `fetch` for FCM API |
| `base64url()` encoding | Pure function, but not directly exported |

### Recommendations for Future Testing

1. **Export `base64url` and `getAccessToken`** — Make them testable by exporting
2. **Mock `fetch` and `crypto.subtle`** — Test the full JWT flow
3. **Integration tests** — Use Miniflare's `undici` to test with mocked Google APIs
4. **Add error scenario tests** — Test FCM API errors, invalid service account, etc.

---

## Architecture Notes

The worker uses a simple routing pattern:

```javascript
export default {
  async fetch(request, env) {
    const url = new URL(request.url);
    const path = url.pathname;

    if (path === "/test" && request.method === "GET") { ... }
    if (path === "/send-push" && request.method === "POST") { ... }

    return jsonResponse({ error: "Not found" }, 404);
  },
};
```

**Key design decisions:**
- Path + method are checked together (not separate middleware)
- Unmatched routes return 404 (not 405 Method Not Allowed)
- All responses include CORS headers via `jsonResponse()` helper
