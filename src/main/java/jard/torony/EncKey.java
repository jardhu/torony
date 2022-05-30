package jard.torony;

import net.minecraft.item.ItemStack;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/***
 *  EncKey
 *  TODO: Write a description for this file.
 *
 *  Created by jard at 12:28 on May 25, 2022.
 ***/
final record EncKey (byte [] salt, byte [] hash) {
    private static SecretKeyFactory factory;

    static {
        try {
            factory = SecretKeyFactory.getInstance ("PBKDF2WithHmacSHA256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException ("PBKDF2WithHmacSHA256 couldn't be found, this should not happen!");
        }
    }

    public EncKey (String pass, byte [] salt) {
        this (
                salt,
                getHash (pass, salt)
        );
    }

    private static final byte [] getHash (String pass, byte [] salt) {
        try {
            return factory.generateSecret (
                    new PBEKeySpec (pass.toCharArray (), salt, 10000, 256)
            ).getEncoded ();
        } catch (InvalidKeySpecException e) {
            // This should also not happen. Java is fucking stupid
            throw new RuntimeException (e);
        }
    }
}
