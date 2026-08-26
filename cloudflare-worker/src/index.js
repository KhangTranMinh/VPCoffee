/**
 * Cloudflare Worker — FCM Push Notification Proxy
 *
 * This worker receives push notification requests from the Android app,
 * authenticates with Firebase using a service account, and sends the
 * notification via FCM HTTP v1 API.
 *
 * The service account JSON is stored as an encrypted Cloudflare secret
 * (SERVICE_ACCOUNT_JSON), never exposed to the client.
 *
 * POST body: { token, title, body, data? }
 * Returns:   { success: true, messageId } or { error: "..." }
 */

export default {
  async fetch(request, env) {
    const url = new URL(request.url);
    const path = url.pathname;

    // Handle CORS preflight requests
    if (request.method === "OPTIONS") {
      return new Response(null, {
        headers: {
          "Access-Control-Allow-Origin": "*",
          "Access-Control-Allow-Methods": "GET, POST, OPTIONS",
          "Access-Control-Allow-Headers": "Content-Type",
        },
      });
    }

    // Route: GET /test — health check
    if (path === "/test" && request.method === "GET") {
      return jsonResponse({
        status: "ok",
        message: "VPCoffee Push Worker is running",
        timestamp: new Date().toISOString(),
      });
    }

    // Route: POST /send-push — send FCM push notification
    if (path === "/send-push" && request.method === "POST") {
      return await handleSendPush(request, env);
    }

    // Unknown route
    return jsonResponse({ error: "Not found" }, 404);
  },
};

/**
 * Handle POST /send-push — send an FCM push notification.
 */
async function handleSendPush(request, env) {
  try {
    const { token, title, body, data } = await request.json();

    // Validate required fields
    if (!token || !title || !body) {
      return jsonResponse(
        { error: "Missing required fields: token, title, body" },
        400
      );
    }

    // Step 1: Get OAuth2 access token using the service account
    const accessToken = await getAccessToken(env.SERVICE_ACCOUNT_JSON, env.OAUTH_SCOPE, env.OAUTH_TOKEN_URL);

    // Step 2: Send FCM message via HTTP v1 API
    const fcmResponse = await fetch(env.FCM_API_URL, {
      method: "POST",
      headers: {
        Authorization: `Bearer ${accessToken}`,
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        message: {
          token,
          notification: { title, body },
          data: data || {},
        },
      }),
    });

    const fcmResult = await fcmResponse.json();

    if (!fcmResponse.ok) {
      console.error("FCM error:", fcmResult);
      return jsonResponse(
        { error: fcmResult.error?.message || "FCM request failed" },
        500
      );
    }

    return jsonResponse({ success: true, messageId: fcmResult.name });
  } catch (err) {
    console.error("Worker error:", err);
    return jsonResponse({ error: err.message }, 500);
  }
}

/**
 * Get OAuth2 access token using service account JWT.
 *
 * Flow:
 * 1. Build a JWT (header + payload) with the service account email
 * 2. Sign it with the service account's RSA private key
 * 3. Exchange the signed JWT for a short-lived access token
 *
 * @param {string} serviceAccountJson - JSON string of the service account key
 * @param {string} scope - OAuth2 scope (e.g. firebase.messaging)
 * @param {string} tokenUrl - OAuth2 token endpoint URL
 * @returns {string} OAuth2 access token
 */
async function getAccessToken(serviceAccountJson, scope, tokenUrl) {
  const sa = JSON.parse(serviceAccountJson);
  const now = Math.floor(Date.now() / 1000);

  // Build JWT header and payload
  const header = { alg: "RS256", typ: "JWT" };
  const payload = {
    iss: sa.client_email,       // service account email
    scope: scope,               // requested OAuth scope
    aud: tokenUrl,              // token endpoint (JWT audience)
    iat: now,                   // issued at
    exp: now + 3600,            // expires in 1 hour
  };

  // Base64url encode header and payload
  const encodedHeader = base64url(JSON.stringify(header));
  const encodedPayload = base64url(JSON.stringify(payload));
  const dataToSign = `${encodedHeader}.${encodedPayload}`;

  // Parse the PEM private key into binary format
  const privateKeyPem = sa.private_key
    .replace("-----BEGIN PRIVATE KEY-----", "")
    .replace("-----END PRIVATE KEY-----", "")
    .replace(/\s/g, "");

  const binaryKey = Uint8Array.from(atob(privateKeyPem), (c) =>
    c.charCodeAt(0)
  );

  // Import the private key for signing (Web Crypto API)
  const privateKey = await crypto.subtle.importKey(
    "pkcs8",
    binaryKey,
    { name: "RSASSA-PKCS1-v1_5", hash: "SHA-256" },
    false,
    ["sign"]
  );

  // Sign the JWT
  const signature = await crypto.subtle.sign(
    "RSASSA-PKCS1-v1_5",
    privateKey,
    new TextEncoder().encode(dataToSign)
  );

  const encodedSignature = base64url(signature);
  const jwt = `${dataToSign}.${encodedSignature}`;

  // Exchange JWT for an access token
  const tokenResponse = await fetch(tokenUrl, {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body: `grant_type=urn:ietf:params:oauth:grant-type:jwt-bearer&assertion=${jwt}`,
  });

  const tokenResult = await tokenResponse.json();

  if (!tokenResult.access_token) {
    throw new Error("Failed to get access token: " + JSON.stringify(tokenResult));
  }

  return tokenResult.access_token;
}

/**
 * Base64url encode (RFC 4648 §5 — no padding).
 * Works with both strings and ArrayBuffers.
 *
 * @param {string|ArrayBuffer} input
 * @returns {string}
 */
function base64url(input) {
  let str;
  if (typeof input === "string") {
    str = btoa(input);
  } else {
    str = btoa(String.fromCharCode(...new Uint8Array(input)));
  }
  return str.replace(/\+/g, "-").replace(/\//g, "_").replace(/=+$/, "");
}

/**
 * Helper to return a JSON response with CORS headers.
 *
 * @param {object} data - Response body
 * @param {number} status - HTTP status code
 * @returns {Response}
 */
function jsonResponse(data, status = 200) {
  return new Response(JSON.stringify(data), {
    status,
    headers: {
      "Content-Type": "application/json",
      "Access-Control-Allow-Origin": "*",
    },
  });
}
