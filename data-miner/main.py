import asyncio
import logging
import os
from typing import List

import psycopg2
import socks
from dotenv import load_dotenv
from telethon import TelegramClient

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
proxy_host = os.environ.get("PROXY_HOST")
proxy_port = int(os.environ.get("PROXY_PORT"))
proxy_username = os.environ.get("PROXY_USERNAME")
proxy_password = os.environ.get("PROXY_PASSWORD")

proxy = None
use_proxy = os.environ.get("USE_PROXY", "False").lower() == "true"
if use_proxy:
    proxy = (socks.SOCKS5, proxy_host, proxy_port, True, proxy_username, proxy_password)


async def fetch_messages(client: TelegramClient, chat_id: int, offset_id: int = 0):
    messages = []

    async for msg in client.iter_messages(chat_id, reverse=True, limit=1000, offset_id=offset_id):
        messages.append(msg)
    return messages


async def save_messages_to_db(messages: List, conn):
    cursor = conn.cursor()

    for msg in messages:
        try:

            file_names = []
            if msg.document is not None:
                for attr in msg.document.attributes:
                    if hasattr(attr, 'file_name'):
                        file_name = attr.file_name
                        file_names.append(file_name)

            if msg.text is None:
                continue
            cursor.execute(
                R"""INSERT INTO message ("offset", chat_id, sender_id, content,send_time) VALUES (%s, %s, %s, %s, %s) ON CONFLICT DO NOTHING;""",
                (msg.id, msg.chat.id, msg.sender.id, "\n".join(file_names) + "\n" + msg.text, msg.date))
            conn.commit()
        except Exception as e:
            logging.error(f"Error occurred: {e}")


async def main():
    async with TelegramClient(phone, api_id, api_hash, proxy=proxy, ) as client:
        logging.info("Open TelegramClient")

        while True:
            try:
                conn = psycopg2.connect(database=database, user=user, password=password, host=host, port=port)
                cursor = conn.cursor()
                cursor.execute("SELECT chat_id FROM offsets;")
                chat_ids = [row[0] for row in cursor.fetchall()]

                for chat_id in chat_ids:
                    logging.info(f"Processing chat_id {chat_id}")
                    cursor = conn.cursor()
                    cursor.execute("SELECT last_offset FROM offsets WHERE chat_id = %s;", (chat_id,))
                    offset = cursor.fetchone()

                    if offset is not None:
                        offset_id = offset[0]
                    else:
                        offset_id = 0

                    logging.info(f"Start fetch messages from chat_id {chat_id} with offset {offset_id}")
                    messages = await fetch_messages(client, chat_id, offset_id)
                    logging.info(f"End fetch messages, found {len(messages)} messages")

                    if len(messages) > 0:
                        date = messages[-1].date
                        logging.info(f"Last message date: {date}")

                    if messages:
                        await save_messages_to_db(messages, conn)
                        cursor.execute(
                            "INSERT INTO offsets (chat_id, last_offset) VALUES (%s, %s) ON CONFLICT (chat_id) DO UPDATE SET last_offset = %s;",
                            (chat_id, messages[-1].id, messages[-1].id))

                conn.commit()
                conn.close()
                logging.info("Database connection closed.")

                await asyncio.sleep(10)

            except Exception as e:
                logging.error(f"Error occurred: {e}")


if __name__ == '__main__':
    logging.info("Starting the program.")
    asyncio.run(main())
