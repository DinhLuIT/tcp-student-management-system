package com.ptit.studentmgmt.common.util;

import com.ptit.studentmgmt.common.dto.BatchStudentDTO;
import com.ptit.studentmgmt.common.dto.DBConfigDTO;
import com.ptit.studentmgmt.common.dto.ServerResponseDTO;
import com.ptit.studentmgmt.common.dto.StudentDTO;
import com.ptit.studentmgmt.common.dto.StudentResultDTO;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Tiện ích chuyển đổi JSON hai chiều an toàn.
 * Hỗ trợ tự động nhận diện và sử dụng Jackson nếu có trong Classpath,
 * đồng thời tích hợp sẵn parser/generator thuần Java để ứng dụng luôn chạy ổn định
 * ngay cả khi không có thư viện ngoài.
 */
public final class JsonUtil {
    private static Object jacksonMapper = null;
    private static boolean jacksonAvailable = false;

    static {
        try {
            Class<?> clazz = Class.forName("com.fasterxml.jackson.databind.ObjectMapper");
            jacksonMapper = clazz.getDeclaredConstructor().newInstance();
            jacksonAvailable = true;
        } catch (Throwable t) {
            jacksonAvailable = false;
        }
    }

    private JsonUtil() {}

    public static String toJson(Object obj) {
        if (obj == null) return "{}";

        if (jacksonAvailable && jacksonMapper != null) {
            try {
                Method writeValueAsString = jacksonMapper.getClass()
                        .getMethod("writeValueAsString", Object.class);
                return (String) writeValueAsString.invoke(jacksonMapper, obj);
            } catch (Exception ignored) {
                // Fallback to built-in serializer
            }
        }

        // Built-in Serializer thuần Java
        if (obj instanceof DBConfigDTO) {
            DBConfigDTO d = (DBConfigDTO) obj;
            return String.format("{\"host\":\"%s\",\"port\":%d,\"databaseName\":\"%s\",\"username\":\"%s\",\"password\":\"%s\"}",
                    escape(d.getHost()), d.getPort(), escape(d.getDatabaseName()), escape(d.getUsername()), escape(d.getPassword()));
        } else if (obj instanceof StudentDTO) {
            StudentDTO s = (StudentDTO) obj;
            return String.format("{\"studentId\":\"%s\",\"fullName\":\"%s\",\"mathScore\":%s,\"literatureScore\":%s,\"englishScore\":%s}",
                    escape(s.getStudentId()), escape(s.getFullName()), s.getMathScore(), s.getLiteratureScore(), s.getEnglishScore());
        } else if (obj instanceof StudentResultDTO) {
            StudentResultDTO r = (StudentResultDTO) obj;
            return String.format("{\"studentId\":\"%s\",\"fullName\":\"%s\",\"mathScore\":%s,\"literatureScore\":%s,\"englishScore\":%s,\"averageScore\":%s,\"academicRank\":\"%s\"}",
                    escape(r.getStudentId()), escape(r.getFullName()), r.getMathScore(), r.getLiteratureScore(), r.getEnglishScore(), r.getAverageScore(), escape(r.getAcademicRank()));
        } else if (obj instanceof ServerResponseDTO) {
            ServerResponseDTO res = (ServerResponseDTO) obj;
            return String.format("{\"statusCode\":%d,\"message\":\"%s\",\"success\":%b}",
                    res.getStatusCode(), escape(res.getMessage()), res.isSuccess());
        } else if (obj instanceof BatchStudentDTO) {
            BatchStudentDTO b = (BatchStudentDTO) obj;
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("{\"fileName\":\"%s\",\"totalRecords\":%d,\"students\":[", escape(b.getFileName()), b.getTotalRecords()));
            List<StudentDTO> list = b.getStudents();
            if (list != null) {
                for (int i = 0; i < list.size(); i++) {
                    sb.append(toJson(list.get(i)));
                    if (i < list.size() - 1) sb.append(",");
                }
            }
            sb.append("]}");
            return sb.toString();
        } else if (obj instanceof List) {
            List<?> list = (List<?>) obj;
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < list.size(); i++) {
                sb.append(toJson(list.get(i)));
                if (i < list.size() - 1) sb.append(",");
            }
            sb.append("]");
            return sb.toString();
        }

        return "{}";
    }

    public static DBConfigDTO parseDBConfig(String json) {
        if (json == null || json.trim().isEmpty()) return new DBConfigDTO();
        String host = extractString(json, "host", "localhost");
        int port = extractInt(json, "port", 1433);
        String dbName = extractString(json, "databaseName", "StudentManagementDB");
        String user = extractString(json, "username", "sa");
        String pass = extractString(json, "password", "");
        return new DBConfigDTO(host, port, dbName, user, pass);
    }

    public static StudentDTO parseStudent(String json) {
        if (json == null || json.trim().isEmpty()) return new StudentDTO();
        String id = extractString(json, "studentId", "");
        String name = extractString(json, "fullName", "");
        double math = extractDouble(json, "mathScore", 0.0);
        double lit = extractDouble(json, "literatureScore", 0.0);
        double eng = extractDouble(json, "englishScore", 0.0);
        return new StudentDTO(id, name, math, lit, eng);
    }

    public static ServerResponseDTO parseServerResponse(String json) {
        if (json == null || json.trim().isEmpty()) return ServerResponseDTO.error(500, "Phản hồi rỗng");
        int code = extractInt(json, "statusCode", 500);
        String msg = extractString(json, "message", "");
        boolean success = extractBoolean(json, "success", false);
        return new ServerResponseDTO(code, msg, success);
    }

    public static BatchStudentDTO parseBatchStudent(String json) {
        if (json == null || json.trim().isEmpty()) return new BatchStudentDTO();
        String fileName = extractString(json, "fileName", "upload.csv");
        List<StudentDTO> students = new ArrayList<>();

        int arrStart = json.indexOf("\"students\":[");
        if (arrStart != -1) {
            int openBracket = json.indexOf('[', arrStart);
            int closeBracket = json.lastIndexOf(']');
            if (openBracket != -1 && closeBracket > openBracket) {
                String sub = json.substring(openBracket + 1, closeBracket);
                Pattern p = Pattern.compile("\\{[^\\{\\}]+\\}");
                Matcher m = p.matcher(sub);
                while (m.find()) {
                    students.add(parseStudent(m.group()));
                }
            }
        }
        return new BatchStudentDTO(fileName, students);
    }

    public static List<StudentResultDTO> parseStudentResults(String json) {
        List<StudentResultDTO> list = new ArrayList<>();
        if (json == null || json.trim().isEmpty()) return list;

        Pattern p = Pattern.compile("\\{[^\\{\\}]+\\}");
        Matcher m = p.matcher(json);
        while (m.find()) {
            String item = m.group();
            String id = extractString(item, "studentId", "");
            String name = extractString(item, "fullName", "");
            double math = extractDouble(item, "mathScore", 0.0);
            double lit = extractDouble(item, "literatureScore", 0.0);
            double eng = extractDouble(item, "englishScore", 0.0);
            double avg = extractDouble(item, "averageScore", 0.0);
            String rank = extractString(item, "academicRank", "");
            if (!id.isEmpty()) {
                list.add(new StudentResultDTO(id, name, math, lit, eng, avg, rank));
            }
        }
        return list;
    }

    // Các hàm helper phân tích chuỗi an toàn
    private static String extractString(String json, String key, String defaultVal) {
        Pattern p = Pattern.compile("\"" + key + "\"\\s*:\\s*\"([^\"]*)\"");
        Matcher m = p.matcher(json);
        if (m.find()) return unescape(m.group(1));
        return defaultVal;
    }

    private static int extractInt(String json, String key, int defaultVal) {
        Pattern p = Pattern.compile("\"" + key + "\"\\s*:\\s*(-?\\d+)");
        Matcher m = p.matcher(json);
        if (m.find()) {
            try { return Integer.parseInt(m.group(1)); } catch (Exception ignored) {}
        }
        return defaultVal;
    }

    private static double extractDouble(String json, String key, double defaultVal) {
        Pattern p = Pattern.compile("\"" + key + "\"\\s*:\\s*(-?\\d+(\\.\\d+)?)");
        Matcher m = p.matcher(json);
        if (m.find()) {
            try { return Double.parseDouble(m.group(1)); } catch (Exception ignored) {}
        }
        return defaultVal;
    }

    private static boolean extractBoolean(String json, String key, boolean defaultVal) {
        Pattern p = Pattern.compile("\"" + key + "\"\\s*:\\s*(true|false)");
        Matcher m = p.matcher(json);
        if (m.find()) {
            return Boolean.parseBoolean(m.group(1));
        }
        return defaultVal;
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
    }

    private static String unescape(String s) {
        if (s == null) return "";
        return s.replace("\\\"", "\"").replace("\\\\", "\\").replace("\\n", "\n");
    }
}
