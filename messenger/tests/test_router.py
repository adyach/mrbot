from unittest.mock import MagicMock

import messenger.facebook.router as router
import messenger.api

def test_facebook_message():
    messenger.api = MagicMock()
    messenger.api.get_front_door.return_value = {'data': 'test'}

    data = {'object': 'page', 'entry': [{'messaging': [{'message': {'text': 'front_door'}, 'sender': {'id': 'andy'}}]}]}
    router.route_message(data)
    assert 'andy' in router._users
    assert router._users['andy'].intersection({'front door'})


if __name__ == '__main__':
    test_facebook_message()
