from room_expander.conf.env import settings
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker

engine = create_engine(settings.postgres_connection_string(), connect_args={})

DBSession = sessionmaker(bind=engine)
