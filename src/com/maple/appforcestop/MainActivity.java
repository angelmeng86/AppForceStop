
package com.maple.appforcestop;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    
    private static final String tag = "maple";
    
    private List<String> protectedPackages = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        Button btn = (Button)findViewById(R.id.btn_test);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                forceStop();
            }
        });
        
        initProcetedPackage();
    }
    
    private void initProcetedPackage() {
        //白名单，不需要强制停止的
        protectedPackages.add("com.maple.appforcestop");
        protectedPackages.add("com.tencent.mobileqq");
        protectedPackages.add("com.tencent.mm");
        protectedPackages.add("com.baidu.input");
    }
    
    private void killProcesses(String packname) {
        ActivityManager am = (ActivityManager)this.getSystemService(
                Context.ACTIVITY_SERVICE);
        am.forceStopPackage(packname);
    }
    
    public void forceStop() {
        try {
            String data = execCommand("pm list packages -3");
            String[] packages = data.split("\n");
            Log.d(tag, "pm list packages -3:" + packages.length);
            for(String name : packages) {
                String[] str = name.split(":");
                if(str.length > 1) {
                    if(protectedPackages.contains(str[1])) {
                        Log.d(tag, "protectedPackage:" + str[1]);
                    }
                    else {
                        killProcesses(str[1]);
                    }
                }
            }
            Log.d(tag, "forceStop success.");
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(tag, e.toString());
        }
        Toast.makeText(this, "应用强制停止执行完成！", Toast.LENGTH_LONG).show();
    }
    
    public String execCommand(String command) throws IOException {
        Runtime runtime = Runtime.getRuntime();  
        Process proc = runtime.exec(command);
            InputStream inputstream = proc.getInputStream();
            InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
            BufferedReader bufferedreader = new BufferedReader(inputstreamreader);
            String line = "";
            StringBuilder sb = new StringBuilder(line);
            while ((line = bufferedreader.readLine()) != null) {
                    sb.append(line);
                    sb.append('\n');
            }
            //使用exec执行不会等执行成功以后才返回,它会立即返回
            //所以在某些情况下是很要命的(比如复制文件的时候)
            //使用wairFor()可以等待命令执行完成以后才返回
            try {
                if (proc.waitFor() != 0) {
                    Log.d(tag, "exit value = " + proc.exitValue());
                }
            }
            catch (InterruptedException e) {  
                Log.d(tag, e.toString());
            }
            return sb.toString();
        }
}
