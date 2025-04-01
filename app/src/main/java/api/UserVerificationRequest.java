package api;

public class UserVerificationRequest {
    private String userId;
    private String verification_code;

    public UserVerificationRequest(String userId, String verification_code) {
        this.userId = userId;
        this.verification_code = verification_code;
    }
}
