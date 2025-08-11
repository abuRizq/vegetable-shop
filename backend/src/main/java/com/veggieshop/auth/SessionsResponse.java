package com.veggieshop.auth;

import java.util.List;

public class SessionsResponse {
    private final List<SessionDto> sessions;
    private final Long currentSessionId;

    public SessionsResponse(List<SessionDto> sessions, Long currentSessionId) {
        this.sessions = sessions;
        this.currentSessionId = currentSessionId;
    }
    public List<SessionDto> getSessions() { return sessions; }
    public Long getCurrentSessionId() { return currentSessionId; }
}
