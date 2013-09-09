package tankmania.common;

import tankmania.server.TMServer;
import tankmania.client.TMClientUI;

public class Shell
{
    public static void printHelp() {
        System.out.println("Usage: client address  port name tank-type gun-type");
        System.out.println("       server map-name port");
    }
    
    public static void main(String[] args) {
        if (args.length < 3) {
            printHelp();
            return;
        }

        if (args[0].equals("client")) {
            if (args.length < 6) {
                printHelp();
                return;
            }

            String address = args[1];
            int    portNum = Integer.decode(args[2]);
            String name    = args[3];
            int    tankType = Integer.decode(args[4]);
            int    gunType  = Integer.decode(args[5]);
            TMClientUI.runClient(address, name, portNum, tankType, gunType);
        } else if (args[0].equals("server")) {
            String mapName = args[1];
            int    portNum = Integer.decode(args[2]);
            TMServer.runServer(mapName, portNum);
        } else {
            System.out.println("Wrong arguments");
        }
    }
}
