package utils;

public interface Observer {
        void update(Observable observable);
        void update(Observable observable, Object data);
}
