package com.sg.util;

/*
 * File: src/com/sg/util/PeerEntry.java
 * Description: PeerEntry helper class for Assignment 8.
 * Author: Swajeet Swarnkar
 * M.Tech. IT NIT Raipur 2023-25
 * */
public class PeerEntry {

  private String ipAddr = null;
  private String alias = null;

  public PeerEntry(String ipAddr, String alias) {
    this.ipAddr = ipAddr;
    this.alias = alias;
  }

  public PeerEntry(String peerRepresentation) throws IllegalArgumentException {
    // "127.0.0.1+myname"
    String[] contents = peerRepresentation.split("\\+");
    try {
      String ipAddr = contents[0];
      String alias = contents[1];
      this.ipAddr = ipAddr;
      this.alias = alias;
    } catch (ArrayIndexOutOfBoundsException e) {
      throw new IllegalArgumentException("Can not create peer entry from representation " + peerRepresentation);
    }
  }

  public String getIpAddr() { return ipAddr; }
  public String getAlias() { return alias; }

  @Override
  public int hashCode() {
    return (ipAddr + "+" + alias).hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }

    PeerEntry otherPeer = (PeerEntry) obj;
    if (ipAddr == null && otherPeer.ipAddr == null) {
      return alias.equals(otherPeer.alias);
    } else if (ipAddr != null && ipAddr.equals(otherPeer.ipAddr)) {
      return alias.equals(otherPeer.alias);
    }

    return false;
  }
  
  @Override
  public String toString() {
    return String.format("%s+%s", ipAddr, alias);
  }
}
