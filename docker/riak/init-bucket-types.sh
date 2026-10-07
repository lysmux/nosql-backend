#!/bin/sh
set -eu

# erl_call mode: nodetool cannot reach the node in this image, and `riak admin`
# over erl_call mangles JSON arguments and hides output, hence plain `riak eval`
export USE_NODETOOL=false

until riak ping >/dev/null 2>&1; do
  sleep 1
done
riak admin wait-for-service riak_kv

counters_status() {
  riak eval 'riak_core_bucket_type:status(<<"counters">>).'
}

if [ "$(counters_status)" = "undefined" ]; then
  riak eval 'riak_core_bucket_type:create(<<"counters">>, [{datatype, counter}]).'
fi

until [ "$(counters_status)" = "active" ]; do
  riak eval 'riak_core_bucket_type:activate(<<"counters">>).' >/dev/null || true
  sleep 1
done
