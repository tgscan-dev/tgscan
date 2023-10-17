# import asyncio
# from datetime import datetime
#
# from telethon import TelegramClient
#
# from room_expander.conf.env import settings
# api_id = 1024
# api_hash = "b18441a1ff607e10a989891a5462e627"
#
# async def main():
#     client = TelegramClient(settings.SESSION_FILE, api_id, api_hash)
#     await client.start("1")
#     messages =await client.get_messages("ipl_gl_king",
#                                         search="https://t.me/",
#                                         limit=100,reverse=True)
#     print(messages)
# if __name__ == '__main__':
#   asyncio.run(main())
