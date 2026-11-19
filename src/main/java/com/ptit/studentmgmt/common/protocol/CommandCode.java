package com.ptit.studentmgmt.common.protocol;

public final class CommandCode {
    private CommandCode() {}

    // Magic number: 0xCAFE (2 bytes)
    public static final short MAGIC = (short) 0xCAFE;

    // Ping - Heartbeat (0x0001)
    public static final short CMD_PING = 0x0001;

    // Database Connection
    public static final short CMD_DB_CONNECT_REQ = 0x0010;
    public static final short CMD_DB_CONNECT_RESP = 0x0011;

    // Add Single Student
    public static final short CMD_ADD_STUDENT_REQ = 0x0020;
    public static final short CMD_ADD_STUDENT_RESP = 0x0021;

    // Batch File Upload
    public static final short CMD_UPLOAD_FILE_REQ = 0x0030;
    public static final short CMD_UPLOAD_FILE_RESP = 0x0031;

    // Calculate & Fetch Results
    public static final short CMD_CALC_RESULTS_REQ = 0x0040;
    public static final short CMD_CALC_RESULTS_RESP = 0x0041;

    // General Error
    public static final short CMD_ERROR = 0x0099;

    public static String getCommandName(short code) {
        switch (code) {
            case CMD_PING: return "CMD_PING";
            case CMD_DB_CONNECT_REQ: return "CMD_DB_CONNECT_REQ";
            case CMD_DB_CONNECT_RESP: return "CMD_DB_CONNECT_RESP";
            case CMD_ADD_STUDENT_REQ: return "CMD_ADD_STUDENT_REQ";
            case CMD_ADD_STUDENT_RESP: return "CMD_ADD_STUDENT_RESP";
            case CMD_UPLOAD_FILE_REQ: return "CMD_UPLOAD_FILE_REQ";
            case CMD_UPLOAD_FILE_RESP: return "CMD_UPLOAD_FILE_RESP";
            case CMD_CALC_RESULTS_REQ: return "CMD_CALC_RESULTS_REQ";
            case CMD_CALC_RESULTS_RESP: return "CMD_CALC_RESULTS_RESP";
            case CMD_ERROR: return "CMD_ERROR";
            default: return "UNKNOWN_CMD(0x" + Integer.toHexString(code) + ")";
        }
    }
}
