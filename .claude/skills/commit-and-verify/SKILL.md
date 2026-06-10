---
name: commit-and-verify
description: Build a new Docker image, bring up containers, verify the import API returns 200 OK, then stage selected files, commit, and push the current feature branch. Use when the user wants to verify and commit changes.
disable-model-invocation: false
tools: Bash, AskUserQuestion
---

# Commit and Verify

End-to-end workflow: build → bring up → verify API → stage → commit → push.

## Steps

### 1. Bring down any existing containers

```bash
./bring-down.sh
```

No permission needed for container operations.

### 2. Build image and bring up containers

```bash
./bring-up.sh
```

No permission needed. Wait for it to complete.

### 3. Wait for the app to be healthy

Poll `/actuator/health` until `"status":"UP"` or until 60 seconds have elapsed:

```bash
for i in $(seq 1 12); do
  STATUS=$(curl -s http://localhost:8080/actuator/health | grep -o '"status":"UP"')
  if [ "$STATUS" = '"status":"UP"' ]; then echo "UP"; break; fi
  echo "Waiting... ($i/12)"
  sleep 5
done
```

If the app never comes up, report the failure and stop — do not proceed to commit.

### 4. Make the POST request to the import endpoint

No permission needed for this request.

```bash
curl -s -o /dev/null -w "%{http_code}" -X POST \
  "http://localhost:8080/api/person/import?filePath=/home/app/People.csv"
```

- If HTTP status is `200` → proceed.
- If anything else → report the status code and the response body, then stop. Do not commit.

### 5. Ask the user which files to stage

Show the output of `git status` and ask the user to confirm which files should be staged. Wait for their answer before proceeding.

### 6. Stage the confirmed files

Stage only the files the user approved.

### 7. Ask the user for a commit message

Suggest one following the project convention `<type>: <short description>` based on the staged changes, but let the user override it.

### 8. Commit

```bash
git commit -m "<message>

Co-Authored-By: Claude Sonnet 4.6 <noreply@anthropic.com>"
```

### 9. Push the current branch

```bash
git push
```

Report the push result to the user.
