#!/bin/bash
# PostToolUse hook: detects git push and prompts Claude to create a PR.

INPUT=$(cat)

COMMAND=$(echo "$INPUT" | python3 -c "
import sys, json
d = json.load(sys.stdin)
print(d.get('tool_input', {}).get('command', ''))
" 2>/dev/null)

RESPONSE=$(echo "$INPUT" | python3 -c "
import sys, json
d = json.load(sys.stdin)
print(d.get('tool_response', ''))
" 2>/dev/null)

# Only act on git push commands
if [[ "$COMMAND" != *"git push"* ]]; then
    exit 0
fi

# Only act if push succeeded (output contains branch arrow e.g. "feature -> feature")
if ! echo "$RESPONSE" | grep -q "->"; then
    exit 0
fi

cd "$(dirname "$0")/.." || exit 0

BRANCH=$(git branch --show-current)

# Skip pushes to main/master
if [[ "$BRANCH" == "main" || "$BRANCH" == "master" ]]; then
    exit 0
fi

# Skip if a PR already exists for this branch
if gh pr view "$BRANCH" > /dev/null 2>&1; then
    exit 0
fi

# Collect changed files vs main
FILES=$(git diff origin/main...HEAD --name-only 2>/dev/null)

if [ -z "$FILES" ]; then
    exit 0
fi

# Collect per-file diffs (capped at 200 lines each to avoid flooding context)
DIFFS=""
while IFS= read -r file; do
    FILE_DIFF=$(git diff origin/main...HEAD -- "$file" 2>/dev/null | head -200)
    DIFFS+="### $file\n\`\`\`diff\n$FILE_DIFF\n\`\`\`\n\n"
done <<< "$FILES"

echo "Git push to branch '$BRANCH' succeeded and no PR exists yet."
echo ""
echo "Please create a PR now using: gh pr create --title \"<title>\" --body \"<body>\""
echo ""
echo "The PR body must include:"
echo "1. A bullet list of all changed files"
echo "2. For each file: a short explanation of WHAT changed and WHY (use your conversation context for the why)"
echo ""
echo "Changed files vs main:"
echo "$FILES"
echo ""
echo "Per-file diffs:"
printf "$DIFFS"
