#!/bin/bash
# PostToolUse hook: runs Checkstyle on any edited Java file and reports violations
# so Claude can fix them immediately in the same turn.

INPUT=$(cat)
FILE=$(echo "$INPUT" | python3 -c "
import sys, json
d = json.load(sys.stdin)
print(d.get('tool_input', {}).get('file_path', ''))
" 2>/dev/null)

# Only act on Java source files
if [[ "$FILE" != *.java ]]; then
    exit 0
fi

cd "$(dirname "$0")/.." || exit 0

OUTPUT=$(./mvnw -q checkstyle:check 2>&1)
EXIT_CODE=$?

if [ $EXIT_CODE -ne 0 ]; then
    echo "Checkstyle violations detected after editing $FILE:"
    echo "$OUTPUT" | grep "\[ERROR\].*\.java"
fi
