package jard.torony.mixin.hook;

import jard.torony.Torony;
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

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;

/***
 *  HookTextureLoadMixin
 *  TODO: Write a description for this file.
 *
 *  Created by jard at 02:41 on May 25, 2022.
 ***/
@Mixin (ResourceTexture.TextureData.class)
public abstract class HookTextureLoadMixin implements Closeable {
    @Inject (method = "load", at = @At ("HEAD"), cancellable = true)
    private static void hookLoad (ResourceManager resourceManager, Identifier id, CallbackInfoReturnable <ResourceTexture.TextureData> info) {
        // Look through every file in enc to find a matching hash
        Identifier found = Torony.getViaObfKey (resourceManager, id);

        if (found != null) {
            Resource resource = null;
            ResourceTexture.TextureData textureData;
            try {
                resource = resourceManager.getResource (found);

                byte [] decrypted = Torony.decrypt (resource.getInputStream ());

                if (decrypted == null)
                    return;

                NativeImage nativeImage = NativeImage.read (new ByteArrayInputStream (decrypted));
                TextureResourceMetadata metadata = resource.getMetadata (TextureResourceMetadata.READER);
                textureData = new ResourceTexture.TextureData (metadata, nativeImage);
                resource.close ();
            } catch (Throwable t) {
                try {
                    if (resource != null)
                        try {
                            resource.close ();
                        } catch (Throwable throwable2) {
                            t.addSuppressed (throwable2);
                        }
                    throw t;
                } catch (IOException e) {
                    textureData = new ResourceTexture.TextureData (e);
                }
            }

            info.setReturnValue (textureData);
            info.cancel ();
        }
    }
}
