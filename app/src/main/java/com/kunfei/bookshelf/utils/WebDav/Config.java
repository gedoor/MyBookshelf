package com.kunfei.bookshelf.utils.WebDav;

public class Config {

    public static void registerWebDavURLHandler() {
        String ver, pkgs;

        ver = System.getProperty("java.version");
        if (ver.startsWith("1.1.") || ver.startsWith("1.2.")) {
            throw new RuntimeException(
                    "org.xdty.webdav requires Java 1.3 or above. You are running " + ver);
        }
        pkgs = System.getProperty("java.protocol.handler.pkgs");
        if (pkgs == null) {
            System.setProperty("java.protocol.handler.pkgs", "org.xdty.webdav");
        } else if (!pkgs.contains("org.xdty.webdav")) {
            pkgs += "|org.xdty.webdav";
            System.setProperty("java.protocol.handler.pkgs", pkgs);
        }
    }
}