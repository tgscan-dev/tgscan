import asyncio

from room_expander.crawler.link_crawler import LinkCrawler

if __name__ == "__main__":
    crawler = LinkCrawler()
    asyncio.run(crawler.run())
