#!/bin/sh
set -e

host="$1"
shift
cmd="$@"

until nc -z ${host%:*} ${host##*:}; do
  >&2 echo "Wait-for-it: $host is unavailable - sleeping"
  sleep 1
done

>&2 echo "Wait-for-it: $host is up - executing command"
exec $cmd
