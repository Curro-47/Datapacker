package ck47.datapacker.util;

public enum EventType {
    CHAT;

    public static EventType fromString(String str) {
        switch (str) {
            case "chat" -> {
                return EventType.CHAT;
            }
            default -> {
                return null;
            }
        }
    }
}
