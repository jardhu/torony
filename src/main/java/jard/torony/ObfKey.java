package jard.torony;

import net.minecraft.util.Identifier;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/***
 *  ObfKey
 *  TODO: Write a description for this file.
 *
 *  Created by jard at 00:40 on May 25, 2022.
 ***/
public final record ObfKey (byte [] salt, byte [] hash) {
    // Create an ObfKey from an unhashed Identifier with a random salt
    ObfKey (Identifier id) {
        this (id, Torony.randomBytes (32));
    }

    // Create an ObfKey from an unhashed Identifier with a specified salt
    ObfKey (Identifier id, byte [] salt) {
        this (salt, getHash (id, salt));
    }

    // Create the corresponding ObfKey from a hashed Identifier
    public ObfKey (String id) {
        this (HexFormat.of ().parseHex (id.substring (0, 64)), HexFormat.of ().parseHex (id.substring (64, 128)));
    }

    private static byte [] getHash (Identifier id, byte [] salt) {
        MessageDigest sha256;
        ByteArrayOutputStream stream = new ByteArrayOutputStream ();
        byte [] fullPath = id.toString ().getBytes(StandardCharsets.UTF_8);
        byte [] hash;
        try {
            sha256 = MessageDigest.getInstance ("SHA-256");
            stream.write (salt);
            stream.write (fullPath);

            hash = sha256.digest (stream.toByteArray ());
            stream.close ();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException ("SHA-256 couldn't be found, this should not happen!");
        } catch (IOException e) {
            throw new RuntimeException (e);
        }

        return hash;
    }

    public String toString () {
        return HexFormat.of ().formatHex (salt) + HexFormat.of ().formatHex (hash);
    }
}
