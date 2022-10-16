package whiteboard;

import java.net.InetSocketAddress;

public class User {

    public String username;
    public boolean manager;
    public InetSocketAddress address;

    public User(String username, boolean manager, InetSocketAddress address) {
        this.username = username;
        this.manager = manager;
        this.address = address;
    }

}
