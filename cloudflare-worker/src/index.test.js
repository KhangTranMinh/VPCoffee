import { describe, it, expect, vi, beforeEach } from "vitest";
import worker from "./index.js";

// Mock environment variables
const env = {
  FCM_API_URL: "https://fcm.googleapis.com/v1/projects/test/messages:send",
  OAUTH_TOKEN_URL: "https://oauth2.googleapis.com/token",
  OAUTH_SCOPE: "https://www.googleapis.com/auth/firebase.messaging",
  SERVICE_ACCOUNT_JSON: JSON.stringify({
    client_email: "test@test.iam.gserviceaccount.com",
    private_key: "-----BEGIN PRIVATE KEY-----\nMIIBVAIBADANBg...\n-----END PRIVATE KEY-----",
  }),
};

// Helper to create a mock Request
function createRequest(method, path, body = null) {
  const url = `https://worker.test${path}`;
  const options = { method };
  if (body) {
    options.body = JSON.stringify(body);
    options.headers = { "Content-Type": "application/json" };
  }
  return new Request(url, options);
}

describe("Cloudflare Worker", () => {
  describe("GET /test", () => {
    it("returns ok status", async () => {
      const req = createRequest("GET", "/test");
      const res = await worker.fetch(req, env);
      const data = await res.json();

      expect(res.status).toBe(200);
      expect(data.status).toBe("ok");
      expect(data.message).toBe("VPCoffee Push Worker is running");
      expect(data.timestamp).toBeDefined();
    });
  });

  describe("POST /send-push", () => {
    it("returns 400 when token is missing", async () => {
      const req = createRequest("POST", "/send-push", {
        title: "Hello",
        body: "World",
      });
      const res = await worker.fetch(req, env);
      const data = await res.json();

      expect(res.status).toBe(400);
      expect(data.error).toBe("Missing required fields: token, title, body");
    });

    it("returns 400 when title is missing", async () => {
      const req = createRequest("POST", "/send-push", {
        token: "fcm-token",
        body: "World",
      });
      const res = await worker.fetch(req, env);
      const data = await res.json();

      expect(res.status).toBe(400);
      expect(data.error).toBe("Missing required fields: token, title, body");
    });

    it("returns 400 when body is missing", async () => {
      const req = createRequest("POST", "/send-push", {
        token: "fcm-token",
        title: "Hello",
      });
      const res = await worker.fetch(req, env);
      const data = await res.json();

      expect(res.status).toBe(400);
      expect(data.error).toBe("Missing required fields: token, title, body");
    });

    it("returns 400 when body is empty", async () => {
      const req = createRequest("POST", "/send-push", {});
      const res = await worker.fetch(req, env);
      const data = await res.json();

      expect(res.status).toBe(400);
      expect(data.error).toBe("Missing required fields: token, title, body");
    });
  });

  describe("Unknown routes", () => {
    it("returns 404 for unknown path", async () => {
      const req = createRequest("GET", "/unknown");
      const res = await worker.fetch(req, env);
      const data = await res.json();

      expect(res.status).toBe(404);
      expect(data.error).toBe("Not found");
    });

    it("returns 404 for root path", async () => {
      const req = createRequest("GET", "/");
      const res = await worker.fetch(req, env);
      const data = await res.json();

      expect(res.status).toBe(404);
      expect(data.error).toBe("Not found");
    });
  });

  describe("CORS", () => {
    it("handles OPTIONS preflight request", async () => {
      const req = createRequest("OPTIONS", "/send-push");
      const res = await worker.fetch(req, env);

      expect(res.status).toBe(200);
      expect(res.headers.get("Access-Control-Allow-Origin")).toBe("*");
      expect(res.headers.get("Access-Control-Allow-Methods")).toContain("POST");
      expect(res.headers.get("Access-Control-Allow-Headers")).toContain("Content-Type");
    });

    it("includes CORS headers in responses", async () => {
      const req = createRequest("GET", "/test");
      const res = await worker.fetch(req, env);

      expect(res.headers.get("Access-Control-Allow-Origin")).toBe("*");
    });
  });

  describe("HTTP methods", () => {
    it("returns 404 for PUT on /send-push (method not matched)", async () => {
      const req = createRequest("PUT", "/send-push");
      const res = await worker.fetch(req, env);
      const data = await res.json();

      // Worker checks path + method together, so unmatched method falls to 404
      expect(res.status).toBe(404);
      expect(data.error).toBe("Not found");
    });

    it("returns 404 for DELETE on /send-push (method not matched)", async () => {
      const req = createRequest("DELETE", "/send-push");
      const res = await worker.fetch(req, env);
      const data = await res.json();

      expect(res.status).toBe(404);
      expect(data.error).toBe("Not found");
    });
  });
});
