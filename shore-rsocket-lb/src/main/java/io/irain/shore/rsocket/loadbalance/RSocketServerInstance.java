package io.irain.shore.rsocket.loadbalance;

import io.rsocket.transport.ClientTransport;
import io.rsocket.transport.netty.client.TcpClientTransport;
import io.rsocket.transport.netty.client.WebsocketClientTransport;

import java.net.URI;

/**
 * RSocketServerInstance.
 *
 * @author youta
 */
public class RSocketServerInstance {
    private String host;
    private int port;
    /**
     * schema, such as tcp, ws, wss
     */
    private String schema = "tcp";
    /**
     * path, for websocket only
     */
    private String path;

    /**
     * RSocketServerInstance.
     */
    public RSocketServerInstance() {
    }

    /**
     * RSocketServerInstance.
     *
     * @param host host
     * @param port port
     */
    public RSocketServerInstance(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * getHost.
     *
     * @return host
     */
    public String getHost() {
        return host;
    }

    /**
     * setHost.
     *
     * @param host host
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * getPort.
     *
     * @return port
     */
    public int getPort() {
        return port;
    }

    /**
     * setPort.
     *
     * @param port port
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * getSchema.
     *
     * @return schema
     */
    public String getSchema() {
        return schema;
    }

    /**
     * setSchema.
     *
     * @param schema schema
     */
    public void setSchema(String schema) {
        this.schema = schema;
    }

    /**
     * getPath.
     *
     * @return path
     */
    public String getPath() {
        return path;
    }

    /**
     * setPath.
     *
     * @param path path
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * isWebSocket.
     *
     * @return boolean
     */
    public boolean isWebSocket() {
        return "ws".equals(this.schema) || "wss".equals(this.schema);
    }

    /**
     * getURI.
     *
     * @return URI
     */
    public String getURI() {
        if (isWebSocket()) {
            return schema + "://" + host + ":" + port + path;
        } else {
            return schema + "://" + host + ":" + port;
        }
    }

    /**
     * getClientTransport.
     *
     * @return ClientTransport
     */
    public ClientTransport constructClientTransport() {
        if (this.isWebSocket()) {
            return WebsocketClientTransport.create(URI.create(getURI()));
        }
        return TcpClientTransport.create(host, port);
    }

    /**
     * equals.
     *
     * @return String equals
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RSocketServerInstance that = (RSocketServerInstance) o;
        if (port != that.port) return false;
        return host.equals(that.host);
    }

    /**
     * hashCode.
     *
     * @return String hashCode
     */
    @Override
    public int hashCode() {
        int result = host.hashCode();
        result = 31 * result + port;
        return result;
    }

}
