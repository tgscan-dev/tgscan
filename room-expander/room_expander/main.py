import asyncio

from room_expander.crawler.link_crawler import LinkCrawler

if __name__ == "__main__":
    crawler = LinkCrawler(2)
    asyncio.run(crawler.run())
