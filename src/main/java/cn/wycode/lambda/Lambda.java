package cn.wycode.lambda;

import com.aliyun.fc.runtime.Context;
import com.aliyun.fc.runtime.StreamRequestHandler;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class Lambda implements StreamRequestHandler {


    @Override
    public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
        String line = reader.readLine();
        while (line != null) {
            context.getLogger().info(line);
            output.write(line.getBytes());
            line = reader.readLine();
        }
    }
}
