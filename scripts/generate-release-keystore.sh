#!/usr/bin/env bash

set -euo pipefail

project_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
release_dir="$project_dir/release"
keystore_path="$release_dir/cullpear-self-release.jks"
secrets_path="$release_dir/github-secrets.env"
alias_name="cullpear-self-release"

if [[ -n "${JAVA_HOME:-}" && -x "$JAVA_HOME/bin/keytool" ]]; then
  keytool_command="$JAVA_HOME/bin/keytool"
elif [[ -x "/Applications/Android Studio.app/Contents/jbr/Contents/Home/bin/keytool" ]]; then
  keytool_command="/Applications/Android Studio.app/Contents/jbr/Contents/Home/bin/keytool"
else
  keytool_command="$(command -v keytool)"
fi

if [[ -e "$keystore_path" || -e "$secrets_path" ]]; then
  echo "Release credentials already exist in $release_dir; refusing to overwrite them." >&2
  exit 1
fi

umask 077
mkdir -p "$release_dir"
password="$(openssl rand -base64 48 | tr -dc 'A-Za-z0-9' | head -c 40)"

"$keytool_command" -genkeypair -noprompt \
  -keystore "$keystore_path" \
  -storetype PKCS12 \
  -storepass "$password" \
  -keypass "$password" \
  -alias "$alias_name" \
  -keyalg RSA \
  -keysize 3072 \
  -validity 10000 \
  -dname "CN=CullPear Self Release, OU=Personal, O=zxm965, C=CN" >/dev/null

keystore_base64="$(openssl base64 -A -in "$keystore_path")"

{
  echo "ANDROID_KEYSTORE_BASE64=$keystore_base64"
  echo "ANDROID_KEYSTORE_PASSWORD=$password"
  echo "ANDROID_KEY_ALIAS=$alias_name"
  echo "ANDROID_KEY_PASSWORD=$password"
} > "$secrets_path"

chmod 600 "$keystore_path" "$secrets_path"
echo "Created release keystore: $keystore_path"
echo "Created GitHub Secrets values: $secrets_path"
echo "Back up both files securely. They are ignored by Git."
