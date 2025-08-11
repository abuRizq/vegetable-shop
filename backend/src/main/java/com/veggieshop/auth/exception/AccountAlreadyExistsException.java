    package com.veggieshop.auth.exception;

    import com.veggieshop.core.exception.ApplicationException;
    import org.springframework.http.HttpStatus;

    import java.util.LinkedHashMap;
    import java.util.Map;

    /**
     * Thrown when attempting to create an account that already exists
     * (e.g., same email or username). Defaults to 409 Conflict.
     */
    public final class AccountAlreadyExistsException extends ApplicationException {

        private static final long serialVersionUID = 1L;

        public static final HttpStatus DEFAULT_STATUS  = HttpStatus.CONFLICT;
        public static final String     DEFAULT_CODE    = "ACCOUNT_ALREADY_EXISTS";
        public static final String     DEFAULT_MESSAGE =
                "An account with the provided identifier already exists.";

        /**
         * Full-argument constructor.
         */
        protected AccountAlreadyExistsException(HttpStatus status,
                                                String code,
                                                String message,
                                                Throwable cause,
                                                Map<String, Object> metadata) {
            super(status, code, message, cause, metadata);
        }

        /** Uses default message. */
        public AccountAlreadyExistsException() {
            this(DEFAULT_STATUS, DEFAULT_CODE, DEFAULT_MESSAGE, null, null);
        }

        /** Custom message. */
        public AccountAlreadyExistsException(String message) {
            this(DEFAULT_STATUS, DEFAULT_CODE, message, null, null);
        }

        /** Custom message with cause. */
        public AccountAlreadyExistsException(String message, Throwable cause) {
            this(DEFAULT_STATUS, DEFAULT_CODE, message, cause, null);
        }

        /** Default message with cause. */
        public AccountAlreadyExistsException(Throwable cause) {
            this(DEFAULT_STATUS, DEFAULT_CODE, DEFAULT_MESSAGE, cause, null);
        }

        /** Custom message with metadata. */
        public AccountAlreadyExistsException(String message, Map<String, Object> metadata) {
            this(DEFAULT_STATUS, DEFAULT_CODE, message, null, metadata);
        }

        /** Custom message with cause and metadata. */
        public AccountAlreadyExistsException(String message, Throwable cause, Map<String, Object> metadata) {
            this(DEFAULT_STATUS, DEFAULT_CODE, message, cause, metadata);
        }

        /** Custom error code with message. */
        public AccountAlreadyExistsException(String errorCode, String message) {
            this(DEFAULT_STATUS, errorCode, message, null, null);
        }

        /** Factory for an email-based conflict. */
        public static AccountAlreadyExistsException forEmail(String email) {
            Map<String, Object> meta = new LinkedHashMap<>();
            if (email != null) meta.put("email", email);
            return new AccountAlreadyExistsException("An account with this email already exists.", meta);
        }

        /** Factory for a username-based conflict. */
        public static AccountAlreadyExistsException forUsername(String username) {
            Map<String, Object> meta = new LinkedHashMap<>();
            if (username != null) meta.put("username", username);
            return new AccountAlreadyExistsException("An account with this username already exists.", meta);
        }

        /** Factory for an arbitrary identifier key/value. */
        public static AccountAlreadyExistsException forIdentifier(String identifierKey, String identifierValue) {
            Map<String, Object> meta = new LinkedHashMap<>();
            if (identifierKey != null && identifierValue != null) {
                meta.put(identifierKey, identifierValue);
            }
            return new AccountAlreadyExistsException(DEFAULT_MESSAGE, meta);
        }

        @Override
        public AccountAlreadyExistsException newInstance(HttpStatus status,
                                                         String code,
                                                         String message,
                                                         Throwable cause,
                                                         Map<String, Object> metadata) {
            return new AccountAlreadyExistsException(status, code, message, cause, metadata);
        }
    }
