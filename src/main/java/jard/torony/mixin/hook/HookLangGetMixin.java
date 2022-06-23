package jard.torony.mixin.hook;

import jard.torony.Torony;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.language.TranslationStorage;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import net.minecraft.util.Language;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/***
 *  HookLangGetMixin
 *  TODO: Write a description for this file.
 *
 *  Created by jard at 03:31 on June 22, 2022.
 ***/
@Mixin(TranslationStorage.class)
public abstract class HookLangGetMixin extends Language {
    @Shadow
    @Final
    private Map <String, String> translations;

    private final Map <String, String> encryptedTranslations = new HashMap <> ();

    @Inject (method = "get", at = @At ("HEAD"), cancellable = true)
    private void hookGet (String key, CallbackInfoReturnable <String> info) {
        if (translations.containsKey (key))
            return;

        if (encryptedTranslations.containsKey (key)) {
            info.setReturnValue (encryptedTranslations.get (key));
            info.cancel ();
            return;
        }

        ResourceManager resourceManager = MinecraftClient.getInstance ().getResourceManager ();

        String namespace = "";
        String [] delimited = key.split ("\\.");
        if (delimited.length >= 2)
            // We're going to assume that mods use the correct tooltip format. If they don't then tough shit.
            namespace = delimited [1].toLowerCase ();

        String lang = MinecraftClient.getInstance ().getLanguageManager ().getLanguage ().getCode ();

        Identifier langId;
        try {
            langId = new Identifier (namespace, lang + "_" + key.toLowerCase ());
        } catch (InvalidIdentifierException e) {
            return;
        }
        // Look through every file to find a matching hash
        Identifier found = Torony.getViaObfKey (resourceManager, langId);

        if (found != null) {
            Resource resource = null;
            String langEntry;
            try {
                resource = resourceManager.getResource (found);

                byte [] decrypted = Torony.decrypt (resource.getInputStream ());

                if (decrypted == null)
                    return;

                langEntry = new String (decrypted, StandardCharsets.UTF_8);
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
                    langEntry = key;
                }
            }

            encryptedTranslations.putIfAbsent (key, langEntry);
            info.setReturnValue (langEntry);
            info.cancel ();
        }
    }
}
