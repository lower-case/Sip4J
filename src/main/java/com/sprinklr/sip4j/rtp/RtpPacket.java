package com.sprinklr.sip4j.rtp;

import java.util.Arrays;

import static com.sprinklr.sip4j.utils.Constants.RTP_HEADER_SIZE;

/**
RTP Packet transported between RTP endpoints
 */
public class RtpPacket {

    //Fields that compose the RTP header, refer https://dl.acm.org/doi/pdf/10.17487/RFC3550
    private static final int VERSION = 2; //version will remain constant, change if rtp version changes
    private static final int PADDING = 0;
    private static final int EXTENSION = 0; //no extension headers
    private static final int CC = 0; //no contributors since single source
    private int marker = 0; //depending on application logic, may be used to indicate end of audio stream
    private int payloadType;
    private int sequenceNumber;
    private int timeStamp;
    private int ssrc;

    //Bitstream of the RTP header
    private byte[] header;

    //size of the RTP payload
    private int payloadSize;
    //Bitstream of the RTP payload
    private byte[] payload;

    /**
     * Constructor of an RTPpacket object from header fields and the payload
     * @param payloadType This field identifies the format of the RTP payload and determines its interpretation by the application.
     * @param sequenceNumber The sequence number increments by one for each RTP data packet sent, and may be used by the receiver to detect packet loss and to restore packet sequence.
     * @param timeStamp The timestamp reflects the sampling instant of the first octet in the RTP data packet.
     * @param ssrc The source of a stream of RTP packets, identified by a 32-bit numeric SSRC identifier
     * @param marker The interpretation of the marker is defined by a profile.
     * @param data the payload byte array
     * @param dataLength the length of the payload byte array
     */
    public RtpPacket(int payloadType, int sequenceNumber, int timeStamp, int ssrc, int marker, byte[] data, int dataLength) {

        //fill changing header fields:
        this.sequenceNumber = sequenceNumber;
        this.timeStamp = timeStamp;
        this.payloadType = payloadType;
        this.ssrc = ssrc;
        this.marker = marker;

        //build the header bistream:
        header = new byte[RTP_HEADER_SIZE];

        //fill the header array of byte with RTP header fields
        header[0] = (byte) (VERSION << 6 | PADDING << 5 | EXTENSION << 4 | CC);
        header[1] = (byte) (marker << 7 | this.payloadType & 0x000000FF);
        header[2] = (byte) ((this.sequenceNumber & 0xFF00) >> 8);
        header[3] = (byte) (this.sequenceNumber & 0x00FF);
        header[4] = (byte) ((this.timeStamp & 0xFF000000) >> 24);
        header[5] = (byte) ((this.timeStamp & 0x00FF0000) >> 16);
        header[6] = (byte) ((this.timeStamp & 0x0000FF00) >> 8);
        header[7] = (byte) (this.timeStamp & 0x000000FF);
        header[8] = (byte) ((this.ssrc & 0xFF000000) >> 24);
        header[9] = (byte) ((this.ssrc & 0x00FF0000) >> 16);
        header[10] = (byte) ((this.ssrc & 0x0000FF00) >> 8);
        header[11] = (byte) (this.ssrc & 0x000000FF);

        //fill the payload bitstream:
        payloadSize = dataLength;

        //fill payload array of byte from data (given in parameter of the constructor)
        payload = Arrays.copyOf(data, payloadSize);
    }

    /**
     * Re-constructs an RtpPacket object from the packet bytes
     * @param packet The original RtpPacket in bytes
     * @param packetSize The size of the packet bytes array
     */
    public RtpPacket(byte[] packet, int packetSize) {

        //check if total packet size is lower than the header size
        if (packetSize >= RTP_HEADER_SIZE) {
            //get the header bitsream:
            header = new byte[RTP_HEADER_SIZE];
            System.arraycopy(packet, 0, header, 0, RTP_HEADER_SIZE);

            //get the payload bitstream:
            payloadSize = packetSize - RTP_HEADER_SIZE;
            payload = new byte[payloadSize];
            System.arraycopy(packet, RTP_HEADER_SIZE, payload, 0, packetSize - RTP_HEADER_SIZE);

            //interpret the changing fields of the header:
            marker = (header[1] & 0xFF & 0x80) >> 7;
            payloadType = header[1] & 0xFF & 0x7F;
            sequenceNumber = (header[3] & 0xFF) + ((header[2] & 0xFF) << 8);
            timeStamp = (header[7] & 0xFF) + ((header[6] & 0xFF) << 8) + ((header[5] & 0xFF) << 16) + ((header[4] & 0xFF) << 24);
            ssrc = (header[11] & 0xFF) + ((header[10] & 0xFF) << 8) + ((header[9] & 0xFF) << 16) + ((header[8] & 0xFF) << 24);
        }
    }

    /**
     * Extracts the payload from the packet
     * @param data The byte array into which the payload if copied
     */
    public void getPayload(byte[] data) {

        if (payloadSize >= 0) System.arraycopy(payload, 0, data, 0, payloadSize);

    }

    /**
     * Gets the size of the payload bytes
     * @return the payload bytes array length
     */
    public int getPayloadLength() {
        return (payloadSize);
    }

    /**
     * Gets the size of the packet bytes
     * @return the packet bytes array length
     */
    public int getLength() {
        return (payloadSize + RTP_HEADER_SIZE);
    }

    /**
     * Extracts the RtpPacket object into a byte array to be sent in an RTP session
     * @param packet The byte array into which the information is copied
     */
    public void getPacket(byte[] packet) {
        //construct the packet = header + payload
        if (RTP_HEADER_SIZE >= 0) System.arraycopy(header, 0, packet, 0, RTP_HEADER_SIZE);
        if (payloadSize >= 0) System.arraycopy(payload, 0, packet, RTP_HEADER_SIZE, payloadSize);

    }

    public int getTimeStamp() {
        return (timeStamp);
    }

    public int getSequenceNumber() {
        return (sequenceNumber);
    }

    public int getSsrc() {
        return ssrc;
    }

    public int getPayloadType() {
        return (payloadType);
    }

    public String getHeaderAsString() {
        return "Version:" + VERSION + " Padding:" + PADDING + " Extension: " + EXTENSION + " CC: " + CC + " Marker:" + marker
                + " PayloadType:" + payloadType + " SequenceNumber:" + sequenceNumber + " TimeStamp:" + timeStamp;
    }
}