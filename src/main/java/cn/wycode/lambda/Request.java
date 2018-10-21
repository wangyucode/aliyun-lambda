package cn.wycode.lambda;

import java.util.Map;

public class Request {

    public String method;
    public String uri;
    public String protocolVersion;
    public Map<String, String> headers;
    public String body;
}
