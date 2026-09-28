package fu.de200357.account;

public class TokenResult {
    private final ResultCode code;
    private final String token;

    public TokenResult(ResultCode code, String token) {
        this.code = code;
        this.token = token;
    }

    public ResultCode getCode() { return code; }
    public String getToken() { return token; }
}
