#!/usr/bin/env bash
# Uso (desde la terminal de IntelliJ, dentro de la carpeta MoodSpace):
#   bash git_setup.sh https://github.com/TU_USUARIO/MoodSpace.git
set -e
REMOTE="$1"

git init -b main

git add .gitignore settings.gradle.kts build.gradle.kts gradle.properties app/build.gradle.kts README.md
git commit -m "chore: configuración inicial del proyecto Android con Kotlin"

git add app/src/main/AndroidManifest.xml app/src/main/res/values app/src/main/java/com/moodspace/app/data
git commit -m "feat: base de datos SQLite, tema y manifest"

git add app/src/main/java/com/moodspace/app/RegisterActivity.kt app/src/main/res/layout/activity_register.xml
git commit -m "PROYEC-7 #comment HU01 alta de cuenta con validaciones"

git add app/src/main/java/com/moodspace/app/LoginActivity.kt app/src/main/java/com/moodspace/app/SessionManager.kt app/src/main/res/layout/activity_login.xml
git commit -m "PROYEC-8 #comment HU02 inicio de sesión y sesión persistente"

git add app/src/main/java/com/moodspace/app/ProfileActivity.kt app/src/main/res/layout/activity_profile.xml
git commit -m "PROYEC-9 #comment HU03 editar perfil"

git add app/src/main/java/com/moodspace/app/MainActivity.kt app/src/main/res/layout/activity_main.xml
git commit -m "PROYEC-10 #comment HU04 registrar estado de ánimo"

git add app/src/main/java/com/moodspace/app/HistoryActivity.kt app/src/main/res/layout/activity_history.xml
git commit -m "PROYEC-11 #comment HU05 consultar historial"

git add -A
git diff --cached --quiet || git commit -m "chore: archivos restantes del Sprint 1"

if [ -n "$REMOTE" ]; then
  git remote add origin "$REMOTE"
  git push -u origin main
fi

git log --oneline
