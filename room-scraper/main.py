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

api_id = int(os.environ.get("ROOM_SCRAPER_API_ID"))
api_hash = os.environ.get("ROOM_SCRAPER_API_HASH")
phone = os.environ.get("ROOM_SCRAPER_PHONE")

proxy_host = os.environ.get("PROXY_HOST")
proxy_port = int(os.environ.get("PROXY_PORT"))
proxy_username = os.environ.get("PROXY_USERNAME")
proxy_password = os.environ.get("PROXY_PASSWORD")

proxy = (socks.SOCKS5, 'localhost', 7890, True, 'log', 'pass')

client = TelegramClient(phone, api_id, api_hash, proxy=proxy)
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')


class BotExtra:
    def __init__(self, commands):
        self.commands = commands


class Room:
    def __init__(self, room_id, username, name, jhi_desc, member_cnt, type, status, msg_cnt, extra):
        self.room_id = room_id
        self.user_name = username
        self.name = name
        self.jhi_desc = jhi_desc
        self.member_cnt = member_cnt
        self.type = type
        self.status = status
        self.msg_cnt = msg_cnt
        self.extra = extra


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
    page = 0
    size = 10

    while True:

        logging.info(f"start craw page {page}")
        cursor = conn.cursor()
        cursor.execute("""select link
                            from room
                            order by id
                            limit %s offset %s; ;""", (size, page * size))
        all = cursor.fetchall()
        if len(all) == 0:
            break

        for item in all:
            username = item[0].split("/")[-1]

            # -1001340684391
            # https: // t.me / tgcnx
            rooms = None
            try:
                rooms = await parse_rooms(username)

            except BaseException:
                logging.warning(f"not exist username: {username}")
                continue

            sql = """
                                            insert into room_v2 (room_id, username, name, jhi_desc, member_cnt, msg_cnt, type, status, collected_at, lang, tags, extra)
                                            values (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
                                            on conflict(room_id)
                                            DO UPDATE SET
                                                member_cnt = EXCLUDED.member_cnt,
                                                msg_cnt = EXCLUDED.msg_cnt
                                        """

            try:
                values = [
                    (room.room_id, room.user_name, room.name, room.jhi_desc, room.member_cnt, room.msg_cnt, room.type,
                     room.status, None, None, None, room.extra) for room in rooms]

                cursor.executemany(sql, values)
            except Exception as e:
                logging.error(f"Error occurred: {e}")
                pass

        cursor.close()
        conn.commit()
        logging.info(f"end craw page {page}")
        page = page + 1

    conn.close()


async def parse_rooms(username):
    entity = await client.get_entity(username)
    room_type = get_room_type(entity)
    if room_type == "BOT":
        return [Room("BOT#" + str(entity.id), username, entity.first_name, None, 0, "BOT", "NEW", None, None)]

    if room_type == "CHANNEL" or room_type == "GROUP":
        full_chat = await client(functions.channels.GetFullChannelRequest(entity))
        members_count = full_chat.full_chat.participants_count
        about = full_chat.full_chat.about
        messages = await client.get_messages(entity, limit=0)
        room = Room(room_type + "#" + str(entity.id), username, entity.title, about, members_count, room_type,
                    "COLLECTED", messages.total,
                    None)
        rooms = []
        rooms.append(room)
        user_map = {}
        for user in full_chat.users:
            user_map[user.id] = user
        for bot in full_chat.full_chat.bot_info:
            bot_user_id = bot.user_id
            bot_user = user_map[bot_user_id]
            if bot_user is not None:
                room0 = Room("BOT#" + bot_user_id, bot_user.username, bot_user.first_name, bot.description, 0, "BOT",
                             "COLLECTED", 0, json.dumps(BotExtra(bot.commands), default=lambda x: x.__dict__))
                rooms.append(room0)
        return rooms


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
