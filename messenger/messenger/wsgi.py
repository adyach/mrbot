from app import app
import bus

if __name__ == "__main__":
    bus.listen_on_door_state()
    app.run()
