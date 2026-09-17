# Veyra

**Veyra — Never Miss Your Stop.**

Veyra is an offline-first Android journey protection app built around two safety-critical jobs: **tracking** and **alarm**.

## Core safety design

The critical path is local:

`GPS / GNSS → validation → protection confidence → local alarm`

An already-started protected journey does not require internet access, Maps, AI, Firebase, Supabase, a train API, or the Veyra backend to perform the primary wake decision.

The cloud path is separate:

`Android → Veyra Gateway → Maps / Places / Routes / Geocoding / Supabase / Firebase / AI / Train provider`

Cloud failures must degrade gracefully rather than disabling local protection.

## Project layout

```text
Veyra/
├── android/             # Jetpack Compose Android app
├── backend/             # Spring Boot optional cloud gateway
├── supabase/            # PostgreSQL + RLS schema
├── docs/                # Architecture, security, release notes
└── .github/workflows/   # CI
```

## Android capabilities

- Jetpack Compose UI
- Local Room journey history
- Foreground location tracking
- Freshness, accuracy, mock-location and impossible-jump validation
- Adaptive GPS update frequency near the destination
- Local protection confidence engine using distance, accuracy, trend, speed and bearing
- Dedicated alarm notification + lock-screen alarm activity
- Vibration + system alarm audio
- Exact AlarmManager backup with inexact/idle fallback when exact access is unavailable
- Reboot/time-change recovery for the time fallback
- Device safety checks for permission, location, notifications, alarm volume, full-screen alarm capability, battery optimization and exact-alarm access
- Optional Firebase Analytics and FCM
- Optional Veyra backend client for Places, Routes, geocoding and train provider calls

## Optional backend integrations

The Spring Boot gateway supports:

- Supabase Auth and PostgREST
- Supabase journey sync
- Google Places Text Search
- Google Routes API
- Google geocoding
- OpenAI / Gemini / Anthropic adapters
- Configurable train-tracking provider
- Firebase Admin / FCM server push

These integrations are never required by the local alarm path.

## Build from an Android phone

The repository is organized so it can be opened in a mobile IDE that supports Gradle projects. The Android project lives under `android/`.

For local backend testing on an Android emulator, the default backend URL is `http://10.0.2.2:8080`. Debug builds allow cleartext only for this local development case. Release builds keep cleartext disabled.

To use another backend URL, pass a Gradle property such as:

```text
-PveyraBackendUrl=https://your-api.example.com/
```

## Firebase setup

Create an Android app in Firebase with package name `com.vjagarwal.veyra`, download `google-services.json`, and place it at:

`android/app/google-services.json`

The repository contains `android/app/google-services.json.example` as a template. The real file is ignored by Git.

Firebase's current Android setup uses the Google Services Gradle plugin and the Firebase Android BoM. citeturn894310search0turn894310search2

## Supabase setup

Run `supabase/schema.sql` in the Supabase SQL editor. Configure the backend with:

```text
SUPABASE_URL=
SUPABASE_ANON_KEY=
SUPABASE_SERVICE_ROLE_KEY=
```

Keep the service-role key server-side only.

## Maps / Places / Routes

Configure:

```text
GOOGLE_MAPS_SERVER_KEY=
GOOGLE_PLACES_URL=https://places.googleapis.com/v1
GOOGLE_ROUTES_URL=https://routes.googleapis.com/directions/v2:computeRoutes
GOOGLE_GEOCODE_URL=https://maps.googleapis.com/maps/api/geocode/json
```

The mobile app should not contain the server API key.

## AI providers

Use one backend adapter at a time:

```text
AI_PROVIDER=openai|gemini|anthropic|none
AI_API_KEY=
AI_MODEL=
AI_BASE_URL=
```

AI is for explanations/assistance only and must not make the wake decision.

## Train provider

The train integration is deliberately provider-neutral because paid/public rail APIs have different contracts:

```text
TRAIN_API_BASE_URL=
TRAIN_API_KEY=
TRAIN_API_SEARCH_PATH=/trains/{number}
```

## Firebase server push

For FCM server delivery set:

```text
FIREBASE_SERVICE_ACCOUNT_JSON={...}
```

Never commit the service-account JSON or private key.

## Security

See `docs/security.md`. See `docs/release-checklist.md` before a public Play release.

## Important Android limitation

No ordinary Android application can promise a 100% alarm guarantee against force-stop, device power loss, revoked permissions, severe OEM background restrictions, hardware failure, or an unavailable location source. Veyra therefore uses layered local protection and explicitly surfaces device-health problems before a journey starts.
