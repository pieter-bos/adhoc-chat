package client;

import java.net.InetAddress;
import java.util.HashMap;

/**
 * Two way mapping between clients username and internet addresses
 */
public class ClientAddressMapper {
    private HashMap<String, InetAddress> usernameAddressMap;
    private HashMap<InetAddress, String> addressUsernameMap;

    /**
     * Constructor
     */
    public ClientAddressMapper() {
        usernameAddressMap = new HashMap<>();
        addressUsernameMap = new HashMap<>();
    }

    /**
     * Gets address by username
     * @param username
     * @return
     */
    public InetAddress getAddress(String username) {
        return usernameAddressMap.get(username);
    }

    /**
     * Gets username by address
     * @param address
     * @return
     */
    public String getUsername(InetAddress address) {
        return addressUsernameMap.get(address);
    }

    /**
     * Puts <username, address> tuple
     * @param username
     * @param address
     */
    public void put(String username, InetAddress address) {
        usernameAddressMap.put(username, address);
        addressUsernameMap.put(address, username);
    }

    /**
     * Removes tuple by username
     * @param username
     */
    public void removeAddress(String username) {
        InetAddress address = usernameAddressMap.remove(username);
        addressUsernameMap.remove(address);
    }

    /**
     * Removes tuple by address
     * @param address
     */
    public void removeUsername(InetAddress address) {
        String username = addressUsernameMap.remove(address);
        usernameAddressMap.remove(username);
    }
}
