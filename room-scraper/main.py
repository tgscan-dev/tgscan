import json
import logging
import os

import psycopg2
import socks
from dotenv import load_dotenv
from telethon import TelegramClient, functions
from telethon.tl.functions.messages import GetFullChatRequest
from telethon.tl.types import User, Channel, Chat

# api_id = 25722866
# api_hash = 'd4b109fd0e41e783c67f0a30eb1b0a24'
# phone = '+62 815 7480 8858'
# proxy = (socks.SOCKS5, 'localhost', 7890, True, 'log', 'pass')
# database = "demo"
# user = "demo"
# password = "tgscan1024"
# host = "localhost"
# port = "5432"

load_dotenv()

logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')

database = os.environ.get("PG_DATABASE")
user = os.environ.get("PG_USER")
password = os.environ.get("PG_PASSWORD")
host = os.environ.get("PG_HOST")
port = os.environ.get("PG_PORT")

api_id = int(os.environ.get("API_ID"))
api_hash = os.environ.get("API_HASH")
phone = os.environ.get("PHONE")

proxy = (socks.SOCKS5, 'localhost', 4781, True, 'log', 'pass')

client = TelegramClient(phone, api_id, api_hash, proxy=proxy)
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')


class BotExtra:
    def __init__(self, commands):
        self.commands = commands


class Room:
    def __init__(self, room_id, username, name, jhi_desc, member_cnt, type, status, msg_cnt, extra, lang, tags, link,
                 id):
        self.id = id
        self.room_id = room_id
        self.user_name = username
        self.link = link
        self.name = name
        self.jhi_desc = jhi_desc
        self.member_cnt = member_cnt
        self.type = type
        self.status = status
        self.msg_cnt = msg_cnt
        self.extra = extra
        self.lang = lang
        self.tags = tags


async def get_group_members(group_id):
    # 获取群组详细信息
    full_chat = await client(GetFullChatRequest(group_id))
    # 从详细信息中获取群组成员数量
    members_count = full_chat.full_chat.participants_count
    return members_count


async def main():
    # bot = "rabbitolddriver"
    # bot_ = await parse_rooms(bot)
    # return
    conn = psycopg2.connect(database=database, user=user, password=password, host=host, port=port)
    size = 10

    while True:

        # logging.info(f"start craw page {page}")
        cursor = conn.cursor()
        fetch_sql = """select link,lang,tags,id
                            from room
                            where msg_cnt is null 
                            limit """ + str(size)
        logging.info("fetch sql: %s", fetch_sql)
        cursor.execute(fetch_sql)
        all = cursor.fetchall()
        logging.info("all: %s", all)
        if len(all) == 0:
            break

        for item in all:
            link = item[0]
            username = link.split("/")[-1]
            lang = item[1]
            tags = item[2]
            id = item[3]

            try:
                (db_room, new_bots) = await parse_rooms(username, link, lang, tags, id)

            except object as e:
                db_room = Room(id=id, username=username, link=link, msg_cnt=0, room_id=None, name=None, jhi_desc=None,
                               member_cnt=None, type=None, status=None, extra=None, lang=None, tags=None)
                await save2db(cursor, db_room, [])
                logging.warning(f"not exist username: {username}")
                continue

            await save2db(cursor, db_room, new_bots)

        conn.commit()
        cursor.close()

    conn.close()


async def save2db(cursor, db_room, new_bots):
    db_room_sql = """
            update room
            set msg_cnt = %s,
                username=%s,
                room_id=%s,
                extra=%s
            where id =%s;
"""
    cursor.execute(db_room_sql, (db_room.msg_cnt, db_room.user_name, db_room.room_id, db_room.extra, db_room.id))
    if len(new_bots) > 0:
        bots_sql = """
                                                insert into room (room_id, username, name, jhi_desc, member_cnt, msg_cnt, type, status, collected_at, lang, tags, extra, link)
                                                values (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s,%s)
                                                on conflict(room_id)
                                                DO UPDATE SET
                                                    member_cnt = EXCLUDED.member_cnt,
                                                    msg_cnt = EXCLUDED.msg_cnt
                                            """

        try:
            values = [
                (room.room_id, room.user_name, room.name, room.jhi_desc, room.member_cnt, room.msg_cnt,
                 room.type,
                 room.status, None, room.lang, room.tags, room.extra, room.link) for room in new_bots]

            cursor.executemany(bots_sql, values)
        except Exception as e:
            logging.error(f"Error occurred: {e}")
            pass


async def parse_rooms(username, link, lang, tags, id) -> (Room, list[Room]):
    entity = await client.get_entity(username)
    room_type = get_room_type(entity)
    if room_type == "BOT":
        return (
            Room("BOT#" + str(entity.id), username, entity.first_name, None, 0, "BOT", "NEW", None, None, lang, tags,
                 link, id), [])

    if room_type == "CHANNEL" or room_type == "GROUP":
        full_chat = await client(functions.channels.GetFullChannelRequest(entity))
        members_count = full_chat.full_chat.participants_count
        about = full_chat.full_chat.about
        messages = await client.get_messages(entity, limit=0)
        room = Room(room_type + "#" + str(entity.id), username, entity.title, about, members_count, room_type,
                    "COLLECTED", messages.total,
                    None, lang, tags, link, id)

        bots = []
        user_map = {}
        for user in full_chat.users:
            user_map[user.id] = user
        for bot in full_chat.full_chat.bot_info:
            bot_user_id = bot.user_id
            bot_user = user_map[bot_user_id]
            if bot_user is not None:
                room0 = Room("BOT#" + str(bot_user_id), bot_user.username, bot_user.first_name, bot.description, 0,
                             "BOT",
                             "COLLECTED", 0, json.dumps(BotExtra(bot.commands), default=lambda x: x.__dict__), None,
                             None, "https://t.me/" + username, None)
                bots.append(room0)
        return room, bots


def get_room_type(entity):
    if isinstance(entity, User):  # 检查是否是用户或机器人
        # print(f"{username} is a user or bot.")
        if entity.bot:  # 检查是否是机器人
            return "BOT"
        else:  # 否则就是用户
            return "USER"
    elif isinstance(entity, Channel):  # 检查是否是频道或超级群组
        if entity.megagroup:  # 检查是否是超级群组
            return "GROUP"
        else:  # 否则就是频道
            return "CHANNEL"
    elif isinstance(entity, Chat):  # 检查是否是普通群组
        return "GROUP"
    else:  # 无法判断类型
        return "UNKNOWN"


with client:
    client.loop.run_until_complete(main())
