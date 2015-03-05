package com.yogpc.qp.container;

import java.io.IOException;

public interface IPacketContainer {
  public void receivePacket(byte[] ba) throws IOException;
}
