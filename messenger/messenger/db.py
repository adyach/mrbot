import logging
import os

from tinydb import Query
from tinydb import TinyDB

_LOG = logging.getLogger('db')


class UsersDb(object):

    def get_users_by_service(self, service):
        raise NotImplementedError()

    def get_user_services_by_id(self, user_id):
        raise NotImplementedError()

    def set_user_services(self, user_id, services):
        raise NotImplementedError()


class FacebookUsersTinyDb(UsersDb):
    def __init__(self):
        self.db = TinyDB(os.getenv('PATH_DB_USERS','/data/db/users.json'))

        def get_users_by_service(self, service):
            users = self.db.search(Query().services.any({service}))
            if users:
                return [u['user_id'] for u in users]
            else:
                _LOG.warning('no users for service:%s found in database'.format(service))
                return None

        def get_user_services_by_id(self, user_id):
            users = self.db.search(Query()['user_id'] == user_id)
            if users:
                return users[0]['services']
            _LOG.warning('no services found for user:%s'.format(user_id))
            return None

        def set_user_services(self, user_id, services):
            return self.db.insert({'user_id': user_id, 'services': services})
