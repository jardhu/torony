package jard.torony;

import com.google.common.base.Predicates;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.AEADBadTagException;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.List;
import java.util.Map;

public class Torony implements ModInitializer {
	private static final Logger LOGGER = LoggerFactory.getLogger("torony");

	private static final SecureRandom random = new SecureRandom ();
	private static final Path baseDir = Path.of ("").toAbsolutePath ().getParent ().resolve ("src/main/generated");

	@Override
	public void onInitialize() {
		if (! FabricLoader.getInstance ().isDevelopmentEnvironment ())
			throw new RuntimeException ("This is an indev version of Torony, you should not have this!");
	}

	// Context is the *exact* game context which decryption would be valid (the conditions that need to be met)
	public static void encryptResource (Identifier pathTo, Identifier id, GameContext context) throws IOException {
		ResourceManager manager = MinecraftClient.getInstance ().getResourceManager ();
		Resource baseResource = manager.getResource (pathTo);

		encryptRaw (baseResource.getInputStream (), id, context);

		baseResource.close ();
	}

	public static void encryptLang (Map <String, String> entries, Identifier id, GameContext context) throws IOException {
		for (Map.Entry <String, String> entry : entries.entrySet ()) {
			String lang = entry.getKey ();
			Identifier langId = new Identifier (id.getNamespace (), lang + "_" + id.getPath ());

			encryptRaw (new ByteArrayInputStream (entry.getValue ().getBytes(StandardCharsets.UTF_8)), langId, context);
		}
	}

	public static void encryptRaw (InputStream plaintext, Identifier id, GameContext context) throws IOException {
		ResourceManager manager = MinecraftClient.getInstance ().getResourceManager ();

		Identifier alreadyExists = getViaObfKey (manager, id);
		if (alreadyExists != null) {
			LOGGER.warn ("'" + alreadyExists + "' already encrypts '" + id + "'!");
			return;
		}

		ObfKey fileId = new ObfKey (id);
		EncKey encKey = context.getEncryptKey ();

		GCMParameterSpec iv = new GCMParameterSpec (128, Torony.randomBytes (12));
		SecretKeySpec aesKey = new SecretKeySpec (encKey.hash (), "AES");

		byte [] ciphertext;
		try (plaintext) {
			Cipher cipher = Cipher.getInstance ("AES/GCM/NOPADDING");
			cipher.init (Cipher.ENCRYPT_MODE, aesKey, iv);
			cipher.updateAAD ("torony".getBytes(StandardCharsets.UTF_8));

			ciphertext = cipher.doFinal (plaintext.readAllBytes ());
		} catch (GeneralSecurityException e) {
			throw new RuntimeException ("Encryption failed: ", e);
		}

		plaintext.close ();

		File file = baseDir.resolve (String.format ("assets/%s/secret/%s", id.getNamespace (), fileId)).toFile ();
		file.createNewFile ();

		OutputStream out = new FileOutputStream (file);

		out.write (iv.getIV ());
		out.write (encKey.salt ());
		out.write (ciphertext);

		LOGGER.info ("Encrypted '" + id + "' to '" + file.getPath () + "'");

		out.close ();
	}


	// Context is all relevant info that may be used for decryption
	public static byte [] decrypt (InputStream encrypted) throws IOException {
		try {
			Cipher cipher = Cipher.getInstance ("AES/GCM/NOPADDING");

			GCMParameterSpec  iv = new GCMParameterSpec (128, encrypted.readNBytes (12));
			byte [] salt       = encrypted.readNBytes (32);
			byte [] ciphertext = encrypted.readAllBytes ();

			encrypted.close ();

			if (GameContext.currentContext == null)
				return null;

			List <EncKey> keySpace = GameContext.currentContext.getDecryptKeys (salt);

			for (EncKey key : keySpace) {
				try {
					SecretKeySpec aesKey = new SecretKeySpec (key.hash (), "AES");

					cipher.init (Cipher.DECRYPT_MODE, aesKey, iv);
					cipher.updateAAD ("torony".getBytes (StandardCharsets.UTF_8));

					return cipher.doFinal (ciphertext);
				} catch (AEADBadTagException e) {
					continue;
				}
			}
		} catch (GeneralSecurityException e) {
			throw new RuntimeException ("Decryption failed: ", e);
		}
		return null;
	}

	public static Identifier getViaObfKey (ResourceManager manager, Identifier id) {
		List <Identifier> files = (List <Identifier>) manager.findResources ("secret/", Predicates.alwaysTrue ());

		for (Identifier file : files) {
			ObfKey check = new ObfKey (file.getPath ().substring (7));
			ObfKey test = new ObfKey (id, check.salt ());

			if (test.toString ().equals (check.toString ()))
				return file;
		}

		return null;
	}

	static byte [] randomBytes (int size) {
		byte [] block = new byte [size];
		random.nextBytes (block);

		return block;
	}
}
