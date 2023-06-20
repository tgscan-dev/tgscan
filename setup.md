## install docker

```shell
curl -fsSL https://get.docker.com -o get-docker.sh
sh get-docker.sh
```

## setup db

```shell
docker compose -f db.yml up -d
```

## install elasticsearch plugin

```shell
docker exec -it tg_scan_elasticsearch bash
 ./bin/elasticsearch-plugin install --batch https://github.com/medcl/elasticsearch-analysis-pinyin/releases/download/v7.17.6/elasticsearch-analysis-pinyin-7.17.6.zip
 ./bin/elasticsearch-plugin install --batch https://github.com/medcl/elasticsearch-analysis-ik/releases/download/v7.17.6/elasticsearch-analysis-ik-7.17.6.zip
 ./bin/elasticsearch-plugin install --batch https://github.com/medcl/elasticsearch-analysis-stconvert/releases/download/v7.17.6/elasticsearch-analysis-stconvert-7.17.6.zip
exit
```

## restart db

```shell
docker compose -f db.yml restart

```

## init db schema

- schema path: api-server/src/main/resources/sql/schema.sql

## init index mapping

- exec script on kibana dev console
- script path: api-server/src/main/resources/idx/*.txt

## copy `.env.template` to `.env`, and fill in the required information

```bash
cp .env.template .env
```

## build the project

```bash 
sh startup.sh
```