from sqlalchemy import TIMESTAMP, Column, Integer, String, Text
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.sql import func

Base = declarative_base()  # type: ignore


class Room(Base):  # type: ignore
    __tablename__ = "room"

    id = Column(Integer, primary_key=True)
    link = Column(String(255), nullable=False)
    name = Column(String(255))
    jhi_desc = Column(Text)
    member_cnt = Column(Integer)
    type = Column(String(255))
    status = Column(String(255), nullable=False)
    collected_at = Column(TIMESTAMP)
    lang = Column(String(255))
    tags = Column(String(2048))
    msg_cnt = Column(Integer)
    room_id = Column(String(255), unique=True)
    username = Column(String(255))
    extra = Column(Text)
    expand_batch = Column(Integer, default=0)
    web_crawl_batch = Column(Integer, default=0)
    last_web_crawl_at = Column(TIMESTAMP, default=func.current_timestamp())
    created_at = Column(TIMESTAMP, default=func.current_timestamp())
