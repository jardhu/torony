package jard.torony;

import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;

import java.util.ArrayList;
import java.util.List;

/***
 *  ContextSensitiveIdentifier
 *  An optioned, {@link GameContext}-sensitive Identifier which can represent one of two different {@link Identifier}s.
 *
 *  If a {@link Resource} is loaded with this identifier but the resource file could not be loaded (e.g. failed decryption),
 *  this {@code Identifier} will invalidate itself to the default {@code Identifier}.
 *
 *  Created by jard at 02:58 on June 05, 2022.
 ***/
public final class ContextSensitiveIdentifier extends Identifier {
    private static final List <Pair <Identifier, GameContext>> cache = new ArrayList <> ();

    private final Identifier defaulted;
    private final int hash;
    private final GameContext context;
    private boolean invalidated = false;

    public ContextSensitiveIdentifier (Identifier primary, Identifier defaulted) {
        super (primary.getNamespace (), primary.getPath ());
        this.defaulted = defaulted;

        context = GameContext.currentContext;

        hash = primary.hashCode () + defaulted.hashCode () + context.hashCode ();

        if (cache.contains (new Pair <> (primary, context)))
            invalidated = true;
    }

    public void invalidate () {
        if (! invalidated) {
            cache.add (new Pair <> (new Identifier (namespace, path), context));
            invalidated = true;
        }
    }

    @Override
    public String getPath() {
        if (invalidated)
            return defaulted.getPath ();
        return this.path;
    }

    public String getNamespace() {
        if (invalidated)
            return defaulted.getNamespace ();

        return this.namespace;
    }

    public String toString() {
        if (invalidated)
            return defaulted.toString ();

        return this.namespace + ":" + this.path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof Identifier identifier)) {
            return false;
        } else {
            boolean contextSensitive = true;

            if (o instanceof ContextSensitiveIdentifier csid) {
                contextSensitive = context.equals (csid.context);
            }

            return contextSensitive && getNamespace ().equals(identifier.getNamespace ()) && getPath ().equals(identifier.getPath ());
        }
    }

    public int hashCode() {
        return hash;
    }

    public int compareTo(Identifier identifier) {
        int i = getPath ().compareTo(identifier.getPath ());
        if (i == 0) {
            i = getNamespace ().compareTo(identifier.getNamespace ());
        }

        return i;
    }
}
