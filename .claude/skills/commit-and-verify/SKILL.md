---
name: commit-and-verify
description: To commit changes. Builds a new Docker image, bring up containers, run unit and integration test, commit, and push the current feature branch.
disable-model-invocation: false
tools: Bash, AskUserQuestion
---

# Overview

This skill will build new image, brings up containers, stage files for commit, and push the current branch.

## Steps

### 1. Bring down any existing containers

```bash
./bring-down.sh
```
No permission needed for container operations.

### 2. Build new jar

```bash
./mvnw clean package -DskipTests
```
If build fails, report the failure and stop — do not proceed to next step.

### 3. Build image and bring up containers

```bash
./bring-up.sh
```

No permission needed. Wait for it to complete.

### 4. Wait for the app to be healthy

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

### 5. Run unit and integration tests

```bash
# Run unit tests
./mvnw test

# Run integration tests (Cucumber)
./mvnw verify
```

- If any tests fail, report the failure and stop — do not proceed to commit.
- If all tests pass, proceed to step 5.


### 6. Ask the user which files to stage

Show the output of `git status` and ask the user to confirm which files should be staged. Wait for their answer before proceeding.

### 7. Stage the confirmed files

Stage only the files the user approved.

### 8. Ask the user for a commit message

Suggest one following the project convention `<type>: <short description>` based on the staged changes, but let the user override it.

### 9. Commit

```bash
git commit -m "<message>"
```
### 10. Pull the latest changes from the main branch

```bash
git pull --rebase origin main
git status
```
confirm with the user before proceeding. If there are conflicts, report them and stop — do not proceed to push.
### 11. Push the current branch

```bash
git push
```
Report the push result to the user.
