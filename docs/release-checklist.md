# Veyra release checklist

## Android project

- [ ] `applicationId` remains `com.vjagarwal.veyra` unless Firebase and release configuration are updated together.
- [ ] Release signing key is stored outside Git and injected through secure build secrets.
- [ ] `google-services.json` belongs to the correct Firebase Android app and is not committed.
- [ ] Release build uses HTTPS for the backend.
- [ ] Google Maps/Places/Routes keys are restricted and never embedded as unrestricted server credentials.

## Safety validation

- [ ] Fine/precise location permission flow tested.
- [ ] Location services off/on tested.
- [ ] Notifications denied/enabled tested.
- [ ] Full-screen alarm access tested on Android versions that expose the setting.
- [ ] Alarm volume at zero tested and blocked by the pre-journey safety check.
- [ ] Exact alarm unavailable tested; fallback alarm still schedules.
- [ ] Battery optimization enabled tested on at least one device.
- [ ] Phone locked during a journey tested.
- [ ] Screen off tested.
- [ ] Internet disabled during a journey tested.
- [ ] GPS accuracy degraded or temporarily lost tested.
- [ ] Mock location rejected where the platform exposes that state.
- [ ] Impossible location jumps rejected.
- [ ] Journey reaches the wake zone and alarm triggers.
- [ ] User can stop the alarm.
- [ ] Reboot recovery of the time fallback tested.
- [ ] OEM-specific battery/background behavior tested on supported target devices.

## Cloud integrations

- [ ] Supabase RLS policies verified using a non-service-role client.
- [ ] Service-role key exists only on the backend.
- [ ] Firebase Admin credentials exist only on the backend.
- [ ] AI provider credentials exist only on the backend.
- [ ] Train API credentials exist only on the backend.
- [ ] Backend JWT protection is enabled before public deployment.
- [ ] Push endpoint requires authentication before public deployment.
- [ ] Provider outage returns a non-fatal disabled/provider-error response.

## Privacy

- [ ] Location permission disclosure is accurate.
- [ ] Privacy policy explains local journey data, optional cloud sync, analytics and push notifications.
- [ ] User can delete local journey history.
- [ ] User can sign out and revoke cloud access where implemented.
- [ ] Data retention rules are documented.

## Play release

- [ ] Review current Google Play requirements for foreground location, exact alarms and full-screen notifications before submission.
- [ ] Verify only permissions actually used by the final release are declared.
- [ ] Test the final signed artifact on physical devices.
- [ ] Test upgrade from the previous release.
