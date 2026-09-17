# Veyra Architecture

## Safety-critical path

`Fused Location Provider + GNSS/network location + motion sensors -> LocationValidator -> ProtectionEngine -> AlarmService`

The path is local and continues when the network is unavailable.

## Cloud path

`Android -> Veyra backend -> Supabase / Firebase / Google Maps / train provider / AI provider`

Cloud features provide search, history sync, route enrichment, train information, analytics and optional push notifications. They must fail closed and never block the local alarm engine.

## Reliability checks

Veyra rejects stale fixes, invalid accuracy, impossible jumps and obviously inconsistent speed/bearing. It combines distance, location accuracy, recent trend, movement state and backup timing before arming the alarm.

## Android lifecycle

Tracking runs as a foreground location service. Journey state is persisted locally so process recreation can recover an active journey. A backup AlarmManager trigger is scheduled from an ETA estimate where possible.
