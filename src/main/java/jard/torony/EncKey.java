package jard.torony;

import net.minecraft.client.resource.metadata.TextureResourceMetadata;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.ResourceTexture;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

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
