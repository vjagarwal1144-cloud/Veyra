# Veyra

Veyra is an offline-first journey protection app focused on two core jobs: **tracking** and **alarm**.

## Safety architecture

The safety-critical path is local: Android location + sensors -> reliability checks -> local alarm. Internet, maps, AI, train APIs and cloud notifications are optional enhancements and never prerequisites for an already-started protected journey.

## Repository

- `android/` native Android app (Jetpack Compose, foreground tracking service, local journey storage, alarm guardian)
- `backend/` Spring Boot gateway for optional cloud integrations
- `supabase/` PostgreSQL/RLS schema
- `.github/` CI workflow
- `docs/` architecture and security notes

## Before release

Provider credentials, signing keys, Firebase configuration, Supabase secrets and API keys must be supplied through local properties/environment/GitHub Actions secrets. They are intentionally excluded from this repository.

This repository contains a production-oriented foundation; Android OEM killing, user force-stop, battery depletion, revoked permissions and OS-level restrictions cannot be made into a 100% guarantee by an application.