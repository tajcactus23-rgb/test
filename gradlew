#!/bin/sh
APP_HOME=$( cd "$( dirname "$0" )" && pwd )
chmod +x "$APP_HOME/gradle-wrappers/bin/gradle"
exec "$APP_HOME/gradle-wrappers/bin/gradle" "$@"