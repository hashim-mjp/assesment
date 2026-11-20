# Sparse frontend tests (T14 / FR10)

No automated FE suite was added: the Next.js app has no Jest/Vitest/RTL
dependencies, and adding them solely for one form-mapping test was deferred
per dependency discipline (plan T14: "or document manual-only").

## Manual smoke (UI meaningful errors)

1. Create ticket with blank title → banner shows API `message`; field hint under title from `details[]`.
2. Force illegal transition (e.g. crafted `POST .../transition`) → detail shows 422 `message`; status badge unchanged.
3. List: search `q` + status filter update URL query params and results.

Automated FR10 coverage for error JSON remains in backend WebMvc/IT suites.
