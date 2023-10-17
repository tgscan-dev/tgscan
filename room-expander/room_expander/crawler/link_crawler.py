import asyncio
import os
import re
from datetime import datetime

from loguru import logger
from sqlalchemy import desc
from sqlalchemy.sql.operators import and_
from telethon import TelegramClient
from toolz.curried import compose_left, map, unique

from room_expander.conf.env import settings
from room_expander.dao.database import DBSession
from room_expander.dao.models import Room
from room_expander.utils.toolzex import or_else, prop

api_id = 1024
api_hash = "b18441a1ff607e10a989891a5462e627"


async def sleep():
    logger.info("sleep 10s")
    await asyncio.sleep(10)


def build_rooms(links):
    return compose_left(
        map(lambda link: Room(link=link, status="NEW")),
        list,
    )(links)


def parse_links(msgs):
    return compose_left(
        map(
            compose_left(
                prop("text"),
                or_else(""),
            )
        ),
        " ".join,
        extract_telegram_links,
        unique,
        map(str.lower),
        list,
    )(msgs)


def extract_telegram_links(text):
    pattern = r"https?://t\.me/[\w/]+"
    return re.findall(pattern, text)


def get_from_datetime():
    s = os.getenv("FROM_DATETIME", datetime.now().__str__())
    return datetime.strptime(s, "%Y-%m-%d %H:%M:%S.%f")


class LinkCrawler:
    def __init__(self, expand_batch=None) -> None:
        self.client = TelegramClient(settings.SESSION_FILE, api_id, api_hash)
        self.expand_batch = expand_batch

    async def run(self):
        logger.info("client start...")
        await self.client.start("9")
        logger.info("client started")

        with DBSession() as db_sess:
            if self.expand_batch is None:
                self.expand_batch = (
                    db_sess.query(Room)
                    .order_by(desc("expand_batch"))
                    .limit(1)
                    .one()
                    .expand_batch
                    + 1
                )
            logger.info(f"expand batch {self.expand_batch}")
            while True:
                rooms = (
                    db_sess.query(Room)
                    .filter(
                        and_(
                            Room.status != "ERROR",
                            Room.expand_batch <= self.expand_batch,
                        )
                    )
                    .limit(100)
                    .all()
                )
                await self.fetch_and_save_rooms(db_sess, self.expand_batch, rooms)

    async def fetch_and_save_rooms(self, db_sess, expand_batch, rooms):
        for room in rooms:
            room.expand_batch = expand_batch
            room.last_expand_at = datetime.now()
            logger.info(f"expand room {room.link}")
            links = await self.crawl_links(room)
            logger.info(f"room {room.link} has {len(links)} links, save to db")

            for link in links:
                if db_sess.query(Room).filter(Room.link == link).count() == 0:
                    db_sess.add(
                        Room(link=link, status="NEW", created_at=datetime.now())
                    )
            db_sess.commit()
            await sleep()

    async def crawl_links(self, room):
        try:
            room_link = room.link
            room_name = room_link.split("/")[-1]
            res = set()
            async for message in self.client.iter_messages(
                room_name,
                search="https://t.me/",
                offset_date=room.last_expand_at,
                limit=10000000,
            ):
                links = extract_telegram_links(message.text)
                for link in links:
                    res.add(link.lower())
            return res
        except Exception as e:
            logger.error(f"crawl room {room.link} error {e}")
            return set()
