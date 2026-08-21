# VPCoffee - Claude Code Rules

## GitHub Push

When pushing to GitHub, always use the token from `credentials/.env`:

1. Read `credentials/.env` to get `GITHUB_TOKEN`
2. Set remote URL with token: `git remote set-url origin https://<TOKEN>@github.com/KhangTranMinh/VPCoffee.git`
3. Push: `git push`
4. Restore original URL: `git remote set-url origin https://github.com/KhangTranMinh/VPCoffee.git`

**Why:** Default GitHub credentials don't have push access. The token provides the necessary permissions.

## Build & Run

After code changes, always compile, install, and launch on device:

**Main app (VPCoffee):**
```bash
./gradlew installDebug
adb shell am start -n com.vpcoffee/.MainActivity
```

**Push Test app:**
```bash
./gradlew :pushtest:installDebug
adb shell am start -n com.vpcoffee.pushtest/.MainActivity
```

## Deploy to Firebase App Distribution

Use Fastlane to deploy with auto-increment version code:

**Deploy VPCoffee:**
```bash
bundle exec fastlane deploy_vpcoffee
```

**Deploy PushTest:**
```bash
bundle exec fastlane deploy_pushtest
```

**Deploy both apps:**
```bash
bundle exec fastlane deploy_all
```

**Why:** Fastlane auto-increments versionCode before building and deploys to the "internal" group using the service account in `credentials/service-account.json`.
