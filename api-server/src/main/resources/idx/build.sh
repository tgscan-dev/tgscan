curl -X 'POST' \
  'http://localhost:8080/internal/sync/fullSync4room?idx=room.0719' \
  -H 'accept: */*' \
  -d ''

curl -X 'POST' \
  'http://localhost:8080/internal/sync/fullSync4message?idx=message.0708' \
  -H 'accept: */*' \
  -d ''

curl -X 'POST' \
  'http://localhost:8080/internal/crawler/disableRoomCrawler' \
  -H 'accept: */*' \
  -d ''



curl -X 'POST' \
  'http://localhost:8080/internal/crawler/rescanRoom' \
  -H 'accept: */*' \
  -d ''


curl -X 'POST' \
  'http://localhost:8080/internal/data-patch/enable' \
  -H 'accept: */*' \
  -d ''


docker logs -f --tail 100 tgscan-api-server-1
docker logs tgscan-api-server-1|grep fetch

docker logs -f --tail 10 tgscan-data-miner-1