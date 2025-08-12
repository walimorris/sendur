package io.sendur.component.connection;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.Socket;

@Component
public class DefaultSocketFactory implements SocketFactory {

    @Override
    public Socket create() throws IOException {
        return new Socket();
    }
}
