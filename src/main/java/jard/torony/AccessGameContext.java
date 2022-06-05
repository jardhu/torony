package jard.torony;

/***
 *  AccessGameContext
 *  Interface class that allows subclasses to modify the game context.
 *
 *  TODO: This needs to be made private to Torony, however Java sucks.
 *
 *  Created by jard at 23:10 on May 29, 2022.
 ***/
public interface AccessGameContext {
    default void setCurrentContext (GameContext context) {
        GameContext.currentContext = context;
    }
}
