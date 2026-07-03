package com.techvedika.harmonycvi.gateway.cloud;

public interface AiServerController {

    void handleIdleServer();
    
    void restartAIServer();

    String getAIServerStatus();

    boolean waitUntilServerRunning();
}
