package cn.wycode.lambda;

import com.aliyun.fc.runtime.Context;
import com.aliyun.fc.runtime.FunctionComputeLogger;
import com.aliyun.fc.runtime.StreamRequestHandler;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class Lambda implements StreamRequestHandler {


    @Override
    public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException {
        FunctionComputeLogger logger = context.getLogger();
        StringBuilder sb = new StringBuilder();
        byte[] buffer = new byte[1024];
        int n;
        while ((n=input.read(buffer))!=-1){
            sb.append(new String(buffer,0,n,StandardCharsets.UTF_8));
        }
        logger.info(sb.toString());
        output.write(sb.toString().getBytes(StandardCharsets.UTF_8));


    }
}
