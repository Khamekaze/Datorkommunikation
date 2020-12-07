package com.khamurai.labb1;

import java.io.IOException;
import java.net.*;

public class Main {

    static String[] servers = {"blb1.ntp.se", "gbg1.ntp.se"};

    public static void main(String[] args) {
        try(DatagramSocket socket = new DatagramSocket()){

            DatagramPacket packet = datagramPacket();

            socket.send(packet);
            System.out.println("Sent request");
            socket.receive(packet);
            SNTPMessage response = new SNTPMessage(packet.getData());
            System.out.println("Got reply");

            System.out.println();

            double destinationTimestamp =
                    (System.currentTimeMillis() / 1000.0) + 2208988800.0;
            double localClockOffset =
                    ((response.getReceiveTimeStamp() - response.getOriginateTimeStamp()) +
                            (response.getTransmitTimeStamp() - destinationTimestamp)) / 2;

            System.out.println("Local offset: " + localClockOffset * 1000 + " ms");

            System.out.println("=====================");
            System.out.println(response.toString());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static DatagramPacket datagramPacket() {
        DatagramPacket packet = null;
        for(String s : servers) {
            try {
                InetAddress adress = InetAddress.getByName(s);
                SNTPMessage message = new SNTPMessage();
                byte[] buf = message.toByteArray();
                packet = new DatagramPacket(buf, buf.length, adress, 123);
                break;
            } catch(Exception e) {
                System.out.println("Could not connect to server, trying next.");
            }
        }
        return packet;
    }
}
