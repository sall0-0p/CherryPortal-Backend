# API Endpoints

Base URL: `/api/v1`

---

## Authentication — `/api/v1/auth`

Authentication uses **server-side sessions**. On successful login, the server creates a session and returns a `JSESSIONID` cookie. All subsequent requests from the client must include that cookie. Logout invalidates the session server-side.

Usernames and passwords are handled via the `local_credentials` table. The `users` table holds the portal identity — it is auth-method-agnostic by design. Local username/password is just one possible "front door"; external providers (e.g. Discord OAuth) will be added as additional credential types feeding into the same `users` table.

Passwords are hashed with BCrypt before storage. Raw passwords are never persisted.

---

### `POST /api/v1/auth/register`

Creates a new account account with local (username/password) credentials.

**Auth required:** No

**Request body:**
```json
{
  "username": "string",
  "displayName": "string",
  "password": "string"
}
```

| Field | Notes |
|---|---|
| `username` | Must be unique across all local credentials |
| `displayName` | The name shown in the UI; does not need to be unique |
| `password` | Plaintext — hashed with BCrypt before storage |

**Responses:**

| Status | Condition |
|---|---|
| `200 OK` | Account created successfully. Body is empty. |
| `400 Bad Request` | Username is already taken. Body: `"Username taken"` |

**Notes:**
- Does not auto-login. The client must follow up with `POST /login` to obtain a session.
- New accounts are created with status `ACTIVE`.

---

### `POST /api/v1/auth/login`

Authenticates with local credentials and establishes a session.

**Auth required:** No

**Request body:**
```json
{
  "username": "string",
  "password": "string"
}
```

**Responses:**

| Status | Condition |
|---|---|
| `200 OK` | Credentials valid. Sets `JSESSIONID` cookie. Body is empty. |
| `401 Unauthorized` | Wrong password or username does not exist. Body is empty. |

**Notes:**
- Both "wrong password" and "unknown username" return 401 with no distinguishing message intentionally — revealing which one is correct would allow username enumeration.
- A new session is created on each successful login (session fixation protection — the old session ID is never reused).

---

### `POST /api/v1/auth/logout`

Invalidates the current session.

**Auth required:** No (safe to call even without a session)

**Request body:** None

**Responses:**

| Status | Condition |
|---|---|
| `200 OK` | Always. Session is invalidated if one existed; no-op otherwise. |

---

## Users — `/api/v1/users`

---

### `GET /api/v1/users/me`

Returns the portal profile of the currently authenticated account.

**Auth required:** Yes (valid session cookie)

**Request body:** None

**Response `200 OK`:**
```json
{
  "id": 1,
  "displayName": "string",
  "status": "ACTIVE"
}
```

| Field | Notes |
|---|---|
| `id` | Internal portal account ID |
| `displayName` | Display name set at registration |
| `status` | One of: `ACTIVE`, `SUSPENDED` |

**Responses:**

| Status | Condition |
|---|---|
| `200 OK` | Session valid; returns profile. |
| `401 Unauthorized` | No session, or session has expired/been invalidated. |
