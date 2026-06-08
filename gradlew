#!/bin/sh
APP_HOME=$( cd "$( dirname "$0" )" && pwd )
exec "$APP_HOME/gradle-wrappers/bin/gradle" "$@"