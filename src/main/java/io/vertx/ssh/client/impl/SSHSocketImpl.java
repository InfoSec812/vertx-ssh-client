package io.vertx.ssh.client.impl;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.ssh.client.ReadableOutputStream;
import io.vertx.ssh.client.SSHSocket;
import io.vertx.ssh.client.WriteableInputStream;


public class SSHSocketImpl implements SSHSocket {

  private Handler<Throwable> exHandler;
  private Handler<Buffer> handler;
  private final ReadableOutputStream inFromSSH;
  private final WriteableInputStream outToSSH;
  private final Vertx vertx;

  public SSHSocketImpl(Vertx vertx, String host, String user, String pass, String key, int port) throws JSchException {
    super();
    this.vertx = vertx;
    JSch jsch = new JSch();
    Session session;
    if (key!=null) {
      jsch.addIdentity(key);
      session = jsch.getSession(user, host, port);
    } else {
      session = jsch.getSession(user, host, port);
      session.setPassword(pass);
    }
  }

  @Override;
  public SSHSocket exceptionHandler(Handler<Throwable> handler) {
    return this;
  }

  @Override
  public SSHSocket handler(Handler<Buffer> handler) {
    
  }

  @Override
  public SSHSocket pause() {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public SSHSocket resume() {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public SSHSocket endHandler(Handler<Void> endHandler) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public SSHSocket write(Buffer data) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public SSHSocket setWriteQueueMaxSize(int maxSize) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public SSHSocket drainHandler(Handler<Void> handler) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public String writeHandlerID() {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public SSHSocket write(String str) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public SSHSocket write(String str, String enc) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public SSHSocket sendFile(String filename) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public SSHSocket sendFile(String filename, Handler<AsyncResult<Void>> resultHandler) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public SSHSocket getFile(String filename, Handler<AsyncResult<Buffer>> resultHandler) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public void close() {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public SSHSocket closeHandler(Handler<Void> handler) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public boolean writeQueueFull() {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

}
