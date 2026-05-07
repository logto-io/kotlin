# Release process

Releases are automated via [release-please](https://github.com/googleapis/release-please) and the workflow at `.github/workflows/release.yml`. The source of truth for the version is the `logtoSdk` variable in `gradle/logto.versions.toml`.

## How it works

1. Every PR to `master` uses [Conventional Commits](https://www.conventionalcommits.org/) (the `commitlint` workflow enforces this on contributor PRs; release-please's own release PR uses a fixed, conformant title pattern instead).
2. release-please scans commits since the last release tag and maintains an open **release PR** titled `release: X.Y.Z`. The next version is computed from commit types:

   | Commit prefix | Bump |
   |---|---|
   | `fix:` | patch (X.Y.**Z+1**) |
   | `feat:` | minor (X.**Y+1**.0) |
   | `feat!:` or commit body containing `BREAKING CHANGE:` | major (**X+1**.0.0) |
   | `chore:` / `refactor:` / `docs:` / `test:` / `ci:` / `revert:` | no bump (still listed in changelog) |

3. Merging the release PR triggers the workflow again. release-please creates the git tag `vX.Y.Z` and a GitHub Release with auto-generated notes immediately, then sets `releases_created=true`.
4. The publish job (gated on `releases_created`) runs next:
   - Builds and signs `io.logto.sdk:kotlin` and `io.logto.sdk:android` with in-memory PGP keys.
   - Uploads artifacts to the Sonatype Central Portal staging area.
   - Calls the Central Portal promote endpoint with `?publishing_type=automatic` so the deployment is auto-released without a manual click.

> **Recovery from partial failure.** Because the tag and GitHub Release are created before publish, a publish failure leaves a tag pointing at a version that is not yet on Maven Central. If this happens:
>
> - Investigate the failed step in the Actions log (most common: expired `OSSRH_PASSWORD`, GPG key issues).
> - Re-run the failed `publish` job from the Actions UI. Sonatype's staging is transient, so re-uploading the same version is safe until the deployment is released.
> - If artifacts are already on Maven Central but the workflow failed at a later step, the release is effectively complete — just verify on Maven Central and move on.
> - As a last resort, delete the tag and GitHub Release, fix the underlying issue, and re-run the manual fallback below.

## Day-to-day maintainer workflow

- Write good Conventional Commit titles in feature PRs. That's it.
- When you want to ship: review and merge the open `release: X.Y.Z` PR.
- Wait a few minutes for the workflow. Verify the new version on [Maven Central](https://search.maven.org/search?q=io.logto.sdk).

If `release: X.Y.Z` does not appear, there are no `feat:` or `fix:` commits since the last release. Other commit types do not trigger a version bump.

## Required secrets (one-time setup)

Configured under **Settings → Secrets and variables → Actions**:

| Secret | Source |
|---|---|
| `OSSRH_USERNAME` | Username from <https://central.sonatype.com/usertoken> |
| `OSSRH_PASSWORD` | The user token from the same page (expires yearly — re-generate when publish returns 401) |
| `OSSRH_GPG_PRIVATE_KEY` | `gpg --export-secret-keys --armor <KEY_ID>` output, including `-----BEGIN/END PGP PRIVATE KEY BLOCK-----` markers |
| `OSSRH_GPG_PASSPHRASE` | Passphrase for the GPG key |

The signing key is shared within the team — ask the current maintainer for the key ID and ensure its public key is published on `keys.openpgp.org` (or another major keyserver) so Maven Central can verify signatures.

When the GPG key approaches expiration, extend it instead of generating a new one (keeps the fingerprint and Maven Central trust intact):

```bash
gpg --edit-key <KEY_ID>
# > expire   (extend primary)
# > key 1
# > expire   (extend each subkey)
# > save
gpg --keyserver keys.openpgp.org --send-keys <KEY_ID>
gpg --export-secret-keys --armor <KEY_ID>  # update OSSRH_GPG_PRIVATE_KEY secret
```

## Manual fallback

If the automated workflow ever fails and you need to ship urgently, the manual flow still works:

1. Bump `logtoSdk` in `gradle/logto.versions.toml` locally.
2. Configure `~/.gradle/gradle.properties` with `ossrhUsername`, `ossrhPassword`, plus signing properties (`signing.keyId`, `signing.password`, `signing.secretKeyRingFile`) — or `useInMemoryPgpKeys` via `-PsigningKey=...` and `-PsigningPassword=...` (omit the password only for unencrypted keys).
3. `./gradlew :kotlin-sdk:kotlin:publish :android-sdk:android:publish` (single invocation so both modules land in the same staging repository).
4. Promote and release the deployment:
   ```bash
   AUTH=$(printf '%s:%s' "$OSSRH_USERNAME" "$OSSRH_PASSWORD" | base64 | tr -d '\n')
   curl -X POST -H "Authorization: Basic $AUTH" \
     "https://ossrh-staging-api.central.sonatype.com/manual/upload/defaultRepository/io.logto.sdk?publishing_type=automatic"
   ```
   Drop `?publishing_type=automatic` if you want to manually click "Release" on <https://central.sonatype.com/publishing>.
5. Open a `release: X.Y.Z` PR with the version bump and merge it.
6. Tag `vX.Y.Z` and create a GitHub Release.

## Troubleshooting

| Symptom | Likely cause / fix |
|---|---|
| `401 Unauthorized` on publish | OSSRH user token expired — regenerate at <https://central.sonatype.com/usertoken> and update both `OSSRH_USERNAME` / `OSSRH_PASSWORD` secrets |
| `Could not read PGP secret key` | `OSSRH_GPG_PRIVATE_KEY` secret missing the `-----BEGIN/END-----` markers — re-export and paste the full block |
| Signing fails with "no public key" on Central Portal validation | Public key not published to keyserver — `gpg --keyserver keys.openpgp.org --send-keys <KEY_ID>` |
| GPG key shows `[expired]` | Extend with `gpg --edit-key`, see GPG section above. Don't forget subkeys |
| Workflow runs but no release PR appears | No `feat:` / `fix:` commits since last release — only those bump versions |
| Deployment stuck at `VALIDATED` on Central Portal | The `publishing_type=automatic` step did not auto-release. Click "Release" in the UI; investigate the workflow logs afterwards |
