package com.yogpc.mc_lib;

import java.io.IOException;

public interface IPacketContainer {
  public void receivePacket(byte[] ba) throws IOException;
}
