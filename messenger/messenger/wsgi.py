import logging

from app import app

_LOG = logging.getLogger('mrbot.uwsgi')

if __name__ == "__main__":
    logging.basicConfig(level=getattr(logging, 'INFO', None))
    app.run()
