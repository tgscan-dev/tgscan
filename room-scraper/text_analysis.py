import logging
import os
import threading
from json import JSONDecodeError
from typing import List
import psycopg2
import langchain
import openai
from dotenv import load_dotenv
from langchain import LLMChain
from langchain.chat_models import ChatOpenAI
from langchain.output_parsers import PydanticOutputParser, OutputFixingParser
from langchain.prompts import (
    ChatPromptTemplate,
    PromptTemplate,
    SystemMessagePromptTemplate,
    HumanMessagePromptTemplate,
)
from langchain.schema import OutputParserException
from pydantic import BaseModel, Field
import concurrent.futures
from langchain.cache import InMemoryCache

langchain.debug = False
# langchain.verbose = True
openai.api_key = "sk-123"
openai.api_base = "http://localhost:9998"

load_dotenv()
langchain.cache = InMemoryCache()


class TelegramGroup(BaseModel):
    language: str = Field(description="language of telegram group")
    tags: List[str] = Field(description="list of tags of telegram group")


def calc_group(name: str, desc: str) -> TelegramGroup:
    template = "你是一个聪明的电报文本语种识别和分类工具，根据电报群名称和描述，返回其语种和标签。语种返回英文单词，不要缩写，比如Chinese，English等；标签最多选3个，标签可选范围只有：Blogs,News and media,Humor and entertainment,Technologies,Economics,Business and startups,Cryptocurrencies,Travel,Marketing, PR, advertising,Psychology,Design,Politics,Art,Law,Education,Books,Linguistics,Career,Edutainment,Courses and guides,Sport,Fashion and beauty,Medicine,Health and Fitness,Pictures and photos,Software & Applications,Video and films,Music,Games,Food and cooking,Quotes,Handiwork,Family & Children,Nature,Interior and construction,Telegram,Instagram,Sales,Transport,Religion,Esoterics,Darknet,Bookmaking,Shock content,Erotic,Adult,Other 。用英文回复."
    system_message_prompt = SystemMessagePromptTemplate.from_template(template)
    parser = PydanticOutputParser(pydantic_object=TelegramGroup)

    human_message_prompt = HumanMessagePromptTemplate.from_template(
        "Answer the user query.\n{format_instructions}\ngroup name:{name}, group description:{desc}\n")

    chat_prompt = ChatPromptTemplate.from_messages(
        [system_message_prompt, human_message_prompt]
    )
    llm = ChatOpenAI(temperature=0.0, request_timeout=20)
    chain = LLMChain(llm=llm, prompt=chat_prompt)
    autofix_parser = OutputFixingParser.from_llm(parser=parser, llm=llm)
    output = chain.run({'format_instructions': parser.get_format_instructions(), 'name': name, 'desc': desc})
    group = autofix_parser.parse(output)
    return group


logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')
database = os.environ.get("PG_DATABASE")
user = os.environ.get("PG_USER")
password = os.environ.get("PG_PASSWORD")
host = os.environ.get("PG_HOST")
port = os.environ.get("PG_PORT")


def fetch_and_save_tg_group(id, name, desc, cursor):
    group = None
    while True:
        try:
            group = calc_group(name, desc)
            break
        except (JSONDecodeError, OutputParserException) as e:
            logging.error("calc_group parse err, will retry once")
        except Exception as e:
            logging.error("calc_group err", e)
            break
    logging.info(name + ": " + group.__str__())

    sql = """
                                  update room
                                  set lang = %s,
                                      tags=%s
                                  where id=%s;
                                          """

    try:

        cursor.execute(sql, (group.language, ",".join(group.tags), id))
    except Exception as e:
        logging.error(f"Error occurred: {e}")
        pass
    pass


if __name__ == '__main__':
    conn = psycopg2.connect(database=database, user=user, password=password, host=host, port=port)
    page = 0
    size = 300

    while True:

        try:
            logging.info(f"start calc page {page}")
            cursor = conn.cursor()
            cursor.execute("""select id,name,jhi_desc 
                                from room
                                where lang isnull 
                                order by id
                                limit %s offset %s; ;""", (size, page * size))
            all = cursor.fetchall()
            if len(all) == 0:
                break

            threads = []

            for item in all:
                id = item[0]
                name = item[1]
                desc = item[2]

                t = threading.Thread(target=fetch_and_save_tg_group, args=(id, name, desc, cursor))
                threads.append(t)
                t.start()

            for t in threads:
                t.join()

            cursor.close()
            conn.commit()
            logging.info(f"end calc page {page}")
            page = page + 1
        except:
            logging.error("error occurred, will retry")
            pass

    conn.close()
