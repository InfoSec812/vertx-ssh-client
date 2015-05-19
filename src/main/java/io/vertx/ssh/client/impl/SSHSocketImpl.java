package io.vertx.ssh.client.impl;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.ssh.client.ReadableOutputStream;
import io.vertx.ssh.client.SSHSocket;
import io.vertx.ssh.client.WriteableInputStream;
import java.util.UUID;
import org.mvel2.util.ThisLiteral;


public class SSHSocketImpl implements SSHSocket {

  private Handler<Throwable> exHandler;
  private Handler<Buffer> handler;
  private Handler<Void> endHandler;
  private Handler<Void> drainHandler;
  private final ReadableOutputStream inFromSSH;
  private final WriteableInputStream outToSSH;
  private final Vertx vertx;
  private final Session session;
  private final JSch jsch;
  private final Channel shell;
  private final String writeHandlerID;
  private final MessageConsumer registration;
  private boolean paused = false;

  public SSHSocketImpl(Vertx vertx, String host, String user, String pass, String key, int port) throws JSchException {
    super();
    this.vertx = vertx;
    jsch = new JSch();
    if (key!=null) {
      jsch.addIdentity(key);
      session = jsch.getSession(user, host, port);
    } else {
      session = jsch.getSession(user, host, port);
      session.setPassword(pass);
    }
    shell = session.openChannel("shell");
    inFromSSH = new ReadableOutputStream(vertx);
    outToSSH = new WriteableInputStream(vertx);
    shell.setInputStream(outToSSH);
    shell.setOutputStream(inFromSSH);
    writeHandlerID = UUID.randomUUID().toString();
    Handler<Message<Buffer>> writeHandler = msg -> write(msg.body());
    registration = vertx.eventBus().<Buffer>localConsumer(writeHandlerID).handler(writeHandler);
  }

  @Override
  public SSHSocket exceptionHandler(Handler<Throwable> handler) {
    this.exHandler = handler;
    return this;
  }

  @Override
  public SSHSocket handler(Handler<Buffer> handler) {
    this.handler = handler;
    return this;
  }

  @Override
  public SSHSocket pause() {
    this.paused = true;
    inFromSSH.pause();
    return this;
  }

  @Override
  public SSHSocket resume() {
    inFromSSH.resume();
    this.paused = false;
    return this;
  }

  @Override
  public SSHSocket endHandler(Handler<Void> endHandler) {
    this.endHandler = endHandler;
  }

  @Override
  public SSHSocket write(Buffer data) {
    outToSSH.write(data);
    return this;
  }

  @Override
  public SSHSocket setWriteQueueMaxSize(int maxSize) {
    outToSSH.setWriteQueueMaxSize(maxSize);
    return this;
  }

  @Override
  public SSHSocket drainHandler(Handler<Void> handler) {
    this.drainHandler = handler;
    return this;
  }

  @Override
  public String writeHandlerID() {
    return writeHandlerID;
  }

  @Override
  public SSHSocket write(String str) {
    outToSSH.write(Buffer.buffer(str));
    return this;
  }

  @Override
  public SSHSocket write(String str, String enc) {
    outToSSH.write(Buffer.buffer(str, enc));
    return this;
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
  public SSHSocket sendFile(String filename, Buffer fileData, Handler<AsyncResult<Void>> resultHandler) {
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
