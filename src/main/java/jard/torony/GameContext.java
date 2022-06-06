package jard.torony;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

/***
 *  GameContext
 *  TODO: Write a description for this file.
 *
 *  Created by jard at 12:56 on May 25, 2022.
 ***/
public record GameContext (String ... objects) {
    static GameContext currentContext = null;

    public EncKey getEncryptKey () {
        return new EncKey (Arrays.stream (objects).reduce ("", (s1, s2) -> s1 + s2), Torony.randomBytes (32));
    }

    public List <EncKey> getDecryptKeys (byte [] salt) {
        List <EncKey> ret = new ArrayList <> ();
        List <String> subsets = powReduce (List.of (objects));

        for (String s : subsets) {
            ret.add (new EncKey (s, salt));
        }

        return ret;
    }

    private List <String> powReduce (List <String> subObjects) {
        if (subObjects.size () == 1)
            return subObjects;

        String head = subObjects.get (0);
        List <String> excludeHead = powReduce (subObjects.subList (1, subObjects.size ()));
        List <String> includeHead = new ArrayList <> ();

        excludeHead.forEach (str -> includeHead.add (head + str));
        includeHead.addAll (excludeHead);

        return includeHead;
    }

    public static GameContext fromItemPlayer (ItemStack item, @Nullable UUID playerId) {
        return new GameContext (
                playerId == null ? "" : playerId.toString (),
                stripUnneededNbt (item).writeNbt (new NbtCompound ()).toString ()
        );
    }

    private static ItemStack stripUnneededNbt (ItemStack item) {
        ItemStack ret = item.copy ();

        ret.removeSubNbt (ItemStack.ENCHANTMENTS_KEY);
        ret.removeSubNbt (ItemStack.DAMAGE_KEY);
        ret.removeSubNbt (ItemStack.COLOR_KEY);
        ret.removeSubNbt (ItemStack.LORE_KEY);
        ret.removeSubNbt ("RepairCost");
        ret.setCount (1);

        return ret;
    }

    @Override
    public boolean equals (Object o) {
        if (! (o instanceof GameContext context))
            return false;

        for (String s : objects) {
            if (! Arrays.stream (context.objects).anyMatch (s::equals))
                return false;
        }

        return true;
    }

    @Override
    public int hashCode () {
        return Arrays.stream(objects).map (String::hashCode).reduce (0, Integer::sum);
    }
}
