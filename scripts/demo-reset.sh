#!/usr/bin/env bash
# Reset the cross-repo upgrade demo so it can be re-run live.
# Requires: gh CLI authenticated with access to sohan-demo-org.
#
#   scripts/demo-reset.sh            # close Devin PRs + delete devin/* branches in both consumers
#   scripts/demo-reset.sh --release 2.0.0   # ...and also delete the GitHub release + tag v2.0.0 on the lib
set -euo pipefail

ORG="${ORG:-sohan-demo-org}"
LIB="$ORG/acme-pricing-lib"
CONSUMERS=("$ORG/acme-checkout-service" "$ORG/acme-invoicing-worker")
RELEASE=""

while [[ $# -gt 0 ]]; do
  case "$1" in
    --release) RELEASE="$2"; shift 2 ;;
    *) echo "unknown arg: $1" >&2; exit 2 ;;
  esac
done

for repo in "${CONSUMERS[@]}"; do
  echo "== $repo"
  gh pr list -R "$repo" --state open --json number,headRefName,title \
    --jq '.[] | select(.headRefName | startswith("devin/")) | "\(.number)\t\(.headRefName)\t\(.title)"' |
  while IFS=$'\t' read -r num branch title; do
    echo "  closing PR #$num ($branch): $title"
    gh pr close -R "$repo" "$num" --delete-branch --comment "Closed by demo-reset.sh" >/dev/null
  done
  # branches without PRs
  gh api "repos/$repo/branches" --paginate --jq '.[].name | select(startswith("devin/"))' |
  while read -r branch; do
    echo "  deleting branch $branch"
    gh api -X DELETE "repos/$repo/git/refs/heads/$branch" >/dev/null || true
  done
done

if [[ -n "$RELEASE" ]]; then
  echo "== $LIB release v$RELEASE"
  gh release delete -R "$LIB" "v$RELEASE" --yes --cleanup-tag 2>/dev/null && echo "  deleted release + tag" || echo "  no release v$RELEASE"
  echo "  note: the published package version $RELEASE stays in GitHub Packages; release.yml skips re-publishing it."
fi
echo "done"
