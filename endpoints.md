# API Endpoints

Base URL: `/api/v1`

---

## Authentication — `/api/v1/auth`

Authentication uses **server-side sessions**. On successful login, the server creates a session and returns a `JSESSIONID` cookie. All subsequent requests from the client must include that cookie. Logout invalidates the session server-side.

Usernames and passwords are handled via the `local_credentials` table. The `accounts` table holds the portal identity — it is auth-method-agnostic by design. Local username/password is just one possible "front door"; external providers (e.g. Discord OAuth) will be added as additional credential types feeding into the same `accounts` table. Each account has a `profiles` row (same PK) that holds display information.

Passwords are hashed with BCrypt before storage. Raw passwords are never persisted.

---

### `POST /api/v1/auth/register`

Creates a new account with local (username/password) credentials.

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

## Accounts — `/api/v1/accounts`

---

### `GET /api/v1/accounts/me`

Returns the account info of the currently authenticated account.

**Auth required:** Yes (valid session cookie)

**Request body:** None

**Response `200 OK`:**
```json
{
  "id": 1,
  "status": "ACTIVE"
}
```

| Field | Notes |
|---|---|
| `id` | Internal portal account ID |
| `status` | One of: `ACTIVE`, `SUSPENDED` |

**Responses:**

| Status | Condition |
|---|---|
| `200 OK` | Session valid; returns account info. |
| `401 Unauthorized` | No session, or session has expired/been invalidated. |

---

### `GET /api/v1/accounts/{id}/profile`

Returns the profile of the account with the given ID.

**Auth required:** Yes (valid session cookie)

**Path parameters:**

| Parameter | Notes |
|---|---|
| `id` | Account ID |

**Request body:** None

**Response `200 OK`:**
```json
{
  "displayName": "string"
}
```

| Field | Notes |
|---|---|
| `displayName` | Display name set at registration |

**Responses:**

| Status | Condition |
|---|---|
| `200 OK` | Account and profile found. |
| `401 Unauthorized` | No session, or session has expired/been invalidated. |
| `404 Not Found` | No account with the given ID exists. Body: `"No such account exists."` |

---

## Roles — `/api/v1/roles`

Roles group a set of permission keys and can be assigned to accounts. Roles are never hard-deleted — archiving a role revokes all its active assignments and marks it inactive.

---

### `GET /api/v1/roles`

Returns all non-archived roles.

**Auth required:** Yes

**Response `200 OK`:**
```json
[
  {
    "id": "uuid",
    "name": "string",
    "description": "string",
    "emoji": "string",
    "color": "string",
    "permissions": ["string"],
    "createdAt": "instant",
    "archivedAt": null
  }
]
```

---

### `POST /api/v1/roles`

Creates a new role.

**Auth required:** Yes

**Request body:**
```json
{
  "name": "string",
  "description": "string",
  "emoji": "string",
  "color": "string",
  "permissions": ["string"]
}
```

| Field | Notes |
|---|---|
| `permissions` | Optional. Set of permission keys to attach immediately. |

**Responses:**

| Status | Condition |
|---|---|
| `200 OK` | Role created. Returns the created role. |

---

### `GET /api/v1/roles/{id}`

Returns a single role by ID.

**Auth required:** Yes

**Responses:**

| Status | Condition |
|---|---|
| `200 OK` | Role found. |
| `404 Not Found` | No role with that ID. |

---

### `PATCH /api/v1/roles/{id}`

Partially updates a role. Only provided (non-null) fields are changed.

**Auth required:** Yes

**Request body:**
```json
{
  "name": "string",
  "description": "string",
  "emoji": "string",
  "color": "string"
}
```

**Responses:**

| Status | Condition |
|---|---|
| `200 OK` | Updated. Returns the updated role. |
| `404 Not Found` | No role with that ID. |

---

### `DELETE /api/v1/roles/{id}`

Archives a role and revokes all its active assignments.

**Auth required:** Yes

**Responses:**

| Status | Condition |
|---|---|
| `204 No Content` | Archived. |
| `404 Not Found` | No role with that ID. |

---

### `POST /api/v1/roles/{id}/permissions/{key}`

Adds a permission key to a role.

**Auth required:** Yes

**Path parameters:**

| Parameter | Notes |
|---|---|
| `key` | Permission key string (e.g. `core.example.permission`) |

**Responses:**

| Status | Condition |
|---|---|
| `204 No Content` | Permission added. |
| `400 Bad Request` | Unknown permission key. |
| `404 Not Found` | No role with that ID. |

---

### `DELETE /api/v1/roles/{id}/permissions/{key}`

Removes a permission key from a role.

**Auth required:** Yes

**Responses:**

| Status | Condition |
|---|---|
| `204 No Content` | Permission removed. |
| `400 Bad Request` | Unknown permission key. |
| `404 Not Found` | No role with that ID. |
