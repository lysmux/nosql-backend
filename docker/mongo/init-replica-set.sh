#!/bin/sh
set -eu

until [ "$(mongosh --quiet --eval 'db.hello().ok' 2>/dev/null)" = "1" ]; do
  sleep 1
done

mongosh --quiet --eval '
  try {
    rs.status();
  } catch (e) {
    if (e.code !== 94) throw e;
    rs.initiate({_id: "rs0", members: [{_id: 0, host: "localhost:27017"}]});
  }
'
