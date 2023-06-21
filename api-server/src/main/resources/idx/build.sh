curl -X 'POST' \
  'http://localhost:8080/internal/sync/fullSync4room?idx=room.0620' \
  -H 'accept: */*' \
  -d ''

curl -X 'POST' \
  'http://localhost:8080/internal/sync/fullSync4message?idx=message.0620' \
  -H 'accept: */*' \
  -d ''


curl -X 'POST' \
  'http://localhost:8080/internal/crawler/rescanRoom' \
  -H 'accept: */*' \
  -d ''

docker logs -f --tail 10 tgscan-api-server-1
docker logs tgscan-api-server-1|grep fetch

docker logs -f --tail 10 tgscan-data-miner-1