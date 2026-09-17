# Security and privacy

Never commit API keys, Firebase service credentials, signing keys or Supabase service-role keys.

Use Supabase RLS for user-owned records. The Android client should only use public/anon credentials where applicable; privileged provider keys belong in the backend.

Location history is sensitive. Veyra stores only the information needed for an active journey and user-requested history. Provide deletion/export controls before production release.

The alarm is a safety aid, not a guaranteed life-safety mechanism. User-facing safety checks must explain permission, battery, OEM and force-stop limitations.
