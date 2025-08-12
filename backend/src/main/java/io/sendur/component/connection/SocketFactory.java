package io.sendur.component.connection;

import java.io.IOException;
import java.net.Socket;

public interface SocketFactory {
    Socket create() throws IOException;
}
