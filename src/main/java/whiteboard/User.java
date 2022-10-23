package whiteboard;

import java.net.InetSocketAddress;

/**
 * Object containing all attrbitues of an individual client (user or manager)
 */
public class User {

    public String username;
    public boolean manager;
    public InetSocketAddress address;
    public boolean approved;

    public User(String username, boolean manager, InetSocketAddress address, boolean approved) {
        this.username = username;
        this.manager = manager;
        this.address = address;
        this.approved = approved;
    }

}
